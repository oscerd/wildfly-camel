/*
 * #%L
 * Wildfly Camel :: Subsystem
 * %%
 * Copyright (C) 2013 - 2014 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.wildfly.extension.camel.security;

import java.security.Principal;

import org.jboss.gravia.utils.IllegalArgumentAssertion;


/**
 * A username password authentication token
 *
 * @author Thomas.Diesler@jboss.com
 * @since 03-Jul-2015
 */
public class UsernamePasswordAuthentication implements Authentication, Principal {

    private final String username;
    private final char[] password;

    public UsernamePasswordAuthentication(String username, char[] password) {
        IllegalArgumentAssertion.assertNotNull(username, "username");
        IllegalArgumentAssertion.assertNotNull(password, "password");
        this.username = username;
        this.password = password;
    }

    @Override
    public String getName() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }
}
