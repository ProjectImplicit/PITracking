package org.uva.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.mail.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.implicit.RegisteredUserDAO;
import org.json.JSONException;
import org.json.JSONObject;
import org.uva.Implicit;
import org.uva.dao.oracle.PITrackingDAO;
import org.uva.dao.oracle.TaskDAO;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.yale.its.util.ChainedException;

import org.apache.log4j.*;
import org.implicit.random.RandomStudyManager;
import org.implicit.random.RandomStudyConfigReader;
//import org.implicit.Implicit;


public class PITracking  extends HttpServlet{
	
	
		
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException { 
		try {
			System.out.println("starting do get");
			doPost(request,response);
		} catch (ServletException e) {
			e.printStackTrace();
		}
	} 
	
	public void doPost(HttpServletRequest request,HttpServletResponse response)
			throws IOException, ServletException{
		
		String returnString ="There was a problem with the post";
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		PrintWriter out = response.getWriter();
			
		try {
			System.out.println("starting do post");
			//out.println("started post");
			String str;
			String key = null;
			String val= null;
			Object o = null;
			StringBuilder sb = new StringBuilder();
		    BufferedReader br = request.getReader();
		      	    
		    while( (str = br.readLine()) != null ){
		        sb.append(str);
		        System.out.println(str);
		    }    
			try {
				if (sb.length()!=0){
					JSONObject jObj = new JSONObject(sb.toString());
					Iterator it = jObj.keys(); //gets all the keys
					HashMap map = new HashMap();
					map.put("resp", response);
					while(it.hasNext())
					{
						key = (String)it.next();
						val  = (String) jObj.get(key).toString();
						map.put(key,val);
					}
					br.close();
					returnString = process(map);
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println(e1.getMessage());
			}
			out.println(returnString);
				
		} catch (Exception e) {
			System.out.println(e.getMessage());
			out.println("There was an error in backend, contact admin. "+e.getMessage());
			e.printStackTrace();
		}finally{
			out.close();
		}
		
				
	}
	

	
	
	private String process(HashMap map) throws Exception{
		
		
		int [] threadsIndex={0};
		int [] maxActive={1};
		String csv = "";
		//Integer countComplete;
		//Integer countInComplete;
		ArrayList studiesResult = new ArrayList();
		ArrayList result = new ArrayList();
		ArrayList studies = new ArrayList();
		int switchcase=0;
		ArrayList times =  new ArrayList();
		boolean useThreads;
		
		ArrayList runnigThreads = new ArrayList();
		runnigThreads.clear();
		//int max=500;//maxmimum splits
		
		String since = (String) map.get("since");
		String until = (String) map.get("until");
		String study = (String) map.get("study");
		String task = (String) map.get("task");
		String datac = (String) map.get("datac");
		String timec = (String) map.get("timec");
		String dayc = (String) map.get("dayc");
		String weekc = (String) map.get("weekc");
		String monthc = (String) map.get("monthc");
		String yearc = (String) map.get("yearc");
		String studyc =(String) map.get("studyc");
		String taskc = (String) map.get("taskc");
		String db = (String) map.get("db");
		String current = (String) map.get("current");
		String test = (String) map.get("testDB");
		int method = Integer.parseInt((String) map.get("method"));
		String cach = (String) map.get("refresh");
		String cURL  = (String) map.get("curl");
		String hURL  = (String) map.get("hurl");
		String confPath  = (String) map.get("cpath");
		String HistoryPath  = (String) map.get("hpath");
		String tasksmethod = (String) map.get("tasksM");
		String threads = (String) map.get("threads");
		String threadsNum = (String) map.get("threadsNum");
		String baseURL= (String) map.get("baseURL");
		String endTask  = (String) map.get("endTask");
		PITrackingDAO tracker  = new PITrackingDAO();
		System.out.println("starting process");
		
		
		
		try {
		
			
			if (threads.equals("yes")) {
				useThreads=true;
			}else{
				useThreads=false;
			}
			maxActive[0]=Integer.parseInt(threadsNum);
			times = getTimes(since,until,dayc,weekc,monthc,yearc,timec);
			if (timec.equals("true")&& monthc.equals("true")){
				HashMap time = (HashMap) times.get(0);
				since = (String) time.get("since");
				time=(HashMap) times.get(times.size()-1);
				until = (String) time.get("until"); 
				
				
			}
			
			db = fixDB(db,test);
			Studies manager =Studies.getInstance();
			manager.setBaseURL(baseURL);
			if (method==Studies.PATH){
				manager.setConfigPath(confPath);
				manager.setHistoryPath(HistoryPath);
				manager.setMethod(Studies.PATH);
			}
			if (method==Studies.URL){
				manager.setConfigURL(cURL);
				manager.setHistoryURL(hURL);
				manager.setMethod(Studies.URL);
				
			}
			if (method==Studies.BACKEND){
				manager.setHistoryURL(hURL);
				manager.setMethod(Studies.BACKEND);
				
			}
			
			
			
			if (current.equals("Current")){
				if (cach.equals("no")){
					manager.readCurrentStudies(true);
				}else{
					manager.setDaysDiff(Integer.parseInt(cach));
					manager.readCurrentStudies(true);
					
				}
				
				studies = manager.getCurrentStudies();
				studiesResult = filterStudies(studies,study);
				
			}else{
				if (current.endsWith("History")){
					if (cach.equals("no")){
						manager.readCurrentStudies(true);
						manager.readHistoryStudies(false);
					}else{
						manager.setDaysDiff(Integer.parseInt(cach));
						manager.readCurrentStudies(true);
						manager.readHistoryStudies(false);
						
					}
					studies = manager.getBothStudies();
					studiesResult = filterStudies(studies,study);
				}
				if (current.equals("Any")){
					if (!db.equals("both")){
						studiesResult = tracker.getStudiesfromDB(test+db,study);
					}else{
						studiesResult = tracker.getStudiesfromDB(test+"std",study);
						ArrayList temp = tracker.getStudiesfromDB(test+"research",study);
						studiesResult =filterDuplicates(studiesResult,temp);
						
						
					}
				}
			}
			
			System.out.println("number of studies are: "+studiesResult.size());
			/////////////////////////
			if (!studyc.equals("true") && !taskc.equals("true") ){
			 
				csv = onlySessionSelected(map,csv,tracker,times,studiesResult,runnigThreads,maxActive,threadsIndex);
				
			}
			if (studyc.equals("true") && !taskc.equals("true") ){
				
				ArrayList sessions = new ArrayList();
				ArrayList sessions1 = new ArrayList(); 
				HashMap trackerStudyGroups = new HashMap();
				trackerStudyGroups.put("type", "getsession");
				
				if ( db.equals("both")){
			
						for (int i=0;i<studiesResult.size();i++){//per study
							String name = (String)studiesResult.get(i);
								if (!name.equals("")){
									if (useThreads==true){
										map.put("connect", test+"std");
										setTrackerGroups(trackerStudyGroups,map,name,"getsession",times);
									}else{
										//sessions = tracker.getSession((String)studiesResult.get(i),since,until,test+"std",task);
										sessions = tracker.getSession((String)studiesResult.get(i),since,until,test+"std",task);
										//ArrayList records = tracker.createRS(sessions,null,(String)studiesResult.get(i),"std",studyc,taskc,datac,timec,task,since,until);						
										ArrayList records = createRS(sessions,null,(String)studiesResult.get(i),"std",studyc,taskc,datac,timec,task,since,until);
										csv = turntoCSV(records,db,studyc,taskc,datac,timec,csv);
									}
								}
			

						}
					
						if (useThreads==true){

							    csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
						}
						
						trackerStudyGroups.clear();
						trackerStudyGroups.put("type", "getsession");
						for (int i=0;i<studiesResult.size();i++){//per study
							
							String name = (String)studiesResult.get(i);
								if (!name.equals("")){
									if (useThreads==true){
										map.put("connect", test+"research");
										//setTrackerGroups(trackerStudyGroups,name,since,until,test+"research",times,task,timec,datac,"getsession");
										setTrackerGroups(trackerStudyGroups,map,name,"getsession",times);
									}else{
										sessions1 = tracker.getSession((String)studiesResult.get(i),since,until,test+"research",task);
										//ArrayList records = tracker.createRS(sessions1,null,(String)studiesResult.get(i),"research",studyc,taskc,datac,timec,task,since,until);						
										ArrayList records = createRS(sessions1,null,(String)studiesResult.get(i),"research",studyc,taskc,datac,timec,task,since,until);
										csv = turntoCSV(records,db,studyc,taskc,datac,timec,csv);
									}
								}
							
						}
						
						if (useThreads==true){
							
								csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
						}
					
				}else{
						
					for (int i=0;i<studiesResult.size();i++){//per study
						String name = (String)studiesResult.get(i);
							if (!name.equals("")){
								if (useThreads==true){
									map.put("connect", test+db);
									setTrackerGroups(trackerStudyGroups,map,name,"getsession",times);
									//setTrackerGroups(trackerStudyGroups,name,since,until,test+db,times,task,timec,datac,"getsession");
								}else{

									sessions = tracker.getSessionByCreationDate((String)studiesResult.get(i),since,until,test+db,task);
									for (int tindex=0;tindex<times.size();tindex++){
										HashMap time = (HashMap) times.get(tindex);
										since = (String) time.get("since");
										until = (String) time.get("until");
										//ArrayList records = tracker.createRS(sessions,null,(String)studiesResult.get(i),db,studyc,taskc,datac,timec,task,since,until);						
										ArrayList records = createRS(sessions,null,(String)studiesResult.get(i),db,studyc,taskc,datac,timec,task,since,until);
										csv = turntoCSV(records,db,studyc,taskc,datac,timec,csv);
									}
										
									
								}
					
							}
					}
					if (useThreads==true){
						csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
					}
		
				}
	
			}
			
			
			if ( taskc.equals("true") ){
				ArrayList sessions = new ArrayList(); 
				ArrayList tasks = new ArrayList();
				ArrayList trackers =new ArrayList();
				HashMap trackerStudyGroups = new HashMap();
				trackerStudyGroups.put("type", "getsession");
				
				if (db.equals("both")){
					
					for (int i=0;i<studiesResult.size();i++){//per study
						
						String name = (String)studiesResult.get(i);
						tasks.clear();
						if (!name.equals("") ){
							if (useThreads==true){
								map.put("connect", test+"std");
								setTrackerGroups(trackerStudyGroups,map,name,"gettasksII",times);
								//setTrackerGroups(trackerStudyGroups,name,since,until,test+"std",times,task,timec,datac,"gettasksII");
							}else{
								sessions = tracker.getSession((String)studiesResult.get(i),since,until,test+"std",task);
								tracker.getstartedTasks(test+"std",sessions,tasks);
								tracker.calculateCompleted(sessions);
								//ArrayList records = tracker.createRS(sessions,tasks,(String)studiesResult.get(i),"std",studyc,taskc,datac,timec,task,since,until);
								ArrayList records = createRS(sessions,tasks,(String)studiesResult.get(i),"std",studyc,taskc,datac,timec,task,since,until);
								csv = turntoCSV(records,test+"std",studyc,taskc,datac,timec,csv);
							}
								
						}
						
					}
					if (useThreads==true){
							//csv = processGroupsByTaskByOneII(trackerStudyGroups,csv,maxActive,threadsIndex,times,studyc,taskc,datac,timec,runnigThreads);
						      csv =	processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
					}
					
					trackerStudyGroups.clear();
					trackerStudyGroups.put("type", "getsession");
					for (int i=0;i<studiesResult.size();i++){//per study
						String name = (String)studiesResult.get(i);
						tasks.clear();
							if (!name.equals("") ){
								if (useThreads==true){
									map.put("connect", test+"research");
									setTrackerGroups(trackerStudyGroups,map,name,"gettasksII",times);
									///setTrackerGroups(trackerStudyGroups,name,since,until,test+"research",times,task,timec,datac,"gettasksII");
									
								}else{
									sessions = tracker.getSession((String)studiesResult.get(i),since,until,test+"research",task);
									tracker.getstartedTasks(test+"research",sessions,tasks);
									tracker.calculateCompleted(sessions);
									//ArrayList records = tracker.createRS(sessions,tasks,(String)studiesResult.get(i),"research",studyc,taskc,datac,timec,task,since,until);						
									ArrayList records = createRS(sessions,tasks,(String)studiesResult.get(i),"research",studyc,taskc,datac,timec,task,since,until);
									csv = turntoCSV(records,"research",studyc,taskc,datac,timec,csv);
								}
							}
						
					}
					if (useThreads==true){
							//csv = processGroupsByTaskByOneII(trackerStudyGroups,csv,maxActive,threadsIndex,times,studyc,taskc,datac,timec,runnigThreads);
						csv =	processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
					}
				}else{
					
					// TODO TASKS				
					for (int i=0;i<studiesResult.size();i++){//per study
						
						String name = (String)studiesResult.get(i);
						tasks.clear();
							if (!name.equals("") ){
								if (useThreads==true){
									map.put("connect", test+db);
									setTrackerGroups(trackerStudyGroups,map,name,"gettasksII",times);
									//setTrackerGroups(trackerStudyGroups,name,since,until,test+db,times,task,timec,datac,"gettasksII");
								}else{
									//tasks.clear();
									sessions = tracker.getSession((String)studiesResult.get(i),since,until,test+db,task);
									tracker.getstartedTasks(test+db,sessions,tasks);
									tracker.calculateCompleted(sessions);
									//ArrayList records = tracker.createRS(sessions,tasks,(String)studiesResult.get(i),db,studyc,taskc,datac,timec,task,since,until);
									ArrayList records = createRS(sessions,tasks,(String)studiesResult.get(i),db,studyc,taskc,datac,timec,task,since,until);
									csv = turntoCSV(records,db,studyc,taskc,datac,timec,csv);
									
								}
						}
						
					}
					if (useThreads==true){
							  //csv = processGroupsByTaskByOneII(trackerStudyGroups,csv,maxActive,threadsIndex,times,studyc,taskc,datac,timec,runnigThreads);
						csv =	processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
					}
				}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw e;
		}
		return csv;
		
		
		
	}
	// TODO complete
	private String studySessionSelected(HashMap map,String csv,PITrackingDAO tracker,ArrayList times,ArrayList studiesResult,ArrayList runnigThreads,int[] maxActive,int[] threadsIndex ) throws Exception{
		
		return null;
	}
	private String onlySessionSelected(HashMap map,String csv,PITrackingDAO tracker,ArrayList times,ArrayList studiesResult,ArrayList runnigThreads,int[] maxActive,int[] threadsIndex ) throws Exception{
		
		
		String study = (String) map.get("study");
		String task = (String) map.get("task");
		String studyc =(String) map.get("studyc");
		String taskc = (String) map.get("taskc");
		String db = (String) map.get("db");
		String test = (String) map.get("testDB");
		String since = (String) map.get("since");
		String until = (String) map.get("until");
		String datac = (String) map.get("datac");
		String timec = (String) map.get("timec");
		
		db = fixDB(db,test);
		if ( ( (String)map.get("threads") ).equals("yes") ){
			
			ArrayList sessions = new ArrayList();
			ArrayList sessions1 = new ArrayList(); 
			HashMap trackerStudyGroups = new HashMap();
			trackerStudyGroups.put("type", "totalsessions");
			for (int i=0;i<times.size();i++){
				
				HashMap timeArray = (HashMap) times.get(i);
				timeArray.put("NumberOfCompletedSessions", 0);
				timeArray.put("NumberOfInCompletedSessions", 0);
			}
			if ( db.equals("both")){
					for (int i=0;i<studiesResult.size();i++){//per study
						String name = (String)studiesResult.get(i);
							if (!name.equals("")){
								map.put("connect", test+"std");
									setTrackerGroups(trackerStudyGroups,map,name,"getTotalSessions",times);
									//setTrackerGroups(trackerStudyGroups,name,since,until,test+"std",times,task,timec,datac,"getTotalSessions");
							}
					}
				    csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
					trackerStudyGroups.clear();
					trackerStudyGroups.put("type", "totalsessions");
					for (int i=0;i<studiesResult.size();i++){//per study
						
						String name = (String)studiesResult.get(i);
							if (!name.equals("")){
								map.put("connect", test+"research");
								setTrackerGroups(trackerStudyGroups,map,name,"getTotalSessions",times);
								//setTrackerGroups(trackerStudyGroups,name,since,until,test+"research",times,task,timec,datac,"getTotalSessions");
								
							}
						
					}
					csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
					
				
			}else{
					
				for (int i=0;i<studiesResult.size();i++){//per study
					String name = (String)studiesResult.get(i);
						if (!name.equals("")){
							map.put("connect", test+db);
							setTrackerGroups(trackerStudyGroups,map,name,"getTotalSessions",times);
							//setTrackerGroups(trackerStudyGroups,name,since,until,test+db,times,task,timec,datac,"getTotalSessions");
											
						}
				}
				csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
				
	
			}
			
		}else{
			if (db.equals("both")){
				ArrayList records = tracker.getSessionPerResultSet("", since, until, test+"std", task, timec, datac,times);
				csv = turntoCSV(records,db,studyc,taskc,datac,timec,csv);
				ArrayList records1 = tracker.getSessionPerResultSet("", since, until, test+"research", task, timec, datac,times);
				csv = turntoCSV(records1,db,studyc,taskc,datac,timec,csv);
			}else{
				ArrayList records = tracker.getSessionPerResultSet(study, since, until, test+db, task, timec, datac,times);
				csv = turntoCSV(records,db,studyc,taskc,datac,timec,csv);
				
				//}else{
					
					
					
			//	}
						
			}
			
		}
		
		return csv;
	}
	private ArrayList filterDuplicates (ArrayList study1,ArrayList study2){
		
		ArrayList combine=new ArrayList();
		for (int i=0;i<study2.size();i++){
			
			String name = (String) study2.get(i);
			if (!study1.contains(name)) study1.add(name);
				
			
		}
		
		
		return study1;
		
	}
	private ArrayList filterStudies(ArrayList studies,String studyc){
		ArrayList result = new ArrayList();
		
		
		if (!studyc.equals("")){
			for (int i=0;i<studies.size();i++){
				String study1 = (String) studies.get(i);
				if (study1.contains(studyc)){
					result.add(study1);
				}
				
			}
			return result;	
		}else{
			return studies;
		}
		
	}
	private String fixDB(String db,String test){
		
		
		if (db.equals("Demo")) return "std";
		if (db.equals("Research")) return "research";
		if (db.equals("Both")) return "both";
		
		
		
		return null;
	}
	
		

	private ArrayList getTimes(String since,String until,String day,String week,String month,String year,String timec) throws ParseException{
		
		Integer sinceDays;
		Integer sinceYears;
		Integer sinceMonths;
		Integer untilDays;
		Integer untilYears;
		Integer untilMonths;
		
		
		ArrayList<HashMap> times = new ArrayList<HashMap>();
		String[] sinceParts =since.split("/");
		String[] untilParts =until.split("/");
		if (!timec.equals("true")){
			HashMap time = new HashMap();
			time.put("since", since);
			time.put("until", until);
			times.add(time);
			return times;
			
		}
		sinceDays = Integer.parseInt(sinceParts[1]);
		sinceMonths = Integer.parseInt(sinceParts[0]);
		sinceYears = Integer.parseInt(sinceParts[2]);
		untilDays = Integer.parseInt(untilParts[1]); 
		untilMonths = Integer.parseInt(untilParts[0]);
		untilYears = Integer.parseInt(untilParts[2]);
		
		if (year.equals("true")){
			
			Calendar startCale=new GregorianCalendar();
			Calendar endCal=new GregorianCalendar();

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		    Date startDate = sdf.parse(since);
		    Date endDate = sdf.parse(until);
		    
			startCale.setTime(startDate);
			endCal.setTime(endDate);

			
			int diff = untilYears-sinceYears;
			if (diff>0){
				for (int i=sinceYears;i<=untilYears;i++){
					if (i==sinceYears){
						HashMap time = new HashMap();
						Date tmp = startCale.getTime();
						String daterep = sdf.format(tmp);
						time.put("since",daterep);
						time.put("until", "12/31/"+i);
						times.add(time);
				
					}
					if (i==untilYears){
						HashMap time = new HashMap();
						time.put("since","01/01/"+i);
						Date tmp = endCal.getTime();
						String daterep = sdf.format(tmp);
						time.put("until", daterep);
						times.add(time);
												
					}
					if (i>sinceYears && i<untilYears){
						HashMap time = new HashMap();
						time.put("since","01/01/"+i);
						time.put("until","12/31/"+i);
						times.add(time);
						
					}
				}
				
			}else{
				HashMap time = new HashMap();
				Date tmp = startCale.getTime();
				String daterep = sdf.format(tmp);
				time.put("since",daterep);
				tmp = endCal.getTime();
				daterep = sdf.format(tmp); 
				time.put("until",daterep);
				
				times.add(time);
				
			}

			
		
		}
		if (month.equals("true")){
			
			Calendar startCale=new GregorianCalendar();
			Calendar endCal=new GregorianCalendar();

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		    Date startDate = sdf.parse(since);
		    Date endDate = sdf.parse(until);
		    
			startCale.setTime(startDate);
			endCal.setTime(endDate);

			

			int daysDifference = 0;
			String[] months;
			Integer[] monthDays;
			months = new String[] {"January","February","March","April","May","June","July","August","September","October","November","December"};
			monthDays = new Integer[] {31,28,31,30,31,30,31,31,30,31,30,31};
			if (sinceYears.equals(untilYears)){
				for (int i=sinceMonths;i<=untilMonths;i++){
					HashMap time = new HashMap();
					time.put("since",i+"/"+"1/"+sinceYears );
					int days = monthDays[i-1];
					time.put("until",i+"/"+days+"/"+sinceYears);
					times.add(time);
					
				}
				
			}else{
				for (int j=sinceYears;j<=untilYears;j++){
					if(j==sinceYears){
						for (int i=sinceMonths;i<=12;i++){
							HashMap time = new HashMap();
							time.put("since",i+"/"+"1/"+j );
							int days = monthDays[i-1];
							time.put("until",i+"/"+days+"/"+j);
							times.add(time);
							
						}
						
					}
					if(j==untilYears){
						
						for (int i=1;i<=untilMonths;i++){
							HashMap time = new HashMap();
							time.put("since",i+"/"+"1/"+j );
							int days = monthDays[i-1];
							time.put("until",i+"/"+days+"/"+j);
							times.add(time);
						}
						
					}
					if(j<untilYears && j>sinceYears){
						
						for (int i=1;i<=12;i++){
							HashMap time = new HashMap();
							time.put("since",i+"/"+"1/"+j );
							int days = monthDays[i-1];
							time.put("until",i+"/"+days+"/"+j);
							times.add(time);
						}
						
					}
					
					
					
				}
				
			}
					
			
	
		}
		if (day.equals("true")){
			
			
			Calendar startCale=new GregorianCalendar();
			Calendar endCal=new GregorianCalendar();

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		    Date startDate = sdf.parse(since);
		    Date endDate = sdf.parse(until);
		    
			startCale.setTime(startDate);
			endCal.setTime(endDate);

			int daysDifference = 0;
			Date d1 = startCale.getTime();
			Date d2 =endCal.getTime();
			daysDifference =  (int)( ( d2.getTime() - d1.getTime() )/ (1000 * 60 * 60 * 24) ) ;
			GregorianCalendar newcal = new GregorianCalendar();
			newcal.setTime(startDate);
			int j=1;
			int index =0;
			for (int i=0;(i<=daysDifference);i=i+1){
				HashMap time  = new HashMap();
				Date tmp = newcal.getTime();
				String daterep = sdf.format(tmp);
				time.put("since",daterep);
				time.put("until",daterep);
				times.add(time);
				newcal.add(Calendar.DAY_OF_MONTH,j);
				
			}
			
		}
		if (week.equals("true")){
			
			Calendar startCale=new GregorianCalendar();
			Calendar endCal=new GregorianCalendar();

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		    Date startDate = sdf.parse(since);
		    Date endDate = sdf.parse(until);
		    
			startCale.setTime(startDate);
			endCal.setTime(endDate);

			int daysDifference = 0;
			Date d1 = startCale.getTime();
			Date d2 =endCal.getTime();
			daysDifference =  (int)( ( d2.getTime() - d1.getTime() )/ (1000 * 60 * 60 * 24) ) ;
			daysDifference++;
			GregorianCalendar newcal = new GregorianCalendar();
			newcal.setTime(startDate);
			int period =6;
			int reminder=daysDifference%period;
			int iteration = daysDifference/period;
			
			for (int i=0;i<iteration;i++){
				
				HashMap time  = new HashMap();
				Date tmp = newcal.getTime();
				String daterep = sdf.format(tmp);
				time.put("since",daterep);
				newcal.add(Calendar.DAY_OF_MONTH,(period-1));
				tmp = newcal.getTime();
				daterep = sdf.format(tmp);
				time.put("until",daterep);
				times.add(time);
				newcal.add(Calendar.DAY_OF_MONTH,1);
				
			}
			
			if (reminder>0){
				HashMap time  = new HashMap();
				Date tmp = newcal.getTime();
				String daterep = sdf.format(tmp);
				time.put("since",daterep);
				newcal.add(Calendar.DAY_OF_MONTH,(reminder-1));
				tmp = newcal.getTime();
				daterep = sdf.format(tmp);
				time.put("until",daterep);
				times.add(time);
			}
			

		
		}
	
		
		return times;
		
	}
	

	
	private String createCVSForOneGroup(HashMap group,String csv,String studyc,String taskc,String datac,String timec) throws Exception{
		
		//System.out.println("starting createCVSForOneGroup");
		
		ArrayList trackers = (ArrayList) group.get("trackers");
	    ArrayList tasks;
	    try{
		    if ((ArrayList) group.get("tasks")!=null){
		    	tasks = (ArrayList) group.get("tasks");
		    }
		    else{
		    	tasks=new ArrayList();
		    }
			
		    for (int j=0;j<trackers.size();j++){
				PITrackingDAO track=(PITrackingDAO)trackers.get(j);
				HashMap arg = track.getArguments();
				String db = (String)arg.get("db");
				if (db.contains("std")){
					db="std";
				}else{
					db="research";
							
				}
				HashMap res = (HashMap) track.getResults();
				csv = turntoCSV((ArrayList)res.get("records"),db,studyc,taskc,datac,timec,csv);
				
			}   
	    }catch(Exception e){
	    	StringWriter sw = new StringWriter();
	    	e.printStackTrace(new PrintWriter(sw));
	    	String exceptionAsString = sw.toString();
	    	System.out.println("Exception in createCVSForOneGroup "+e.getMessage()+":"+exceptionAsString);
	    }
	    
		return csv;
	}
	
	private String createCVS(HashMap groupThreadas,String cvs,String studyc,String taskc,String datac,String timec) throws Exception{
		
		String csv1=cvs;
		Iterator entries = groupThreadas.entrySet().iterator();
		
		while (entries.hasNext()) {//per study
			
			Map.Entry entry = (Map.Entry) entries.next();
		    String key = (String)entry.getKey();
		    if (!key.equals("type")){
		    	HashMap value = (HashMap)entry.getValue();
		    	ArrayList trackers = (ArrayList) value.get("trackers");
		    	ArrayList tasks;
			    if ((ArrayList) value.get("tasks")!=null){
			    	tasks = (ArrayList) value.get("tasks");
			    }
			    else{
			    	tasks=new ArrayList();
			    }
			    
			    
				for (int j=0;j<trackers.size();j++){
					PITrackingDAO track=(PITrackingDAO)trackers.get(j);
					//ArrayList sessions1 = (track).getsessionArray();
					HashMap arg = track.getArguments();
					String db = (String)arg.get("db");
					if (db.contains("std")){
						db="std";
					}else{
						db="research";
								
					}
					HashMap res = (HashMap) track.getResults();						
					csv1 = turntoCSV((ArrayList)res.get("records"),db,studyc,taskc,datac,timec,csv1);
				}
		    }
		    
		}
		return csv1;
		
	}

	
	public String turntoCSV(ArrayList rs,String db,String studyc,String taskc,String datac,String timec,String csv) throws Exception{
		
		try{
			System.out.println("turn csv: ");
			//System.out.print(rs);
			if (rs==null){
				System.out.println("rs is null");
				
			}
			
			if (csv.equals("")){
				if (studyc.equals("true")){
					csv = "Study,";//Started,Completed,CR%"+System.getProperty("line.separator");
				}else{
					if (!(studyc.equals("true")) && taskc.equals("true")){
						csv = "Study,";//Started,Completed,CR%"+System.getProperty("line.separator");
					}
	
				}
					
				
				if(taskc.equals("true")){
					
					csv = csv + "Task,";//Started,Completed,CR%"+System.getProperty("line.separator");
				}
				if (timec.equals("true")){
					
					csv = csv + "Date,"; 
					
				}
				if (datac.equals("true")){
					csv = csv + "Data Group,";
					
				}
				
				csv = csv + "Started,Completed,CR%"+System.getProperty("line.separator");	
				
				
			}
			
			
			Iterator itr = rs.iterator();
			while(itr.hasNext()){//iterate all records
				ArrayList row = new ArrayList();
				row = (ArrayList)itr.next();
				Iterator itrII = row.iterator();
				while (itrII.hasNext()){// iterate on fields in record
					
					csv += itrII.next();
					if (itrII.hasNext()){
						csv += ",";//if not last 
					}else{
						csv += "%";
						
					}
				}
				csv += System.getProperty("line.separator");  
				
			}
			
			return csv;
		}catch(Exception e){
			System.out.println("Excpetion in turntoCSV");
			throw e;
		}
		
	}

	private ArrayList clone (ArrayList array){
		ArrayList result = new ArrayList();
		for (int i=0;i<array.size();i++){
			HashMap time = new HashMap ((HashMap) array.get(i));
			result.add(time);
		}
		return result;
		
		
	}
	private void setTrackerGroups(HashMap trackerGroups,HashMap map,String name,String functionType,ArrayList times) throws Exception{
		
		
			String since = (String) map.get("since");
			String until = (String) map.get("until");
			String task = (String) map.get("task");
			String datac = (String) map.get("datac");
			String timec = (String) map.get("timec");
			String db = (String) map.get("connect");
			String endTask = (String) map.get("endTask");
			String filter = (String) map.get("filter");
			
		
		
			HashMap studyGroup = new HashMap();
			studyGroup.put("studyname", new String(name));
			ArrayList trackers = new ArrayList();
			ArrayList threads = new ArrayList();
			//ArrayList tasks = new ArrayList();
			studyGroup.put("trackers", trackers);
			//studyGroup.put("threads", threads);
			//studyGroup.put("tasks", tasks);
			HashMap args=new HashMap();
			args.put("id",0);
			//args.put("method", "getsession");
			args.put("method",new String(functionType));
			args.put("studyname", new String(name));
			args.put("since", new String(since));
			args.put("until", new String(until));
			args.put("db", new String(db));
			args.put("task", new String(task));
			args.put("filter", new String(filter));
			//args.put("tasks",tasks);
			args.put("timec",new String(timec));
			args.put("datac",new String(datac));
			args.put("times",clone(times));
			args.put("endTask",new String(endTask));
			PITrackingDAO tracker1= new PITrackingDAO(args);
			//ArrayList sessions = new ArrayList();
			HashMap res = tracker1.getResults();
			//res.put("sessions", sessions);
			trackers.add(tracker1);
			//Thread t = new Thread(tracker1);
			//threads.add(t);
			
			
			trackerGroups.put(name, studyGroup);
		

		
	}

	private void addThreadstoList(Thread t,int[] threadsIndexA,int[] maxActiveA,ArrayList runnigThreads) throws Exception{
		
		int threadsIndex = threadsIndexA[0];
		int maxActive = maxActiveA[0];
		try{
			
			
			if (threadsIndex==maxActive){
				for (int i=0;i<runnigThreads.size();i++){
					Thread j = (Thread) runnigThreads.get(i);
					j.join();
				}
				threadsIndex=0;
				runnigThreads.add(t);
				t.start();
				threadsIndex++;
			}else{
				runnigThreads.add(t);
				t.start();
				threadsIndex++;
			}
		}catch(RuntimeException e){
			System.out.println("Exception in addThreadstoList ");
			throw new Exception("excpetion in DataBase methods: "+e.getMessage());
			
		}
		threadsIndexA[0] = threadsIndex;
		
		
	}

	
private void startGroupThreads(HashMap groupThreadas,int[] maxindex,int[] index,ArrayList globalThreads) throws Exception{
		
		
		Iterator entries = groupThreadas.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
		    //String key = (String)entry.getKey();
		    HashMap value = (HashMap)entry.getValue();
		    //(HashMap group,int maxindex,int index,ArrayList globalThreads
		   	startThreads(value,maxindex,index,globalThreads);
		    
		}
	}
private String processGroupsByTaskByOneII(HashMap groupThreadas,String csv,int[] max,int tindex[],ArrayList times,String studyc,String taskc,String datac,String timec,ArrayList runnigThreads) throws Exception{
	
	int index=0;
	Iterator entries = groupThreadas.entrySet().iterator();
	HashMap newGroups = new HashMap();
	while (entries.hasNext()) {
		
		Map.Entry entry = (Map.Entry) entries.next();
	    String key = (String)entry.getKey();
	    HashMap value = (HashMap)entry.getValue();
	    ArrayList trackers = (ArrayList) value.get("trackers");
	    PITrackingDAO track = (PITrackingDAO) trackers.get(0);
	    HashMap args = track.getArguments();
	    args.put("method", "gettasksII");
	    startThreads(value,max,tindex,runnigThreads);
	    waitUntilFinished(runnigThreads);
	    //HashMap group,String csv,ArrayList times,String studyc,String taskc,String datac,String timec)
	    csv = createCVSForOneGroup(value,csv,studyc,taskc,datac,timec);
	    ((ArrayList)value.get("trackers")).clear();
	    ((ArrayList)value.get("threads")).clear();
	    runnigThreads.clear();
//	    groupThreadas.remove(key);
	    groupThreadas.put(key,null);
		
	}
	return csv;
	
	
	
	

}
// TODO progressbar 
private void progressBar(HashMap studyGroup,HashMap map) throws Exception{
	
	HttpServletResponse resp = (HttpServletResponse) map.get("resp");
	PrintWriter writer = (PrintWriter) resp.getWriter();
	Iterator entries = studyGroup.entrySet().iterator();
	int all = studyGroup.size();
	int finished =0;
	String key;
	HashMap value = null;
	try{
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
		    key = (String)entry.getKey();
		    if (!key.equals("type")){
		    	value = (HashMap)entry.getValue();
		    	ArrayList trackers = (ArrayList) value.get("trackers");
		    	PITrackingDAO track=(PITrackingDAO)trackers.get(0);
				HashMap res = (HashMap) track.getResults();						
				if ((ArrayList)res.get("records")!=null) finished++;
		    }
		}
	}catch(Exception e){}
	//writer.print("id: " + "ServerTime" + "\n");
	//writer.print("data: " + new Date().toLocaleString() + "\n\n");
	//String data = String.valueOf( (int)( ( finished/all )*100 ) )  ;
	//writer.write("event: progress\n");
	//writer.write("data:" + data  + "\n\n");
	//write.println("progress:"+ ( (finished/all)*100 ) );
	//writer.flush();
	//resp.flushBuffer();
}
private String processGroupByOne(HashMap studygroup,String csv,ArrayList times,ArrayList runnigThreads,String studyc,String taskc,String datac,String timec,int[] Threadmax,int[] threadindex,HashMap map) throws Exception{
	
	int index=0;
	Iterator entries = studygroup.entrySet().iterator();
	String key;
	HashMap value = null;
	try{
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
		    key = (String)entry.getKey();
		    if (!key.equals("type")){
		    	value = (HashMap)entry.getValue();
		    	startThreads(value,Threadmax,threadindex,runnigThreads);
		    }
		  			
		}
		waitUntilFinished(runnigThreads);
	    runnigThreads.clear();
	   	if (( (String)studygroup.get("type")).equals("totalsessions") ){
			HashMap newvalue = sumUp(studygroup,datac,timec);
			csv = createCVSForOneGroup(newvalue,csv,studyc,taskc,datac,timec);
		}else{
			csv = createCVS(studygroup,csv,studyc,taskc,datac,timec);
		
		}
			
	}catch(Exception e){
		System.out.println("Exception in processGroupByOne"+e.getMessage());
		throw e;
	}
	return csv;

}

