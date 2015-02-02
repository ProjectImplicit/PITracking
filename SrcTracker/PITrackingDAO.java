package org.uva.dao.oracle;


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
//import org.uva.dao.ConnectionPool;
import org.uva.util.sessionBean;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.RootLogger;



public class PITrackingDAO implements Runnable {
	
	
//	static Logger logger = Logger.getLogger(PITrackingDAO.class);
	private HashMap arguments =null;
	private HashMap results;
	
		
	
	public void initLogger(){
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.DEBUG);

		//Define log pattern layout
		PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");

		//Add console appender to root logger
		rootLogger.addAppender(new ConsoleAppender(layout));

		try

		{

			//Define file appender with layout and output log file name
	
			RollingFileAppender fileAppender = new RollingFileAppender(layout, "pitracker.log");
			//Add the appender to root logger
	
			rootLogger.addAppender(fileAppender);
		}

		catch (IOException e)

		{

		System.out.println("Failed to add appender !!");

		}
	}
	
	public PITrackingDAO() throws Exception{
		arguments = new HashMap();
		results=new HashMap();
		
	}
	
	public PITrackingDAO(HashMap args) throws Exception{
		arguments = args;
		results=new HashMap();
	
	}
	
	public ArrayList getsessionArray(){
		return (ArrayList) results.get("sessions");
	}
	public HashMap getResults(){
		return results;
	}
	public HashMap getArguments(){
		return arguments;
	}
	public void setMethod(String v){
		arguments.put("method", v);
	}
	public void setTasks(ArrayList tasks){
		arguments.put("tasks", tasks);
	}
	
	
	
	public ArrayList getAllTrails(String studayName,String dataGroup) throws Exception{
		
		Connection connection = null;
		ArrayList Trials = new ArrayList();//ArrayList to hold all records
		
		String queryComplete = "SELECT DISTINCT questionnaire_name FROM YUIAT_QUESTIONNAIRE_DATA_V where SESSION_ID IN (select session_ID from yuiat_sessions_v where study_name='"+studayName+"')";
		try{

			PITConnection.getInstance(false);
			connection = PITConnection.getConnection(dataGroup);
			if (connection.isClosed()){
				PITConnection.getInstance(true);
				connection = PITConnection.getConnection(dataGroup);
			}
			PreparedStatement ps = connection.prepareStatement(queryComplete);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				HashMap task = new HashMap();
				task.put("name", rs.getString(1));
				Trials.add(task);
			}
		
		}catch (Exception e) {
			throw e;
		} finally {
			try {
				if (connection!=null) connection.close();
			} catch (Exception ce) {throw ce;};
		}
		return Trials;
		
	}
	public ArrayList getTrials(ArrayList sessions,String id,String dataGroup) throws Exception{
		
		Connection connection = null;
		ArrayList Trials = new ArrayList();//ArrayList to hold all records
		boolean exit=false;
		String queryComplete = "SELECT DISTINCT questionnaire_name FROM yuiat_questionnaire_data_v where session_id ='"+id+"'";
		try{

			PITConnection.getInstance(false);
			connection = PITConnection.getConnection(dataGroup);
			if (connection.isClosed()){
				PITConnection.getInstance(true);
				connection = PITConnection.getConnection(dataGroup);
			}
			PreparedStatement ps = connection.prepareStatement(queryComplete);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				HashMap task = new HashMap();
				task.put("name", rs.getString(1));
				Trials.add(task);
			}
			queryComplete = "SELECT DISTINCT session_id,study_name,questionnaire_name FROM yuiat_questionnaire_data_v where session_id =?";
			ps = connection.prepareStatement(queryComplete);
			for (int i=0;i<sessions.size();i++){//go over all sessions
				ArrayList session  = new ArrayList();
				session = (ArrayList) sessions.get(i);
				String sessionid = (String) session.get(0);
				String status = null;
				if (session.get(2) !=null){
					 status  = (String)session.get(2) ;
				}else
				{
					status = "null";
				}
				
				String C = new String("C");
				if (!(status.equals(C))){
					ps.setFloat(1, Float.parseFloat(sessionid));
					rs = ps.executeQuery();
					//connection.commit();
					while (rs.next()) {//for records of this session
						exit=false;
						String taskName = rs.getString(3);
						for (int j=0;j<Trials.size()&&exit==false;j++){//go over array of trials
							HashMap trial = new HashMap();
							trial = (HashMap) Trials.get(j);
							if(trial.get("name").equals(taskName )){
								Integer comp = (Integer) trial.get("completed");
								if (comp != null){
									comp ++;
									trial.put("completed", comp);
								}else{
									trial.put("completed", 1);
								}
								exit=true;
							}//end if
									
						}//end for
								    	
					}// end while
				}

			}

		}catch (Exception e) {
			throw e;
		} finally {
			try {
				if (connection!=null) connection.close();
			} catch (Exception ce) {throw new Exception ("problem closing connection"+ce.getMessage());};
		}
				
		
		return Trials;
				
	}
	
	
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
						if (name.equals("control")) System.out.println(id);
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

	public String getSessionString(ArrayList sessions) throws Exception{
		
		try{
			
			String res=new String();
			int size = sessions.size();
			for (int i=0;i<size;i++){
				sessionBean session = (sessionBean) sessions.get(i);
				String id = (String) session.getId();
				res=res+id;
				if (i!=(size-1)) res+=",";
			}
			return res;
		}catch (Exception e){
			throw new Exception("error in getSessionString: "+e.getMessage());
		}
			
		
	}

	
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
	
	
	private sessionBean findsessionByID(ArrayList sessions,String id){
		
		sessionBean session = new sessionBean();
		for (int i=0;i<sessions.size();i++){
			session= (sessionBean) sessions.get(i);
			String sid = session.getId();
			if (sid.equals(id)){
				return session;
			}
			
		}
		
		return session;
	}
	

	
	
	////////////////////////////////////
	//Desc: Update the tasks in session bean 
	//
	//
	//
	////////////////////////////////////
	
	
	public void getstartedTasks(String dataGroup,ArrayList sessions,ArrayList studyTasks) throws Exception{
		
		Connection connection = null;
		boolean exit=false;
		try{
			//ConnectionPool.getInstance(true);
			//connection = ConnectionPool.getConnection(dataGroup);
			PITConnection.getInstance(false);
			connection = PITConnection.getConnection(dataGroup);
			if (connection.isClosed()){
				PITConnection.getInstance(true);
				connection = PITConnection.getConnection(dataGroup);
			}
						
			String sessionsString;
			if (sessions.size()==0) return;
			String questionare = "SELECT DISTINCT TASK_ID,CREATION_DATE FROM yuiat_session_tasks_v where session_id = ? order by CREATION_DATE";
			//connection.setAutoCommit(false);				
			PreparedStatement ps = connection.prepareStatement(questionare);
			//ps.setFetchSize(1000);
			
			for (int j=0;j<sessions.size();j++){
				
				sessionBean session = (sessionBean) sessions.get(j);
				String id = session.getId();
				long sessionID = Long.parseLong(id);
				ArrayList tasks = new ArrayList<HashMap>();
				
				//ps.setInt(1, sessionID);//(1,id);
				ps.setLong(1, sessionID);
				//ps.setObject(1, sessionID);
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
			
	
/////////////end try///////////////////			
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
	
	
	public ArrayList getStudiesfromDB(String db,String name) throws Exception{
		
		Connection connection = null;
		ArrayList recordSet = new ArrayList();
		String questionare = "";
		
		
		questionare = "SELECT DISTINCT study_name FROM yuiat_sessions_v where study_name like '%"+name+"%'";
		try{
			//ConnectionPool.getInstance(true);
			//connection = ConnectionPool.getConnection(db);
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
			//logger.debug(e.getMessage());
			System.out.println(e.getMessage());
			throw (new Exception("Exception in getStudiesfromDB: "+e.getMessage()));
			
		} finally {
			try {
				if (connection!=null) connection.close();
				//Thread.sleep(200);
			} catch (Exception ce) {
				ce.printStackTrace();
				throw ce;
			};
		}
		
		return recordSet;
	}
	

public ArrayList getSessionPerResultSetTask(String StudyName,String from,String until,String db,String task,String timec,
		String datac,ArrayList times,String filter) throws Exception{
	
	
	Connection connection = null;
	boolean where = false;
	ArrayList recordSet = new ArrayList();
	String questionare = "";
	
	
	
	
	
	//System.out.println("starting getSessionByCreationDate");
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
	//						if (sessionDate.after(untilDate)){
	//							stop=true;
	//						}
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
					
					//if (taskName.equals(task.get("name")) || taskName.equals("")){
					String tName = (String) taskfromArray.get("name");
					if (tName.contains(filter) || filter.equals("")){
						//if (session==null) throw new Exception("session is null");
						recordT.add(StudyName);
						float started = ((Integer) taskfromArray.get("started"));
						float complete = ((Integer) taskfromArray.get("completed"));
						//int inComplete = (Integer) task.get("Incomplete");
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
		//logger.debug(e.getMessage());
		e.printStackTrace();
		System.out.println("Error in connection to DataBase: "+e.getMessage());
		throw new Exception("error in getSession: "+e.getMessage());
		//return recordSet;
		//throw new Exception("error in getSession: "+e.getMessage());
	
	} finally {
		try {
			if (connection!=null) connection.close();
			//Thread.sleep(200);
		} catch (Exception ce) {
			ce.printStackTrace();
			throw ce;}
	}
	//System.out.println("ending get sessionbycreation date recird size: "+recordSet.size());
	return recordSet;

}

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
// TODO finish function
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
			//if (con!=null) con.close();
			//Thread.sleep(200);
		} catch (Exception ce) {
			ce.printStackTrace();
			throw ce;}
	}
		
}
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
//						if (sessionDate.after(untilDate)){
//							stop=true;
//						}
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
//						if ( rs.getString(3) == null || !(rs.getString(3).equals(( "C" )) )){
//							
//							if (!endTask.equals("")){
//								if ( visited( rs.getString(1),endTask,psII) ){
//									timeArray.put("NumberOfCompletedSessions", ++co);
//								}else{
//									timeArray.put("NumberOfInCompletedSessions", ++inco);
//								}
//							}else{
//								timeArray.put("NumberOfInCompletedSessions", ++inco);
//							}
//						
//						}else{
//							timeArray.put("NumberOfCompletedSessions", ++co);
//						}
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
		//logger.debug(e.getMessage());
		e.printStackTrace();
		System.out.println("Error in connection to DataBase: "+e.getMessage());
		throw new Exception("error in getSession: "+e.getMessage());
		//return recordSet;
		//throw new Exception("error in getSession: "+e.getMessage());
	
	} finally {
		try {
			if (connection!=null) connection.close();
			//Thread.sleep(200);
		} catch (Exception ce) {
			ce.printStackTrace();
			throw ce;}
	}
	//System.out.println("ending get sessionbycreation date recird size: "+recordSet.size());
	return recordSet;
	
	
}

