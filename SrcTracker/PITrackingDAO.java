package org.uva.tracker;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.sql.DataSource;
import org.uva.util.PITConnection;
import org.uva.util.sessionBean;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.RootLogger;


/**
 * 
 *  
 * 
 * Runnable class that run the main query threads.
 * 
 * Each tracker runs a seperate query, and saves the result.    
 * 
 * 
 * Saves the result in a results HashMap. 
 * 
 * Date created : 01-Feb-2014
 * 
 * @version $Revision: 10720 $
 * 
 * @author Ben G 
 * 
 * 
 * 
 */

public class PITrackingDAO implements Runnable {
	
	
	/**
	 * 
	 * HashMap that saves the arguments for the tracker queries.
	 * 
	 * The arguments are set by the tracker servlet. 
	 * 
	 */

	private HashMap arguments =null;
	
	/**
	 * 
	 * HashMap that saves the results of the tracker queries.
	 * 
	 * Those results are processed into CSV format by the tracker 
	 * 
	 * Servlet.
	 * 
	 */
	
	private HashMap results;
	
	
	
	/**
	 * 
	 * Initialize loger
	 * 
	 * 
	 * 	 
	 * @param 
	 * 			
	 * 
	 * @return void
	 * 
	 */
		
	
	public void initLogger(){
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.DEBUG);
		PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
		rootLogger.addAppender(new ConsoleAppender(layout));

		try

		{
			RollingFileAppender fileAppender = new RollingFileAppender(layout, "pitracker.log");
			rootLogger.addAppender(fileAppender);
		}

		catch (IOException e)

