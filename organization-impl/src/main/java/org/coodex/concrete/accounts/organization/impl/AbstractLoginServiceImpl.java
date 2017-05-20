/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.accounts.organization.impl;

import org.coodex.concrete.accounts.AbstractAdministratorFactory;
import org.coodex.concrete.accounts.AccountID;
import org.coodex.concrete.accounts.AccountsCommon;
import org.coodex.concrete.accounts.TOTPAuthenticator;
import org.coodex.concrete.accounts.organization.api.AbstractLoginService;
import org.coodex.concrete.accounts.organization.entities.AbstractPersonAccountEntity;
import org.coodex.concrete.accounts.organization.entities.AbstractPositionEntity;
import org.coodex.concrete.accounts.organization.entities.LoginCacheEntryEntity;
import org.coodex.concrete.accounts.organization.repositories.AbstractPersonAccountRepo;
import org.coodex.concrete.accounts.organization.repositories.LoginCacheEntryRepo;
import org.coodex.concrete.common.AccountsErrorCodes;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.util.Common;

import javax.inject.Inject;
import java.util.Calendar;

import static org.coodex.concrete.common.AccountsErrorCodes.*;
import static org.coodex.concrete.common.ConcreteContext.putLoggingData;

/**
 * Created by davidoff shen on 2017-05-18.
 */