private HashMap sumUp(HashMap studygroup,String datac,String timec) throws Exception{
	HashMap newStudy = new HashMap();
	PITrackingDAO newTrack = new PITrackingDAO();
	ArrayList newrecordset = new ArrayList();
	ArrayList  newTrackers = new ArrayList();
	String newDB = new String();
	float started=0;
	float completed=0;
	PITrackingDAO track = new PITrackingDAO();
	Iterator entries = studygroup.entrySet().iterator();
	String key;
	HashMap value = new HashMap();
	try{
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
		    key = (String)entry.getKey();
		    if (!key.equals("type")){
		    	value = (HashMap)entry.getValue();
		    	ArrayList trackers = (ArrayList) value.get("trackers");
			    track=(PITrackingDAO)trackers.get(0);
			    HashMap res = (HashMap) track.getResults();
				ArrayList rec = (ArrayList) res.get("records");
				if (newrecordset.size()==0){
					for (int z=0;z<rec.size();z++){
						ArrayList onerec = (ArrayList) rec.get(z);
						newrecordset.add(onerec);
					}
					
				}else{
					for (int i=0;i<rec.size();i++){
						ArrayList onenewrec = (ArrayList) rec.get(i);
						ArrayList oneoldrec = (ArrayList) newrecordset.get(i);
						if (timec.equals("true")){
							if (datac.equals("true")){
								int recStarted = Integer.parseInt((String) onenewrec.get(2));
								int recCompleted  = Integer.parseInt((String) onenewrec.get(3));
								int oldrecStarted = Integer.parseInt((String)oneoldrec.get(2)) +recStarted; 
								int oldreccompletd = Integer.parseInt((String)oneoldrec.get(3)) +recCompleted; 
								oneoldrec.set(2,String.valueOf(oldrecStarted) );
								oneoldrec.set(3,String.valueOf(oldreccompletd) );
								float sum;
								if (oldrecStarted!=0){
									sum = ((float)oldreccompletd/(float)oldrecStarted)*100;
								}else{
									sum=0;
								}
								
								oneoldrec.set(4,String.valueOf((int)sum) );
								
								
							}else{
								int recStarted = Integer.parseInt((String) onenewrec.get(1));
								int recCompleted  = Integer.parseInt((String) onenewrec.get(2));
								int oldrecStarted = Integer.parseInt((String)oneoldrec.get(1)) +recStarted;
								int oldrecCompleted  = Integer.parseInt((String)oneoldrec.get(2)) +recCompleted;
								oneoldrec.set(1, String.valueOf(oldrecStarted)) ;
								oneoldrec.set(2,String.valueOf(oldrecCompleted)) ;
								float sum;
								if (oldrecStarted!=0){
									sum = ((float)oldrecCompleted/(float)oldrecStarted)*100;
								}else{
									sum=0;
								}
								oneoldrec.set(3,String.valueOf((int)sum) );
								
							}
							
						}else{
							if (datac.equals("true")){
								
								int recStarted = Integer.parseInt((String) onenewrec.get(1));
								int recCompleted  = Integer.parseInt((String) onenewrec.get(2));
								int oldrecStarted = Integer.parseInt((String)oneoldrec.get(1)) +recStarted;
								int oldrecCompleted  = Integer.parseInt((String)oneoldrec.get(2)) +recCompleted;
								oneoldrec.set(1, String.valueOf(oldrecStarted)) ;
								oneoldrec.set(2,String.valueOf(oldrecCompleted)) ;
								float sum;
								if (oldrecStarted!=0){
									sum = ((float)oldrecCompleted/(float)oldrecStarted)*100;
								}else{
									sum=0;
								}
								oneoldrec.set(3,String.valueOf((int)sum) );

								
							}else{
								int recStarted = Integer.parseInt((String) onenewrec.get(0));
								int recCompleted  = Integer.parseInt((String) onenewrec.get(1));
								int oldrecStarted = Integer.parseInt((String)oneoldrec.get(0)) +recStarted;
								int oldrecCompleted  = Integer.parseInt((String)oneoldrec.get(1)) +recCompleted;
								oneoldrec.set(0,String.valueOf(oldrecStarted));
								oneoldrec.set(1,String.valueOf(oldrecCompleted));
								float sum;
								if (oldrecStarted!=0){
									sum = ((float)oldrecCompleted/(float)oldrecStarted)*100;
								}else{
									sum=0;
								}
								oneoldrec.set(2,String.valueOf((int)sum) );

								
							}
							
						}
						
					}
					
				}
		    }
		}
//				for (int j=0;j<newrecordset.size();j++){
//					
//					ArrayList record  = (ArrayList) newrecordset.get(j);
//					int recStarted;
//					int recCompleted;
//					int recCR;
//					recStarted = Integer.parseInt((String) record.get(1));
//						recCompleted  = Integer.parseInt((String) record.get(2));
//						recCR  = Integer.parseInt((String) record.get(3));
//					}
//					
//				}
//				
//				
//				
//				
//				started +=recStarted;
//				completed +=recCompleted;
//		    }
//			
//		}
//		HashMap args = track.getArguments();
//		newDB = (String) args.get("db");
//		
//		newrec.add((String)String.valueOf((int)started));
//		newrec.add((String)String.valueOf((int)completed));
//		float sum = (completed/started)*100;
//		newrec.add((String)String.valueOf((int)sum));
//		newrecordset.add(newrec);
		HashMap results =  newTrack.getResults();
		HashMap newargs = newTrack.getArguments();
		newargs.put("db", newDB);
		results.put("records", newrecordset);
		newTrackers.add(newTrack);
		newStudy.put("trackers", newTrackers);
		
		
		
	}catch(Exception e ){
		System.out.println(e.getMessage());
	}
	return newStudy;
	
}

