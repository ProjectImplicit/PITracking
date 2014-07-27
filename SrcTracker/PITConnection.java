package org.uva.tracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.uva.dao.ConnectionPool;


/**
 * 
 *  
 * 
 * Singleton class manage database connections
 * 
 * 
 * Date created : 01-Feb-2014
 * 
 * @version $Revision: 10716 $
 * 
 * @author Ben G 
 * 
 * 
 * 
 */

public class PITConnection {
	
	private static PITConnection singleton;

	private HashMap dataSourceHashMap;

	private Calendar cacheDate;
	
	
	
	
	
	private PITConnection() {
	}

	public static PITConnection getInstance(boolean refreshCache) {
		synchronized (PITConnection.class) {
			
			Context initContext=null;
			Context envContext=null;
			DataSource std=null;
			DataSource research = null;
			DataSource fpi=null;
			DataSource oldwareresearch=null;
			DataSource newwaredemo=null;
			DataSource newwareresearch=null;
			DataSource test=null;
				
			
			
			if (singleton == null || refreshCache) {
				singleton = new PITConnection();
				singleton.dataSourceHashMap = new HashMap();
				
				try {
					initContext = new InitialContext();
					envContext = (Context) initContext
							.lookup("java:/comp/env");
					std = (DataSource) envContext.lookup("test/jdbc/std");
					research = (DataSource) envContext.lookup("test/jdbc/research");
					//fpi = (DataSource) envContext.lookup("tesst/jdbc/fpi");
					newwareresearch = (DataSource) envContext.lookup("jdbc/dw/research");
					oldwareresearch = (DataSource) envContext.lookup("jdbc/dw/research");
					newwaredemo = (DataSource) envContext.lookup("jdbc/dw/std");
					//test = (DataSource) envContext.lookup("jdbc/blabla");
				} catch (Exception e) {
					
					System.out.print(e);
				}
					singleton.dataSourceHashMap.put("teststd", std);
					singleton.dataSourceHashMap.put("testresearch", research);
					//singleton.dataSourceHashMap.put("fpi", fpi);
					singleton.dataSourceHashMap.put("oldwarehouseresearch", oldwareresearch);
					singleton.dataSourceHashMap.put("oldwarehousestd", oldwareresearch);
					singleton.dataSourceHashMap.put("newwarehouseresearch", newwareresearch);
					singleton.dataSourceHashMap.put("newwarehousestd", newwaredemo);
					singleton.dataSourceHashMap.put("test", test);
					
					singleton.cacheDate = new GregorianCalendar();
		
			}
		}
		return singleton;
	}

	public static Connection getNewConnection(String dataID) throws Exception{
		
		Connection con = null;
		if (dataID.equals("testresearch")){
			
			con = DriverManager.getConnection("jdbc:oracle:thin:@dw2.psyc.virginia.edu:1521:XE","yuiat_research_user","resuser");
		}
		if (dataID.equals("teststd")){
			
			con = DriverManager.getConnection("jdbc:oracle:thin:@dw2.psyc.virginia.edu:1521:XE","yuiat_std_user","stduser");
		
		}
			
		return con;
		
		
	}
public static Connection getConnection(String databaseId) throws Exception {
		try {
			
			if (singleton == null) {
				singleton = PITConnection.getInstance(false);
			}
			
			DataSource dataSource = (DataSource) singleton.dataSourceHashMap
					.get(databaseId);
			
			// If you deploy your application to JBoss,try the following code 
			// instead of returning normal java.sql.Conneciton 
			//return ((WrappedConnection) dataSource.getConnection()).getUnderlyingConnection();
			
			return (dataSource.getConnection());
		} catch (SQLException e) {
			
			throw new Exception("Unable to get connection for database id: "
					+ databaseId, e);
			
		}
	}	protected static boolean hasConnectionPool(String databaseId) {
		if (singleton == null) {
			singleton = PITConnection.getInstance(false);
		}
		return singleton.dataSourceHashMap.containsKey(databaseId);
	}

	/**
	 * Returns the connection pool cache date formatted as a date-time stamp
	 * 
	 * @return the date the connection pool was created in date-time stamp
	 *         format
	 */
	public static String getCacheDateFormatted() {
		return singleton.cacheDate.toString();
	}

}
	


