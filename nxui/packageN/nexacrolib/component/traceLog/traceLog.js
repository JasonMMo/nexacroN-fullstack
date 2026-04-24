
/****
tobesoft cho.h.h
NRE : nexacro.xml 파일에 traceLog 값 설정  
     <traceDEBUG>boolean:true</traceDEBUG>
     <traceLEVEL>number:2</traceLEVEL>
로그파일 위치 및 파일명
	폴더 : 내문서
	파일명 : nexa_debug.날짜

traceLEVEL
2: 최하위 모드
0: 체크할 함수의 시작도 포함 , 각 컴포넌트의 event log
-1: senddata , receivedata
*****/

nexacro.__bTrace = false;
	
if ( nexacro._Browser == "Runtime" && nexacro._OS == "Windows") {
	nexacro.__bTrace = nexacro.getPrivateProfile("traceDEBUG");
} else if (nexacro._OS == "Windows") {
	var url_param = window.location.search;
	if ( url_param.indexOf("traceDEBUG") >= 0 )	{
		nexacro.__bTrace = true;
	}
}
if ( nexacro.__bTrace ) {
	nexacro.__traceLogInit = function ()
	{
		nexacro.__traceLevel_index = 0;
		nexacro.__traceLogFile = "";
		nexacro.__traceUnitSeperator = "_,_";
		if ( nexacro._Browser == "Runtime") {
			nexacro.__traceLevel = nexacro.getPrivateProfile("traceLEVEL");
			nexacro.__traceLogSize = nexacro.getPrivateProfile("traceLogSize");
			
			if ( nexacro.__traceLogSize == undefined || !nexacro.__traceLogSize )
			{
				nexacro.__traceLogSize = 1024000 * 10;
				nexacro.setPrivateProfile("traceLogSize",nexacro.__traceLogSize);
			}
			if ( nexacro.__traceLevel == undefined || !nexacro.__traceLevel )
			{
				nexacro.__traceLevel = 0;
				nexacro.setPrivateProfile("traceLEVEL",nexacro.__traceLevel);
			}
			var d = new Date(),
			month = ''+(d.getMonth()+1),
			day   = ''+d.getDate(),
			year  = ''+d.getFullYear();

			if(month.length < 2) month = '0'+month;
			if(day.length < 2) day = '0'+day;
			
			var logfile = nexacro.getPrivateProfile("traceLogFile");
			var str = "nexa_debug_"+[year,month,day].join('-');
			if ( logfile && logfile.indexOf(str) >= 0 )
			{
				str = logfile;
			} 
			nexacro.__traceLogFile = str + ".log";
		} else {
			nexacro.__traceLevel = 0;
			nexacro.__traceLogSize = 10000; // traceLog count $%로 시작하는 로그
		}
	}

	nexacro.__traceLogInit();

	nexacro.__traceCurTime = function (curdate)
	{
		if (!curdate)
			curdate = new nexacro.Date();
		var millisec = curdate.getMilliseconds();

		return curdate.toZeroDigitString(curdate.getHours(),2) + ":" + 
		       curdate.toZeroDigitString(curdate.getMinutes(),2) + ":" + 
			   curdate.toZeroDigitString(curdate.getSeconds(),2) + ":" + curdate.toZeroDigitString(millisec, 3);
	}

	var _pEventListener = nexacro.EventListener.prototype;
	var __formobj;

	nexacro.__logRowInit = function(addcnt) {
		var level = addcnt?nexacro.__traceLevel_index+addcnt:nexacro.__traceLevel_index;
		var d = new nexacro.Date(),
			month = d.toZeroDigitString(d.getMonth()+1,2),
			day   = d.toZeroDigitString(d.getDate(),2),
			year  = d.getFullYear();
		
		//if(month.length < 2) month = '0'+month;
		//if(day.length < 2) day = '0'+day;
		
		var tm = [year,month,day].join('-');
		var treestate = level==0?0:1;
		return "$%"+tm +nexacro.__traceUnitSeperator + nexacro.__traceCurTime(d) + nexacro.__traceUnitSeperator + level + nexacro.__traceUnitSeperator + treestate + nexacro.__traceUnitSeperator;
	}

	traceLog = function() {
		var app = this;
		var vf = app["trace_virtualfile"];
		
		var msgs = [];
		var cnt = arguments.length;
		for(var i = 0 ; i < cnt;i++){
			if(arguments[i] == undefined){
				msgs.push("undefined");
			}else if(arguments[i] == null){
				msgs.push("null");
			}else{
				//var str = nexacro.__traceCurTime() + ","+arguments[i];
				msgs.push(arguments[i]);
			}
		}
		
		var msg = msgs.join('')+"\n";
		if (nexacro._Browser == "Runtime") { 
			if (!vf) {
				vf = new nexacro.VirtualFile("trace_virtualfile");
				app["trace_virtualfile"] = vf;
				vf.addEventHandler("onsuccess",function(obj,e) {
					//trace(e.reason + "====" + e.filesize);
					if ( e.reason == 9 && e.filesize > nexacro.__traceLogSize) { 
						obj.close();
						
						var d = new nexacro.Date(),
							month = d.toZeroDigitString(d.getMonth()+1,2),
							day   = d.toZeroDigitString(d.getDate(),2),
							year  = d.getFullYear(),
							hour  = d.toZeroDigitString(d.getHours(),2),
							minut = d.toZeroDigitString(d.getMinutes(),2),
							sec   = d.toZeroDigitString(d.getSeconds(),3);
						
						var tm = [hour,minut,sec].join('');		
						var str = "nexa_debug_"+[year,month,day].join('-')+"-"+tm;
						nexacro.__traceLogFile = str + ".log";
						//trace("logfile : " + nexacro.__traceLogFile);
						obj.open("%MYDOCUMENT%"+nexacro.__traceLogFile,VirtualFile.openWrite|VirtualFile.openAppend|VirtualFile.openText);		
						nexacro.setPrivateProfile("traceLogFile",str);
						obj.write(msg);
						//obj.close();					
					} ;
				},this);
			}
			try{
				vf.open("%MYDOCUMENT%"+nexacro.__traceLogFile,VirtualFile.openWrite|VirtualFile.openAppend|VirtualFile.openText);
				vf.write(msg);
				vf.getFileSize();
				vf.close();
			}catch(e){
			}
		} else {			
			if (!vf) {
				vf = "trace_virtualfile";
				nexacro.__traceLogblobObject = [];
				nexacro.__traceLogCnt = 0;
				app["trace_virtualfile"] = vf;

				var divobj = document.createElement("div");
				divobj.setAttribute("id","nexacro_tracelog_msg");
				divobj.setAttribute("style","position: fixed; z-index: 9999; width:235px; height: 70px;");
				divobj.style.backgroundColor = "green";
				var sHTML = '<html>';
				sHTML += '<head>';
				sHTML += '<meta charset="utf-8">';
				sHTML += '</head>';
				sHTML += '<body>';
				sHTML += '  <input type="button" value="save" style="left:2px; top:3px; width:45px;" onclick="traceLog(\'saveLOG\');" />';
				sHTML += '  <input id="logInfo" type="text" value="logging..." style="left:50px; top:3px; width:180px; height:20px" />';
				sHTML += '  <input id="logsize" type="button" value="<<" style="left:2px; width:45px; top:25px;" />';
				sHTML += '  <input id="logmsg" type="text" value="log count:100 , file count:2" style="left:50px; top:25px; width:180px; height:20px" />';
				sHTML += '  <input id="logconsole" type="button" value="file->console" style="left:2px; width:100px; top:48px;" />';
				sHTML += '</body>';
				sHTML += '</html>';
				divobj.innerHTML = sHTML;
				document.body.appendChild(divobj);
				var btn_logsize = document.getElementById("logsize");
				btn_logsize.onclick = function(){
					                    if ( btn_logsize.value == "<<" ) {
											divobj.style.width = "50px";
											btn_logsize.value = ">>";
										} else {
											divobj.style.width = "235px";
											btn_logsize.value = "<<";
										}
				                      };
				var logconsole = document.getElementById("logconsole");
				logconsole.onclick = function(){
										console.clear();
										//trace = traceLog;
										for ( var i = 0 ; i < nexacro.__traceLogblobObject.length;i++)
											console.log(nexacro.__traceLogblobObject[i]);
										//var strData = nexacro.__traceLogblobObject.join('')+"\n";
										nexacro.__traceLogSize = -1;
										nexacro.__traceLogblobObject.splice(0, nexacro.__traceLogblobObject.length);
										var logmsg = document.getElementById("logmsg");
										logmsg.value = "log count:" + nexacro.__traceLogblobObject.length + " , file count:" + nexacro.__traceLogCnt;
				                     };
			}
			var logmsg = document.getElementById("logmsg");
			if (msgs != "saveLOG" && nexacro.__traceLogSize == -1){
				nexacro.__traceLogblobObject.push(msg);
				logmsg.value = "log count:" + nexacro.__traceLogblobObject.length + " , file count:" + nexacro.__traceLogCnt;
				console.log(msg);
				return;
			}
			if (msgs == "saveLOG" || nexacro.__traceLogblobObject.length > nexacro.__traceLogSize)
			{
				var strData = nexacro.__traceLogblobObject.join('')+"\n";
				nexacro.__traceLogblobObject.splice(0, nexacro.__traceLogblobObject.length);

				var D = document,
					A = arguments,
					a = D.createElement("a"),
					t = "text/plain";

				a.href = "data:" + t + "charset=utf-8," + escape(strData);
				var d = new nexacro.Date(),
					month = d.toZeroDigitString(d.getMonth()+1,2),
					day   = d.toZeroDigitString(d.getDate(),2),
					year  = d.getFullYear(),
					hour  = d.toZeroDigitString(d.getHours(),2),
					minut = d.toZeroDigitString(d.getMinutes(),2),
					sec   = d.toZeroDigitString(d.getSeconds(),3);

				var tm = [hour,minut,sec].join('');		
				var str = "nexa_debug_"+[year,month,day].join('-')+"-"+tm;
				nexacro.__traceLogFile = str + ".log";

				nexacro.__traceLogCnt++;
				if (window.navigator && window.navigator.msSaveOrOpenBlob) {
					var blobObject = new Blob([strData]);
					
					return window.navigator.msSaveBlob(blobObject, nexacro.__traceLogFile);
				} 
				if ('download' in a) { //FF20, CH19
					a.setAttribute("download", nexacro.__traceLogFile);
					D.body.appendChild(a);
					setTimeout(function() {
						var e = D.createEvent("MouseEvents");
						e.initMouseEvent("click", true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
						a.dispatchEvent(e);
						D.body.removeChild(a);
					}, 66);
				}
			}
			if (msgs != "saveLOG") {
				nexacro.__traceLogblobObject.push(msg);
			}
			logmsg.value = "log count:" + nexacro.__traceLogblobObject.length + " , file count:" + nexacro.__traceLogCnt;
		}
	}	
	
	if ( nexacro.__traceLevel < 2 ) {
		_pEventListener._fireEvent = function (obj, evt)
		{
			var i, ret;
			var h;
			var handlers = this._user_handlers;
			var len = handlers.length;

			try
			{
				var retrtl = false;
				if (len > 0 && evt._xposConvertToRtl)
				{ 
					retrtl = evt._xposConvertToRtl(obj, evt.fromreferenceobject);
				}

				for (i = 0; i < len; i++)
				{
					h = handlers[i];
					if (obj._is_alive !== false && obj.enableevent !== false)
					{
						// --> XFDL에 정의된 이벤트 함수 실행 로그
						try {
							var _handle_target = h.target;
							for (var pname in _handle_target) 
							{ 
								var pval = _handle_target[pname]; 
								if (typeof pval == "function" && _handle_target.hasOwnProperty(pname) ) 
								{
									if (pval === h.handler) 
									{ 
										var form_url     = obj._refform._url;
										var func_name    = pname;
										var form_path    = _handle_target._unique_id;
										var form_name    = _handle_target.name;
										var from_obj     = evt.fromobject.id;
										var from_refobj  = evt.fromreferenceobject.id;
										
										traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "dispatch event: " + func_name + nexacro.__traceUnitSeperator + form_name + nexacro.__traceUnitSeperator +" (in " + form_url + ")" +
										"$$            path:" + form_path + " (name: " + form_name + ")" +
										"$$            from:" + from_obj + " (ref: " + from_refobj + ")");
										break; 
									} 
								}
							}
						}catch(e){
						}
						// --> 끝
						ret = h.handler.call(h.target, obj, evt);
					}
					if (evt)
					{
						this.defaultprevented = evt._prevented;
						this.stoppropagation = evt._stoppropagation;
					}
				}

				if (retrtl)
				{ 
					evt._xposConvertToLtr(obj);
				}
			}
			catch (e)
			{
				if (e.obj)
				{
					nexacro._onSystemError(e.obj, e.name, e.message);
				}
				else
				{
					var msg = nexacro._getExceptionMessage(e);

					var environment = nexacro.getEnvironment();
					if (environment)
					{
						nexacro._onSystemError(environment, e.name, msg);
					}
				}
			}

			handlers = this._sys_handlers;
			len = handlers.length;

			for (i = 0; i < len; i++)
			{
				h = handlers[i];
				if (obj._is_alive !== false && obj.enableevent !== false)
					ret = h.handler.call(h.target, obj, evt);
				if (evt)
				{
					this.defaultprevented = evt._prevented;
					this.stoppropagation = evt._stoppropagation;
				}
			}

			return ret;
		};

		_pEventListener._fireUserEvent = function (obj, evt)
		{
			var i, ret;
			var h;
			var handlers = this._user_handlers;
			var len = handlers.length;

			try
			{
				if (len > 0 && evt._xposConvertToRtl)
				{ 
					evt._xposConvertToRtl(obj, evt.fromreferenceobject);
				}

				for (i = 0; i < len; i++)
				{
					h = handlers[i];

					if (obj.enableevent !== false)
					{
						// --> XFDL에 정의된 이벤트 함수 실행 로그
						try {
							var _handle_target = h.target;
							for (var pname in _handle_target) 
							{ 
								var pval = _handle_target[pname]; 
								if (typeof pval == "function" && _handle_target.hasOwnProperty(pname) ) 
								{
									if (pval === h.handler) 
									{ 
										var form_url     = obj._refform._url;
										var func_name    = pname;
										var form_path    = _handle_target._unique_id;
										var form_name    = _handle_target.name;
										var from_obj     = evt.fromobject.id;
										var from_refobj  = evt.fromreferenceobject.id;
										
										traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "dispatch event: " + func_name + nexacro.__traceUnitSeperator + form_name + nexacro.__traceUnitSeperator +" (in " + form_url + ")"+
										"$$            path:" + form_path + " (name: " + form_name + ")"+
										"$$            from:" + from_obj + " (ref: " + from_refobj + ")");
										break; 
									} 
								}
							}
						}catch(e){}
						// --> 끝
						
						ret = h.handler.call(h.target, obj, evt);
					}
					if (evt)
					{
						this.defaultprevented = evt._prevented;
						this.stoppropagation = evt._stoppropagation;
					}

					if (!obj._is_alive) // event에서 this가 파괴된 경우
						break;
				}
			}
			catch (e)
			{
				if (e.obj)
				{
					nexacro._onSystemError(e.obj, e.name, e.message);
				}
				else
				{
					var msg = nexacro._getExceptionMessage(e);

					var environment = nexacro.getEnvironment();
					if (environment)
					{
						nexacro._onSystemError(environment, e.name, msg);
					}
				}
			}
			return ret;
		};

		_pEventListener._fireCheckEvent = function (obj, evt)
		{
			var i, ret;
			var handlers = this._user_handlers;
			var len = handlers.length;
			var h;

			try
			{
				var retrtl = false;
				if (len > 0 && evt._xposConvertToRtl)
				{ 
					retrtl = evt._xposConvertToRtl(obj, evt.fromreferenceobject);
				}

				for (i = 0; i < len; i++)
				{
					h = handlers[i];

					if (obj.enableevent !== false)
					{
						// --> XFDL에 정의된 이벤트 함수 실행 로그
						try{
							var _handle_target = h.target;
							for (var pname in _handle_target) 
							{ 
								var pval = _handle_target[pname]; 
								if (typeof pval == "function" && _handle_target.hasOwnProperty(pname) ) 
								{
									if (pval === h.handler) 
									{ 
										var form_url     = obj._refform._url;
										var func_name    = pname;
										var form_path    = _handle_target._unique_id;
										var form_name    = _handle_target.name;
										var from_obj     = evt.fromobject.id;
										var from_refobj  = evt.fromreferenceobject.id;
										
										traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "dispatch event: " + func_name + nexacro.__traceUnitSeperator + form_name + nexacro.__traceUnitSeperator + " (in " + form_url + ")"+
										"$$            path:" + form_path + " (name: " + form_name + ")"+
										"$$            from:" + from_obj + " (ref: " + from_refobj + ")");
										break; 
									} 
								}
							}
						}catch(e){}
						// --> 끝
						
						ret = h.handler.call(h.target, obj, evt);
					}
					if (evt)
					{
						this.defaultprevented = evt._prevented;
						this.stoppropagation = evt._stoppropagation;
					}

					if (ret)
						ret = nexacro._toBoolean(ret);

					if (ret != null && (!ret))
						return false;
				}

				if (retrtl)
				{ 
					evt._xposConvertToLtr(obj);
				}
			}
			catch (e)
			{
				if (e.obj)
				{
					nexacro._onSystemError(e.obj, e.name, e.message);
				}
				else
				{
					var msg = nexacro._getExceptionMessage(e);

					var environment = nexacro.getEnvironment();
					if (environment)
					{
						nexacro._onSystemError(environment, e.name, msg);
					}
				}
			}

			handlers = this._sys_handlers;
			len = handlers.length;

			for (i = 0; i < len; i++)
			{
				h = handlers[i];
				if (obj.enableevent)
					ret = h.handler.call(h.target, obj, evt);
				if (evt)
				{
					this.defaultprevented = evt._prevented;
					this.stoppropagation = evt._stoppropagation;
				}
				if (ret != null && (!ret))
					return false;
			}

			return true;
		};
	}

	

	// application 
	//if (context && (context._is_form || context._is_application))
		
	var _pApplication = nexacro.Application;
	var _pTransactionItem = nexacro.TransactionItem.prototype ;
	var _pProgressData = nexacro._ProgressData.prototype;
	var _pDataset = nexacro.Dataset.prototype;
	var _pGrid = nexacro.Grid.prototype;

	var __pLoadManager = nexacro._LoadManager.prototype;
	var __pCommunicationItem = nexacro._CommunicationItem.prototype;


	var _pEnvironment = nexacro.Environment.prototype;
	_pEnvironment._orgOn_fire_onerror = _pEnvironment.on_fire_onerror;
	_pEnvironment.on_fire_onerror = function (obj, errortype, errormsg, errorobj, statuscode, requesturi, locationuri) {
		traceLog(nexacro.__logRowInit() + "-999" + nexacro.__traceUnitSeperator + "Environment on_fire_onerror" + nexacro.__traceUnitSeperator + "Error" + nexacro.__traceUnitSeperator + " statuscode : " + statuscode + " $$ requesturi : " + requesturi + " $$ errormsg : " + errormsg );
		return this._orgOn_fire_onerror(obj, errortype, errormsg, errorobj, statuscode, requesturi, locationuri);
	}
	
	nexacro._orgOnSystemError = nexacro._onSystemError;
	nexacro._onSystemError = function (obj, errortype, errormsg) {
		traceLog(nexacro.__logRowInit() + "-999" + nexacro.__traceUnitSeperator + "nexacro _onSystemError" + nexacro.__traceUnitSeperator + "Error" + nexacro.__traceUnitSeperator + " errortype : " + errortype + " $$ errormsg : " + errormsg );
		nexacro._orgOnSystemError(obj, errortype, errormsg);
	}
	
	if ( nexacro.__traceLevel < 2 ) {
		nexacro._loadJSModule_org = nexacro._loadJSModule;
		nexacro._loadJSModule = function (path, target, handler, bcache, service, async) {
			//nexacro.__traceLevel_index += 1;
			if ( nexacro.__traceLevel < 1 ) traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "nexacro _loadJSModule" + nexacro.__traceUnitSeperator + path + nexacro.__traceUnitSeperator + "path : " + path);
			this._loadJSModule_org (path, target, handler, bcache, service, async);
		}

		nexacro._loadJSText_org = nexacro._loadJSText;
		nexacro._loadJSText = function (path, target, handler, service, async) {
			//nexacro.__traceLevel_index += 1;
			if ( nexacro.__traceLevel < 1 ) traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "nexacro _loadJSText"+ nexacro.__traceUnitSeperator + path + nexacro.__traceUnitSeperator + "path : " + path);
			this._loadJSText_org (path, target, handler, service, async);
		}

		nexacro._loadCSSModule_org = nexacro._loadCSSModule;
		nexacro._loadCSSModule = function (path, target, handler, bcache, service, async, cssreq, cssfiletype) {
			//nexacro.__traceLevel_index += 1;
			if ( nexacro.__traceLevel < 1 ) traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "nexacro _loadCSSModule" + nexacro.__traceUnitSeperator + path + nexacro.__traceUnitSeperator + "path : " + path);
			this._loadCSSModule_org (path, target, handler, bcache, service, async, cssreq, cssfiletype);
		}
		
	}

