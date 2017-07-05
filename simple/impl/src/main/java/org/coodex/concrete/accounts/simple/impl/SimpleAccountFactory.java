package org.coodex.concrete.accounts.simple.impl;

import org.coodex.concrete.accounts.AccountID;
import org.coodex.concrete.common.AcceptableAccountFactory;
import org.coodex.concrete.common.Account;
import org.coodex.util.Profile;

import java.io.Serializable;

/**
 * Created by davidoff shen on 2017-07-05.
 */
public class SimpleAccountFactory implements AcceptableAccountFactory<AccountID> {
    @Override
    public <ID extends Serializable> Account<ID> getAccountByID(ID id) {
        return (Account<ID>) new SimpleAccount(id.toString());
    }

    @Override
    public boolean accept(AccountID param) {
        boolean isSimple = param != null && param.getType() == AccountID.TYPE_SIMPLE;

        return isSimple && Profile.getResource("accounts/" + param.getId() + ".properties") != null;
    }
}
