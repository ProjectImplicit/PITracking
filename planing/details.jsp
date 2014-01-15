<%@ page import="java.io.*, java.util.*" %>
<% response.setHeader("p3p","CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\""); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <title></title>
    <style type="text/css">
      <!--
        body {
            color: #000000;
            background-color: #FFFFFF;
            font-family: Arial, "Times New Roman", Times;
            font-size: 16px;
        }

        A:link {
            color: blue
        }

        A:visited {
            color: blue
        }

        td {
            color: #000000;
            font-family: Arial, "Times New Roman", Times;
            font-size: 16px;
        }

        .code {
            color: #000000;
            font-family: "Courier New", Courier;
            font-size: 16px;
        }
      -->
    </style>
</head>

<body>

<%
String user = request.getParameter("user");
%>
<center>
Detailed study statistics since June 1, 2010 for user <%=user%>:
<pre><%!
class StreamGobbler extends Thread
{
    InputStream is;
    String type;     
    JspWriter out ;

    StreamGobbler( InputStream is, String type, JspWriter out )     {
        this.is = is;
        this.type = type;
        this.out = out ;
    }     

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                out.println(type + "" + line);    
            } catch (IOException ioe)
              {
                ioe.printStackTrace();  
              }
    }
} 
%><%!
    public boolean execWait(String comm, JspWriter out ){
        try{
            Process proc = Runtime.getRuntime().exec(comm);
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", out );
            
            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "", out );
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            int returnVal = proc.waitFor();
            if (returnVal != 0) {
                return(false);
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
%><%

String execute="successful";

if ((user!=null) && (user.length()>2)) {
   execWait("/usr/lib/oracle/xe/dev2/grepbreakup.sh "+user,out);
 } else execute="failure";
%></pre>

Outcome is <%=execute%><br>
</center>
</body>
</html>

grepbreakup.sh

#!/bin/bash
# first, strip underscores this part is zapped
first=$1
CLEAN=${first/_/_/}
# next, replace spaces with underscores
CLEAN=${CLEAN// /_}
# now, clean out anything that's not alphanumeric or an underscore
CLEAN=${CLEAN//[^.a-zA-Z0-9_]/}
if [ ${#CLEAN} -gt 2 ]
then
 grep -i $CLEAN /home/dev2users/dev2/research/data/detailedbreakup.txt
else 
 echo "Bad username <br/>"
fi


breakup.sql->detailedbreakup.txt
set pagesize 10000;
set linesize 200;
set wrap off;
column study_name format a45;
column task_id format a25;
select yuiat_sessions_V.study_name||CHR(9)||yuiat_session_tasks_V.task_id||CHR(9)||count(yuiat_session_tasks_V.task_id) as record
from yuiat_sessions_V, yuiat_session_tasks_V 
where yuiat_sessions_V.session_id = yuiat_session_tasks_V.session_id and yuiat_sessions_V.session_date>'01-JUN-10'
group by yuiat_sessions_V.study_name, yuiat_session_tasks_V.task_id
order by upper(yuiat_sessions_V.study_name), count(yuiat_session_tasks_V.task_id) desc ;
quit;
