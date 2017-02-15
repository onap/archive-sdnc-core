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

package org.openecomp.sdnc.sli.resource.dblib.jndi;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openecomp.sdnc.sli.resource.dblib.CachedDataSource;
import org.openecomp.sdnc.sli.resource.dblib.DBConfigException;
import org.openecomp.sdnc.sli.resource.dblib.config.BaseDBConfiguration;
import org.openecomp.sdnc.sli.resource.dblib.config.JndiConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @version $Revision: 1.2 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */
public class JndiCachedDataSource extends CachedDataSource
{
	private static Logger LOGGER = LoggerFactory.getLogger(JndiCachedDataSource.class);
	/**
	 * @param alarmLog 
	 * @param jdbcElem
	 * @throws SAXException 
	 * @throws ScpTblUpdateError 
	 */
	public JndiCachedDataSource(BaseDBConfiguration xmlElem) throws DBConfigException
	{
		super(xmlElem);
	}

	protected void configure(BaseDBConfiguration xmlElem) throws DBConfigException {
		JndiConfiguration jdbcConfig = (JndiConfiguration)xmlElem;
    	String jndiContextFactoryStr = jdbcConfig.getJndiContextFactory(); 
    	String jndiURLStr = jdbcConfig.getJndiURL();
    	String jndiSourceStr = jdbcConfig.getJndiSource();
    	
    	if(jdbcConfig.getConnTimeout() > 0){
    		this.CONN_REQ_TIMEOUT = jdbcConfig.getConnTimeout();
    	}
		if(jdbcConfig.getRequestTimeout() > 0){
			this.DATA_REQ_TIMEOUT = jdbcConfig.getRequestTimeout();
		}
    	
    	super.setDbConnectionName(jdbcConfig.getJndiConnectionName());
    	
    	if(jndiContextFactoryStr == null || jndiContextFactoryStr.length() == 0)
    	{
//    		throw new DBConfigException("The jndi configuration is incomplete: jndiContextFactory");
    	}
    	if(jndiURLStr == null || jndiContextFactoryStr.length() == 0)
    	{
//    		throw new ScpTblUpdateError("The jndi configuration is incomplete: jndiURL");
    	}
    	if(jndiSourceStr == null || jndiSourceStr.length() == 0)
    	{
    		throw new DBConfigException("The jndi configuration is incomplete: jndiSource");
    	}

    	Properties env = new Properties();
    	Context ctx; 
		try
		{
			if(jndiContextFactoryStr != null && jndiContextFactoryStr.length() != 0){
				env.put(Context.INITIAL_CONTEXT_FACTORY, jndiContextFactoryStr);
				ctx = new InitialContext(env);
			} else {
				ctx = new InitialContext();
			}
			ds = (javax.sql.DataSource) ctx.lookup (jndiSourceStr);
			if(ds == null)
			{
				this.initialized = false;
				LOGGER.error("AS_CONF_ERROR: Failed to initialize DataSource <"+getDbConnectionName()+"> using JNDI <"+jndiSourceStr+">");
				return;
			} else {
				this.initialized = true;
				LOGGER.info("JndiCachedDataSource <"+getDbConnectionName()+"> configured successfully.");
				return;
			}
		} catch (NamingException exc) {
			this.initialized = false;
			LOGGER.error("AS_CONF_ERROR" + exc.getMessage());

		} catch(Throwable exc) {
			this.initialized = false;
			LOGGER.error("AS_CONF_ERROR: " + exc.getMessage());
		}
    }

	public static JndiCachedDataSource createInstance(BaseDBConfiguration config) {
		return new JndiCachedDataSource(config);
	}
	
	public String toString(){
		return getDbConnectionName();
	}

	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}
}
