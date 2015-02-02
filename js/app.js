

//(function (window) {
var csv;
var csvName;
var submit;
var resultCVS;
var dbChoise;
var currentChoise;
var lastTaskVal ='';
var lastStudyVal ='';
var test = 'newwarehouse';
var ctrlMode = false; 
var method='3';
var refresh ='no';
var threads='yes';

var curl='http://app-dev-01.implicit.harvard.edu/implicit/research/library/randomStudiesConfig/RandomStudiesConfig.xml';
var hurl='http://app-dev-01.implicit.harvard.edu/implicit/research/library/randomStudiesConfig/HistoryRand.xml';
var cpath='research/library/randomStudiesConfig/RandomStudiesConfig.xml';
var hpath='research/library/randomStudiesConfig/HistoryRand.xml';
var baseURL='http://app-dev-01.implicit.harvard.edu/implicit';
var threadsNum='3';
var down=true;
var pihistory = [];
var historyLimit =5;
var historyIndex=0;
var hostoryCurrent=0;

// window.PITRACKER= PITRACKER;
//} (window));

/////////Functions/////////

$( document ).ready(function() {

	$('#historyRight').on('click',function(){

		if (historyCurrent===historyIndex) return;
		historyCurrent++;
		var historyObj = pihistory[historyCurrent-1];
		var csv = historyObj.csv;
		var data = historyObj.data;
		if (data.db==='Research'){
			$(document).find('#dbButton').html('<span class="ui-button-text">Research <i class="icon-caret-down" ></i>');
		}
		if (data.db==='Demo'){
			$(document).find('#dbButton').html('<span class="ui-button-text">Demo <i class="icon-caret-down" ></i>');
			$(document).find('#buttonStudies').html('<span class="ui-button-text">Any <i class="icon-caret-down" ></i>');
			dbChoise='demo';
			$("#buttonStudies").button("option", "disabled", true);
		}
		if (data.current==='Both'){
			$(document).find('#dbButton').html('<span class="ui-button-text">Both <i class="icon-caret-down" ></i>');
			$(document).find('#buttonStudies').html('<span class="ui-button-text">Any <i class="icon-caret-down" ></i>');
			dbChoise = 'both';
			$("#buttonStudies").button("option", "disabled", true);
			$('#dataC').prop('checked', true);
		}
		if (data.current==='Current'){
			$(document).find('#buttonStudies').html('<span class="ui-button-text">Current <i class="icon-caret-down" ></i>');
		}
		if (data.current==='History'){
			$(document).find('#buttonStudies').html('<span class="ui-button-text">History <i class="icon-caret-down" ></i>');
		}
		if (data.current==='Any'){
			$(document).find('#buttonStudies').html('<span class="ui-button-text">Any <i class="icon-caret-down" ></i>');
		}
		if (data.studyc === 'true'){
			$('#studyC').prop('checked', true);
		}else{
			$('#studyC').prop('checked', false);
		}
		if (data.taskc === 'true'){
			$('#taskC').prop('checked', true);
		}else{
			$('#taskC').prop('checked', false);
		}
		if (data.datac=== 'true'){
			$('#dataC').prop('checked', true);
		}else{
			$('#dataC').prop('checked', false);
		}
		if (data.timec=== 'true'){
			$('#timeC').prop('checked', true);
			$('#timeTable').fadeIn();
		}else{
			$('#timeC').prop('checked', false);
			$('#timeTable').fadeOut();
		}
		if (data.dayc ==='true'){
			$('#dayC').prop('checked', true);
		}else{
			$('#dayC').prop('checked', false);
		}
		if (data.weekc=== 'true'){
			$('#weekC').prop('checked', true);
		}else{
			$('#weekC').prop('checked', false);
		}
		if (data.monthc=== 'true'){
			$('#monthC').prop('checked', true);
		}else{
			$('#monthC').prop('checked', false);
		}
		if (data.yearc ==='true'){
			$('#yearC').prop('checked', true);
		}else{
			$('#yearC').prop('checked', false);
		}
		if (data.zero ==='true'){
			$('#zero').prop('checked', true);
		}else{
			$('#zero').prop('checked', false);
		}
		
		$(document).find('#studyI').val(data.study);
		$(document).find('#taskI').val(data.task);
		$(document).find('#sinceI').val(data.since);
		$(document).find('#untilI').val(data.until);
		$(document).find('#completedI').val(data.endTask);
		$(document).find('#filterI').val(data.filter);
		setTable(csv);
		resultCVS= csv;
	});
	$('#historyLeft').on('click',function(){
		if (historyCurrent===1) return;
		historyCurrent--;
		var historyObj = pihistory[historyCurrent-1];
		var csv = historyObj.csv;
		var data = historyObj.data;
		if (data.db==='Research'){
			$(document).find('#dbButton').html('<span class="ui-button-text">Research <i class="icon-caret-down" ></i>');
		}
		if (data.db==='Demo'){
			$(document).find('#dbButton').html('<span class="ui-button-text">Demo <i class="icon-caret-down" ></i>');
			$(document).find('#buttonStudies').html('<span class="ui-button-text">Any <i class="icon-caret-down" ></i>');
			dbChoise='demo';
			$("#buttonStudies").button("option", "disabled", true);
		}
		if (data.current==='Both'){
			$(document).find('#dbButton').html('<span class="ui-button-text">Both <i class="icon-caret-down" ></i>');
			$(document).find('#buttonStudies').html('<span class="ui-button-text">Any <i class="icon-caret-down" ></i>');
			dbChoise = 'both';
			$("#buttonStudies").button("option", "disabled", true);
			$('#dataC').prop('checked', true);
		}
		if (data.current==='Current'){
			$(document).find('#buttonStudies').html('<span class="ui-button-text">Current <i class="icon-caret-down" ></i>');
			$("#buttonStudies").button("option", "disabled", false);
		}
		if (data.current==='History'){
			$(document).find('#buttonStudies').html('<span class="ui-button-text">History <i class="icon-caret-down" ></i>');
			$("#buttonStudies").button("option", "disabled", false);
		}
		if (data.current==='Any'){
			$(document).find('#buttonStudies').html('<span class="ui-button-text">Any <i class="icon-caret-down" ></i>');
		}
		if (data.studyc === 'true'){
			$('#studyC').prop('checked', true);
		}else{
			$('#studyC').prop('checked', false);
		}
		if (data.taskc === 'true'){
			$('#taskC').prop('checked', true);
		}else{
			$('#taskC').prop('checked', false);
		}
		if (data.datac=== 'true'){
			$('#dataC').prop('checked', true);
		}else{
			$('#dataC').prop('checked', false);
		}
		if (data.timec=== 'true'){
			$('#timeC').prop('checked', true);
			$('#timeTable').fadeIn();
		}else{
			$('#timeC').prop('checked', false);
			$('#timeTable').fadeOut();

		}
		if (data.dayc ==='true'){
			$('#dayC').prop('checked', true);
		}else{
			$('#dayC').prop('checked', false);
		}
		if (data.weekc=== 'true'){
			$('#weekC').prop('checked', true);
		}else{
			$('#weekC').prop('checked', false);
		}
		if (data.monthc=== 'true'){
			$('#monthC').prop('checked', true);
		}else{
			$('#monthC').prop('checked', false);
		}
		if (data.yearc ==='true'){
			$('#yearC').prop('checked', true);
		}else{
			$('#yearC').prop('checked', false);
		}
		if (data.zero ==='true'){
			$('#zero').prop('checked', true);
		}else{
			$('#zero').prop('checked', false);
		}
		$(document).find('#studyI').val(data.study);
		$(document).find('#taskI').val(data.task);
		$(document).find('#sinceI').val(data.since);
		$(document).find('#untilI').val(data.until);
		$(document).find('#completedI').val(data.endTask);
		$(document).find('#filterI').val(data.filter);
		setTable(csv);
		resultCVS= csv;
	});
	// eventSource.addEventListener('progress', function(event) {
 //        $('.progress-bar').css('width', event.data+'%').attr('aria-valuenow', event.data);
	// }, false);
	//Init////////////////////////
	$("#sinceIcon").click(function() { 
  		$("#sinceI").datepicker( "show" );
  	});
  	
  	$("#untilIcon").click(function() { 
  		$("#untilI").datepicker( "show" );
  	});
  	//////////////////////////////////////////
	$('button').button();
	//////////////////////////////////////////
	$('#studyC').prop('checked', true);
	//$("#sinceI").datepicker({defaultDate: '01/01/1985'});
	$("#sinceI").datepicker();
	$("#untilI").datepicker();
	$('#timeTable').hide();
	//$("#buttonStudies").button("option", "disabled", true);
	//$('#alert').css("display", "none");
	// $( "#historyLeft" ).tooltip({
	//     position: {
	//         my: "center top+15",
	//         at: "center bottom",
	//         using: function( position, feedback ) {
	//             $( this ).css( position );
	//             $( "&lt;div&gt;" )
	//             .addClass( "arrow top" )
	//             .addClass( feedback.vertical )
	//             .addClass( feedback.horizontal )
	//             .appendTo( this );
	//         }
	//     }
	// });
	$("#dialog").dialog({
	    autoOpen: false,
	    width: 600,
	    position: 'center',
	    modal: true,
	    buttons: {
	        'Cancel request' : function () {
	            $(this).dialog("close");
	        },
	        'Continue with the request' : function () {
	        	$(this).dialog("close");	
	        	$('#CSVTable').html("");
				$("#gif").css("display", "block");
				$('#alert').css("display", "none");
				var data = getData();
				postForm(data);
	        }
	        
	    }
	});
	$("#dialog2").dialog({
	    autoOpen: false,
	    width: 600,
	    position: 'center',
	    modal: true,
	    buttons: {
	        'Ok' : function () {
	            $(this).dialog("close");
	        }
	    }
	});

	$("#downloadDialog").dialog({
	    autoOpen: false,
	    width: 600,
	    position: "top", 
	    modal: true,
	    buttons: {
	        'Ok' : function () {
	        	csvName = $('#downloadI').val();
	        	var blob = new Blob([resultCVS], {type: "text/plain;charset=utf-8"});
        		saveAs(blob,csvName+'.csv');
	            $(this).dialog("close");
	        },
	        'Cancel' : function () {
	        	$(this).dialog("close");

	        }
	    }
	});

	$("#cpanel").dialog({
	    autoOpen: false,
	    width: 600,
	    position: 'center',
	    modal: true,
	    buttons: {
	        'Ok' : function () {
	        	//alert($('#provider').val());
	        	
	        	/////////
	        	if ( $('#provider').val()==='path') method='1';
				if ( $('#provider').val()==='back') method='2';
				if ( $('#provider').val()==='url') method='3';
				refresh = $('#refresh').val();
				test = $('#connect').val();
				threads = $('#threads').val();
				curl = $('#curl').val();
				hurl = $('#hurl').val();
				cpath = $('#cpath').val();
				hpath = $('#hpath').val();
				threadsNum = $('#threadsNum').val();
				baseURL = $('#baseURL').val();
				console.log('method: '+method+' connect: '+test+' refresh: '+refresh+' curl: '+curl+' hurl: '+hurl+' cpath: '+cpath+' hpath: '+hpath+' threads: '+threads+ 'threadsNum '+threadsNum);
	            $(this).dialog("close");
	        }
	    }
	});

	$("#dialog3").dialog({
	    autoOpen: false,
	    width: 600,
	    position: 'center',
	    modal: true,
	    buttons: {
	        'Ok' : function () {
	            $(this).dialog("close");
	        }
	    }
	});
	$('#refresh').val(refresh);
	if (method==='1') $('#provider').val('path');
	if (method==='2') $('#provider').val('back');
	if (method==='3') $('#provider').val('url');
	if (test==='newwarehouse') $('#connect').val('newwarehouse');
	$('#curl').val(curl);
	$('#hurl').val(hurl);
	$('#cpath').val(cpath);
	$('#hpath').val(hpath);
	$('#threadsNum').val(threadsNum);
	$('#baseURL').val(baseURL);
////////////////////////
 
    ///this works
    $(document).keydown(function(e){
	    if(e.ctrlKey){
	        ctrlMode = true;
	    };
    });

    $(document).keyup(function(e){
    	ctrlMode = false;
    });

	$(document).keydown(function(e){
		 if (e.which==77){

		 	if (ctrlMode===true){
		 		$('#cpanel').dialog('open');
		 	}
		 }
	});
	
	$('#filterI').on('keyup',function(){

		var val = $('#studyI').val();
		if (lastStudyVal==='' ){

			$('#studyC').prop('checked',true);
		 }
		 lastStudyVal = val;



	});
	// $('#taskI').on('keyup',function(){
		
	// 	var val = $('#taskI').val();
	// 	if (lastTaskVal==='' ){

	// 		$('#taskC').prop('checked',true);
	// 	 }
	// 	 lastTaskVal = val;
		
	// });




	 $('#timeC').on('click',function(){
		 if ($('#timeC').is(":checked")){
		 	$('#monthC').prop('checked', true);
		 	$('#weekC').prop('checked', false);
		 	$('#dayC').prop('checked', false);
		 	$('#yearC').prop('checked', false);
			$('#timeTable').fadeIn();
			
		 }else{
		 	
		 	$('#timeTable').fadeOut();
		 }
		
	 });
	$(document).find('#currentLi').on('click',function(){
		
		$(document).find('#buttonStudies').html('<span class="ui-button-text">Current <i class="icon-caret-down" ></i>');
		currentChoise='current;'

	});
	$(document).find('#HistoryLi').on('click',function(){
		
		$(document).find('#buttonStudies').html('<span class="ui-button-text">History <i class="icon-caret-down" ></i>');
		currentChoise = 'history';

	});
	$(document).find('#AllLi').on('click',function(){
		
		$(document).find('#buttonStudies').html('<span class="ui-button-text">Any <i class="icon-caret-down" ></i>');
		currentChoise = 'all';

	});
	$(document).find('#DemoLi').on('click',function(){
		
		$(document).find('#dbButton').html('<span class="ui-button-text">Demo <i class="icon-caret-down" ></i>');
		$(document).find('#buttonStudies').html('<span class="ui-button-text">Any <i class="icon-caret-down" ></i>');
		dbChoise='demo';
		$("#buttonStudies").button("option", "disabled", true);

	});
	$(document).find('#ResearchLi').on('click',function(){
		
		$(document).find('#dbButton').html('<span class="ui-button-text">Research <i class="icon-caret-down" ></i>');
		$(document).find('#buttonStudies').html('<span class="ui-button-text">Current <i class="icon-caret-down" ></i>');
		dbChoise = 'research';
		$("#buttonStudies").button("option", "disabled", false);
		
		

	});
	$(document).find('#BothLi').on('click',function(){
		
		$(document).find('#dbButton').html('<span class="ui-button-text">Both <i class="icon-caret-down" ></i>');
		$(document).find('#buttonStudies').html('<span class="ui-button-text">Any <i class="icon-caret-down" ></i>');
		dbChoise = 'both';
		$("#buttonStudies").button("option", "disabled", true);
		$('#dataC').prop('checked', true);


	});
	$('#dayC').on('click', function(){
		if ($('#dayC').is(":checked")){
			$('#weekC').prop('checked', false);
			$('#monthC').prop('checked', false);
			$('#yearC').prop('checked', false);
		}
	});
	$('#weekC').on('click', function(){
		if ($('#weekC').is(":checked")){
			$('#dayC').prop('checked', false);
			$('#monthC').prop('checked', false);
			$('#yearC').prop('checked', false);
		}
	});
	$('#monthC').on('click', function(){
		if ($('#monthC').is(":checked")){
			$('#weekC').prop('checked', false);
			$('#dayC').prop('checked', false);
			$('#yearC').prop('checked', false);
		}
	});
	$('#yearC').on('click', function(){
		if ($('#yearC').is(":checked")){
			$('#weekC').prop('checked', false);
			$('#monthC').prop('checked', false);
			$('#dayC').prop('checked', false);
		}
	});

	$('#collapse').on('click', function(){
		
		if (down==true){
        $("#instruct1").slideDown("slow");
        //$("#collapse span span").text('Show');

        down=false;

      }else{
        $("#instruct1").slideUp("slow");
        down=true;
        //$("#collapse span span").text('Hide');

      }

	});

	
	download = function(){
		window.scrollTo(0,0);
		$('#downloadDialog').dialog('open');
	}
  	
	
	getData = function(){

		var data = {};
		var curr = $(document).find('#buttonStudies').html();
		var start = curr.indexOf('">');
		var index = curr.indexOf('<i');
		curr = curr.substr(start+2,index-start-3);
		var db = $(document).find('#dbButton').html();
		start = db.indexOf('">');
		var index2 = db.indexOf('<i');
		db = db.substr(start+2,index2-start-3);
		data.current = curr;
		data.db = db;
		data.study = $(document).find('#studyI').val();
		data.task = $(document).find('#taskI').val();
		data.since = $(document).find('#sinceI').val();
		data.until = $(document).find('#untilI').val();
		data.endTask=$(document).find('#completedI').val();
		data.filter = $(document).find('#filterI').val();

		if ($('#studyC').is(":checked")){
			data.studyc = 'true';
		}else{
			data.studyc = '';
		}
		if ($('#taskC').is(":checked")){
			data.taskc = 'true';
		}else{
			data.taskc = '';
		}
		if ($('#dataC').is(":checked")){
			data.datac = 'true';
		}else{
			data.datac = '';
		}
		if ($('#timeC').is(":checked")){
			data.timec = 'true';
		}else{
			data.timec = '';
		}
		if ($('#dayC').is(":checked")){
			data.dayc = 'true';
		}else{
			data.dayc = '';
		}
		if ($('#weekC').is(":checked")){
			data.weekc = 'true';
		} else{
			data.weekc = '';
		}
		if ($('#monthC').is(":checked")){
			data.monthc = 'true';
		}else{
			data.monthc = '';
		}
		if ($('#yearC').is(":checked")){
			data.yearc = 'true';
		}else{
			data.yearc = '';

		}
		if ($('#zero').is(":checked")){
			data.zero = 'true';
		}else{
			data.zero = '';

		}
		data.testDB = test;
		data.method = method;
		data.refresh =refresh;
		data.curl=curl;
		data.hurl=hurl;
		data.cpath=cpath;
		data.hpath=hpath;
		data.tasksM='3';
		data.threads = threads;
		data.threadsNum = threadsNum;
		data.baseURL = baseURL;
		return data;

	}

	handleZero = function(csv){

		var AnelysedCSV='';
		if( ($('#zero').is(':checked')) ){
		
			 var lines =csv.split("\n");
			 var commas = lines[0].split(",");
			 var index;
			  for (var i=0;i<commas.length;i++){
			  	if (commas[i]==='Started'){
			  		index=i;
			  	}
			  }
			  for(var i = lines.length - 1; i >= 0; i--) {
			  	var line = lines[i];
			  	var lineCommas = line.split(",");
			  	var started  = lineCommas[index];
			  	if (started==='0'){
			  		lines.splice(i,1);
					
			  	}
			  }
			
			for (var i=0;i<lines.length;i++){
				AnelysedCSV += lines[i]+'\n';
			}
			return AnelysedCSV;
		}else{
			return csv;
		}
		
	}
	setHistory = function(csv,data){
		var historyObj={};
		historyObj.csv = csv;
		historyObj.data = data;
		pihistory.push(historyObj);
		historyIndex++;
		historyCurrent=historyIndex;

	}
	postForm = function(Reqdata){

		$.post( '/implicit/PITracking', JSON.stringify(Reqdata))
		.done(function( data ) {
			
		  	resultCVS = data;
		  	resultCVS = handleZero(resultCVS);
		  	setHistory(resultCVS,Reqdata);
		  	if (resultCVS!="\r\n" || resultCVS===undefined || resultCVS===null || resultCVS===''||resultCVS==':logger:'){
		  		$('#result').val(data);
			    $("#gif").css("display", "none");
				setTable(resultCVS);
		  	}else{
		  		$("#gif").css("display", "none");
		  		$('#alert').css("display", "block");

		  	}
		  	
		});

	}
	setTable = function(resultCVS){
		$('#CSVTable').CSVToTable(resultCVS,{
 				tableClass:'tablesorter'
    			}).bind("loadComplete",function() { 
 				$('#CSVTable').find('#cvsT').addClass('tablesorter');
 				$('#CSVTable').find('table').tablesorter();
	 	});
	}
	isEmpty = function (value){
		if (value==='') return true;
		if (value===undefined) return true;
		if (value===null) return true;
		return false;
	}
	submit= function(){
		
		var studyI = $(document).find('#studyI').val();
		var taskI  = $(document).find('#taskI').val();
		var sinceI = $(document).find('#sinceI').val();
		var untilI = $(document).find('#untilI').val();
		var curr = $(document).find('#buttonStudies').html();
		var start = curr.indexOf('">');
		var index = curr.indexOf('<i');
		curr = curr.substr(start+2,index-start-3);
		if(Date.parse(untilI)<Date.parse(sinceI)){ 
			alert('Please enter correct dates');
			return;
		}
		if (untilI=='') {
			var currentdate = new Date();
			var datetime =  (currentdate.getMonth()+1)+"/"+currentdate.getDate()+"/"+currentdate.getFullYear();
			untilI= datetime;
			$(document).find('#untilI').val(untilI);
		}
		if (sinceI==''){
			 var currentdate = new Date(); 
			 var month = currentdate.getMonth();
			 var year = currentdate.getFullYear();
			 if (month===0){
			 	month=12;
			 	year=year-1;
			 }
			 var datetime =  month+"/01/"+year;
			 sinceI = datetime;
			 $(document).find('#sinceI').val(sinceI);
		}
		if(isEmpty(studyI) && curr=='Any'){
			$('#dialog').dialog('open');
		}else{
			$('#CSVTable').html("");
			$("#gif").css("display", "block");
			$('#alert').css("display", "none");
			var data = getData();
			postForm(data);

		}

	}

});


