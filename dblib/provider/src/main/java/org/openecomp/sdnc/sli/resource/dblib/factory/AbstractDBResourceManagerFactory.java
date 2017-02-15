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

package org.openecomp.sdnc.sli.resource.dblib.factory;

import org.openecomp.sdnc.sli.resource.dblib.jdbc.JdbcDbResourceManagerFactory;
import org.openecomp.sdnc.sli.resource.dblib.jndi.JNDIDbResourceManagerFactory;

/**
 * @version $Revision: 1.1 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */
public class AbstractDBResourceManagerFactory {

	public static AbstractResourceManagerFactory getFactory(String type) throws FactoryNotDefinedException {
		
		if("JNDI".equals(type)){
			try {
				return JNDIDbResourceManagerFactory.createIntstance();
			} catch (Exception e) {
			}
		}
		// JDBC
		return JdbcDbResourceManagerFactory.createIntstance();
	}
}
