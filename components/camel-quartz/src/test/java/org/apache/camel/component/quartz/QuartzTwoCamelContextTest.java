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
package org.apache.camel.component.quartz;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class QuartzTwoCamelContextTest {

    private DefaultCamelContext camel1;
    private DefaultCamelContext camel2;

    @BeforeEach
    public void setUp() throws Exception {
        camel1 = new DefaultCamelContext();
        camel1.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("quartz://myGroup/myTimerName?cron=0/1+*+*+*+*+?").to("mock:one");
            }
        });
        camel1.start();

        camel2 = new DefaultCamelContext();
        camel2.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("quartz://myOtherGroup/myOtherTimerName?cron=0/1+*+*+*+*+?").to("mock:two");
            }
        });
        camel2.start();
    }

    @AfterEach
    public void tearDown() {
        camel1.stop();
        camel2.stop();
    }

    @Test
    public void testTwoCamelContext() throws Exception {
        MockEndpoint mock1 = camel1.getEndpoint("mock:one", MockEndpoint.class);
        mock1.expectedMinimumMessageCount(2);

        MockEndpoint mock2 = camel2.getEndpoint("mock:two", MockEndpoint.class);
        mock2.expectedMinimumMessageCount(6);
        mock1.assertIsSatisfied();

        assertDoesNotThrow(() -> camel1.stop());

        mock2.assertIsSatisfied();

        assertDoesNotThrow(() -> camel2.stop());
    }

    @Test
    public void testThirdCamelContext() throws Exception {
        camel1.stop();

        camel2.stop();

        DefaultCamelContext camel3 = new DefaultCamelContext();
        camel3.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("quartz://myThirdGroup/myThirdTimerName?cron=0/1+*+*+*+*+?").to("mock:three");
            }
        });
        assertDoesNotThrow(camel3::start);

        MockEndpoint mock3 = camel3.getEndpoint("mock:three", MockEndpoint.class);
        mock3.expectedMinimumMessageCount(2);

        mock3.assertIsSatisfied();
        assertDoesNotThrow(camel3::stop);
    }

}
