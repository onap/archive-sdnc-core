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

package org.openecomp.sdnc.sli.resource.dblib;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observer;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import org.openecomp.sdnc.sli.resource.dblib.config.BaseDBConfiguration;
import org.openecomp.sdnc.sli.resource.dblib.pm.SQLExecutionMonitor;
import org.openecomp.sdnc.sli.resource.dblib.pm.SQLExecutionMonitorObserver;
import org.openecomp.sdnc.sli.resource.dblib.pm.SQLExecutionMonitor.TestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @version $Revision: 1.13 $
 * Change Log
 * Author         Date     Comments
 * ============== ======== ====================================================
 * Rich Tabedzki
 */

public abstract class CachedDataSource implements DataSource, SQLExecutionMonitorObserver
{
	private static Logger LOGGER = LoggerFactory.getLogger(CachedDataSource.class);

	protected static final String AS_CONF_ERROR = "AS_CONF_ERROR: ";

	protected long CONN_REQ_TIMEOUT = 30L;
	protected long DATA_REQ_TIMEOUT = 100L;

	private final SQLExecutionMonitor monitor;
	protected DataSource ds = null;
	protected String connectionName = null;
	protected boolean initialized = false;

	private long interval = 1000;
	private long initisalDelay = 5000;
	private long expectedCompletionTime = 50L;
	private boolean canTakeOffLine = true;
	private long unprocessedFailoverThreshold = 3L;


	public CachedDataSource(BaseDBConfiguration jdbcElem) throws DBConfigException
	{
		configure(jdbcElem);
		monitor = new SQLExecutionMonitor(this);
	}

	protected abstract void configure(BaseDBConfiguration jdbcElem) throws DBConfigException;
	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection() throws SQLException
	{
		return ds.getConnection();
	}

