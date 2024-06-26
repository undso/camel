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
package org.apache.camel.processor;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
@Timeout(60)
public class MulticastParallelLastTimeoutTest extends ContextTestSupport {

    private final Phaser phaser = new Phaser(3);

    @BeforeEach
    void sendEarly() {
        Assumptions.assumeTrue(context.isStarted(), "The test cannot be run because the context is not started");
        template.sendBody("direct:start", "Hello");
    }

    @Test
    public void testMulticastParallelLastTimeout() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        // C will timeout so we only get A and B
        mock.expectedBodiesReceived("AB");

        phaser.awaitAdvanceInterruptibly(0, 5000, TimeUnit.SECONDS);

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start").multicast(new AggregationStrategy() {
                    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                        if (oldExchange == null) {
                            return newExchange;
                        }

                        String body = oldExchange.getIn().getBody(String.class);
                        oldExchange.getIn().setBody(body + newExchange.getIn().getBody(String.class));
                        return oldExchange;
                    }
                }).parallelProcessing().timeout(2000).to("direct:a", "direct:b", "direct:c")
                        // use end to indicate end of multicast route
                        .end().to("mock:result");

                from("direct:a").process(e -> phaser.arriveAndAwaitAdvance()).delay(500).setBody(constant("A"));

                from("direct:b").process(e -> phaser.arriveAndAwaitAdvance()).setBody(constant("B"));

                from("direct:c").process(e -> phaser.arriveAndAwaitAdvance()).delay(3000).setBody(constant("C"));
            }
        };
    }
}
