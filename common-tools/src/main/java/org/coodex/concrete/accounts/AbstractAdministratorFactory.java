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

package org.coodex.concrete.accounts;

import org.coodex.concrete.common.AcceptableAccountFactory;
import org.coodex.concrete.common.Account;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;

import java.io.Serializable;

import static org.coodex.concrete.accounts.AccountID.TYPE_ADMINISTRATOR;
import static org.coodex.concrete.common.AccountsErrorCodes.LOGIN_FAILED;

/**
 * Created by davidoff shen on 2017-05-19.
 */
public abstract class AbstractAdministratorFactory implements AcceptableAccountFactory<AccountID> {

    private Token token = TokenWrapper.getInstance();

    @Override
    public <ID extends Serializable> Account<ID> getAccountByID(ID id) {
        if (id == null || !(id instanceof AccountID)) return null;
        return (Account<ID>) getAdministrator(((AccountID) id).getId());
    }

    @Override
    public boolean accept(AccountID param) {
        return param != null && param.getType() == TYPE_ADMINISTRATOR;
    }


    public void login(String id, String password, String authCode) {
        Administrator administrator = getAdministrator(id);
        if (administrator.verify(password, authCode)) {
            token.setAccount(administrator);
            token.setAccountCredible(true);
        } else {
            throw new ConcreteException(LOGIN_FAILED);
        }
    }


    protected abstract Administrator getAdministrator(String id);
}
