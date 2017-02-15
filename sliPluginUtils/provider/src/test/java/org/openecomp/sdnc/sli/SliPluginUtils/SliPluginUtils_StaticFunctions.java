/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.sli.SliPluginUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliPluginUtils_StaticFunctions {
    private static final Logger LOG = LoggerFactory.getLogger(SliPluginUtils_StaticFunctions.class);
    SliPluginUtils utils = new SliPluginUtils();
    private SvcLogicContext ctx;
    private HashMap<String,String> parameters;


    @Before
    public void setUp() throws Exception {
        this.ctx = new SvcLogicContext();
        parameters = new HashMap<>();
    }

    // TODO: javadoc
    @Test
    public final void testCtxGetBeginsWith() {
        ctx.setAttribute("service-data.oper-status.order-status", "InProgress");
        ctx.setAttribute("service-data.service-information.service-instance-id", "USOSTCDALTX0101UJZZ01");
        ctx.setAttribute("service-data.service-information.service-type", "VMS");

        Map<String, String> entries = SliPluginUtils.ctxGetBeginsWith(ctx, "service-data.service-information");

        assertEquals( "USOSTCDALTX0101UJZZ01", entries.get("service-data.service-information.service-instance-id") );
        assertEquals( "VMS", entries.get("service-data.service-information.service-type") );
        assertFalse( entries.containsKey("service-data.oper-status.order-status") );
    }

    // TODO: javadoc
    @Test
    public final void testCtxListRemove_index() throws SvcLogicException {
        LOG.trace("=== testCtxListRemove_index ===");
        ctx.setAttribute("service-data.vnf-l3[0].vnf-host-name", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[0].device-host-name", "device-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[1].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2].vnf-host-name", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3[2].device-host-name", "device-host-name_2");
        ctx.setAttribute("service-data.vnf-l3_length", "3");

        parameters.put("index", "1");
        parameters.put("list_pfx", "service-data.vnf-l3");

        utils.ctxListRemove( parameters, ctx );
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        assertEquals("2", ctx.getAttribute("service-data.vnf-l3_length"));
        assertEquals("vnf-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].vnf-host-name"));
        assertEquals("device-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].device-host-name"));
        assertEquals("vnf-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].vnf-host-name"));
        assertEquals("device-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].device-host-name"));
    }

    // TODO: javadoc
    @Test
    public final void textCtxListRemove_keyValue() throws SvcLogicException {
        LOG.trace("=== textCtxListRemove_keyValue ===");
        ctx.setAttribute("service-data.vnf-l3[0].vnf-host-name", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[0].device-host-name", "device-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[1].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2].vnf-host-name", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3[2].device-host-name", "device-host-name_2");
        // 2nd entry
        ctx.setAttribute("service-data.vnf-l3[3].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[3].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3_length", "4");

        parameters.put("list_pfx", "service-data.vnf-l3");
        parameters.put("key", "vnf-host-name");
        parameters.put("value", "vnf-host-name_1");

        utils.ctxListRemove( parameters, ctx );
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        assertEquals("2", ctx.getAttribute("service-data.vnf-l3_length"));
        assertEquals("vnf-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].vnf-host-name"));
        assertEquals("device-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].device-host-name"));
        assertEquals("vnf-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].vnf-host-name"));
        assertEquals("device-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].device-host-name"));
    }

    // TODO: javadoc
    @Test
    public final void textCtxListRemove_keyValue_nullkey() throws SvcLogicException {
        LOG.trace("=== textCtxListRemove_keyValue_nullkey ===");
        ctx.setAttribute("service-data.vnf-l3[0]", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1]", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2]", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3_length", "3");

        parameters.put("list_pfx", "service-data.vnf-l3");
        parameters.put("value", "vnf-host-name_1");

        utils.ctxListRemove( parameters, ctx );
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        assertEquals("2", ctx.getAttribute("service-data.vnf-l3_length"));
        assertEquals("vnf-host-name_0", ctx.getAttribute("service-data.vnf-l3[0]"));
        assertEquals("vnf-host-name_2", ctx.getAttribute("service-data.vnf-l3[1]"));
    }

    // TODO: javadoc
    @Test
    public final void textCtxListRemove_keyValueList() throws SvcLogicException {
        LOG.trace("=== textCtxListRemove_keyValueList ===");
        ctx.setAttribute("service-data.vnf-l3[0].vnf-host-name", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[0].device-host-name", "device-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[1].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2].vnf-host-name", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3[2].device-host-name", "device-host-name_2");
        // 2nd entry
        ctx.setAttribute("service-data.vnf-l3[3].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[3].device-host-name", "device-host-name_1");
        // entries with only 1 of 2 key-value pairs matching
        ctx.setAttribute("service-data.vnf-l3[4].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[4].device-host-name", "device-host-name_4");
        ctx.setAttribute("service-data.vnf-l3[5].vnf-host-name", "vnf-host-name_5");
        ctx.setAttribute("service-data.vnf-l3[5].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3_length", "6");

        parameters.put("list_pfx", "service-data.vnf-l3");
        parameters.put("keys_length", "2");
        parameters.put("keys[0].key", "vnf-host-name");
        parameters.put("keys[0].value", "vnf-host-name_1");
        parameters.put("keys[1].key", "device-host-name");
        parameters.put("keys[1].value", "device-host-name_1");

        utils.ctxListRemove( parameters, ctx );
        SliPluginUtils.logContextMemory(ctx, LOG, SliPluginUtils.LogLevel.TRACE);

        assertEquals("4", ctx.getAttribute("service-data.vnf-l3_length"));
        assertEquals("vnf-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].vnf-host-name"));
        assertEquals("device-host-name_0", ctx.getAttribute("service-data.vnf-l3[0].device-host-name"));
        assertEquals("vnf-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].vnf-host-name"));
        assertEquals("device-host-name_2", ctx.getAttribute("service-data.vnf-l3[1].device-host-name"));
        assertEquals("vnf-host-name_1", ctx.getAttribute("service-data.vnf-l3[2].vnf-host-name"));
        assertEquals("device-host-name_4", ctx.getAttribute("service-data.vnf-l3[2].device-host-name"));
        assertEquals("vnf-host-name_5", ctx.getAttribute("service-data.vnf-l3[3].vnf-host-name"));
        assertEquals("device-host-name_1", ctx.getAttribute("service-data.vnf-l3[3].device-host-name"));
    }

    // TODO: javadoc
    @Test(expected=SvcLogicException.class)
    public final void testCtxListRemove_nullListLength() throws SvcLogicException {
        LOG.trace("=== testCtxListRemove_nullListLength ===");
        ctx.setAttribute("service-data.vnf-l3[0].vnf-host-name", "vnf-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[0].device-host-name", "device-host-name_0");
        ctx.setAttribute("service-data.vnf-l3[1].vnf-host-name", "vnf-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[1].device-host-name", "device-host-name_1");
        ctx.setAttribute("service-data.vnf-l3[2].vnf-host-name", "vnf-host-name_2");
        ctx.setAttribute("service-data.vnf-l3[2].device-host-name", "device-host-name_2");


        parameters.put("index", "1");
        parameters.put("list_pfx", "service-data.vnf-l3");

        utils.ctxListRemove( parameters, ctx );
    }

    // TODO: javadoc
    @Test
    public final void testCtxPutAll() {
        HashMap<String, Object> entries = new HashMap<>();
        entries.put("service-data.oper-status.order-status", "InProgress");
        entries.put("service-data.service-information.service-instance-id", "USOSTCDALTX0101UJZZ01");
        entries.put("service-data.request-information.order-number", 1234);
        entries.put("service-data.request-information.request-id", null);

        SliPluginUtils.ctxPutAll(ctx, entries);

        assertEquals( "InProgress", ctx.getAttribute("service-data.oper-status.order-status") );
        assertEquals( "USOSTCDALTX0101UJZZ01", ctx.getAttribute("service-data.service-information.service-instance-id") );
        assertEquals( "1234", ctx.getAttribute("service-data.request-information.order-number") );
        assertFalse( ctx.getAttributeKeySet().contains("service-data.request-information.request-id") );
    }

    // TODO: javadoc
    @Test
    public final void testCtxSetAttribute_LOG() {
        LOG.debug("=== testCtxSetAttribute_LOG ===");
        Integer i = new Integer(3);
        SliPluginUtils.ctxSetAttribute(ctx, "test", i, LOG, SliPluginUtils.LogLevel.TRACE);
    }
}
