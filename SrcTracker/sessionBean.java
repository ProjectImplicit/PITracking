package org.uva.tracker;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 
 *  
 * 
 * Holds information about a session
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



public class sessionBean {
	
	String sessionID;
	String creationDate;
	String studyName;
	String status="NS";
	ArrayList<HashMap> tasks=new ArrayList();
	
	
	
	public void sessionBean(String id,String date,String name){
		this.sessionID = id;
		this.creationDate = date;
		this.studyName = name;
		
	}
	public void sessionBean(){
		
	}
	public boolean tasksExist(){
		if (tasks!=null) return true;
		return false;
		
	}
	public void setDate(String d){
		
		creationDate =d;
	}
	public void setID(String id){
		sessionID =id;
	}
	public void setTasks(ArrayList t){
		tasks =t;
	}
	public String getDate(){
		return creationDate;
	}
	public ArrayList getTasks(){
		return tasks;
	}
	public void setName(String name){
		if (name==null){
			this.studyName="null";
		}else{
			this.studyName=name;
			
		}
		
	}
	public void setStatus(String s){
		if (s==null){
			this.status="null";
			
		}else{
			this.status=s;
			
		}
		
	}
	public String getStatus(){
		return this.status;
	}
	public String getName(){
		return this.studyName;
	}
	public String getId(){
		return this.sessionID;
	}

}