/****
	nexacro._loadADL_org = nexacro._loadADL;
	nexacro._loadADL = function (project_path) {
		nexacro.__traceLevel_index = 0;
//		traceLog(nexacro.__logRowInit() + ",nexacro _loadADL start,"+project_path);
		this._loadADL_org(project_path);
		nexacro.__traceLevel_index = 0;
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "nexacro _loadADL end" + nexacro.__traceUnitSeperator + "project path : " + project_path);	
	}

	_pApplication.loadADL_org = _pApplication.loadADL;
	_pApplication.loadADL = function (url, key) {
		nexacro.__traceLevel_index = 0;
		var s = new nexacro.Date();
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "start Application loadADL" + nexacro.__traceUnitSeperator + "key:" + key + " $$ url:" + url );
		nexacro.__traceLevel_index++;
		this.loadADL_org (url, key);
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		nexacro.__traceLevel_index = 0;
		traceLog(nexacro.__logRowInit() + totaltime  + nexacro.__traceUnitSeperator + "end Application loadADL" + nexacro.__traceUnitSeperator + "key:" + key + " $$ url:" + url );
	}
***/
	_pApplication.orgOn_fire_onload = _pApplication.on_fire_onload;
	_pApplication.on_fire_onload = function (obj, url) {
		var s = new nexacro.Date();
		var lvl = nexacro.__traceLevel_index;
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Application on_fire_onload start" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url);
		nexacro.__traceLevel_index++;
		var rtn = this.orgOn_fire_onload(obj, url);	
		nexacro.__traceLevel_index = lvl;
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "Application on_fire_onload end" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url);
		return rtn;
	}
	_pApplication.on_fire_onbeforeexit_org = _pApplication.on_fire_onbeforeexit;
	_pApplication.on_fire_onbeforeexit = function (obj) {
		if (nexacro._Browser != "Runtime" && nexacro.__traceLogblobObject.length > 2) { 
			alert("save log 버튼을 클릭하세요.");
			return false;
		}
		traceLog(nexacro.__logRowInit() + "-888" + nexacro.__traceUnitSeperator + "Application on_fire_onbeforeexit" + nexacro.__traceUnitSeperator + "exit"+ nexacro.__traceUnitSeperator+"");
		return this.on_fire_onbeforeexit_org(obj);
	}	
	_pApplication._originalOn_fire_onexit = _pApplication.on_fire_onexit;
	_pApplication.on_fire_onexit = function (obj) {
		traceLog(nexacro.__logRowInit() + "-888" + nexacro.__traceUnitSeperator + "Application on_fire_onexit" + nexacro.__traceUnitSeperator + "exit" + nexacro.__traceUnitSeperator+"");
		return this._originalOn_fire_onexit(obj);
	}	
	_pApplication._loadModules_org = _pApplication._loadModules;
	_pApplication._loadModules = function (modules){
		if ( nexacro.__traceLevel < 1 ) traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Application _loadModules" + nexacro.__traceUnitSeperator + nexacro._component_uri + nexacro.__traceUnitSeperator + "modules : " + modules.length);
		this._loadModules_org (modules);
	}

	_pApplication._loadInclude_org = _pApplication._loadInclude;
	_pApplication._loadInclude = function (mainurl, url, asyncmode, service_url) {
		if ( nexacro.__traceLevel < 1 ) traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Application _loadInclude" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url);
		this._loadInclude_org (mainurl, url, asyncmode, service_url);
	}

	_pApplication._loadTheme_org = _pApplication._loadTheme;
	_pApplication._loadTheme = function (themeid) {
		traceLog(nexacro.__logRowInit() + "-111" + nexacro.__traceUnitSeperator + "Application _loadTheme" + nexacro.__traceUnitSeperator + themeid + nexacro.__traceUnitSeperator + "themeid : " + themeid);
		this._loadTheme_org (themeid);
	}

	_pApplication._on_init_org = _pApplication._on_init;
	_pApplication._on_init = function () {
		nexacro.__traceLevel_index = 0;
		var s = new nexacro.Date();
		traceLog(nexacro.__logRowInit() + "-222" + nexacro.__traceUnitSeperator + "Application _on_init start" + nexacro.__traceUnitSeperator + this.key + nexacro.__traceUnitSeperator + "application _on_init" );
		nexacro.__traceLevel_index++;
		this._on_init_org ();
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		nexacro.__traceLevel_index = 0;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "Application _on_init end" + nexacro.__traceUnitSeperator + this.key + nexacro.__traceUnitSeperator + "application _on_init");
	}

	_pApplication._on_load_org = _pApplication._on_load;
	_pApplication._on_load = function (obj, url) {
		nexacro.__traceLevel_index = 0;
		var s = new nexacro.Date();
		traceLog(nexacro.__logRowInit() + "-777" + nexacro.__traceUnitSeperator + "Application _on_load start" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url);
		nexacro.__traceLevel_index++;
		this._on_load_org (obj,url);
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		nexacro.__traceLevel_index = 0;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "Application _on_load end" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url);
	}

	_pApplication.transaction_org = _pApplication.transaction;
	_pApplication.transaction = function (id, url, inDatasetsParam, outDatasetsParam, argsParam, callbackFn, isAsync, datatype, isCompress) {
		nexacro.__traceLevel_index += 1;
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "start transaction" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url + " $$ type : " + datatype + " $$ async : " + isAsync + " $$ callback : " + callbackFn);
		this.transaction_org(id, url, inDatasetsParam, outDatasetsParam, argsParam, callbackFn, isAsync, datatype, isCompress);
		//traceLog(nexacro.__logRowInit() + "[transaction] end transaction " + url + ", type : " + datatype + ", async : " + isAsync + ", callback : " + callbackFn);
	}

	__pLoadManager.loadMainModule_org = __pLoadManager.loadMainModule;
	__pLoadManager.loadMainModule = function (url, cache, async, reload, service) {
		var loglvl = nexacro.__traceLevel_index;
		//else nexacro.__traceLevel_index += 1;
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager loadMainModule" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url + " $$ async : " + async + " $$ reload : " + reload);
		nexacro.__traceLevel_index++;
		this.loadMainModule_org(url, cache, async, reload, service);
		nexacro.__traceLevel_index = loglvl;
	}


	nexacro.__bindLoadDataHandler_org = nexacro.__bindLoadDataHandler;
	nexacro.__bindLoadDataHandler = function (_ajax, pthis) {
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Transaction bindLoadDataHandler[3] "+ nexacro.__traceUnitSeperator + "" + nexacro.__traceUnitSeperator + "");
		return nexacro.__bindLoadDataHandler_org(_ajax, pthis);
	}

	if ( nexacro.__traceLevel < 2 ) {
		__pLoadManager.loadIncludeModule_org = __pLoadManager.loadIncludeModule;
		__pLoadManager.loadIncludeModule = function (url, cache, async, service) {
			var loglvl = nexacro.__traceLevel_index;
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager loadIncludeModule"+ nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url + " $$ async : " + async);
			nexacro.__traceLevel_index++;
			this.loadIncludeModule_org(url, cache, async, service);
			nexacro.__traceLevel_index = loglvl;
		}

		__pLoadManager.loadCssModule_org = __pLoadManager.loadCssModule;
		__pLoadManager.loadCssModule = function (url, cache, async, service, csstype, cssreq) {
			var loglvl = nexacro.__traceLevel_index;
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager loadCssModule" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url + " $$ async : " + async);
			nexacro.__traceLevel_index++;
			this.loadCssModule_org(url, cache, async, service, csstype, cssreq);
			nexacro.__traceLevel_index = loglvl;
		}

		__pLoadManager.loadJSModule_org = __pLoadManager.loadJSModule;
		__pLoadManager.loadJSModule = function (url, cache, async, service) {
			var loglvl = nexacro.__traceLevel_index;
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager loadJSModule" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url +" $$ async : " + async);
			nexacro.__traceLevel_index++;
			this.loadJSModule_org(url, cache, async, service);
			nexacro.__traceLevel_index = loglvl;
		}

		__pLoadManager.loadPreloadJSModule_org = __pLoadManager.loadPreloadJSModule;
		__pLoadManager.loadPreloadJSModule = function (url, target, cache, async, service) {
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager loadPreloadJSModule" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url +" $$ async : " + async);
			this.loadPreloadJSModule_org(url, target, cache, async, service);
		}


		__pLoadManager.reloadCssModule_org = __pLoadManager.reloadCssModule;
		__pLoadManager.reloadCssModule = function (url, cache, async, service, csstype, cssreq) {
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager reloadCssModule" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url +" $$ async : " + async);
			this.reloadCssModule_org(url, cache, async, service, csstype, cssreq);
		}
		
		__pLoadManager.on_load_globalmodule_org = __pLoadManager.on_load_globalmodule;
		__pLoadManager.on_load_globalmodule = function (url, errstatus, jstext, fireerrorcode, returncode, requesturi, locationuri, extramsg) {
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager on_load_globalmodule" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url + " $$ jstext : " + jstext);
			this.on_load_globalmodule_org(url, errstatus, jstext, fireerrorcode, returncode, requesturi, locationuri, extramsg);
		}

		__pLoadManager.on_load_updatemodule_org = __pLoadManager.on_load_updatemodule;
		__pLoadManager.on_load_updatemodule = function (url, errstatus, binarymodule, fireerrorcode, returncode, requesturi, locationuri, extramsg) {
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager on_load_updatemodule" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url );
			this.on_load_updatemodule_org(url, errstatus, binarymodule, fireerrorcode, returncode, requesturi, locationuri, extramsg);
		}

	}

	__pLoadManager.on_load_localmodule_org = __pLoadManager.on_load_localmodule;
	__pLoadManager.on_load_localmodule = function (url, errstatus, module, fireerrorcode, returncode, requesturi, locationuri, extramsg) {
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager on_load_localmodule" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url + " $$ errstatus : " + errstatus);
		this.on_load_localmodule_org(url, errstatus, module, fireerrorcode, returncode, requesturi, locationuri, extramsg);
	}

	__pLoadManager._check_fire_oninit_org = __pLoadManager._check_fire_oninit;
	__pLoadManager._check_fire_oninit = function () {
		this._check_fire_oninit_org();
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager _check_fire_oninit" + nexacro.__traceUnitSeperator + this.main_url + nexacro.__traceUnitSeperator + "url : " + this.main_url);
	}

	__pLoadManager._check_fire_onload_org = __pLoadManager._check_fire_onload;
	__pLoadManager._check_fire_onload = function () {
		this._check_fire_onload_org();
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager _check_fire_onload" + nexacro.__traceUnitSeperator + this.main_url + nexacro.__traceUnitSeperator + "mainurl : " + this.main_url + " $$ status: " + this.status);
	}

	__pLoadManager.loadDataModule_org = __pLoadManager.loadDataModule;
	__pLoadManager.loadDataModule = function (url, svcid, indatasets, outdatasets, parameters, callback, async, datatype, compress, service) {
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager loadDataModule" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url +" $$ svcid : " +svcid  + "$$ async : " + async + " $$ callback : " + callback);
		this.loadDataModule_org(url, svcid, indatasets, outdatasets, parameters, callback, async, datatype, compress, service)
	}

	__pLoadManager.on_load_main_org = __pLoadManager.on_load_main;
	__pLoadManager.on_load_main = function (url, errstatus, module, fireerrorcode, returncode, requesturi, locationuri, extramsg) {
		var loglvl = nexacro.__traceLevel_index;
		var s = new nexacro.Date();
		
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "LoadManager on_load_main start" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + " url : " + url + " $$ main_url : " + this.main_url);
		nexacro.__traceLevel_index++;	
		this.on_load_main_org(url, errstatus, module, fireerrorcode, returncode, requesturi, locationuri, extramsg);
		
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		
		nexacro.__traceLevel_index = loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "LoadManager on_load_main end" + nexacro.__traceUnitSeperator + url + nexacro.__traceUnitSeperator + "url : " + url );
	}
	__pCommunicationItem.on_load_module_org = __pCommunicationItem.on_load_module;
	__pCommunicationItem.on_load_module = function (data, cookie, last_modified) {
		nexacro.__traceLevel_index = 0;
		//traceLog(nexacro.__logRowInit() + " start : " + this.path);
		var loglvl = nexacro.__traceLevel_index;
		var s = new nexacro.Date();
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "start CommunicationItem on_load_module" + nexacro.__traceUnitSeperator + this.path + nexacro.__traceUnitSeperator + "path :" + this.path + " $$ cookie : " + cookie);
		//if ( nexacro.__traceLevel < 0 ) {
		//	traceLog(nexacro.__logRowInit(1) + nexacro.__traceUnitSeperator + "CommunicationItem on_load_module data" + nexacro.__traceUnitSeperator + data );
		//}		
		nexacro.__traceLevel_index++;
		this.on_load_module_org(data, cookie, last_modified);
		
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);

		nexacro.__traceLevel_index = loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "end CommunicationItem on_load_module" + nexacro.__traceUnitSeperator + this.path + nexacro.__traceUnitSeperator + "path : " + this.path + " $$ cookie : " + cookie);
	}
	
	__pCommunicationItem.on_load_text_org = __pCommunicationItem.on_load_text;
	__pCommunicationItem.on_load_text = function (data, cookie, last_modified) {
		this.on_load_text_org(data, cookie, last_modified);
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "CommunicationItem on_load_text" + nexacro.__traceUnitSeperator + this.path + nexacro.__traceUnitSeperator + "path : " + this.path);
	}
	
	__pCommunicationItem.on_load_image_org = __pCommunicationItem.on_load_image;
	__pCommunicationItem.on_load_image = function (width, height) {
		this.on_load_image_org(width, height);
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "CommunicationItem on_load_image" + nexacro.__traceUnitSeperator + this.path + nexacro.__traceUnitSeperator + "path : " + this.path);
	}
	
	__pCommunicationItem.on_load_update_org = __pCommunicationItem.on_load_update;
	__pCommunicationItem.on_load_update = function (data, cookie) {
		this.on_load_update_org(data, cookie);
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "CommunicationItem on_load_update" + nexacro.__traceUnitSeperator + this.path + nexacro.__traceUnitSeperator + "path : " + this.path);
	}
	
	__pCommunicationItem.on_load_data_org = __pCommunicationItem.on_load_data;
	__pCommunicationItem.on_load_data = function (data, cookie) {
		this.on_load_data_org(data, cookie);
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "CommunicationItem on_load_data" + nexacro.__traceUnitSeperator + this.path + nexacro.__traceUnitSeperator + "path : " + this.path);
	}
	
	__pCommunicationItem.on_load_dataobject_org = __pCommunicationItem.on_load_dataobject;
	__pCommunicationItem.on_load_dataobject = function (data, cookie) {
		this.on_load_dataobject_org(data, cookie);
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "CommunicationItem on_load_dataobject" + nexacro.__traceUnitSeperator + this.path + nexacro.__traceUnitSeperator + "path : " + this.path);
	}

	// frame
	var _pFrame = nexacro.Frame.prototype;
	var _pMainFrame = nexacro.MainFrame.prototype;
	var _pChildFrame = nexacro.ChildFrame.prototype;
	var _pFrameSetBase = nexacro.FrameSetBase.prototype;


	_pMainFrame.on_created_contents_org = _pMainFrame.on_created_contents;
	_pMainFrame.on_created_contents = function (win) {
		var loglvl = nexacro.__traceLevel_index;
		var s = new nexacro.Date();

		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "MainFrame on_created_contents start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " + this.name);
		nexacro.__traceLevel_index++;
		this.on_created_contents_org(win);
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);		

		nexacro.__traceLevel_index = loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "MainFrame on_created_contents end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " + this.name);
	}

	_pChildFrame.on_created_contents_org = _pChildFrame.on_created_contents;
	_pChildFrame.on_created_contents = function (win) {
		var loglvl = nexacro.__traceLevel_index;
		var s = new nexacro.Date();

		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ChildFrame on_created_contents start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " + this.name);
		this.on_created_contents_org(win);
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		nexacro.__traceLevel_index = loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "ChildFrame on_created_contents end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " + this.name);
	}

	_pChildFrame.showModal_org = _pChildFrame.showModal;
	_pChildFrame.showModal = function (str_id, _parent_frame, arr_arg, opener, callback, is_async)
	{
		var loglvl = nexacro.__traceLevel_index;
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ChildFrame showModal" + nexacro.__traceUnitSeperator + str_id + nexacro.__traceUnitSeperator + "id : " + str_id);
		nexacro.__traceLevel_index++;
		this.showModal_org(str_id, _parent_frame, arr_arg, opener, callback, is_async);
		nexacro.__traceLevel_index=loglvl;
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ChildFrame showModal" + nexacro.__traceUnitSeperator + str_id + nexacro.__traceUnitSeperator + "id : " + str_id + " pos : " + this.left +" $$ " + this.top +" $$ " + this.width +" $$ " + this.height );
		
	}
	
	_pFrameSetBase.on_created_contents_org = _pFrameSetBase.on_created_contents;
	_pFrameSetBase.on_created_contents = function (win) {
		var loglvl = nexacro.__traceLevel_index;
		var s = new nexacro.Date();

		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "FrameSetBase on_created_contents start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " + this.name);
		nexacro.__traceLevel_index++;
		this.on_created_contents_org(win);
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		
		nexacro.__traceLevel_index=loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "FrameSetBase on_created_contents end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " + this.name);
	}

	_pFrame.on_created_org = _pFrame.on_created;
	_pFrame.on_created = function (_window) {
		var loglvl = nexacro.__traceLevel_index;
		var s = new nexacro.Date();
		
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Frame on_created start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " + this.name);
		
		nexacro.__traceLevel_index++;
		this.on_created_org(_window);
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		nexacro.__traceLevel_index = loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "Frame on_created end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " + this.name);
	}


	// form 
	var _pFormBase = nexacro.FormBase.prototype;
	var _pForm = nexacro.Form.prototype;
	var _pInnerForm = nexacro._InnerForm.prototype;
		
	_pFormBase.__getFullName = function ( obj ) {
		if ( obj.parent == undefined ) return obj.name;
		
		var s = obj.name;
		var pObj = obj.parent;
		
		while(1) {
			if ( pObj.parent != undefined ) {
				s = pObj.name+"." + s ;
			} else break;
			pObj = pObj.parent;
		}
		return s;
	}

	_pFormBase.on_created_contents_org = _pFormBase.on_created_contents;
	_pFormBase.on_created_contents = function (win) {
		
		var loglvl = nexacro.__traceLevel_index;
		var s = new nexacro.Date();

		var formname = _pFormBase.__getFullName(this);
		traceLog(nexacro.__logRowInit() +nexacro.__traceUnitSeperator + "FORMBASE created start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " +formname);
		//traceLog(nexacro.__logRowInit() + "[FORM created start] on_created_contents : " +  formname + " , count:" + this.objects.length);
		nexacro.__traceLevel_index++;
		this.on_created_contents_org(win);
		
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);

		nexacro.__traceLevel_index = loglvl;

		//traceLog(nexacro.__logRowInit() + "[FORM created end] on_created_contents : " +  formname + " , count:" + this.objects.length );
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "FORMBASE created end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " + formname);
	}
	_pFormBase._on_init_org = _pFormBase._on_init;
	_pFormBase._on_init = function () {
		
		//if ( nexacro._Browser == "Runtime" ) nexacro.__getDebugMode();
		
		var formname = _pFormBase.__getFullName(this);

		if ( nexacro.__traceLevel < 1 ) traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "FORMBASE _on_init start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " +  formname + " $$ count:bind-" + this.binds.length + " $$ comp-" + this.components.length + " $$ obj-" + this.objects.length + " $$ layout-" + this._layout_list.length);
		this._on_init_org();
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "FORMBASE _on_init end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " +  formname + " $$ count:bind-" + this.binds.length + " $$ comp-" + this.components.length + " $$ obj-" + this.objects.length + " $$ layout-" + this._layout_list.length);
	}
	_pFormBase.loadForm_org = _pFormBase.loadForm;
	_pFormBase.loadForm = function (formurl, async, reload, baseurl) {
		var s = new nexacro.Date();
		var loglvl = nexacro.__traceLevel_index;
		
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "FORMBASE loadForm start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "url : " + formurl);
		
		nexacro.__traceLevel_index++;
		this.loadForm_org(formurl, async, reload, baseurl);
		
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		nexacro.__traceLevel_index = loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "FORMBASE loadForm end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "url : " + formurl);
		
	}
	_pInnerForm._originalLoadForm = _pInnerForm.loadForm;
	_pInnerForm.loadForm = function (formurl, async, reload, baseurl) {
		var s = new nexacro.Date();
		var loglvl = nexacro.__traceLevel_index;
		
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "InnerForm loadForm start" + nexacro.__traceUnitSeperator + formurl + nexacro.__traceUnitSeperator + "url : " + formurl + " $$ baseurl : " + baseurl);
		
		nexacro.__traceLevel_index++;
		var rtn = this._originalLoadForm(formurl, async, reload, baseurl);
		
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		nexacro.__traceLevel_index = loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "InnerForm loadForm end" + nexacro.__traceUnitSeperator + formurl + nexacro.__traceUnitSeperator + "url : " + formurl + " $$ baseurl : " + baseurl);
		return rtn;
	};

	_pForm._on_load_org = _pForm._on_load;
	_pForm._on_load = function (obj, url) {
		var s = new nexacro.Date();
		var loglvl = nexacro.__traceLevel_index;
		var formname = _pFormBase.__getFullName(this);

		__formobj = this;
		if ( nexacro.__traceLevel < 1 ) traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "FORM _on_load start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "url: " + this.getOwnerFrame().formurl+"  $$ name : " +  formname );
		nexacro.__traceLevel_index++;
		this._on_load_org(obj, url);
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		
		nexacro.__traceLevel_index = loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "FORM _on_load end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "url: " + this.getOwnerFrame().formurl + " $$ name : " +  formname + " $$ title : " + this.titletext );
	}

	_pForm.on_fire_oninit_org = _pForm.on_fire_oninit;
	_pForm.on_fire_oninit = function (obj) {
		//if ( nexacro.__traceLevel_index == 0)
		//	nexacro.__traceLevel_index = 1;
		
		var formname = _pFormBase.__getFullName(this);

//		if ( nexacro.__traceLevel < 1 ) traceLog(nexacro.__logRowInit() + "[FORM on_fire_oninit start] on_fire_oninit : " +  formname );
		this.on_fire_oninit_org(obj);
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "FORM on_fire_oninit" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name : " +  formname );
	}

	_pForm.on_fire_onload_org = _pForm.on_fire_onload;
	_pForm.on_fire_onload = function (obj, url) {
		var formname = _pFormBase.__getFullName(this);
		
		__formobj = this;
		
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "FORM on_fire_onload start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name:" +  formname + nexacro.__traceUnitSeperator + " $$ url : " + url);
		this.on_fire_onload_org(obj, url);
		
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "FORM on_fire_onload end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "name:" +  formname + nexacro.__traceUnitSeperator + " $$ url : " + url);
	}

	_pForm.transaction_org = _pForm.transaction;
	_pForm.transaction = function (id, url, inDatasetsParam, outDatasetsParam, argsParam, callbackFn, isAsync, datatype, isCompress) {
		var formname = _pFormBase.__getFullName(this);

		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "FORM transaction start" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "url : " + url  + " $$ name : " + formname + " $$ type : " + datatype + " $$ async : " + isAsync + " $$ callback : " + callbackFn );
		this.transaction_org(id, url, inDatasetsParam, outDatasetsParam, argsParam, callbackFn, isAsync, datatype, isCompress);
		//traceLog(nexacro.__logRowInit() + "[FORM TR] end form transaction : " + url + " , type : " + datatype + " , async : " + isAsync + " , callback : " + callbackFn);
	}

	// etc
	_pTransactionItem._originalOn_start = _pTransactionItem.on_start;
	_pTransactionItem.on_start = function () {
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Transaction on_start[1]" + nexacro.__traceUnitSeperator + this.path + nexacro.__traceUnitSeperator + "url : " + this.path + " $$ time : " + nexacro.__traceCurTime());
		return this._originalOn_start();
	};
	nexacro.__startCommunication_org = nexacro.__startCommunication;
	nexacro.__startCommunication = function (loadItem, path, cachelevel, async, referer, senddata, ndatatype, compress, ver, failpass, service) {
		var httpretry = (loadItem._httpretry !== undefined) ? loadItem._httpretry : nexacro._httpretry;
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Transaction startCommunication[2]" + nexacro.__traceUnitSeperator + path + nexacro.__traceUnitSeperator + "url : " + path + " $$ type : " + loadItem.type + " $$ retry:" + httpretry + " $$ timeout:"+ nexacro._httptimeout+"] $$ async : " + async);
		
		if ( nexacro.__traceLevel < 0 )  {
				traceLog(nexacro.__logRowInit(1) + nexacro.__traceUnitSeperator + "Transaction startCommunication[2] send data" + nexacro.__traceUnitSeperator + "DATA" + nexacro.__traceUnitSeperator + senddata);
		}
		return nexacro.__startCommunication_org(loadItem, path, cachelevel, async, referer, senddata, ndatatype, compress, ver, failpass, service);
	}

	_pTransactionItem.on_load_data_org = _pTransactionItem.on_load_data;
	_pTransactionItem.on_load_data = function (data, cookie, last_modified) {
		var loglvl = nexacro.__traceLevel_index;
		var s = new nexacro.Date();
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Transaction on_load_data[4] start" + nexacro.__traceUnitSeperator + this.svcid + nexacro.__traceUnitSeperator + "svcid : " + this.svcid + " $$ cookie : " + cookie + " $$ path: " + this.path + " $$ data._type_name:" + data._type_name);
		nexacro.__traceLevel_index++;
		if ( nexacro.__traceLevel < 0 )  {
				traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Transaction on_load_data[4] RecvData" + nexacro.__traceUnitSeperator + "DATA" + nexacro.__traceUnitSeperator + data);
		}	
		this.on_load_data_org(data, cookie, last_modified);

		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		nexacro.__traceLevel_index = loglvl;
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "Transaction on_load_data[4] end" + nexacro.__traceUnitSeperator + this.svcid + nexacro.__traceUnitSeperator + "svcid : " + this.svcid + " $$ cookie : " + cookie + " $$ path: " + this.path + " $$ time : " + nexacro.__traceCurTime());
	}

	_pTransactionItem.on_progress_data_org = _pTransactionItem.on_progress_data;
	_pTransactionItem.on_progress_data = function (data, bFinal) {
		var loglvl = nexacro.__traceLevel_index;
		var s = new nexacro.Date();
		if ( !bFinal )
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Transaction on_progress_data[3-1] Final false" + nexacro.__traceUnitSeperator + this.svcid + nexacro.__traceUnitSeperator + "svcid : " + this.svcid + " $$ " + data);
		else
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Transaction on_progress_data[4-1] Final true" + nexacro.__traceUnitSeperator + this.svcid + nexacro.__traceUnitSeperator + "svcid : " + this.svcid );
		nexacro.__traceLevel_index++;
		this.on_progress_data_org(data, bFinal);
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		nexacro.__traceLevel_index = loglvl;
		if ( bFinal )
			traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "Transaction on_progress_data[4-1] end" + nexacro.__traceUnitSeperator + this.svcid + nexacro.__traceUnitSeperator + "svcid : " + this.svcid + " $$ " + data);
	}
	
	_pTransactionItem._originalDeserializeData = _pTransactionItem._deserializeData;
	_pTransactionItem._deserializeData = function (strRecvData, bPending) {
		var rtn = this._originalDeserializeData(strRecvData, bPending);
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Transaction _deserializeData[5]" + nexacro.__traceUnitSeperator + "DATA" + nexacro.__traceUnitSeperator + strRecvData);
		return rtn;
	};
	
	var _pProgressData = nexacro._ProgressData.prototype;
	_pProgressData._on_fire_onload_org = _pProgressData._on_fire_onload;
	_pProgressData._on_fire_onload = function (data, bufferObj, nLoadType) {
		var errorcode = this._error_info[0];
		var errormsg = this._error_info[1];
		var ds = bufferObj._target_ds?bufferObj._target_ds.name:"";
		
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ProgressData _on_fire_onload start" + nexacro.__traceUnitSeperator + ds + nexacro.__traceUnitSeperator + "dataset : " + ds + " $$ errcode : " + errorcode + " $$ errmsg : " + errormsg);
		this._on_fire_onload_org(data, bufferObj, nLoadType);
		traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ProgressData _on_fire_onload end" + nexacro.__traceUnitSeperator + ds + nexacro.__traceUnitSeperator + "dataset : " + ds );
	}