	public CachedRowSet getData(String statement, ArrayList<String> arguments) throws SQLException, Throwable
	{
		TestObject testObject = null;
		testObject = monitor.registerRequest();

		Connection connection = null;
		try {
			connection = this.getConnection();
			if(connection ==  null ) {
				throw new SQLException("Connection invalid");
			}
			if(LOGGER.isDebugEnabled())
				LOGGER.debug("Obtained connection <" + connectionName + ">: "+connection.toString());
			return executePreparedStatement(connection, statement, arguments);
		} finally {
			try {
				if(connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch(Throwable exc) {
				// the exception not monitored
			} finally {
				connection = null;
			}

			monitor.deregisterReguest(testObject);
		}
	}

	public boolean writeData(String statement, ArrayList<String> arguments) throws SQLException, Throwable
	{
		TestObject testObject = null;
		testObject = monitor.registerRequest();

		Connection connection = null;
		try {
			connection = this.getConnection();
			if(connection ==  null ) {
				throw new SQLException("Connection invalid");
			}
			if(LOGGER.isDebugEnabled())
				LOGGER.debug("Obtained connection <" + connectionName + ">: "+connection.toString());
			return executeUpdatePreparedStatement(connection, statement, arguments);
		} finally {
			try {
				if(connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch(Throwable exc) {
				// the exception not monitored
			} finally {
				connection = null;
			}

			monitor.deregisterReguest(testObject);
		}
	}

	private CachedRowSet executePreparedStatement(Connection conn, String statement, ArrayList<String> arguments) throws SQLException, Throwable {
		long time = System.currentTimeMillis();

		CachedRowSet data = null;
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("SQL Statement: "+ statement);
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.debug("Argunments: "+ Arrays.toString(arguments.toArray()));
			}
		}

		ResultSet rs = null;
		try {
			data = RowSetProvider.newFactory().createCachedRowSet();
			PreparedStatement ps = conn.prepareStatement(statement);
			if(arguments != null)
			{
				for(int i = 0, max = arguments.size(); i < max; i++){
					ps.setObject(i+1, arguments.get(i));
				}
			}
			rs = ps.executeQuery();
			data.populate(rs);
		    // Point the rowset Cursor to the start
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL SUCCESS. count=" + data.size()+ ", time(ms): "+ (System.currentTimeMillis() - time));			}
		} catch(SQLException exc){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL FAILURE. time(ms): "+ (System.currentTimeMillis() - time));
			}
			try {	conn.rollback(); } catch(Throwable thr){}
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with arguments: "+arguments.toString(), exc);
			} else {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with no arguments. ", exc);
			}
			throw exc;
		} catch(Throwable exc){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL FAILURE. time(ms): "+ (System.currentTimeMillis() - time));
			}
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with arguments: "+arguments.toString(), exc);
			} else {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with no arguments. ", exc);
			}
			throw exc; // new SQLException(exc);
		} finally {

			try {
				if(rs != null){
					rs.close();
					rs = null;
				}
			} catch(Exception exc){

			}
			try {
				if(conn != null){
					conn.close();
					conn = null;
				}
			} catch(Exception exc){

			}
		}

		return data;
	}

	private boolean executeUpdatePreparedStatement(Connection conn, String statement, ArrayList<String> arguments) throws SQLException, Throwable {
		long time = System.currentTimeMillis();

		CachedRowSet data = null;

		int rs = -1;
		try {
			data = RowSetProvider.newFactory().createCachedRowSet();
			PreparedStatement ps = conn.prepareStatement(statement);
			if(arguments != null)
			{
				for(int i = 0, max = arguments.size(); i < max; i++){
					ps.setObject(i+1, arguments.get(i));
				}
			}
			rs = ps.executeUpdate();
		    // Point the rowset Cursor to the start
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL SUCCESS. count=" + data.size()+ ", time(ms): "+ (System.currentTimeMillis() - time));
			}
		} catch(SQLException exc){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL FAILURE. time(ms): "+ (System.currentTimeMillis() - time));
			}
			try {	conn.rollback(); } catch(Throwable thr){}
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with arguments: "+arguments.toString(), exc);
			} else {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with no arguments. ", exc);
			}
			throw exc;
		} catch(Throwable exc){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("SQL FAILURE. time(ms): "+ (System.currentTimeMillis() - time));
			}
			if(arguments != null && !arguments.isEmpty()) {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with arguments: "+arguments.toString(), exc);
			} else {
				LOGGER.error("<"+connectionName+"> Failed to execute: "+ statement + " with no arguments. ", exc);
			}
			throw exc; // new SQLException(exc);
		} finally {

			try {
				if(conn != null){
					conn.close();
					conn = null;
				}
			} catch(Exception exc){

			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	public Connection getConnection(String username, String password)
			throws SQLException
	{
		return ds.getConnection(username, password);
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException
	{
		return ds.getLogWriter();
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException
	{
		return ds.getLoginTimeout();
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter out) throws SQLException
	{
		ds.setLogWriter(out);
	}

	/* (non-Javadoc)
	 * @see javax.sql.DataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds) throws SQLException
	{
		ds.setLoginTimeout(seconds);
	}


	public final String getDbConnectionName(){
		return connectionName;
	}

	protected final void setDbConnectionName(String name) {
		this.connectionName = name;
	}

	public void cleanUp(){
		ds = null;
		monitor.deleteObservers();
		monitor.cleanup();
	}

	public boolean isInitialized() {
		return initialized;
	}

	protected boolean testConnection(){
		return testConnection(false);
	}

	protected boolean testConnection(boolean error_level){
		Connection conn = null;
		ResultSet rs = null;
		Statement stmt = null;
		try
		{
			Boolean readOnly = null;
			String hostname = null;
			conn = this.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT @@global.read_only, @@global.hostname");   //("SELECT 1 FROM DUAL"); //"select BANNER from SYS.V_$VERSION"
			while(rs.next())
			{
				readOnly = rs.getBoolean(1);
				hostname = rs.getString(2);
//				if(rs.getInt(1)==1){
					if(LOGGER.isDebugEnabled()){
						LOGGER.debug("SQL DataSource <"+getDbConnectionName() + "> connected to " + hostname + ", read-only is " + readOnly + ", tested successfully ");
					}
//				}
			}

		} catch (Throwable exc) {
			if(error_level) {
				LOGGER.error("SQL DataSource <" + this.getDbConnectionName() +	"> test failed. Cause : " + exc.getMessage());
			} else {
				LOGGER.info("SQL DataSource <" + this.getDbConnectionName() +	"> test failed. Cause : " + exc.getMessage());
			}
			return false;
		} finally {
			if(rs != null) {
				try {
					rs.close();
					rs = null;
				} catch (SQLException e) {
				}
			}
			if(stmt != null) {
				try {
					stmt.close();
					stmt = null;
				} catch (SQLException e) {
				}
			}
			if(conn !=null){
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
				}
			}
		}
		return true;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@SuppressWarnings("deprecation")
	public void setConnectionCachingEnabled(boolean state)
	{
//		if(ds != null && ds instanceof OracleDataSource)
//			try {
//				((OracleDataSource)ds).setConnectionCachingEnabled(true);
//			} catch (SQLException exc) {
//				LOGGER.warn("", exc);
//			}
	}

	public void addObserver(Observer observer) {
		monitor.addObserver(observer);
	}

	public void deleteObserver(Observer observer) {
		monitor.deleteObserver(observer);
	}

	public long getInterval() {
		return interval;
	}

	public long getInitialDelay() {
		return initisalDelay;
	}

	public void setInterval(long value) {
		interval = value;
	}

	public void setInitialDelay(long value) {
		initisalDelay = value;
	}

	public long getExpectedCompletionTime() {
		return expectedCompletionTime;
	}

	public void setExpectedCompletionTime(long value) {
		expectedCompletionTime = value;
	}

	public long getUnprocessedFailoverThreshold() {
		return unprocessedFailoverThreshold;
	}

	public void setUnprocessedFailoverThreshold(long value) {
		this.unprocessedFailoverThreshold = value;
	}

	public boolean canTakeOffLine() {
		return canTakeOffLine;
	}

	public void blockImmediateOffLine() {
		canTakeOffLine = false;
		final Thread offLineTimer = new Thread()
		{
			public void run(){
				try {
					Thread.sleep(30000L);
				}catch(Throwable exc){

				}finally{
					canTakeOffLine = true;
				}
			}
		};
		offLineTimer.setDaemon(true);
		offLineTimer.start();
	}

	/**
	 * @return the monitor
	 */
	final SQLExecutionMonitor getMonitor() {
		return monitor;
	}

	protected boolean isSlave() {
		CachedRowSet rs = null;
		boolean isSlave = true;
		String hostname = "UNDETERMINED";
		try {
//			rs = this.getData("show slave status", new ArrayList<String>());
//			while(rs.next()) {
//				String master = rs.getString(2);
//				LOGGER.debug("database <"+connectionName+"> defines master as " + master);
//				if(master == null || master.isEmpty() || master.equals(this.getDbConnectionName())) {
//					isSlave = false;
//				} else {
//					isSlave = true;
//				}
//			}

			boolean localSlave = true;
			rs = this.getData("SELECT @@global.read_only, @@global.hostname", new ArrayList<String>());
			while(rs.next()) {
				localSlave = rs.getBoolean(1);
				hostname = rs.getString(2);
			}
			isSlave = localSlave;
		} catch (SQLException e) {
			LOGGER.error("", e);
			isSlave = true;
		} catch (Throwable e) {
			LOGGER.error("", e);
			isSlave = true;
		}
		if(isSlave){
			LOGGER.debug("SQL SLAVE : "+connectionName + " on server " + hostname);			
		} else {
			LOGGER.debug("SQL MASTER : "+connectionName + " on server " + hostname);			
		}
		return isSlave;
	}
	
	public boolean isFabric() {
		return false;
	}
}
