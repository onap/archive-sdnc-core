/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.openecomp.sdnc.sli.provider;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openecomp.sdnc.sli.SvcLogicAdaptor;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicExpression;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;
import org.openecomp.sdnc.sli.SvcLogicNode;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteNodeExecutor extends SvcLogicNodeExecutor {
	private static final Logger LOG = LoggerFactory
			.getLogger(ExecuteNodeExecutor.class);

	public SvcLogicNode execute(SvcLogicServiceImpl svc, SvcLogicNode node,
			SvcLogicContext ctx) throws SvcLogicException {

		String pluginName = SvcLogicExpressionResolver.evaluate(
				node.getAttribute("plugin"), node, ctx);
		String outValue = "failure";

		if (LOG.isDebugEnabled()) {
			LOG.debug("execute node encountered - looking for plugin "
					+ pluginName);
		}
		
		BundleContext bctx = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();

		ServiceReference sref = bctx.getServiceReference(pluginName);

		if (sref == null) {
			outValue = "not-found";
		} else {
			SvcLogicJavaPlugin plugin  = (SvcLogicJavaPlugin) bctx
					.getService(sref);
			
			String methodName = SvcLogicExpressionResolver.evaluate(node.getAttribute("method"),  node, ctx);
			
			Class pluginClass = plugin.getClass();
			
			Method pluginMethod = null;
			
			try {
				pluginMethod = pluginClass.getMethod(methodName, Map.class, SvcLogicContext.class);
			} catch (Exception e) {
				LOG.error("Caught exception looking for method "+pluginName+"."+methodName+"(Map, SvcLogicContext)");
			}
			
			if (pluginMethod == null) {
				outValue = "unsupported-method";
			} else {
				try {
					
					Map<String, String> parmMap = new HashMap<String, String>();

					Set<Map.Entry<String, SvcLogicExpression>> parmSet = node
							.getParameterSet();

					for (Iterator<Map.Entry<String, SvcLogicExpression>> iter = parmSet
							.iterator(); iter.hasNext();) {
						Map.Entry<String, SvcLogicExpression> curEnt = iter.next();
						String curName = curEnt.getKey();
						SvcLogicExpression curExpr = curEnt.getValue();
						String curExprValue = SvcLogicExpressionResolver.evaluate(curExpr, node, ctx);
						
						LOG.debug("Parameter "+curName+" = "+curExpr.asParsedExpr()+" resolves to "+curExprValue);

						parmMap.put(curName,curExprValue);
					}
					
					pluginMethod.invoke(plugin, parmMap, ctx);
					
					outValue = "success";
				} catch (Exception e) {
					LOG.error("Caught exception executing "+pluginName+"."+methodName, e);
					
					outValue = "failure";
					ctx.setStatus("failure");
				}
			}

		}

		SvcLogicNode nextNode = node.getOutcomeValue(outValue);
		if (nextNode != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("about to execute " + outValue + " branch");
			}
			return (nextNode);
		}

		nextNode = node.getOutcomeValue("Other");
		if (nextNode != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("about to execute Other branch");
			}
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("no " + outValue + " or Other branch found");
			}
		}
		return (nextNode);
	}

}