/*
	if ( nexacro.__traceLevel < 2 ) {
		
		_pProgressData._on_progress_org = _pProgressData._on_progress;
		_pProgressData._on_progress = function (data, bFinal) {
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ProgressData _on_progress" + nexacro.__traceUnitSeperator + "final : " + bFinal);
			this._on_progress_org(data, bFinal);
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ProgressData _on_progress" + nexacro.__traceUnitSeperator + " length : " + this._received_data_length);			
			if ( nexacro.__traceLevel < 0 && bFinal )  {
				traceLog(nexacro.__logRowInit(1) + nexacro.__traceUnitSeperator + "ProgressData data" + nexacro.__traceUnitSeperator + data);
			}
		}

		if (nexacro._Browser != "Runtime") 
		{
			_pProgressDataXML._on_fire_onload_org = _pProgressDataXML._on_fire_onload;
			_pProgressDataXML._on_fire_onload = function (data, bufferObj, nLoadType) {
				if ( nexacro.__traceLevel < 1 )  {
					traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ProgressDataXML _on_fire_onload start" + nexacro.__traceUnitSeperator + "nLoadType : " + nLoadType);
					//traceLog(nexacro.__logRowInit() + ",ProgressDataXML data," + data);
				}
				this._on_fire_onload_org(data, bufferObj, nLoadType);
				traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ProgressDataXML _on_fire_onload end" + nexacro.__traceUnitSeperator + "nLoadType : " + nLoadType);
			}
		}

		_pProgressData._on_fire_onload_org = _pProgressData._on_fire_onload;
		_pProgressData._on_fire_onload = function (data, bufferObj, nLoadType) {
			
			if ( nexacro.__traceLevel < 1 )  {
				traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ProgressData _on_fire_onload start" + nexacro.__traceUnitSeperator + "nLoadType : " + nLoadType);
				//traceLog(nexacro.__logRowInit() + ",ProgressData data," + data);
			}
			this._on_fire_onload_org(data, bufferObj, nLoadType);
			traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "ProgressData _on_fire_onload end" + nexacro.__traceUnitSeperator + "nLoadType : " + nLoadType);
		}
	}
*/
	_pDataset.on_fire_onload_org = _pDataset.on_fire_onload;
	_pDataset.on_fire_onload = function (errcode, errmsg, reason, progressload) {
		this.on_fire_onload_org(errcode, errmsg, reason, progressload);
		if ( nexacro.__traceLevel < 1 )  traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Dataset on_fire_onload " + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + " $$ reason : " + reason);
	}

	_pGrid.on_dsnotify_onload_org = _pGrid.on_dsnotify_onload;
	_pGrid.on_dsnotify_onload = function (obj, e) {
		var s = new nexacro.Date();
		
		if ( nexacro.__traceLevel < 1 )  traceLog(nexacro.__logRowInit() + nexacro.__traceUnitSeperator + "Grid on_dsnotify_onload start" + nexacro.__traceUnitSeperator + obj.name + nexacro.__traceUnitSeperator + "name : " + obj.name);
		
		this.on_dsnotify_onload_org(obj, e);
		var e = new nexacro.Date();
		var totaltime = nexacro.round((e - s) / 1000, 3);
		traceLog(nexacro.__logRowInit() + totaltime + nexacro.__traceUnitSeperator + "Grid on_dsnotify_onload end" + nexacro.__traceUnitSeperator + this.name + nexacro.__traceUnitSeperator + "grid : " + this.name);
	}

}	