		{

		System.out.println("Failed to add appender !!");

		}
	}
	
	
	/**
	 * 
	 * Class constractor
	 * 
	 * 	 
	 * @param 
	 * 			            
	 * 
	 * @return 
	 * 
	 */
	
	public PITrackingDAO() throws Exception{
		arguments = new HashMap();
		results=new HashMap();
		
	}
	
	/**
	 * 
	 * Class constractor
	 * 	 
	 * @param 
	 * 			
	 * 
	 * @return void
	 * 
	 */
	
	public PITrackingDAO(HashMap args) throws Exception{
		arguments = args;
		results=new HashMap();
	
	}
	
	/**
	 * 
	 * Getter of session Array 
	 * 	 
	 * @param 
	 * 			
	 * 
	 * @return session array
	 * 
	 */
	
	public ArrayList getsessionArray(){
		return (ArrayList) results.get("sessions");
	}
	
	/**
	 * 
	 * Handel POST saves request parameters in HashMap.
	 * Returns the Data requested in CSV Form
	 * 
	 * 	 
	 * @param 
	 * 			request and response sevlet objects.
	 *            
	 * 
	 * @return CSV string
	 * 
	 */
	
	public HashMap getResults(){
		return results;
	}
	
	/**
	 * 
	 * Handel POST saves request parameters in HashMap.
	 * Returns the Data requested in CSV Form
	 * 
	 * 	 
	 * @param 
	 * 			request and response sevlet objects.
	 *            
	 * 
	 * @return CSV string
	 * 
	 */
	
	public HashMap getArguments(){
		return arguments;
	}
	
	/**
	 * 
	 * Handel POST saves request parameters in HashMap.
	 * Returns the Data requested in CSV Form
	 * 
	 * 	 
	 * @param 
	 * 			request and response sevlet objects.
	 *            
	 * 
	 * @return CSV string
	 * 
	 */
	
	public void setMethod(String v){
		arguments.put("method", v);
	}
	
	/**
	 * 
	 * Handel POST saves request parameters in HashMap.
	 * Returns the Data requested in CSV Form
	 * 
	 * 	 
	 * @param 
	 * 			request and response sevlet objects.
	 *            
	 * 
	 * @return CSV string
	 * 
	 */
	
	
	public void setTasks(ArrayList tasks){
		arguments.put("tasks", tasks);
	}
	
	

	
	/**
	 * 
	 * Counts the number of completed tasks and
	 * Saves this data in the arrayList of tasks.
	 * 
	 * 	 
	 * @param tasks,comleted
	 * 			tasks ArrayList of tasks   
	 *          completed   
	 * 
	 * @return void
	 * 
	 */
	
	private void calculateTasks(ArrayList<HashMap> tasks,int completed) throws Exception{
		try{
			
			for (int i=0;i<tasks.size();i++){
				HashMap task = tasks.get(i);
				if(task.get("completed")!=null){
					Integer complete = (Integer) task.get("completed");
					complete+= completed;
					task.put("completed",complete);
					
				}else{
					task.put("completed",completed);
				}
				
			}
		}catch(Exception e){
			throw e;
		}
		
	}
	
	/**
	 * 
	 * Calculates the number of sessions completed 
	 * 
	 * or started for a task.
	 * 	 
	 * @param tasks,task,id
	 * 			tasks HashMap of tasks.
	 * 			task  tak to update
	 *          id session id  
	 * 
	 * @return void
	 * 
	 */
	
	private void updateTasks(ArrayList tasks,HashMap task,String id) throws Exception{
		
		try{
			Integer started = (Integer) task.get("started");
			Integer completed = (Integer) task.get("completed");
			if (started == null || completed == null) throw new Exception("updateTasks: started or completed is null");
			boolean exit =false;
			for (int i=0;i<tasks.size()&& !exit;i++){
				HashMap t = (HashMap) tasks.get(i);
				String name = (String) t.get("name");
				if (name==null) throw new Exception("updateTasks: name is null");
				if (name.equals(task.get("name"))){
					exit=true;
					if (started==1){
						int startedmainTask = (Integer) t.get("started");
						startedmainTask++;
						t.put("started", startedmainTask);
					}
					if (completed==1){
						//if (name.equals("control")) System.out.println(id);
						int completedmainTask = (Integer) t.get("completed");
						completedmainTask++;
						t.put("completed", completedmainTask);
						
					}
					
				}
				
			}
		}catch(Exception e){
			throw new Exception("error in updateTasks: "+e.getMessage());
		}
	}
	
	/**
	 * 
	 * Goes over the sessions and update the tasks
	 * 
	 * 
	 * 	 
	 * @param  sessions,tasks  
	 * 			sessions ArrayList of sessions
	 *          tasks ArrayList of tasks  
	 * 
	 * @return void
	 * 
	 */
	
	private void calculateSessionTasks(ArrayList sessions,ArrayList tasks) throws Exception{
	
		try{
			for (int i=0;i<sessions.size();i++){
				sessionBean session= (sessionBean) sessions.get(i);
				ArrayList sessionTasks = session.getTasks();
				for (int j=0;j<sessionTasks.size();j++){			
					HashMap task = (HashMap) sessionTasks.get(j);
					updateTasks(tasks,task,session.getId());
				}
			}
			
		}catch(Exception e){
			throw new Exception("error in calculateSessionTasks: "+e.getMessage());
		}
		
		
	}

	/**
	 * 
	 * Calculate the completed sessions 
	 * 
	 * 
	 * 	 
	 * @param 
	 * 			sessions ArrayList of sessions
	 *            
	 * 
	 * @return void
	 * 
	 */
	
	public void calculateCompleted(ArrayList sessions) throws Exception{
		
			try{
				for (int j=0;j<sessions.size();j++){
					sessionBean session = (sessionBean) sessions.get(j);
					ArrayList tasks = session.getTasks();
					String status = session.getStatus();
					if (status==null) status="null";
					if (!status.equals("C")){
						Iterator it = null;
						if(tasks!=null) {
							it = tasks.iterator();
						}
						while (it.hasNext()){
							HashMap task = (HashMap) it.next();
							if (task!= null){
								if (it.hasNext()){
									task.put("completed", 1);
								}else{
									
									task.put("completed", 0);
								}
							}
						}
						
					}else{
						Iterator it = tasks.iterator();
						while (it.hasNext()){
							HashMap task = (HashMap) it.next();
							if (task!= null){
								
								task.put("completed", 1);
							}
						}
						
					}
				}
			}catch(Exception e){
				throw new Exception("error in calculateCompleted: "+e.getMessage());
			}
				
		
			
	}
	
	/**
	 * 
	 * Get the list of tasks from session tasks.
	 * 
	 * 
	 * 	 
	 * @param dataGroup,sessions,studyTasks
	 * 			
	 * 			dataGroup DataBase string 
	 *            
	 * 
	 * @return void
	 * 
	 */
	
	
	public void getstartedTasks(String dataGroup,ArrayList sessions,ArrayList studyTasks) throws Exception{
		
		Connection connection = null;
		boolean exit=false;
		try{
			PITConnection.getInstance(false);
			connection = PITConnection.getConnection(dataGroup);
			if (connection.isClosed()){
				PITConnection.getInstance(true);
				connection = PITConnection.getConnection(dataGroup);
			}
			String sessionsString;
			if (sessions.size()==0) return;
			String questionare = "SELECT DISTINCT TASK_ID,CREATION_DATE FROM yuiat_session_tasks_v where session_id = ? order by CREATION_DATE";
			PreparedStatement ps = connection.prepareStatement(questionare);
			for (int j=0;j<sessions.size();j++){
				sessionBean session = (sessionBean) sessions.get(j);
				String id = session.getId();
				long sessionID = Long.parseLong(id);
				ArrayList tasks = new ArrayList<HashMap>();
				ps.setLong(1, sessionID);
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					HashMap task = new HashMap();
					String name = rs.getString(1);
					populateTasks(studyTasks,name);
					task.put("name", name);
					task.put("started", 1);
					tasks.add(task);
				}
				session.setTasks(tasks);	
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in connection to DataBase in getstartedTasks: "+e.getMessage());
			throw new Exception("connection error in getstartedTasks: "+e.getMessage());
		} finally {
			try {
				if (connection != null) connection.close();
			}catch (Exception ce) {
				ce.printStackTrace();
				throw new Exception("cannot close connection"+ce.getMessage());
			}
		}
	}
	
	/**
	 * 
	 * Get session of a study from session table
	 * 
	 * 
	 * 	 
	 * @param db,name
	 * 			db DataBase string
	 * 			name Name of study
	 *            
	 * 
	 * @return ArrayList of sessions
	 * 
	 */
	
	public ArrayList getStudiesfromDB(String db,String name) throws Exception{
		
		Connection connection = null;
		ArrayList recordSet = new ArrayList();
		String questionare = "";
		questionare = "SELECT DISTINCT study_name FROM yuiat_sessions_v where study_name like '%"+name+"%'";
		try{
			PITConnection.getInstance(false);
			connection = PITConnection.getConnection(db);
			if (connection.isClosed()){
				PITConnection.getInstance(true);
				connection = PITConnection.getConnection(db);
			}
			connection.setAutoCommit(false);
			PreparedStatement psII = connection.prepareStatement(questionare);
			psII.setFetchSize(3000);
			ResultSet rsII = psII.executeQuery();
			while(rsII.next()){
				String name1 = rsII.getString(1);
				if (!recordSet.contains(name1)) recordSet.add(name1); 
			}
		}catch(Exception e ){
			System.out.println(e.getMessage());
			throw (new Exception("Exception in getStudiesfromDB: "+e.getMessage()));
			
		} finally {
			try {
				if (connection!=null) connection.close();
			} catch (Exception ce) {
				ce.printStackTrace();
				throw ce;
			};
		}
		
		return recordSet;
	}
	

	
	/**
	 * 
	 *	Compute the sessions and tasks for a study 
	 * 	 
	 * @param db,name
	 * 			db DataBase string
	 * 			name Name of study
	 *            
	 * 
	 * @return ArrayList of sessions and tasks
	 * 
	 */
	
	public ArrayList getSessionPerResultSetTask(String StudyName,String from,String until,String db,String task,String timec,
			String datac,ArrayList times,String filter) throws Exception{
		
		
		Connection connection = null;
		boolean where = false;
		ArrayList recordSet = new ArrayList();
		String questionare = "";
		questionare = "SELECT DISTINCT SESSION_ID,study_name,session_status,creation_date FROM yuiat_sessions_v";
		if (StudyName.equals("")){
		}else{
				questionare = questionare + " where study_name ='"+StudyName+"'";
				where=true;
		}
		if (!from.equals("") && !until.equals("")){
			if (where){
				if (from.equals(until)){
					questionare = questionare + " AND TRUNC(CREATION_DATE) = TO_DATE('"+from+"','mm/dd/yyyy')";
				}else{
					questionare = questionare + " AND TRUNC(CREATION_DATE) >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
					
				}
			}else{
				if (from.equals(until)){
					questionare = questionare + " where TRUNC(CREATION_DATE) = TO_DATE('"+from+"','mm/dd/yyyy')";
				}else{
					questionare = questionare + " where TRUNC(CREATION_DATE) >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
					
				}
			}
		}
		if(!task.equals("")){
			if (StudyName=="" || StudyName ==null){
				questionare = "select v.SESSION_ID,v.study_name,v.session_status,v.creation_date from YUIAT_SESSIONS_V v,YUIAT_SESSION_TASKS_V t where v.SESSION_ID=t.SESSION_ID and t.TASK_ID like '%"+task+"%'";
			}else{
				questionare = "select v.SESSION_ID,v.study_name,v.session_status,v.creation_date from YUIAT_SESSIONS_V v,YUIAT_SESSION_TASKS_V t where v.SESSION_ID=t.SESSION_ID and v.STUDY_NAME ='"+StudyName+"' and t.TASK_ID like '%"+task+"%'";
			}
			if (!from.equals("") && !until.equals("")){
				if (from.equals(until)){
					questionare = questionare + " AND TRUNC(v.CREATION_DATE) =  TO_DATE('"+from+"','mm/dd/yyyy')";
				}else{
					questionare = questionare + " AND TRUNC(v.CREATION_DATE) >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(v.CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
					
				}
			}
		}
		questionare = questionare + " order by creation_date";
		try{
			
				PITConnection.getInstance(false);
				connection = PITConnection.getConnection(db);
				if (connection.isClosed()){
					PITConnection.getInstance(true);
					connection = PITConnection.getConnection(db);
				}
			
				connection.setAutoCommit(false);
				PreparedStatement psII = connection.prepareStatement(questionare);
				psII.setFetchSize(100000);
				ResultSet rsII = psII.executeQuery();
				String sinceTime;
				String untilTime;
				SimpleDateFormat sdf; 
				SimpleDateFormat sessiondf;
				boolean stop=false;
				sdf = new SimpleDateFormat("MM/dd/yyyy");
				sessiondf = new SimpleDateFormat("yyyy-MM-dd");
				
				for (int i=0;i<times.size();i++){
					
					HashMap timeArray = (HashMap) times.get(i);
					timeArray.put("NumberOfCompletedSessions", 0);
					timeArray.put("NumberOfInCompletedSessions", 0);
					ArrayList tasks = new ArrayList();
					timeArray.put("tasks", tasks);
					
				}
				while(rsII.next()){
					
					if (rsII.getString(1)!=null && rsII.getString(4)!=null){
						
						String date = rsII.getString(4);
						String[] dates = date.split(" ");
						date = dates[0];
						Date sessionDate = sessiondf.parse(date);
						for (int i=0;i<times.size();i++){
							HashMap timeArray = (HashMap) times.get(i);
							sinceTime = (String) timeArray.get("since");
							untilTime = (String) timeArray.get("until");
							Date sinceDate = sdf.parse(sinceTime);
							Date untilDate = sdf.parse(untilTime);
							if ( ( sessionDate.after(sinceDate) || sessionDate.equals((sinceDate)) ) && ( sessionDate.before(untilDate) || sessionDate.equals(untilDate) ) ){
								
								if ( rsII.getString(3) == null || !(rsII.getString(3).equals(( "C" )) )){
									int inco = (Integer) timeArray.get("NumberOfInCompletedSessions");
									timeArray.put("NumberOfInCompletedSessions", ++inco);
									
									
								}else{
									//System.out.println("session:"+rsII.getString(1)+" date: "+rsII.getString(4));
									int co = (Integer) timeArray.get("NumberOfCompletedSessions");
									timeArray.put("NumberOfCompletedSessions", ++co);
		
									
								}
								ArrayList sessionArray = new ArrayList();
								sessionBean bean = new sessionBean();
								sessionBean session = new sessionBean();
								session.setID(rsII.getString(1));//session id
								session.setName(rsII.getString(2));//study name
								session.setStatus(rsII.getString(3));//session status
								session.setDate(rsII.getString(4));//creation date
								sessionArray.add(session);
								getstartedTasks(db,sessionArray,(ArrayList)timeArray.get("tasks"));
								calculateCompleted(sessionArray);
								calculateSessionTasks(sessionArray,(ArrayList)timeArray.get("tasks"));
								
								
							}
						}
								
					}else{System.out.println("id or date is null");}
				}
					
				for (int i=0;i<times.size();i++){
					ArrayList record=new ArrayList();
					float sCR;
					float cr;
					HashMap timeArray = (HashMap) times.get(i);
					sinceTime = (String) timeArray.get("since");
					untilTime = (String) timeArray.get("until");
					
									
					ArrayList tasks= (ArrayList) timeArray.get("tasks");
					for (int j=0;j<tasks.size();j++){
						ArrayList recordT =  new ArrayList();
						HashMap taskfromArray = new HashMap();
						taskfromArray = (HashMap) tasks.get(j);
						
						
						String tName = (String) taskfromArray.get("name");
						if (tName.contains(filter) || filter.equals("")){
							recordT.add(StudyName);
							float started = ((Integer) taskfromArray.get("started"));
							float complete = ((Integer) taskfromArray.get("completed"));
							recordT.add(taskfromArray.get("name"));
							if (timec.equals("true")){
								if (sinceTime.equals(untilTime)){
									recordT.add(sinceTime);
								}else{
									recordT.add(sinceTime+" - "+untilTime);
								}
							}
							if (datac.equals("true")){
								if (db.contains("std")){
									recordT.add("Demo");//add database
								}
								if (db.contains("research")){
									
									recordT.add("research");
								}
								
							}
							recordT.add(String.valueOf((int)started));
							recordT.add(String.valueOf((int)complete));
							if (started==0){
								recordT.add(String.valueOf(0));
								
							}else{
								recordT.add(String.valueOf( Math.round(((complete)/started)*100 )) );
								
							}
							
						}
						if(recordT.size()!=0) recordSet.add(recordT);
					}
				}
					
				
				
		}catch (Exception e) {
			
			e.printStackTrace();
			System.out.println("Error in connection to DataBase: "+e.getMessage());
			throw new Exception("error in getSession: "+e.getMessage());
			
		} finally {
			try {
				if (connection!=null) connection.close();
			
			} catch (Exception ce) {
				ce.printStackTrace();
				throw ce;}
		}
	
		return recordSet;
	
	}

	/**
	 * 
	 *	Create the SQL string according to arguments  
	 * 	 
	 * @param args
	 * 			args HashMap of arguments
	 * 			
	 *            
	 * 
	 * @return SQL string
	 * 
	 */
	
	private String createSQLString(HashMap args){
		
		String questionare;
		boolean where = false;
		questionare = "SELECT DISTINCT SESSION_ID,study_name,session_status,creation_date FROM yuiat_sessions_v";
		String StudyName = (String)arguments.get("studyname"); 
		String from = (String)arguments.get("since");
		String until = (String)arguments.get("until");
		String task = (String)arguments.get("task");
		//,(String)arguments.get("timec"),(String)arguments.get("datac"),(ArrayList)arguments.get("times")
		
		
		if (StudyName.equals("")){
		}else{
				questionare = questionare + " where study_name ='"+StudyName+"'";
				where=true;
		}
		if (!from.equals("") && !until.equals("")){
			if (where){
				if (from.equals(until)){
					questionare = questionare + " AND TRUNC(CREATION_DATE) = TO_DATE('"+from+"','mm/dd/yyyy')";
				}else{
					questionare = questionare + " AND TRUNC(CREATION_DATE) >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
					
				}
			}else{
				if (from.equals(until)){
					questionare = questionare + " where TRUNC(CREATION_DATE) = TO_DATE('"+from+"','mm/dd/yyyy')";
				}else{
					questionare = questionare + " where TRUNC(CREATION_DATE) >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
					
				}
			}
		}
		if(!task.equals("")){
			if (StudyName=="" || StudyName ==null){
				questionare = "select v.SESSION_ID,v.study_name,v.session_status,v.creation_date from YUIAT_SESSIONS_V v,YUIAT_SESSION_TASKS_V t where v.SESSION_ID=t.SESSION_ID and t.TASK_ID like '%"+task+"%'";
			}else{
				questionare = "select v.SESSION_ID,v.study_name,v.session_status,v.creation_date from YUIAT_SESSIONS_V v,YUIAT_SESSION_TASKS_V t where v.SESSION_ID=t.SESSION_ID and v.STUDY_NAME ='"+StudyName+"' and t.TASK_ID like '%"+task+"%'";
			}
			if (!from.equals("") && !until.equals("")){
				if (from.equals(until)){
					questionare = questionare + " AND TRUNC(v.CREATION_DATE) =  TO_DATE('"+from+"','mm/dd/yyyy')";
				}else{
					questionare = questionare + " AND TRUNC(v.CREATION_DATE) >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(v.CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
					
				}
			}
		}
		questionare = questionare + " order by creation_date";
		return questionare;
		
	}

	/**
	 * 
	 *	Returns boolean if a task was visited 
	 * 	 
	 * @param sessionID,endTask,ps
	 * 			
	 * 			sessionID The session ID.
	 * 			endTask 
	 *          ps The prepared statment to use.  
	 * 
	 * @return true if visited
	 * 
	 */
	
	private boolean visited(String sessionID,String endTask,PreparedStatement ps) throws Exception{
		
		try{
			long id =  Long.parseLong(sessionID);
			ps.setLong(1,id);
			ResultSet rs = ps.executeQuery();
						
			while(rs.next()){
				
				String name = rs.getString(1);
				if (name.contains(endTask)){
					
					return true;
				}
			}
			return false;
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in visited "+e.getMessage());
			throw new Exception("error in visited: "+e.getMessage());
			
		
		} finally {
			try {
			} catch (Exception ce) {
				ce.printStackTrace();
				throw ce;}
		}
			
	}
	
	/**
	 * 
	 *	Calculates the session and tasks  
	 * 	 
	 * @param sessionID,endTask,ps
	 * 			
	 * 			sessionID The session ID.
	 * 			endTask 
	 *          ps The prepared statment to use.  
	 * 
	 * @return Arraylist of records
	 * 
	 */
	
	public ArrayList getSessionPerResultSetStudy() throws Exception{
		
		String questionare = createSQLString(arguments);
		Connection connection = null;
		ArrayList recordSet = new ArrayList();
		String StudyName = (String)arguments.get("studyname"); 
		String from = (String)arguments.get("since");
		String until = (String)arguments.get("until");
		String db = (String)arguments.get("db");
		String task = (String)arguments.get("task");
		String timec = (String)arguments.get("timec");
		String datac = (String)arguments.get("datac");
		String endTask = (String)arguments.get("endTask");
		ArrayList times = (ArrayList)arguments.get("times");
		
		try{
			PITConnection.getInstance(false);
			connection = PITConnection.getConnection(db);
			if (connection.isClosed()){
				PITConnection.getInstance(true);
				connection = PITConnection.getConnection(db);
			}
			connection.setAutoCommit(false);
			
			
			PreparedStatement ps = connection.prepareStatement(questionare);
			ps.setFetchSize(100000);
			ResultSet rs = ps.executeQuery();
			String sinceTime;
			String untilTime;
			SimpleDateFormat sdf; 
			SimpleDateFormat sessiondf;
			boolean stop=false;
			sdf = new SimpleDateFormat("MM/dd/yyyy");
			sessiondf = new SimpleDateFormat("yyyy-MM-dd");
			String questionareII  ="SELECT DISTINCT TASK_ID FROM yuiat_session_tasks_v where session_id = ?";
			PreparedStatement psII = connection.prepareStatement(questionareII);
			for (int i=0;i<times.size();i++){
				
				HashMap timeArray = (HashMap) times.get(i);
				timeArray.put("NumberOfCompletedSessions", 0);
				timeArray.put("NumberOfInCompletedSessions", 0);
			}
			while(rs.next()){
				
				if (rs.getString(1)!=null && rs.getString(4)!=null){
					
					String date = rs.getString(4);
					String[] dates = date.split(" ");
					date = dates[0];
					Date sessionDate = sessiondf.parse(date);
					
					for (int i=0;i<times.size();i++){
							
						HashMap timeArray = (HashMap) times.get(i);
						sinceTime = (String) timeArray.get("since");
						untilTime = (String) timeArray.get("until");
						Date sinceDate = sdf.parse(sinceTime);
						Date untilDate = sdf.parse(untilTime);
						if ( ( sessionDate.after(sinceDate) || sessionDate.equals((sinceDate)) ) && ( sessionDate.before(untilDate) || sessionDate.equals(untilDate) ) ){
							int inco = (Integer) timeArray.get("NumberOfInCompletedSessions");
							int co = (Integer) timeArray.get("NumberOfCompletedSessions");
							if (!endTask.equals("")){
								if ( visited( rs.getString(1),endTask,psII) ){
									timeArray.put("NumberOfCompletedSessions", ++co);
								}else{
									timeArray.put("NumberOfInCompletedSessions", ++inco);
								}
								
							}else{
								if ( rs.getString(3) == null || !(rs.getString(3).equals(( "C" )) )){
									timeArray.put("NumberOfInCompletedSessions", ++inco);
								}else{
									timeArray.put("NumberOfCompletedSessions", ++co);
								}
								
							}
						}
					}
				}else{System.out.println("id or date is null");}
			}
			
				for (int i=0;i<times.size();i++){
					ArrayList record=new ArrayList();
					float sCR;
					float cr;
					HashMap timeArray = (HashMap) times.get(i);
					sinceTime = (String) timeArray.get("since");
					untilTime = (String) timeArray.get("until");
					
					record.add(StudyName);
					if (timec.equals("true")){
						if (sinceTime.equals(untilTime)){
							record.add(sinceTime);
						}else{
							record.add(sinceTime+" - "+untilTime);
						}
					}
					
					if (datac.equals("true")){
						if (db.contains("std")){
							record.add("Demo");//add database
							
						}
						if (db.contains("research")){
							record.add("research");
						}
						
					}
					
					record.add(String.valueOf((Integer)timeArray.get("NumberOfInCompletedSessions")+(Integer)timeArray.get("NumberOfCompletedSessions")));
					record.add(String.valueOf((Integer)timeArray.get("NumberOfCompletedSessions")));
					if ( ( (Integer)timeArray.get("NumberOfCompletedSessions") )!=0){
						 sCR = (Integer)timeArray.get("NumberOfInCompletedSessions")+(Integer)timeArray.get("NumberOfCompletedSessions");
						 cr  = ( ( ( (Integer)timeArray.get("NumberOfCompletedSessions") ) /sCR )*100);
					}else{
						cr=0;
						
					}
					record.add(String.valueOf(Math.round(cr) ));
					recordSet.add(record);
				}
				
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in connection to DataBase: "+e.getMessage());
			throw new Exception("error in getSession: "+e.getMessage());
		} finally {
			try {
				if (connection!=null) connection.close();
	
			} catch (Exception ce) {
				ce.printStackTrace();
				throw ce;}
		}
		return recordSet;
		
		
	}

	/**
	 * 
	 *	Calculates the session and tasks  
	 * 	 
	 * @param args
	 * 			
	 * 			args HashMap of arguments
	 * 			  
	 * 
	 * @return Arraylist of records
	 * 
	 */
	

	public ArrayList getSessionPerResultSet(HashMap args) throws Exception{
		
		String questionare = createSQLString(args);
		Connection connection = null;
		ArrayList recordSet = new ArrayList();
		String StudyName = (String)arguments.get("studyname"); 
		String from = (String)arguments.get("since");
		String until = (String)arguments.get("until");
		String db = (String)arguments.get("db");
		String task = (String)arguments.get("task");
		String timec = (String)arguments.get("timec");
		String datac = (String)arguments.get("datac");
		String endTask = (String)arguments.get("endTask");
		ArrayList times = (ArrayList)arguments.get("times");
		boolean where = false;
		
		
		try{
				
				PITConnection.getInstance(false);
				connection = PITConnection.getConnection(db);
				if (connection.isClosed()){
					PITConnection.getInstance(true);
					connection = PITConnection.getConnection(db);
				}
				connection.setAutoCommit(false);
				PreparedStatement ps = connection.prepareStatement(questionare);
				ResultSet rs = ps.executeQuery();
				String sinceTime;
				String untilTime;
				SimpleDateFormat sdf; 
				SimpleDateFormat sessiondf;
				boolean stop=false;
				sdf = new SimpleDateFormat("MM/dd/yyyy");
				sessiondf = new SimpleDateFormat("yyyy-MM-dd");
				PreparedStatement psTask = null;
				String questionareII  ="SELECT DISTINCT TASK_ID FROM yuiat_session_tasks_v where session_id = ?";
				PreparedStatement psII = connection.prepareStatement(questionareII);
				while(rs.next()){
					
					if (rs.getString(1)!=null && rs.getString(4)!=null){
						
						String date = rs.getString(4);
						String[] dates = date.split(" ");
						date = dates[0];
						Date sessionDate = sessiondf.parse(date);
						
						for (int i=0;i<times.size();i++){
								
							HashMap timeArray = (HashMap) times.get(i);
							sinceTime = (String) timeArray.get("since");
							untilTime = (String) timeArray.get("until");
							Date sinceDate = sdf.parse(sinceTime);
							Date untilDate = sdf.parse(untilTime);
							if ( ( sessionDate.after(sinceDate) || sessionDate.equals((sinceDate)) ) && ( sessionDate.before(untilDate) || sessionDate.equals(untilDate) ) ){
								int inco = (Integer) timeArray.get("NumberOfInCompletedSessions");
								int co = (Integer) timeArray.get("NumberOfCompletedSessions");
								if (!endTask.equals("")){
									if ( visited( rs.getString(1),endTask,psII) ){
										timeArray.put("NumberOfCompletedSessions", ++co);
									}else{
										timeArray.put("NumberOfInCompletedSessions", ++inco);
									}
									
								}else{
									if ( rs.getString(3) == null || !(rs.getString(3).equals(( "C" )) )){
										timeArray.put("NumberOfInCompletedSessions", ++inco);
									}else{
										timeArray.put("NumberOfCompletedSessions", ++co);
									}
									
								}
	
							}
						}
					}else{System.out.println("id or date is null");}
				}
				
				for (int i=0;i<times.size();i++){
					ArrayList record=new ArrayList();
					float sCR;
					float cr;
					HashMap timeArray = (HashMap) times.get(i);
					sinceTime = (String) timeArray.get("since");
					untilTime = (String) timeArray.get("until");
					if (timec.equals("true")){
						if (sinceTime.equals(untilTime)){
							record.add(sinceTime);
						}else{
							record.add(sinceTime+" - "+untilTime);
						}
						
					}
					if (datac.equals("true")){
						if (db.contains("std")){
							record.add("Demo");//add database
						}
						if (db.contains("research")){
							record.add("research");
						}
						
					}
					record.add(String.valueOf((Integer)timeArray.get("NumberOfInCompletedSessions")+(Integer)timeArray.get("NumberOfCompletedSessions")));
					record.add(String.valueOf((Integer)timeArray.get("NumberOfCompletedSessions")));
					if ( ( (Integer)timeArray.get("NumberOfCompletedSessions") )!=0){
						 sCR = (Integer)timeArray.get("NumberOfInCompletedSessions")+(Integer)timeArray.get("NumberOfCompletedSessions");
						 cr  = ( ( ( (Integer)timeArray.get("NumberOfCompletedSessions") ) /sCR )*100);
					}else{
						cr=0;
						
					}
					record.add(String.valueOf(Math.round(cr) ));
					recordSet.add(record);
					
					
						
				}
				
		}catch (Exception e) {
			
			e.printStackTrace();
			System.out.println("Error in connection to DataBase: "+e.getMessage());
			throw new Exception("error in getSession: "+e.getMessage());
		} finally {
			try {
				if (connection!=null) connection.close();
			
			} catch (Exception ce) {
				ce.printStackTrace();
				throw ce;}
		}
		if (StudyName==null) System.out.println("StudyName is null");
		System.out.println(StudyName + ":" +recordSet.toString());
		return recordSet;
		
				
	}
	
	/**
	 * 
	 *	Calculates the sessions for a study  
	 * 	 
	 * @param sessionID,endTask,ps
	 * 			
	 * 			sessionID The session ID.
	 * 			endTask 
	 *          ps The prepared statment to use.  
	 * 
	 * @return Arraylist of records
	 * 
	 */
	
	public ArrayList getSessionPerResultSet(String StudyName,String from,String until,String db,String task,String timec,String datac,ArrayList times) throws Exception{
		
		
		Connection connection = null;
		boolean where = false;
		ArrayList recordSet = new ArrayList();
		String questionare = "";
		questionare = "SELECT DISTINCT SESSION_ID,study_name,session_status,creation_date FROM yuiat_sessions_v";
			
		if (StudyName.equals("")){
		}else{
				questionare = questionare + " where study_name = '"+StudyName+"'";
				where=true;
				
				
		}
		if (!from.equals("") && !until.equals("")){
			if (where){
				if (from.equals(until)){
					questionare = questionare + " AND TRUNC(CREATION_DATE) = TO_DATE('"+from+"','mm/dd/yyyy')";
				}else{
					questionare = questionare + " AND TRUNC(CREATION_DATE) >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
					
				}
			}else{
				if (from.equals(until)){
					questionare = questionare + " where TRUNC(CREATION_DATE) = TO_DATE('"+from+"','mm/dd/yyyy')";
				}else{
					questionare = questionare + " where TRUNC(CREATION_DATE) >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
					
				}
			}
		}
		if(!task.equals("")){
			if (StudyName=="" || StudyName ==null){
				questionare = "select v.SESSION_ID,v.study_name,v.session_status,v.creation_date from YUIAT_SESSIONS_V v,YUIAT_SESSION_TASKS_V t where v.SESSION_ID=t.SESSION_ID and t.TASK_ID like '%"+task+"%'";
			}else{
				questionare = "select v.SESSION_ID,v.study_name,v.session_status,v.creation_date from YUIAT_SESSIONS_V v,YUIAT_SESSION_TASKS_V t where v.SESSION_ID=t.SESSION_ID and v.STUDY_NAME ='"+StudyName+"' and t.TASK_ID like '%"+task+"%'";
			}
			if (!from.equals("") && !until.equals("")){
				if (from.equals(until)){
					questionare = questionare + " AND TRUNC(v.CREATION_DATE) =  TO_DATE('"+from+"','mm/dd/yyyy')";
				}else{
					questionare = questionare + " AND TRUNC(v.CREATION_DATE) >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(v.CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
					
				}
			}
		}
		questionare = questionare + " order by creation_date";
		try{
				PITConnection.getInstance(false);
				connection = PITConnection.getConnection(db);
				if (connection.isClosed()){
					PITConnection.getInstance(true);
					connection = PITConnection.getConnection(db);
				}
				connection.setAutoCommit(false);
				PreparedStatement psII = connection.prepareStatement(questionare);
				psII.setFetchSize(100000);
				ResultSet rsII = psII.executeQuery();
				String sinceTime;
				String untilTime;
				SimpleDateFormat sdf; 
				SimpleDateFormat sessiondf;
				boolean stop=false;
				sdf = new SimpleDateFormat("MM/dd/yyyy");
				sessiondf = new SimpleDateFormat("yyyy-MM-dd");
				
				while(rsII.next()){
					if (rsII.getString(1)!=null && rsII.getString(4)!=null){
						String date = rsII.getString(4);
						String[] dates = date.split(" ");
						date = dates[0];
						Date sessionDate = sessiondf.parse(date);
						for (int i=0;i<times.size();i++){
								
							HashMap timeArray = (HashMap) times.get(i);
							sinceTime = (String) timeArray.get("since");
							untilTime = (String) timeArray.get("until");
							Date sinceDate = sdf.parse(sinceTime);
							Date untilDate = sdf.parse(untilTime);
							if ( ( sessionDate.after(sinceDate) || sessionDate.equals((sinceDate)) ) && ( sessionDate.before(untilDate) || sessionDate.equals(untilDate) ) ){
								int inco = (Integer) timeArray.get("NumberOfInCompletedSessions");
								int co = (Integer) timeArray.get("NumberOfCompletedSessions");
								if ( rsII.getString(3) == null || !(rsII.getString(3).equals(( "C" )) )){
									timeArray.put("NumberOfInCompletedSessions", ++inco);
								}else{
									timeArray.put("NumberOfCompletedSessions", ++co);
								}
							}
						}
					}else{System.out.println("id or date is null");}
				}
				for (int i=0;i<times.size();i++){
					ArrayList record=new ArrayList();
					float sCR;
					float cr;
					HashMap timeArray = (HashMap) times.get(i);
					sinceTime = (String) timeArray.get("since");
					untilTime = (String) timeArray.get("until");
					if (timec.equals("true")){
						if (sinceTime.equals(untilTime)){
							record.add(sinceTime);
						}else{
							record.add(sinceTime+" - "+untilTime);
						}
						
					}
					if (datac.equals("true")){
						if (db.contains("std")){
							record.add("Demo");//add database
						}
						if (db.contains("research")){
							record.add("research");
						}
						
					}
					record.add(String.valueOf((Integer)timeArray.get("NumberOfInCompletedSessions")+(Integer)timeArray.get("NumberOfCompletedSessions")));
					record.add(String.valueOf((Integer)timeArray.get("NumberOfCompletedSessions")));
					if ( ( (Integer)timeArray.get("NumberOfCompletedSessions") )!=0){
						 sCR = (Integer)timeArray.get("NumberOfInCompletedSessions")+(Integer)timeArray.get("NumberOfCompletedSessions");
						 cr  = ( ( ( (Integer)timeArray.get("NumberOfCompletedSessions") ) /sCR )*100);
					}else{
						cr=0;
						
					}
					record.add(String.valueOf(Math.round(cr) ));
					recordSet.add(record);
				}
				
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in connection to DataBase: "+e.getMessage());
			throw new Exception("error in getSession: "+e.getMessage());
		} finally {
			try {
				if (connection!=null) connection.close();
			} catch (Exception ce) {
				ce.printStackTrace();
				throw ce;}
		}
		if (StudyName==null) System.out.println("StudyName is null");
		System.out.println(StudyName + ":" +recordSet.toString());
		return recordSet;
		
				
	}

	/**
	 * 
	 *	Set the tassk array  
	 * 	 
	 * @param tasks,name
	 * 			
	 * 			tasks ArrayList of tasks
	 * 			name Name of study 
	 *            
	 * 
	 * @return void
	 * 
	 */
	private void populateTasks(ArrayList tasks,String name) throws Exception{
		
		try{
			for (int i=0;i<tasks.size();i++){
				
				HashMap task = (HashMap) tasks.get(i);
				String taskName = (String) task.get("name");
				if (taskName.equals(name)){
					return;
					
				}
			}
			HashMap task = new HashMap();
			task.put("name", name);
			task.put("started", 0);
			task.put("completed", 0);
			tasks.add(task);
			
		}catch(Exception e){
			throw new Exception("erro in populateTasks: "+e.getMessage());
		}
		
	}

	
	/**
	 * 
	 *	Main run method of the runnable class 
	 * 	 
	 * @param 
	 * 			
	 * 			 
	 * @return void
	 * 
	 */
	
	public void run() {
		
		try {
			
			if (arguments.get("method").equals("getTotalSessions")){
				
				ArrayList result = getSessionPerResultSet(arguments);
				results.put("records", result);
							
			}
			if (arguments.get("method").equals("getsession")){
				
				ArrayList result = getSessionPerResultSetStudy();
				results.put("records", result);
			}
			if (arguments.get("method").equals("gettasksII")){
					
				ArrayList result = getSessionPerResultSetTask((String)arguments.get("studyname"),(String)arguments.get("since"),(String)arguments.get("until"),
								  (String)arguments.get("db"),(String)arguments.get("task"),(String)arguments.get("timec"),(String)arguments.get("datac"),
								  (ArrayList)arguments.get("times"),(String)arguments.get("filter")	);
				results.put("records", result);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error in run "+e.getMessage());
			throw new RuntimeException (new Exception("The Database refused the connection request" +e.getMessage()));
			
			
		}
		
	}

}
