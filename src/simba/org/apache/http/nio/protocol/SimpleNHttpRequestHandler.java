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

package simba.org.apache.http.nio.protocol;

import java.io.IOException;

import simba.org.apache.http.HttpException;
import simba.org.apache.http.HttpRequest;
import simba.org.apache.http.HttpResponse;
import simba.org.apache.http.annotation.Immutable;
import simba.org.apache.http.protocol.HttpContext;

/**
 * A simple implementation of {@link NHttpRequestHandler} that abstracts away
 * the need to use {@link NHttpResponseTrigger}. Implementations need only to
 * implement {@link #handle(HttpRequest, HttpResponse, HttpContext)}.
 *
 * @since 4.0
 *
 * @deprecated (4.2) use {@link BasicAsyncRequestHandler}
 */
@Deprecated
@Immutable
public abstract class SimpleNHttpRequestHandler implements NHttpRequestHandler {

    public final void handle(
            final HttpRequest request,
            final HttpResponse response,
            final NHttpResponseTrigger trigger,
            final HttpContext context) throws HttpException, IOException {
        handle(request, response, context);
        trigger.submitResponse(response);
    }

    public abstract void handle(HttpRequest request, HttpResponse response, HttpContext context)
        throws HttpException, IOException;

}
