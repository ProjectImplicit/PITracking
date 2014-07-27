


package org.uva.tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
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
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.uva.Implicit;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import edu.yale.its.util.ChainedException;



/**
 * 
 *  
 * 
 * Main servlet controller. 
 * 
 * The Servlet recieves requests from the client and
 * 
 * Return responce in the form of CSV.
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


public class PITracking  extends HttpServlet{
	
	
		
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException { 
		try {
			System.out.println("starting do get");
			doPost(request,response);
		} catch (ServletException e) {
			e.printStackTrace();
		}
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
	
	
	/**
	 * 
	 * Process the request returns the Data requested in CSV Form
	 * 
	 * @param map
	 *            HashMap contains request parameters.
	 *            
	 * 
	 * @return CSV file
	 * 
	 */
	
	
	private String process(HashMap map) throws Exception{
		
		
		int [] threadsIndex={0};
		int [] maxActive={1};
		String csv = "";
		ArrayList studiesResult = new ArrayList();
		ArrayList result = new ArrayList();
		ArrayList studies = new ArrayList();
		int switchcase=0;
		ArrayList times =  new ArrayList();
		boolean useThreads;
		ArrayList runnigThreads = new ArrayList();
		runnigThreads.clear();
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
			if (!studyc.equals("true") && !taskc.equals("true") ){
				csv = onlySessionSelected(map,csv,tracker,times,studiesResult,runnigThreads,maxActive,threadsIndex);
			}
			if (studyc.equals("true") && !taskc.equals("true") ){
				HashMap trackerStudyGroups = new HashMap();
				trackerStudyGroups.put("type", "getsession");
				if ( db.equals("both")){
					for (int i=0;i<studiesResult.size();i++){//per study
						String name = (String)studiesResult.get(i);
							if (!name.equals("")){
								map.put("connect", test+"std");
								setTrackerGroups(trackerStudyGroups,map,name,"getsession",times);
								
							}
					}
					csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
					trackerStudyGroups.clear();
					trackerStudyGroups.put("type", "getsession");
					for (int i=0;i<studiesResult.size();i++){//per study
						String name = (String)studiesResult.get(i);
							if (!name.equals("")){
								
								map.put("connect", test+"research");
								setTrackerGroups(trackerStudyGroups,map,name,"getsession",times);
								
							}
						
					}
					csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
				}else{
					for (int i=0;i<studiesResult.size();i++){//per study
						String name = (String)studiesResult.get(i);
							if (!name.equals("")){
								map.put("connect", test+db);
								setTrackerGroups(trackerStudyGroups,map,name,"getsession",times);
							}
					}
					csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
					
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
							map.put("connect", test+"std");
							setTrackerGroups(trackerStudyGroups,map,name,"gettasksII",times);
						}
						
					}
					csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
					trackerStudyGroups.clear();
					trackerStudyGroups.put("type", "getsession");
					for (int i=0;i<studiesResult.size();i++){//per study
						String name = (String)studiesResult.get(i);
						tasks.clear();
							if (!name.equals("") ){
								map.put("connect", test+"research");
								setTrackerGroups(trackerStudyGroups,map,name,"gettasksII",times);
							}
					}
					csv =	processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);

				}else{
					for (int i=0;i<studiesResult.size();i++){//per study
						String name = (String)studiesResult.get(i);
						tasks.clear();
						if (!name.equals("") ){
								map.put("connect", test+db);
								setTrackerGroups(trackerStudyGroups,map,name,"gettasksII",times);
								
						}
						
					}
					csv =	processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);

				}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw e;
		}
		return csv;
		
		
		
	}
	/**
	 * 
	 * Process the request returns the Data requested in CSV Form
	 * 
	 * @param map,csv,tracker,times,studiesResult,runnigThreads,maxActive,threadsIndex.
	 *            
	 *            HashMap contains request parameters.
	 *            CSV string that will be populated
	 *            Tracker Object that will run the database queries.
	 *            studiesResult list of studies to query.
	 *            runnigThreads array of running threads
	 *            maxActive maximum active threads
	 *            threadsIndex running index that keeps track of threads
	 *            
	 *            
	 * 
	 * @return CSV file
	 * 
	 */
	
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
								
							}
					}
					csv = processGroupByOne(trackerStudyGroups,csv,times,runnigThreads,studyc,taskc,datac,timec,maxActive,threadsIndex,map);
			}else{
				for (int i=0;i<studiesResult.size();i++){//per study
					String name = (String)studiesResult.get(i);
						if (!name.equals("")){
							map.put("connect", test+db);
							setTrackerGroups(trackerStudyGroups,map,name,"getTotalSessions",times);
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
			}
			
		}
		
		return csv;
	}
	
	/**
	 * 
	 * Clean arrays from duplicates and merge arrays 
	 * 
	 * @param study1,study2
	 *            
	 *            study1 first array
	 *            study2 second array
	 *            
	 *            
	 * 
	 * @return cleaned merged array.
	 * 
	 */
	
	private ArrayList filterDuplicates (ArrayList study1,ArrayList study2){
		
		ArrayList combine=new ArrayList();
		for (int i=0;i<study2.size();i++){
			
			String name = (String) study2.get(i);
			if (!study1.contains(name)) study1.add(name);
				
			
		}
		
		
		return study1;
		
	}
	
	/**
	 * 
	 * Add study to array if does not exist 
	 * 
	 * @param studies,studyc
	 *            
	 *            studies array of studies
	 *            studyc study to add
	 *            
	 *            
	 * 
	 * @return studies array
	 * 
	 */
	
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
	
	/**
	 * 
	 * Fix the database params according to JNDI
	 * 
	 * @param db,test
	 *            
	 *            db chosen data base to use
	 *            test test data base or real one.
	 *            
	 *            
	 * 
	 * @return fixed database param
	 * 
	 */
	private String fixDB(String db,String test){
		
		
		if (db.equals("Demo")) return "std";
		if (db.equals("Research")) return "research";
		if (db.equals("Both")) return "both";
		
		
		
		return null;
	}
	
	
	/**
	 * 
	 * Return an array of times based on range and a time selector: d/w/m/y 
	 * 
	 * @param since,until,day,week,month,year,timec
	 *            
	 *            since time range
	 *            until time range
	 *            day time selector string representation of true/false.
	 *            week time selector string representation of true/false.
	 *            month time selector string representation of true/false.
	 *            year time selector string representation of true/false.
	 *            timec time selector string representation of true/false.
	 *            
	 *            
	 * 
	 * @return time array
	 * 
	 */

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
	

	/**
	 * 
	 * Return an array of times based on range and a time selector: d/w/m/y 
	 * 
	 * @param since,until,day,week,month,year,timec
	 *            
	 *            since time range
	 *            until time range
	 *            day time selector string representation of true/false.
	 *            week time selector string representation of true/false.
	 *            month time selector string representation of true/false.
	 *            year time selector string representation of true/false.
	 *            timec time selector string representation of true/false.
	 *            
	 *            
	 * 
	 * @return time array
	 * 
	 */
	private String createCSVForOneGroup(HashMap group,String csv,String studyc,String taskc,String datac,String timec) throws Exception{
		
				
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
	/**
	 * 
	 * Return a CSV from resulted threads 
	 * 
	 * @param  groupThreadas,cvs,studyc,taskc,datac,timec
	 *            
	 *            groupThreadas the group of threads that were run.
	 *            cvs string to be added to.
	 *            studyc selector, string representation of true/false.
	 *            taskc selector, string representation of true/false.
	 *            datac selector, string representation of true/false.
	 *            timec selector, string representation of true/false.
	 *           
	 *            
	 *            
	 * 
	 * @return CSV string
	 * 
	 */
	private String createCSV(HashMap groupThreadas,String cvs,String studyc,String taskc,String datac,String timec) throws Exception{
		
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

	/**
	 * 
	 * Create a CSV from ArrayList
	 * 
	 * @param  rs,db,studyc,taskc,datac,timec,csv
	 *            
	 *            groupThreadas the group of threads that were run.
	 *            cvs string to be added to.
	 *            studyc selector, string representation of true/false.
	 *            taskc selector, string representation of true/false.
	 *            datac selector, string representation of true/false.
	 *            timec selector, string representation of true/false.
	 *           
	 *            
	 *            
	 * 
	 * @return CSV string
	 * 
	 */
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

	/**
	 * 
	 * Clone an Arraylist
	 * 
	 * @param  array
	 *            
	 *            array aaray to clone 
	 *            
	 * 
	 * @return cloned array
	 * 
	 */
	
	private ArrayList clone (ArrayList array){
		ArrayList result = new ArrayList();
		for (int i=0;i<array.size();i++){
			HashMap time = new HashMap ((HashMap) array.get(i));
			result.add(time);
		}
		return result;
		
		
	}
	
	/**
	 * 
	 * Prepare the tracker groups 
	 * 
	 * @param  trackerGroups,map,name,functionType,times
	 *            
	 *            trackerGroups the groups to prepare.
	 *            map HashMap of request parameters
	 *            name name of study
	 *            functionType the type of function only sesstions, or also tasks
	 *            times ArrayList of times 
	 *            
	 * 
	 * @return void
	 * 
	 */
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
			studyGroup.put("trackers", trackers);
			HashMap args=new HashMap();
			args.put("id",0);
			args.put("method",new String(functionType));
			args.put("studyname", new String(name));
			args.put("since", new String(since));
			args.put("until", new String(until));
			args.put("db", new String(db));
			args.put("task", new String(task));
			args.put("filter", new String(filter));
			args.put("timec",new String(timec));
			args.put("datac",new String(datac));
			args.put("times",clone(times));
			args.put("endTask",new String(endTask));
			PITrackingDAO tracker1= new PITrackingDAO(args);
			HashMap res = tracker1.getResults();
			trackers.add(tracker1);
			trackerGroups.put(name, studyGroup);
	}
	
	/**
	 * 
	 * Add a new thread to running list of threads 
	 * 
	 * @param  t,threadsIndexA,maxActiveA,runnigThreads
	 *            
	 *            t Thread to add.
	 *            threadsIndexA Index of number of active threads
	 *            maxActiveA Maximum active threads
	 *            runnigThreads Array of running threads. 
	 *             
	 * 
	 * @return void
	 * 
	 */
	

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

	/**
	 * 
	 * Start running the threads 
	 * 
	 * @param  groupThreadas,maxindex,index,globalThreads
	 *            
	 *            groupThreadas Group of threads to run..
	 *            maxindex max of threads to run
	 *            index index of threads
	 *            globalThreads Array of running threads. 
	 *             
	 * 
	 * @return void
	 * 
	 */
	
	private void startGroupThreads(HashMap groupThreadas,int[] maxindex,int[] index,ArrayList globalThreads) throws Exception{
			
			
		Iterator entries = groupThreadas.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
		    HashMap value = (HashMap)entry.getValue();
		    startThreads(value,maxindex,index,globalThreads);
		    
		}
	}

	/**
	 * 
	 * live progress bar (not used)
	 * 
	 * @param  
	 *            
	 * 
	 * @return void
	 * 
	 */
	
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
/**
 * 
 * Start the threads and return the result in the form of CSV
 * 
 * @param db,test
 *            
 *            db chosen data base to use
 *            test test data base or real one.
 *            
 *            
 * 
 * @return fixed database param
 * 
 */
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
				csv = createCSVForOneGroup(newvalue,csv,studyc,taskc,datac,timec);
			}else{
				csv = createCSV(studygroup,csv,studyc,taskc,datac,timec);
			
			}
				
		}catch(Exception e){
			System.out.println("Exception in processGroupByOne"+e.getMessage());
			throw e;
		}
		return csv;
	
	}

	/**
	 * 
	 * Sum up the sessions records in the HashMap. 
	 * 
	 * @param studygroup,datac,timec
	 *            
	 *            studygroup Group to sum up records.
	 *            datac selector, string representation of true/false.
	 *            timec selector, string representation of true/false.
	 *            
	 *            
	 * 
	 * @return The HashMap after summing up the records
	 * 
	 */
	
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

	/**
	 * 
	 * Start the threads of one group
	 * 
	 * @param group,maxindex,index,globalThreads
	 *            
	 *            group Group of trackers/threads to run
	 *            maxindex Max index of threads 
	 *            index Index of threads 
	 *            globalThreads ArrayList of global threads
	 *            
	 *            
	 * 
	 * @return void
	 * 
	 */
	
	
	private void startThreads(HashMap group,int[] maxindex,int[] index,ArrayList globalThreads) throws Exception{
	
		
		ArrayList trackers =(ArrayList) group.get("trackers");
		try{
			for (int i=0;i<trackers.size();i++){ 
				PITrackingDAO track = (PITrackingDAO) trackers.get(i);
				HashMap args = track.getArguments();
				String method = (String)args.get("method");
				if (method.equals("getsession") || method.equals("gettasks") || method.equals("gettasksII") || method.equals("getTotalSessions")){
					Thread t = new Thread(track);
					addThreadstoList(t,index,maxindex,globalThreads);
				}
				
			}
		}catch(Exception e){
			System.out.println("Exception in startThreads ");
		}
		
	}


	
	/**
	 * 
	 * Wait until all threads finish executing.
	 * 
	 * @param threads
	 *            
	 *            threads ArrayList of threads
	 *            
	 * 
	 * @return void 
	 * 
	 */
	
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



}




