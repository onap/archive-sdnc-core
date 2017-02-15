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

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.openecomp.sdnc.sli.resource.dblib.CachedDataSource;
import org.openecomp.sdnc.sli.resource.dblib.CachedDataSourceFactory;
import org.openecomp.sdnc.sli.resource.dblib.DBResourceManager;
import org.openecomp.sdnc.sli.resource.dblib.config.DbConfigPool;
import org.openecomp.sdnc.sli.resource.dblib.config.JndiConfiguration;
import org.openecomp.sdnc.sli.resource.dblib.factory.AbstractResourceManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision: 1.6 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */
public class JNDIDbResourceManagerFactory extends AbstractResourceManagerFactory {

	private static Logger LOGGER = LoggerFactory.getLogger(JNDIDbResourceManagerFactory.class);

	class MyFutureTask extends FutureTask<DBInitTask>
	{

		public MyFutureTask(Callable<CachedDataSource> result) {
			super((Callable)result);
		}
		
	}

	public CachedDataSource[] initDBResourceManager(DbConfigPool dbConfig, DBResourceManager manager, String sourceName) throws SQLException 
	{	
		// here create the data sources objects
		JndiConfiguration[] list = dbConfig.getJndiDbSourceArray();
		CachedDataSource[] cachedDS = new CachedDataSource[1];

		for(int i=0, max=list.length; i<max; i++){
			if(!sourceName.equals(list[i].getJndiConnectionName()))
				continue;

			JndiConfiguration config = list[i];
			CachedDataSource dataSource = CachedDataSourceFactory.createDataSource(config);
			cachedDS[0] = dataSource;
		}
		return cachedDS;
	}
	
	public CachedDataSource[] initDBResourceManager(DbConfigPool dbConfig, DBResourceManager manager) /* throws Exception */ {
//		WSConfigManagement ws = WSConfigManagement.getInstance();
		
		ExecutorService threadExecutor = Executors.newFixedThreadPool(2);
		// here create the data sources objects
		JndiConfiguration[] list = dbConfig.getJndiDbSourceArray();
		FutureTask<DBInitTask>[] futures = new MyFutureTask[list.length];
		final Set<DBInitTask> tasks = new HashSet<DBInitTask>();
		if(LOGGER.isDebugEnabled())
			LOGGER.debug("Creating datasources.");
		for(int i=0, max=list.length; i<max; i++){
			JndiConfiguration config = list[i];
//			if(manager.getJndiContextFactoryStr()!=null && manager.getJndiContextFactoryStr().trim().length()>0){
//				config.setJndiContextFactory(manager.getJndiContextFactoryStr());
//			}
//			if(manager.getJndiURLStr()!=null && manager.getJndiURLStr().trim().length()>0){
//				config.setJndiURL(manager.getJndiURLStr());
//			}
			DBInitTask task = new DBInitTask(config, tasks);
			tasks.add(task);
			futures[i] = new MyFutureTask(task);
		}

		try {
			synchronized(tasks){
				for(int i=0, max=list.length; i<max; i++){
					threadExecutor.execute(futures[i]);
				}
				// the timeout param is set is seconds. 
				long timeout = ((dbConfig.getTimeout() <= 0) ? 60L : dbConfig.getTimeout());
				timeout *= 1000;
				// the timeout param is set is seconds, hence it needs to be multiplied by 1000. 
				tasks.wait(timeout);
				if(LOGGER.isDebugEnabled())
					LOGGER.debug("initDBResourceManager wait completed.");
			}
		} catch(Exception exc) {
			LOGGER.error("Failed to initialize JndiCachedDataSource. Reason: ", exc);
		}
		
		if(threadExecutor != null){
			try {
				threadExecutor.shutdown();
			} catch(Exception exc){}
		}

		CachedDataSource[] cachedDS = new CachedDataSource[futures.length];
		
		boolean initialized = false;
		for(int i=0; i<futures.length; i++){
			Object obj = null;
			if(futures[i].isDone()){
				try {
					obj = futures[i].get();
					if(obj instanceof CachedDataSource){
						cachedDS[i] = (CachedDataSource)obj;
						initialized = true;
						LOGGER.info("DataSource "+list[i].getJndiConnectionName()+" initialized successfully");
					}
				} catch (InterruptedException exc) {
					LOGGER.error("DataSource "+list[i].getJndiConnectionName()+" initialization failed", exc);
				} catch (ExecutionException exc) {
					LOGGER.error("DataSource "+list[i].getJndiConnectionName()+" initialization failed", exc);
				} catch (Exception exc) {
					LOGGER.error("DataSource "+list[i].getJndiConnectionName()+" initialization failed", exc);
				}
			} else {
				try {
					obj = futures[i].get();
					if(obj instanceof CachedDataSource){

						LOGGER.error("DataSource "+((CachedDataSource)obj).getDbConnectionName()+" failed");
					}
				} catch (Exception exc) {
					LOGGER.error("DataSource "+list[i].getJndiConnectionName()+" initialization failed", exc);
				}
			}
		}

		if(!initialized){
			new Error("Failed to initialize DB Library.");
		}
		return cachedDS;
	}
	
	public static AbstractResourceManagerFactory createIntstance() {
		return new JNDIDbResourceManagerFactory();
	}

}
