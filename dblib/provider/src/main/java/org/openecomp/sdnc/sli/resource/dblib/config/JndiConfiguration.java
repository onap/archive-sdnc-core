/*-
 * ============LICENSE_START=======================================================
 * openecomp
 * ================================================================================
 * Copyright (C) 2016 - 2017 AT&T
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

package org.openecomp.sdnc.sli.resource.dblib.config;

import java.util.Properties;

public class JndiConfiguration extends BaseDBConfiguration{

	public JndiConfiguration(Properties xmlElem) {
		super(xmlElem);
		// TODO Auto-generated constructor stub
	}

	public String getJndiConnectionName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJndiContextFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJndiURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJndiSource() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setJndiContextFactory(String jndiContextFactoryStr) {
		// TODO Auto-generated method stub
		
	}

	public void setJndiURL(String jndiURLStr) {
		// TODO Auto-generated method stub
		
	}

}
