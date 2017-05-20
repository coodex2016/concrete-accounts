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

package org.coodex.concrete.accounts.organization.impl.copiers;

import org.coodex.concrete.accounts.organization.entities.AbstractEntity;
import org.coodex.concrete.accounts.organization.pojo.AbstractPojo;
import org.coodex.concrete.common.AbstractTwoWayCopier;

import static org.coodex.concrete.accounts.AccountsCommon.DATE_FORMATTER_SERVICE_LOADER;

/**
 * Created by davidoff shen on 2017-05-11.
 */
public abstract class PojoCopier<T extends AbstractPojo, E extends AbstractEntity>
        extends AbstractTwoWayCopier<T, E> {
    @Override
    public E copyA2B(T t, E e) {
        e.setName(t.getName());
        return e;
    }

    @Override
    public T copyB2A(E e, T t) {
        t.setName(e.getName());
        t.setCreated(DATE_FORMATTER_SERVICE_LOADER.getInstance().getDateFormat().format(e.getCreated().getTime()));
        return t;
    }
}
