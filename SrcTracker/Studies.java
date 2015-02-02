package org.uva.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.implicit.random.RandomStudy;
import org.implicit.random.RandomStudyConfigReader;
import org.implicit.random.RandomStudyManager;
import org.uva.Implicit;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.yale.its.util.ChainedException;








public class Studies {
	
	private ArrayList currentStudiesID;
	ArrayList HistoryStudiesID;
	String randomStudyConfigURL;
	String HistoryStudyConfigURL;
	String randomStudyConfigPath;
	String HistoryStudyConfigPath;
	Calendar currentStudYDateStamp = null;
	Calendar currentHistoryDateStamp = null;
	int METHOD = 1;// the method to use to get studies: 1: from path, 2: using URl, 3: using manager class
	int DaysDiff=1;
	static int PATH=1;
	static int BACKEND=2;
	static int URL=3;
	static String BASEURL = "http://dw2.psyc.virginia.edu/implicit";
	
	private static Studies instance = null;
	
	protected Studies() {
	      // Exists only to defeat instantiation.
		currentStudiesID = new ArrayList();
		HistoryStudiesID = new ArrayList();
		randomStudyConfigURL="";
		HistoryStudyConfigURL="";
		randomStudyConfigPath="";
		HistoryStudyConfigPath="";
		
	}
    public synchronized static Studies getInstance() {
    
    	if(instance == null) {
    		instance = new Studies();
    	}
    	return instance;
   }
   
   
    public void setDaysDiff(int cach){
    	DaysDiff = cach;
    }
    public void setBaseURL(String base){
    	BASEURL = base;
    }
    public void setDiff(int i){
    	DaysDiff =i;
    }
    public void setConfigURL(String url){
    	this.randomStudyConfigURL = url;
    }
    public void setMethod(int m){
    	this.METHOD =m;
    }
    public void setHistoryURL(String url){
    	this.HistoryStudyConfigURL = url;
    }
    public void setConfigPath(String path){
    	this.randomStudyConfigPath = path;
    }
    public void setHistoryPath(String path){
    	this.HistoryStudyConfigPath = path;
    }
    public ArrayList getCurrentStudies(){
    	ArrayList studies = new ArrayList();
    	for (int i=0;i<currentStudiesID.size();i++){
    		HashMap h = (HashMap) currentStudiesID.get(i);
    		String id = (String) h.get("id");
    		studies.add(id);
    		
    	}
    	return studies;
    }
    public ArrayList getBothStudies(){
    	
    	ArrayList studies = new ArrayList();
    	for (int i=0;i<currentStudiesID.size();i++){
    		HashMap h = (HashMap) currentStudiesID.get(i);
    		String id = (String) h.get("id");
    		studies.add(id);
    		
    	}
    	
    	for (int i=0;i<HistoryStudiesID.size();i++){
    		HashMap h = (HashMap) HistoryStudiesID.get(i);
    		String id = (String) h.get("id");
    		if (!studies.contains(id)) studies.add(id);
    		
    	}
    	return studies;
    }
	public Studies(String currentURL,String historyURL,String currentPath,String hostoryPath){
		
		this.randomStudyConfigURL =currentURL;
		this.HistoryStudyConfigURL = historyURL;
		this.randomStudyConfigPath= currentPath;
		this.HistoryStudyConfigPath = hostoryPath;
		this.currentStudiesID = new ArrayList<String>(); 
		this.HistoryStudiesID = new ArrayList<String>();
		
		
	}
	private ArrayList getStudiesByURL(String url,String type) throws Exception{
		
		String xml=null;
		String studyName;
		BufferedReader br = null;
		InputStreamReader read = null;
		
		try{
			URL oracle = new URL(url);
			read = new InputStreamReader(oracle.openStream()); 
	        br = new BufferedReader(read);
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        Integer index=1;
	        while (line != null) {
	        	index++;
	        	if (index!=4){//want to ignore dtd line
	        		sb.append(line);
	        		sb.append('\n');
	        	}
	            line = br.readLine();
	        }
	        xml = sb.toString();
	
			
		}catch(Exception e){
			throw new Exception("Error in studies.getStudiesByURL: "+e.getMessage());
		}finally {
	        try {
				if (br!= null) { br.close();}else{throw new Exception("BufferedReader is null in studies.getStudiesByURL");}
				if (read != null) {read.close();}else{throw new Exception("InputReader is null in studies.getStudiesByURL");}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		if (type!="history"){
			return processXML(xml,METHOD,null);
			
		}else{
			return processXML(xml,METHOD,HistoryStudiesID);
		}
	}
	
	private ArrayList processXML(String xml,int method,ArrayList studiesArray) throws Exception{
		
		ArrayList studies;
		if (studiesArray==null){
			studies = new ArrayList();
		}else{
			studies =studiesArray;
		}
		 
		 XPathFactory xpathFactory = XPathFactory.newInstance();
		 XPath xpath = xpathFactory.newXPath();

	    InputSource source = new InputSource(new StringReader(xml));
	    String study;
		try {
			String expression = "/randomStudies/study/studyUrl";
			NodeList nodeList = (NodeList) xpath.compile(expression).evaluate(source, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				study =(nodeList.item(i).getFirstChild().getNodeValue());
				study = study.replace(" ","");
			    String name="";
			    if (method == Studies.PATH){
			    	
			    	if(!containStudy(studies,study)){
			    		name=getStudyName(study);
			    		HashMap studyHash = new HashMap();
					    studyHash.put("url",study );
					    studyHash.put("id",name );
					    if (!studies.contains(name)) studies.add(studyHash);
			    	
			    	}
			    }
			    if (method == Studies.URL){
			    	if(!containStudy(studies,study)){
			    		name=getStudyNameByURL(study);
			    		HashMap studyHash = new HashMap();
					    studyHash.put("url",study );
					    studyHash.put("id",name );
					    if (!studies.contains(name)) studies.add(studyHash);
			    	}
			    	
			    }
			   
			    
			}
			
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("number of history studiys: "+ studies.size());
		return studies;
			
	}
	private boolean containStudy(ArrayList studies,String url){
		
		
		for (int i=0;i<studies.size();i++){
			HashMap study = (HashMap) studies.get(i);
			String studyURL = (String) study.get("url");
			if (url.endsWith(studyURL)){
				return true;
				
			}
			
		}
		
		return false;
		
	}
	private ArrayList processStudies(String path) throws Exception{
		
		String xml=null;
		String studyName;
		BufferedReader br = null;
		ArrayList studies = new ArrayList();
		
	    try {
	    	br = new BufferedReader(new FileReader(path));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        Integer index=1;
	        while (line != null) {
	        	index++;
	        	if (index!=4){//want to ignore dtd line
	        		sb.append(line);
	        		sb.append('\n');
	        	}
	            line = br.readLine();
	        }
	        xml = sb.toString();
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
				if (br!= null) { br.close();}else{throw new Exception("BufferedReader is null in studies.processStudies");}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	   

	    XPathFactory xpathFactory = XPathFactory.newInstance();
	    XPath xpath = xpathFactory.newXPath();

	    InputSource source = new InputSource(new StringReader(xml));
	    String study;
		try {
			String expression = "/randomStudies/study/studyUrl";
			NodeList nodeList = (NodeList) xpath.compile(expression).evaluate(source, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				study =(nodeList.item(i).getFirstChild().getNodeValue());
				study = study.replace(" ","");
			    String name =getStudyName(study);
			    //File f = new File(study);
				//studyName = f.getName();
			    if (!studies.contains(name)) studies.add(name);
			}
			
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return studies;
	}
	
	
	public void readCurrentStudies(Boolean refresh) throws Exception{
		
		switch(METHOD) {
		
		case 1:
			if (currentStudiesID.size()==0 || refresh==true){
				
				currentStudYDateStamp = Calendar.getInstance();
				currentStudiesID = processStudies(randomStudyConfigPath);
			}else{
				Calendar now = Calendar.getInstance();
				int daysDifference = 0;
				Date d1 = currentStudYDateStamp.getTime();
				Date d2 =now.getTime();
				daysDifference=(int)( ( d2.getTime() - d1.getTime() )/ (1000 * 60 * 60 * 24) ) ;
				
				if (daysDifference >=DaysDiff){
					currentStudYDateStamp = Calendar.getInstance();
					currentStudiesID = processStudies(randomStudyConfigPath);
					
				}
				
			}
			break;
		case 2:
			
			if (currentStudiesID.size()==0 || refresh==true){
				
				currentStudYDateStamp = Calendar.getInstance();
				ArrayList list = new ArrayList(getBackendStudies());
				currentStudiesID = list;
				
			}else{
				Calendar now = Calendar.getInstance();
				int daysDifference = 0;
				Date d1 = currentStudYDateStamp.getTime();
				Date d2 =now.getTime();
				daysDifference=(int)( ( d2.getTime() - d1.getTime() )/ (1000 * 60 * 60 * 24) ) ;
				if (daysDifference >=DaysDiff){
					currentStudYDateStamp = Calendar.getInstance();
					ArrayList list = new ArrayList(getBackendStudies());
					currentStudiesID = list;
					
				}
				
			}
			break;
		case 3:
			
			if (currentStudiesID.size()==0 || refresh==true){
				
				currentStudYDateStamp = Calendar.getInstance();
				currentStudiesID = getStudiesByURL(randomStudyConfigURL,"current");
				
			}else{
				Calendar now = Calendar.getInstance();
				int daysDifference = 0;
				Date d1 = currentStudYDateStamp.getTime();
				Date d2 =now.getTime();
				daysDifference=(int)( ( d2.getTime() - d1.getTime() )/ (1000 * 60 * 60 * 24) ) ;
				if (daysDifference >=DaysDiff){
					currentStudYDateStamp = Calendar.getInstance();
					currentStudiesID = getStudiesByURL(randomStudyConfigURL,"current");
					
				}
			
			}
		}
		
		
		
	}
	
	
	
	private static List getBackendStudies() throws Exception{
		
		ArrayList studies = new ArrayList();
		RandomStudyConfigReader reader = new RandomStudyConfigReader();
		RandomStudyManager randomStudyManager = reader.read(
				org.implicit.Implicit.getParameter(org.implicit.Implicit.RANDOM_STUDIES_CONFIG), 0);
		//RandomStudyManager randomStudyManager = reader.read(path.toExternalForm(),9);
		List randomStudies =  randomStudyManager.getRandomStudies();
		for (int i=0;i<randomStudies.size();i++){
			RandomStudy obj =  (RandomStudy) randomStudies.get(i);
			String name = obj.getName();
			studies.add(name);
		}
		
		return studies;
	}
	
	
	public void readHistoryStudies(boolean refresh) throws Exception{
		
		
		
		switch(METHOD) {
		
		case 1:
			if (HistoryStudiesID.size()==0 || refresh==true){
				
				currentHistoryDateStamp = Calendar.getInstance();
				HistoryStudiesID = processStudies(HistoryStudyConfigPath);
			}else{
				Calendar now = Calendar.getInstance();
				int daysDifference = 0;
				Date d1 = currentHistoryDateStamp.getTime();
				Date d2 =now.getTime();
				daysDifference=(int)( ( d2.getTime() - d1.getTime() )/ (1000 * 60 * 60 * 24) ) ;
				
				if (daysDifference >=DaysDiff){
					currentHistoryDateStamp = Calendar.getInstance();
					HistoryStudiesID = processStudies(HistoryStudyConfigPath);
					
				}
				
			}
			break;
		
		case 3:
			
			if (HistoryStudiesID.size()==0 || refresh==true){
				
				currentHistoryDateStamp = Calendar.getInstance();
				HistoryStudiesID = getStudiesByURL(HistoryStudyConfigURL,"history");
				
			}else{
				HistoryStudiesID = getStudiesByURL(HistoryStudyConfigURL,"history");
			}
		}
		
	}

	private String getStudyNameByURL(String url) throws Exception{
		
		
		String name="";
		String xml=null;
		boolean exit=false;
		String fullUrl = BASEURL + url;
		BufferedReader br = null;
		InputStreamReader read = null;
			
		try{
			URL oracle = new URL(fullUrl);
			read = new InputStreamReader(oracle.openStream()); 
	        br = new BufferedReader(read);
		}catch(Exception e){
			return "";
		}
		
		try{
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			Integer index=1;
	        while (line != null && !exit) {
	        	if (line.contains("<Study")){
	        		int startindex = line.indexOf("\"", 0);
	    	        int endIndex = line.indexOf("\"", startindex+1);
	    	        name = line.substring(startindex+1, endIndex);
	    	        exit=true;
	           	}
	        	line = br.readLine();
	        }
	        
	    } catch (IOException e) {
			
			return name;
		} finally {
	        try {
				if (br!= null) { br.close();}else{throw new Exception("BufferedReader is null in studies.getStudyNameByURL");}
				if (read != null) {read.close();}else{throw new Exception("InputReader is null in studies.getStudyNameByURL");}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 
		return name;
	
	}
	private String getStudyName(String url){
		
		String name="";
		String xml=null;
		boolean exit=false;
		String fullUrl = Implicit.REALPATH + url;
		BufferedReader br = null;
		try {
			
			br = new BufferedReader(new FileReader(fullUrl));
		}catch(Exception e){
			return "";
		}
		try{	
			StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        Integer index=1;
	        while (line != null && !exit) {
	        	if (line.contains("<Study")){
	        		int startindex = line.indexOf("\"", 0);
	    	        int endIndex = line.indexOf("\"", startindex+1);
	    	        name = line.substring(startindex+1, endIndex);
	    	        exit=true;
	           	}
	        	line = br.readLine();
	        }
	        
	    } catch (IOException e) {
			// TODO Auto-generated catch block
	    	//logger.debug(e.getMessage());
			//e.printStackTrace();
	    	//throw e;
			return name;
		} finally {
	        try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    
		return name;
	}
	
	
	public static  ArrayList filterCurrentStudies(String filter){
		return null;
		
	}
	public static  ArrayList filterHistoryStudies(String filter){
		return null;
		
	}

}
