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
package org.apache.camel.converter;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
public class DateTimeConverterTest extends ContextTestSupport {

    @Test
    public void testToTimeZone() {
        String id = TimeZone.getDefault().getID();

        TimeZone zone = context.getTypeConverter().convertTo(TimeZone.class, id);
        assertNotNull(zone);
        assertEquals(id, zone.getID());
    }

    @Test
    public void testLongToDate() {
        long value = 0;
        Date date = context.getTypeConverter().convertTo(Date.class, value);
        Date expected = new Date(value);
        assertEquals(expected, date);
    }

    @Test
    public void testDateToLong() {
        Date date = new Date(0);
        long l = context.getTypeConverter().convertTo(Long.class, date);
        assertEquals(date.getTime(), l);
    }

    @Test
    public void testToTimeUnit() {
        assertEquals(TimeUnit.DAYS, context.getTypeConverter().convertTo(TimeUnit.class, "DAYS"));
        assertEquals(TimeUnit.MILLISECONDS, context.getTypeConverter().convertTo(TimeUnit.class, "MILLISECONDS"));
    }
}
