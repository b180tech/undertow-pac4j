/*
  Copyright 2014 - 2016 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.undertow.session;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.*;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.undertow.UndertowWebContext;

/**
 * Specific session store for Undertow relying on {@link SessionManager} and {@link SessionConfig}.
 *
 * @author Jerome Leleu
 * @since 1.1.0
 */
public class UndertowSessionStore implements SessionStore {

    private final SessionManager sessionManager;
    private final SessionConfig sessionConfig;

    public UndertowSessionStore() {
        this.sessionManager = new InMemorySessionManager("SessionManager");
        this.sessionConfig = new SessionCookieConfig();
    }

    public UndertowSessionStore(final SessionManager sessionManager, final SessionConfig sessionConfig) {
        this.sessionManager = sessionManager;
        this.sessionConfig = sessionConfig;
    }

    private Session getSession(final WebContext context) {
        final UndertowWebContext webContext = (UndertowWebContext) context;
        final HttpServerExchange exchange = webContext.getExchange();
        Session session = this.sessionManager.getSession(exchange, this.sessionConfig);
        if (session == null) {
            exchange.getAttachment(SessionManager.ATTACHMENT_KEY).createSession(exchange, exchange.getAttachment(SessionConfig.ATTACHMENT_KEY));
            session = this.sessionManager.getSession(exchange, this.sessionConfig);
        }
        return session;
    }

    @Override
    public String getOrCreateSessionId(final WebContext context) {
        return getSession(context).getId();
    }

    @Override
    public Object get(final WebContext context, final String key) {
        return getSession(context).getAttribute(key);
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        final Session session = getSession(context);
        if (value == null) {
            session.removeAttribute(key);
        } else {
            session.setAttribute(key, value);
        }
    }

    public HttpHandler addSessionHandler(final HttpHandler toWrap) {
        return new SessionAttachmentHandler(toWrap, sessionManager, sessionConfig);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }
}
