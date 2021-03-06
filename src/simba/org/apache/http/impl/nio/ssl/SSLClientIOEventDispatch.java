/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package simba.org.apache.http.impl.nio.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import simba.org.apache.http.annotation.Immutable;
import simba.org.apache.http.impl.nio.DefaultClientIOEventDispatch;
import simba.org.apache.http.impl.nio.DefaultHttpClientIODispatch;
import simba.org.apache.http.impl.nio.reactor.SSLIOSession;
import simba.org.apache.http.impl.nio.reactor.SSLSetupHandler;
import simba.org.apache.http.nio.NHttpClientHandler;
import simba.org.apache.http.nio.NHttpClientIOTarget;
import simba.org.apache.http.nio.reactor.IOEventDispatch;
import simba.org.apache.http.nio.reactor.IOSession;
import simba.org.apache.http.params.HttpConnectionParams;
import simba.org.apache.http.params.HttpParams;

/**
 * Default implementation of {@link IOEventDispatch} interface for SSL
 * (encrypted) client-side HTTP connections.
 * <p>
 * The following parameters can be used to customize the behavior of this
 * class:
 * <ul>
 *  <li>{@link simba.org.apache.http.params.CoreProtocolPNames#HTTP_ELEMENT_CHARSET}</li>
 *  <li>{@link simba.org.apache.http.params.CoreConnectionPNames#SO_TIMEOUT}</li>
 *  <li>{@link simba.org.apache.http.params.CoreConnectionPNames#SOCKET_BUFFER_SIZE}</li>
 *  <li>{@link simba.org.apache.http.params.CoreConnectionPNames#MAX_HEADER_COUNT}</li>
 *  <li>{@link simba.org.apache.http.params.CoreConnectionPNames#MAX_LINE_LENGTH}</li>
 * </ul>
 *
 * @since 4.1
 *
 * @deprecated (4.2) use {@link DefaultHttpClientIODispatch}
 */
@Deprecated
@Immutable // provided injected dependencies are immutable
public class SSLClientIOEventDispatch extends DefaultClientIOEventDispatch {

    private final SSLContext sslcontext;
    private final SSLSetupHandler sslHandler;

    /**
     * Creates a new instance of this class to be used for dispatching I/O event
     * notifications to the given protocol handler using the given
     * {@link SSLContext}. This I/O dispatcher will transparently handle SSL
     * protocol aspects for HTTP connections.
     *
     * @param handler the client protocol handler.
     * @param sslcontext the SSL context.
     * @param sslHandler the SSL setup handler.
     * @param params HTTP parameters.
     */
    public SSLClientIOEventDispatch(
            final NHttpClientHandler handler,
            final SSLContext sslcontext,
            final SSLSetupHandler sslHandler,
            final HttpParams params) {
        super(handler, params);
        if (sslcontext == null) {
            throw new IllegalArgumentException("SSL context may not be null");
        }
        if (params == null) {
            throw new IllegalArgumentException("HTTP parameters may not be null");
        }
        this.sslcontext = sslcontext;
        this.sslHandler = sslHandler;
    }

    /**
     * Creates a new instance of this class to be used for dispatching I/O event
     * notifications to the given protocol handler using the given
     * {@link SSLContext}. This I/O dispatcher will transparently handle SSL
     * protocol aspects for HTTP connections.
     *
     * @param handler the client protocol handler.
     * @param sslcontext the SSL context.
     * @param params HTTP parameters.
     */
    public SSLClientIOEventDispatch(
            final NHttpClientHandler handler,
            final SSLContext sslcontext,
            final HttpParams params) {
        this(handler, sslcontext, null, params);
    }

    /**
     * Creates an instance of {@link SSLIOSession} decorating the given
     * {@link IOSession}.
     * <p>
     * This method can be overridden in a super class in order to provide
     * a different implementation of SSL I/O session.
     *
     * @param session the underlying I/O session.
     * @param sslcontext the SSL context.
     * @param sslHandler the SSL setup handler.
     * @return newly created SSL I/O session.
     */
    protected SSLIOSession createSSLIOSession(
            final IOSession session,
            final SSLContext sslcontext,
            final SSLSetupHandler sslHandler) {
        return new SSLIOSession(session, sslcontext, sslHandler);
    }

    protected NHttpClientIOTarget createSSLConnection(final SSLIOSession ssliosession) {
        return super.createConnection(ssliosession);
    }

    @Override
    protected NHttpClientIOTarget createConnection(final IOSession session) {
        SSLIOSession ssliosession = createSSLIOSession(session, this.sslcontext, this.sslHandler);
        session.setAttribute(SSLIOSession.SESSION_KEY, ssliosession);
        NHttpClientIOTarget conn = createSSLConnection(ssliosession);
        try {
            ssliosession.initialize();
        } catch (SSLException ex) {
            this.handler.exception(conn, ex);
            ssliosession.shutdown();
        }
        return conn;
    }

    @Override
    public void onConnected(final NHttpClientIOTarget conn) {
        int timeout = HttpConnectionParams.getSoTimeout(this.params);
        conn.setSocketTimeout(timeout);

        Object attachment = conn.getContext().getAttribute(IOSession.ATTACHMENT_KEY);
        this.handler.connected(conn, attachment);
    }

}