public abstract class AbstractLoginServiceImpl
        <JE extends AbstractPositionEntity, PE extends AbstractPersonAccountEntity<JE>>
        implements AbstractLoginService {

    @Inject
    protected AbstractPersonAccountRepo<PE> personAccountRepo;

    @Inject
    protected LoginCacheEntryRepo loginCacheEntryRepo;

    protected Token token = TokenWrapper.getInstance();

    @Inject
    protected AbstractOrganizationAccountFactory abstractOrganizationAccountFactory;

    @Inject
    protected AbstractAdministratorFactory administratorFactory;

    @Override
    public String login(String account, String password, String authCode) {
        if (Common.isBlank(account)) { // 账户为空，视为管理员登录
            administratorLogin(password, authCode);
            return null;
        } else {
            PE personEntity = Assert.isNull(
                    getPersonEntity(account), AccountsErrorCodes.NONE_THIS_ACCOUNT);

            checkPassword(password, personEntity);
            token.setAccountCredible(isCredible(authCode, personEntity));
            token.setAccount(
                    abstractOrganizationAccountFactory.getAccountByID(
                            new AccountID(AccountID.TYPE_ORGANIZATION, personEntity.getId())));

            putLoggingData("loginUser", personEntity);
            return updateLoginCacheEntry(personEntity);
        }
    }

    protected String updateLoginCacheEntry(PE personEntity) {
        try {
            LoginCacheEntryEntity loginCacheEntryEntity = loginCacheEntryRepo.findOne(personEntity.getId());
            if (loginCacheEntryEntity == null) {
                loginCacheEntryEntity = new LoginCacheEntryEntity();
                loginCacheEntryEntity.setAccountId(personEntity.getId());
                loginCacheEntryEntity.setCredential(newCredential());
                loginCacheEntryEntity.setValidation(getValidationFromNow());
            }
            loginCacheEntryEntity.setLastLogin(Calendar.getInstance());
            setValidation(loginCacheEntryEntity);
            return loginCacheEntryRepo.save(loginCacheEntryEntity).getCredential();
        } catch (RuntimeException e) { // rollback
            token.setAccount(null);
            token.setAccountCredible(false);
            throw e;
        }
    }


    /**
     * 可扩展
     *
     * @param loginCacheEntryEntity
     */
    protected void setValidation(LoginCacheEntryEntity loginCacheEntryEntity) {
        if (AccountsCommon.SETTINGS.getBool("validation.defer", false)) {
            loginCacheEntryEntity.setValidation(getValidationFromNow());
        }
    }

    /**
     * 可扩展，需保证每次均唯一
     *
     * @return
     */
    protected String newCredential() {
        return Common.getUUIDStr();
    }

    /**
     * 可扩展
     *
     * @return
     */
    protected Calendar getValidationFromNow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, AccountsCommon.SETTINGS.getInt("validation.days", 7));
        return calendar;
    }

    protected void checkPassword(String password, PE personEntity) {
        Assert.not(
                AccountsCommon.getEncodedPassword(password).equals(personEntity.getPassword()),
                LOGIN_FAILED);
    }

    protected PE getPersonEntity(String account) {
        if (isCellPhone(account)) {
            return personAccountRepo.findFirstByCellphone(account);
        } else if (isIdCard(account)) {
            return personAccountRepo.findFirstByIdCardNo(account);
        } else if (isEmail(account)) {
            return personAccountRepo.findFirstByEmail(account);
        } else {
            return getAccountEntityBy(account);
        }
    }

    protected boolean isCredible(String authCode, PE personEntity) {
        if (personEntity.getAuthCodeKey() != null) {
            Assert.not(TOTPAuthenticator.authenticate(
                    authCode, personEntity.getAuthCodeKey()), LOGIN_FAILED);
            return true;
        } else {
            return false;
        }
    }


    /**
     * 其他登录方式，实现者自行重载
     *
     * @param account
     * @return
     */
    protected PE getAccountEntityBy(String account) {
        return null;
    }

    protected PE getAccountEntityByCellPhone(String cellPhone) {
        return personAccountRepo.findFirstByCellphone(cellPhone);
    }

    protected PE getAccountEntityByEmail(String email) {
        return personAccountRepo.findFirstByEmail(email);
    }

    protected PE getAccountEntityByIdCardNo(String idCardNo) {
        return personAccountRepo.findFirstByIdCardNo(idCardNo);
    }

    protected boolean hasAtChar(String account) {
        return account.indexOf('@') > 0;
    }

    /**
     * 实现者可重载，增加更完善的校验，例如：全数字，前两位、三位，区域等
     *
     * @param account
     * @return
     */
    protected boolean isCellPhone(String account) {
        return account.length() == 11 && !hasAtChar(account);
    }

    /**
     * 实现者可重载，增加更完善的校验，例如：15位是否全数字、18位校验码、生日有效性校验、行政区划有效性校验等
     *
     * @param account
     * @return
     */
    protected boolean isIdCard(String account) {
        return (account.length() == 15 || account.length() == 18) && !hasAtChar(account);
    }


    /**
     * 实现者可重载，增加更完善的校验
     *
     * @param account
     * @return
     */
    protected boolean isEmail(String account) {
        return hasAtChar(account);
    }

    @Override
    public void administratorLogin(String password, String authCode) {
        administratorFactory.login(AccountsCommon.SETTINGS.getString("administrator.id"), password, authCode);
    }

    @Override
    public void loginWith(String credential) {
        LoginCacheEntryEntity loginCacheEntryEntity =
                Assert.isNull(
                        loginCacheEntryRepo.findFirstByCredential(credential), NONE_THIS_CREDENTIAL);
        PE personEntity = Assert.isNull(
                personAccountRepo.findOne(loginCacheEntryEntity.getAccountId()), NONE_THIS_ACCOUNT);
        token.setAccount(
                abstractOrganizationAccountFactory.getAccountByID(
                        new AccountID(AccountID.TYPE_ORGANIZATION, personEntity.getId())));
        token.setAccountCredible(false);
        putLoggingData("loginUser", personEntity);
    }

    @Override
    public String identification(String authCode) {
        // TODO: 丑陋
        PE personEntity = Assert.isNull(
                personAccountRepo.findOne(((AccountID) (token.currentAccount().getId())).getId()),
                NONE_THIS_ACCOUNT);
        String credential = null;
        if (isCredible(authCode, personEntity)) {
            credential = updateLoginCacheEntry(personEntity);
            token.setAccountCredible(true);
        } else {
            throw new ConcreteException(LOGIN_FAILED);
        }
        return credential;
    }

    @Override
    public void logout() {
        token.invalidate();
    }
}