public ArrayList getSessionPerResultSetStudy(String StudyName,String from,String until,String db,String task,String timec,String datac,ArrayList times) throws Exception{
	
	Connection connection = null;
	boolean where = false;
	ArrayList recordSet = new ArrayList();
	String questionare = "";
	
	
	
	
	
	//System.out.println("starting getSessionByCreationDate");
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
//						if (sessionDate.after(untilDate)){
//							stop=true;
//						}
					if ( ( sessionDate.after(sinceDate) || sessionDate.equals((sinceDate)) ) && ( sessionDate.before(untilDate) || sessionDate.equals(untilDate) ) ){
						
						if ( rsII.getString(3) == null || !(rsII.getString(3).equals(( "C" )) )){
							int inco = (Integer) timeArray.get("NumberOfInCompletedSessions");
							timeArray.put("NumberOfInCompletedSessions", ++inco);
							
							
						}else{
							//System.out.println("session:"+rsII.getString(1)+" date: "+rsII.getString(4));
							int co = (Integer) timeArray.get("NumberOfCompletedSessions");
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
		//logger.debug(e.getMessage());
		e.printStackTrace();
		System.out.println("Error in connection to DataBase: "+e.getMessage());
		throw new Exception("error in getSession: "+e.getMessage());
		//return recordSet;
		//throw new Exception("error in getSession: "+e.getMessage());
	
	} finally {
		try {
			if (connection!=null) connection.close();
			//Thread.sleep(200);
		} catch (Exception ce) {
			ce.printStackTrace();
			throw ce;}
	}
	//System.out.println("ending get sessionbycreation date recird size: "+recordSet.size());
	return recordSet;

}
// TODO TODO
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
			//ps.setFetchSize(100000);
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
						//if (!timeArray.containsKey("NumberOfCompletedSessions")) timeArray.put("NumberOfCompletedSessions", 0);
						//if (!timeArray.containsKey("NumberOfInCompletedSessions")) timeArray.put("NumberOfInCompletedSessions", 0);
						
						Date sinceDate = sdf.parse(sinceTime);
						Date untilDate = sdf.parse(untilTime);
//							if (sessionDate.after(untilDate)){
//								stop=true;
//							}
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
//							if ( rs.getString(3) == null || !(rs.getString(3).equals(( "C" )) )){
//								
////								if (!endTask.equals("")){
////									if ( visited( rs.getString(1),endTask,psII) ){
////										timeArray.put("NumberOfCompletedSessions", ++co);
////									}
////									
////								}else{
////									timeArray.put("NumberOfInCompletedSessions", ++inco);
////								}
////								
//								if (!endTask.equals("")){
//									if ( visited( rs.getString(1),endTask,psII) ){
//										timeArray.put("NumberOfCompletedSessions", ++co);
//									}else{
//										timeArray.put("NumberOfInCompletedSessions", ++inco);
//									}
//								}else{
//									timeArray.put("NumberOfInCompletedSessions", ++inco);
//								}
//							}else{
//								//System.out.println("session:"+rsII.getString(1)+" date: "+rsII.getString(4));
//								
//								timeArray.put("NumberOfCompletedSessions", ++co);
//
//								
//							}
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
		//logger.debug(e.getMessage());
		e.printStackTrace();
		System.out.println("Error in connection to DataBase: "+e.getMessage());
		throw new Exception("error in getSession: "+e.getMessage());
		//return recordSet;
		//throw new Exception("error in getSession: "+e.getMessage());
	
	} finally {
		try {
			if (connection!=null) connection.close();
			//Thread.sleep(200);
		} catch (Exception ce) {
			ce.printStackTrace();
			throw ce;}
	}
	//System.out.println("ending get sessionbycreation date recird size: "+recordSet.size());
	if (StudyName==null) System.out.println("StudyName is null");
	System.out.println(StudyName + ":" +recordSet.toString());
	return recordSet;
	
			
}
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
						//if (!timeArray.containsKey("NumberOfCompletedSessions")) timeArray.put("NumberOfCompletedSessions", 0);
						//if (!timeArray.containsKey("NumberOfInCompletedSessions")) timeArray.put("NumberOfInCompletedSessions", 0);
						
						Date sinceDate = sdf.parse(sinceTime);
						Date untilDate = sdf.parse(untilTime);