private void startThreads(HashMap group,int[] maxindex,int[] index,ArrayList globalThreads) throws Exception{

	
	ArrayList trackers =(ArrayList) group.get("trackers");
	try{
		for (int i=0;i<trackers.size();i++){ 
			PITrackingDAO track = (PITrackingDAO) trackers.get(i);
			HashMap args = track.getArguments();
			String method = (String)args.get("method");
			if (method.equals("getTotalSessions")){
				Thread t = new Thread(track);
				addThreadstoList(t,index,maxindex,globalThreads);
				
			}
			if (method.equals("getsession")){
				Thread t = new Thread(track);
				//int threadsIndex,int maxActive,ArrayList runnigThreads
				addThreadstoList(t,index,maxindex,globalThreads);
			}
			if (method.equals("gettasks")){
				Thread t = new Thread(track);
				addThreadstoList(t,index,maxindex,globalThreads);
			}
			if (method.equals("gettasksII")){
				Thread t = new Thread(track);
				addThreadstoList(t,index,maxindex,globalThreads);
			}
		}
	}catch(Exception e){
		System.out.println("Exception in startThreads ");
	}
	
}


	private void waitUntilFinished(ArrayList threads) throws Exception {
		
		try{
			for (int i=0;i<threads.size();i++){
				Thread t = (Thread)threads.get(i);
				t.join();
			}
		}catch (Exception e){
			System.out.println("Exception in waitUntilFinished");
			throw new Exception("error in waitUntilFinished: "+e.getMessage());
		}
	
		
	}
	private void calulateCompleteGroupsForOneGroup(HashMap value) throws Exception{
		
		ArrayList trackers = (ArrayList) value.get("trackers");
	    for (int i=0;i<trackers.size();i++){
	    	PITrackingDAO track = (PITrackingDAO) trackers.get(i);
	    	HashMap res = track.getResults();
	    	ArrayList sessions =(ArrayList) res.get("sessions");
	    	track.calculateCompleted(sessions);
	    }
		
	}
	private void calulateCompleteGroups(HashMap groups) throws Exception{
		
		Iterator entries = groups.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
		    //String key = (String)entry.getKey();
		    HashMap value = (HashMap)entry.getValue();
		    ArrayList trackers = (ArrayList) value.get("trackers");
		    for (int i=0;i<trackers.size();i++){
		    	PITrackingDAO track = (PITrackingDAO) trackers.get(i);
		    	HashMap res = track.getResults();
		    	ArrayList sessions =(ArrayList) res.get("sessions");
		    	track.calculateCompleted(sessions);
		    }
		}
	}
	

	
	private ArrayList createRS(ArrayList sessions,ArrayList tasks,String name,String db,String studyc,String taskc,String datac,String timec,String taskName,String since,String until) throws Exception{
		
		
		Integer NumberOfCompletedSessions=0;
		Integer NumberOfInCompletedSessions=0;
		float sCR=0;
		float cr=0;
		String studyName="";
		ArrayList recordSet = new ArrayList();
		
		if (sessions==null) {
			
			return recordSet;
		}
		try{
			
			
			for(int i=0;i<sessions.size();i++){
				
				sessionBean session = (sessionBean) sessions.get(i);
				String status =session.getStatus(); 
				if ( status == null || !(status.equals( "C" )) ){
					
					NumberOfInCompletedSessions++;
					
				}else{
					NumberOfCompletedSessions++;
				}
			}
			if (NumberOfCompletedSessions!=0){
				 sCR = NumberOfCompletedSessions+NumberOfInCompletedSessions;
				 cr  = (NumberOfCompletedSessions/sCR )*100;
			}else{
				cr=0;
				
			}
			//////////////creating study record//////////////
			
			if ((NumberOfInCompletedSessions+NumberOfCompletedSessions)==0 && !timec.equals("true")) {
				return recordSet;
			}
			
			sessionBean session=null;
			ArrayList record = new ArrayList();
			if (sessions.size()==0){
				studyName= name;
				
			}else{
				session =(sessionBean)sessions.get(0);
				studyName = session.getName();
				
			}
			/////for only sessions////////////////////////
			if (!(studyc.equals("true")) && !(taskc.equals("true"))){
				if (timec.equals("true")){
					if (since.equals(until)){
						record.add(since);
					}else{
						record.add(since+" - "+until);
					}
					
				}
				if (datac.equals("true")){
					if (db.equals("std")){
						record.add("Demo");//add database
						
					}
					if (db.equals("research")){
						record.add("research");
					}
					
				}
				record.add(String.valueOf(NumberOfInCompletedSessions+NumberOfCompletedSessions));
				record.add(String.valueOf(NumberOfCompletedSessions));
				record.add(String.valueOf(Math.round(cr) ));
				recordSet.add(record);
				return recordSet;
				
			}
			//////////for study row///////////////////
			if (studyc.equals("true") && !taskc.equals("true")){

				record.add(studyName);
				if (timec.equals("true")){
					if (since.equals(until)){
						record.add(since);
					}else{
						record.add(since+" - "+until);
					}
				}
				if (taskc.equals("true")){
					record.add("");//task name for the header
					
				}
				if (datac.equals("true")){
					if (db.equals("std")){
						record.add("Demo");//add database
						
					}
					if (db.equals("research")){
						record.add("research");
					}
					
				}
				
				record.add(String.valueOf(NumberOfInCompletedSessions+NumberOfCompletedSessions));
				record.add(String.valueOf(NumberOfCompletedSessions));
				record.add(String.valueOf(Math.round(cr) ));
				recordSet.add(record);
				//jLogger.debug("datac is: "+datac);
				return recordSet; 
			}
			//////////for tasks/////////////////////////////
			if (taskc.equals("true"))
			{
				calculateSessionTasks(sessions,tasks);
				for (int j=0;j<tasks.size();j++){
					ArrayList recordT =  new ArrayList();
					HashMap task = new HashMap();
					task = (HashMap) tasks.get(j);
					
					//if (taskName.equals(task.get("name")) || taskName.equals("")){
					String tName = (String) task.get("name");
					if (tName.contains(taskName) || taskName.equals("")){
						//if (session==null) throw new Exception("session is null");
						recordT.add(studyName);
						float started = ((Integer) task.get("started"));
						float complete = ((Integer) task.get("completed"));
						//int inComplete = (Integer) task.get("Incomplete");
						recordT.add(task.get("name"));
						if (timec.equals("true")){
							if (since.equals(until)){
								recordT.add(since);
							}else{
								recordT.add(since+" - "+until);
							}
						}
						
						if (datac.equals("true")){
							if (db.equals("std")){
								recordT.add("Demo");//add database
							}
							if (db.equals("research")){
								recordT.add("research");
							}
							if (db.equals("warehouse")){
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
		
		}catch(Exception e){
			e.printStackTrace();
			return recordSet;
			//throw new Exception("error in createRS: "+e.getMessage());
		}
					
		return recordSet;
		
	}
	private void calculateSessionTasks(ArrayList sessions,ArrayList tasks) throws Exception{
		
		try{
			for (int i=0;i<sessions.size();i++){
				sessionBean session= (sessionBean) sessions.get(i);
				ArrayList sessionTasks = session.getTasks();
				for (int j=0;j<sessionTasks.size();j++){			
					HashMap task = (HashMap) sessionTasks.get(j);
					updateTasks(tasks,task);
				}
			}
			
		}catch(Exception e){
			throw new Exception("error in calculateSessionTasks: "+e.getMessage());
		}
		
		
	}
	private void updateTasks(ArrayList tasks,HashMap task) throws Exception{
		
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

}




