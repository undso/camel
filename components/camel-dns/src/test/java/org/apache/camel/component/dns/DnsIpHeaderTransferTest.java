/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.dns;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DnsIpHeaderTransferTest extends CamelTestSupport {

    @EndpointInject("mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce("direct:start")
    protected ProducerTemplate template;

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start").setHeader("foo", constant("bar")).to("dns:ip").to("mock:result");
            }
        };
    }

    @Test
    void testNullIPRequests() throws Exception {
        resultEndpoint.expectedMessageCount(0);

        try {
            template.sendBodyAndHeader("hello", "dns.domain", null);
            fail("Should have thrown exception");
        } catch (Exception t) {
            assertTrue(t.getCause() instanceof IllegalArgumentException);
        }
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    void testEmptyIPRequests() throws Exception {
        resultEndpoint.expectedMessageCount(0);

        try {
            template.sendBodyAndHeader("hello", "dns.domain", "");
            fail("Should have thrown exception");
        } catch (Exception t) {
            assertTrue(t.getCause() instanceof IllegalArgumentException);
        }
        resultEndpoint.assertIsSatisfied();
    }

    @Test
    @Disabled("Run manually, performs DNS lookup to remote apache.org server")
    void testValidIPRequests() throws Exception {
        resultEndpoint.expectedMessageCount(1);

        resultEndpoint.expectedBodiesReceived("40.79.78.1");
        resultEndpoint.expectedHeaderReceived("foo", "bar");

        template.sendBodyAndHeader("hello", "dns.domain", "www.apache.org");
        resultEndpoint.assertIsSatisfied();
    }
}