//							if (sessionDate.after(untilDate)){
//								stop=true;
//							}
						if ( ( sessionDate.after(sinceDate) || sessionDate.equals((sinceDate)) ) && ( sessionDate.before(untilDate) || sessionDate.equals(untilDate) ) ){
							int inco = (Integer) timeArray.get("NumberOfInCompletedSessions");
							int co = (Integer) timeArray.get("NumberOfCompletedSessions");
							if ( rsII.getString(3) == null || !(rsII.getString(3).equals(( "C" )) )){
								
								timeArray.put("NumberOfInCompletedSessions", ++inco);
								
								
							}else{
								//System.out.println("session:"+rsII.getString(1)+" date: "+rsII.getString(4));
								
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
		//logger.debug(e.getMessage());
		e.printStackTrace();
		System.out.println("Error in connection to DataBase: "+e.getMessage());
		throw new Exception("error in getSession: "+e.getMessage());
		//return recordSet;
		//throw new Exception("error in getSession: "+e.getMessage());
	
	} finally {
		try {
			if (connection!=null) connection.close();
			//Thread.sleep(200);
		} catch (Exception ce) {
			ce.printStackTrace();
			throw ce;}
	}
	//System.out.println("ending get sessionbycreation date recird size: "+recordSet.size());
	if (StudyName==null) System.out.println("StudyName is null");
	System.out.println(StudyName + ":" +recordSet.toString());
	return recordSet;
	
			
}
public ArrayList getSessionByCreationDate(String StudyName,String from,String until,String dataGroup,String task) throws Exception{
		
		Connection connection = null;
		boolean where = false;
		ArrayList recordSet = new ArrayList();
		String questionare = "";
		
			//System.out.println("starting getSessionByCreationDate");
		
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
						questionare = questionare + " AND CREATION_DATE >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
						
					}
				}else{
					if (from.equals(until)){
						questionare = questionare + " where TRUNC(CREATION_DATE) = TO_DATE('"+from+"','mm/dd/yyyy')";
					}else{
						questionare = questionare + " where CREATION_DATE >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
						
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
						questionare = questionare + " AND v.CREATION_DATE >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  TRUNC(v.CREATION_DATE) <=  TO_DATE('"+until+"','mm/dd/yyyy')";
						
					}
					
				}
			
			}
			questionare = questionare + " order by creation_date";
	
		try{
			
			PITConnection.getInstance(false);
			connection = PITConnection.getConnection(dataGroup);
			if (connection.isClosed()){
				PITConnection.getInstance(true);
				connection = PITConnection.getConnection(dataGroup);
			}
			connection.setAutoCommit(false);
	
			PreparedStatement psII = connection.prepareStatement(questionare);
			psII.setFetchSize(100000);
			ResultSet rsII = psII.executeQuery();
			
			
			while(rsII.next()){
				if (rsII.getString(1)!=null && rsII.getString(4)!=null){
					sessionBean session = new sessionBean();
					session.setID(rsII.getString(1));//session id
					session.setName(rsII.getString(2));//study name
					session.setStatus(rsII.getString(3));//session status
					session.setDate(rsII.getString(4));//creation date
					recordSet.add(session);
					
				}
			
				
			}
			
	
			
		}catch (Exception e) {
			
			e.printStackTrace();
			System.out.println("Error in connection to DataBase: "+e.getMessage());
			throw new Exception("error in getSession: "+e.getMessage());
			//return recordSet;
			//throw new Exception("error in getSession: "+e.getMessage());
		
		} finally {
			try {
				if (connection!=null) connection.close();
				//Thread.sleep(200);
			} catch (Exception ce) {
				ce.printStackTrace();
				throw ce;}
		}
		//System.out.println("ending get sessionbycreation date recird size: "+recordSet.size());
		return recordSet;
		
	}
	
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
	public ArrayList getSession(String StudyName,String from,String until,String dataGroup,String task) throws Exception{

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
		questionare = questionare + " AND CREATION_DATE >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  CREATION_DATE <=  TO_DATE('"+until+"','mm/dd/yyyy')";

		}
		}else{
		if (from.equals(until)){
		questionare = questionare + " where TRUNC(CREATION_DATE) = TO_DATE('"+from+"','mm/dd/yyyy')";
		}else{
		questionare = questionare + " where CREATION_DATE >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  CREATION_DATE <=  TO_DATE('"+until+"','mm/dd/yyyy')";

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
		questionare = questionare + " AND v.CREATION_DATE >=  TO_DATE('"+from+"','mm/dd/yyyy') AND  v.CREATION_DATE <=  TO_DATE('"+until+"','mm/dd/yyyy')";

		}

		}

		}	

		try{



		//if (arguments!=null){
		//connection = PITConnection.getNewConnection(dataGroup);
		//}else{
		PITConnection.getInstance(false);
		connection = PITConnection.getConnection(dataGroup);
		if (connection.isClosed()){
			PITConnection.getInstance(true);
			connection = PITConnection.getConnection(dataGroup);
		}

//			}


		connection.setAutoCommit(false);
		//Statement stmt = null;
		PreparedStatement psII = connection.prepareStatement(questionare);
		//psII.setFetchSize(100000);
		ResultSet rsII = psII.executeQuery();


		while(rsII.next()){
		sessionBean session = new sessionBean();
		session.setID(rsII.getString(1));//session id
		session.setName(rsII.getString(2));//study name
		session.setStatus(rsII.getString(3));//session status
		session.setDate(rsII.getString(4));//creation date
		recordSet.add(session);
		}



		}catch (Exception e) {
		//logger.debug(e.getMessage());
			e.printStackTrace();
			System.out.println("Error in connection to DataBase: "+e.getMessage());
			throw new Exception("error in getSession: "+e.getMessage());
			//return recordSet;
			//throw new Exception("error in getSession: "+e.getMessage());

		} finally {
			try {
			if (connection!=null) connection.close();
			//Thread.sleep(200);
		} catch (Exception ce) {
			ce.printStackTrace();
		throw ce;}
		}

		return recordSet;

		}


	public void run() {
		
		try {
			
			if (arguments.get("method").equals("getTotalSessions")){
				
				//ArrayList result = getSessionPerResultSet((String)arguments.get("studyname"),(String)arguments.get("since"),(String)arguments.get("until"),(String)arguments.get("db"),(String)arguments.get("task"),(String)arguments.get("timec"),(String)arguments.get("datac"),(ArrayList)arguments.get("times"));
				ArrayList result = getSessionPerResultSet(arguments);
				results.put("records", result);
							
			}
			if (arguments.get("method").equals("getsession")){
				
				 // ArrayList result = getSessionPerResultSetStudy((String)arguments.get("studyname"),(String)arguments.get("since"),(String)arguments.get("until"),(String)arguments.get("db"),(String)arguments.get("task"),(String)arguments.get("timec"),(String)arguments.get("datac"),(ArrayList)arguments.get("times"));
				ArrayList result = getSessionPerResultSetStudy();
				
				results.put("records", result);
			}
//			if (arguments.get("method").equals("gettasks")){
//				
//				getstartedTasks((String)arguments.get("db"),(ArrayList)results.get("sessions"),(ArrayList)arguments.get("tasks"));
//			}
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
