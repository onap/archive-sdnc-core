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


import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SliPluginUtilsActivator implements BundleActivator {

//    private static final String SLIPLUGINUTILS_PROP_VAR = "/slipluginutils.properties";
//    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";

    @SuppressWarnings("rawtypes")
    private final List<ServiceRegistration> registrations = new LinkedList<>();

    private static final Logger LOG = LoggerFactory.getLogger(SliPluginUtilsActivator.class);

    @Override
    public void start(BundleContext ctx) throws Exception {
        // Read properties
        Properties props = new Properties();

        // ---uncomment below when adding properties file---
        /*
        String propDir = System.getenv(SDNC_CONFIG_DIR);
        if (propDir == null) {
            throw new ConfigurationException(
            "Cannot find config file - " + SLIPLUGINUTILS_PROP_VAR + " and " + SDNC_CONFIG_DIR + " unset");
        }
        String propPath = propDir + SLIPLUGINUTILS_PROP_VAR;

        File propFile = new File(propPath);

        if (!propFile.exists()) {
            throw new ConfigurationException("Missing configuration properties file : " + propFile);
        }

        try {
            props.load(new FileInputStream(propFile));
        } catch (Exception e) {
            throw new ConfigurationException("Could not load properties file " + propPath, e);
        }
        */

        SliPluginUtils plugin = new SliPluginUtils(props);

        LOG.info("Registering service "+plugin.getClass().getName());
        registrations.add(ctx.registerService(plugin.getClass().getName(), plugin, null));
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {

        for (@SuppressWarnings("rawtypes") ServiceRegistration registration: registrations)
        {
            registration.unregister();
            registration = null;
        }
    }
}
