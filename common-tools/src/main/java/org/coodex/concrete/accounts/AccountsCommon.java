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

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.common.DateFormatter;
import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.Profile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.coodex.concrete.accounts.Constants.ORGANIZATION_PREFIX;

/**
 * Created by davidoff shen on 2017-05-03.
 */
public class AccountsCommon {
    public static final Profile SETTINGS = Profile.getProfile("concrete_accounts.properties");
//    public static final RecursivelyProfile RECURSIVELY_SETTING =
//            new RecursivelyProfile(SETTINGS);

    private final static PasswordGenerator DEFAULT_PASSWORD_GENERATOR = new PasswordGeneratorImpl();

    public static final AcceptableServiceLoader<String, PasswordGenerator> PASSWORD_GENERATORS =
            new AcceptableServiceLoader<String, PasswordGenerator>(new ConcreteServiceLoader<PasswordGenerator>() {
                @Override
                public PasswordGenerator getConcreteDefaultProvider() {
                    return DEFAULT_PASSWORD_GENERATOR;
                }
            });

    private final static DateFormatter DEFAULT_DATE_FORMATTER = new DateFormatter() {
        private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public DateFormat getDateFormat() {
            return dateFormat;
        }

        @Override
        public DateFormat getDateTimeFormat() {
            return dateTimeFormat;
        }
    };

    public static final ConcreteServiceLoader<DateFormatter> DATE_FORMATTER_SERVICE_LOADER = new ConcreteServiceLoader<DateFormatter>() {
        @Override
        protected DateFormatter getConcreteDefaultProvider() {
            return DEFAULT_DATE_FORMATTER;
        }
    };

    public static final String getDefaultPassword() {
        return getEncodedPassword(null);
    }

    public static final String getEncodedPassword(String pwd) {
        return PASSWORD_GENERATORS.getServiceInstance(ORGANIZATION_PREFIX).encode(null);
    }

    public static final String getApplicationName() {
        return SETTINGS.getString("application.name",
                ConcreteHelper.getProfile().getString("application.name", "coodex.org"));
    }

}
