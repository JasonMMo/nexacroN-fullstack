(function()
{
    return function()
    {
        if (!this._is_form)
            return;
        
        var obj = null;
        
        this.on_create = function()
        {
            this.set_name("traceLog");
            this.set_titletext("New Form");
            if (Form == this.constructor)
            {
                this._setFormPosition(1260,850);
            }
            
            // Object(Dataset, ExcelExportObject) Initialize
            obj = new Dataset("ds_log", this);
            obj._setContents("<ColumnInfo><Column id=\"ymd\" type=\"STRING\" size=\"256\"/><Column id=\"time\" type=\"STRING\" size=\"256\"/><Column id=\"level\" type=\"STRING\" size=\"256\"/><Column id=\"treestate\" type=\"STRING\" size=\"256\"/><Column id=\"total_time\" type=\"FLOAT\" size=\"256\"/><Column id=\"title\" type=\"STRING\" size=\"256\"/><Column id=\"msg\" type=\"STRING\" size=\"256\"/><Column id=\"rownum\" type=\"INT\" size=\"256\"/><Column id=\"checktime\" type=\"FLOAT\" size=\"256\"/></ColumnInfo><Rows><Row><Col id=\"msg\">1</Col><Col id=\"level\">0</Col><Col id=\"treestate\">0</Col><Col id=\"title\">nexacro _onSystemError</Col><Col id=\"total_time\">-999</Col></Row><Row><Col id=\"msg\">2</Col><Col id=\"level\">0</Col><Col id=\"treestate\">1</Col><Col id=\"title\">Application _loadTheme</Col><Col id=\"total_time\">-111</Col></Row><Row><Col id=\"msg\">3</Col><Col id=\"level\">0</Col><Col id=\"treestate\">0</Col><Col id=\"title\">Application _on_init start</Col><Col id=\"total_time\">-222</Col></Row><Row><Col id=\"msg\">4</Col><Col id=\"level\">0</Col><Col id=\"treestate\">1</Col><Col id=\"title\">Application _on_load start</Col><Col id=\"total_time\">-777</Col></Row><Row><Col id=\"msg\">5</Col><Col id=\"level\">0</Col><Col id=\"treestate\">1</Col><Col id=\"title\">Application on_fire_onexit</Col><Col id=\"total_time\">-888</Col></Row></Rows>");
            this.addChild(obj.name, obj);


            obj = new VirtualFile("vf_log", this);
            this.addChild(obj.name, obj);


            obj = new FileDialog("fd_log", this);
            this.addChild(obj.name, obj);
            
            // UI Components Initialize
            obj = new Button("btn_fileopen","930","12","83","26",null,null,null,null,null,null,this);
            obj.set_taborder("0");
            obj.set_text("Load file");
            this.addChild(obj.name, obj);

            obj = new Button("btn_file","855","12","45","26",null,null,null,null,null,null,this);
            obj.set_taborder("1");
            obj.set_text("open");
            this.addChild(obj.name, obj);

            obj = new Edit("edt_path","20","10","830","29",null,null,null,null,null,null,this);
            obj.set_taborder("2");
            this.addChild(obj.name, obj);

            obj = new Grid("Grid01","17","60","488","770",null,null,null,null,null,null,this);
            obj.set_taborder("3");
            obj.set_binddataset("ds_log");
            obj.set_treeusecheckbox("false");
            obj.set_treeinitstatus("collapse,all");
            obj.set_treeusebutton("use");
            obj.set_autofittype("col");
            obj._setContents("<Formats><Format id=\"default\"><Columns><Column size=\"240\"/><Column size=\"53\"/><Column size=\"85\"/><Column size=\"59\"/><Column size=\"48\"/></Columns><Rows><Row size=\"24\"/></Rows><Band id=\"body\"><Cell displaytype=\"treeitemcontrol\" edittype=\"tree\" treelevel=\"bind:level\" text=\"bind:title\"/><Cell col=\"1\" text=\"bind:total_time\" textAlign=\"right\" cssclass=\"expr:total_time == -999 ? &quot;red_color&quot;:total_time &gt; 3.0 ?&quot;red_color&quot;:total_time &gt; 2.0?&quot;yellow_color&quot;:total_time &lt; 0 ?&quot;blue_color&quot;:&quot;&quot;\"/><Cell col=\"2\" text=\"bind:time\" cssclass=\"expr:total_time == -999 ? &quot;red_color&quot;:total_time &gt; 3.0 ?&quot;red_color&quot;:total_time &gt; 2.0?&quot;yellow_color&quot;:total_time &lt; 0 ?&quot;blue_color&quot;:&quot;&quot;\"/><Cell col=\"3\" text=\"bind:rownum\" cssclass=\"expr:total_time == -999 ? &quot;red_color&quot;:total_time &gt; 3.0 ?&quot;red_color&quot;:total_time &gt; 2.0?&quot;yellow_color&quot;:total_time &lt; 0 ?&quot;blue_color&quot;:&quot;&quot;\"/><Cell col=\"4\" text=\"bind:checktime\" cssclass=\"expr:checktime &gt; 100 ? &quot;red_color&quot;:&quot;&quot;\"/></Band></Format></Formats>");
            this.addChild(obj.name, obj);

            obj = new Button("btnLoadText","1037","12","77","26",null,null,null,null,null,null,this);
            obj.set_taborder("4");
            obj.set_text("load text");
            this.addChild(obj.name, obj);

            obj = new Tab("tabLog","520","65","720","765",null,null,null,null,null,null,this);
            obj.set_taborder("5");
            obj.set_tabindex("0");
            this.addChild(obj.name, obj);

            obj = new Tabpage("Tabpage1",this.tabLog);
            obj.set_text("output");
            this.tabLog.addChild(obj.name, obj);

            obj = new TextArea("ta_log","1","2","703","734",null,null,null,null,null,null,this.tabLog.Tabpage1.form);
            obj.set_taborder("0");
            obj.set_wordWrap("char");
            obj.set_scrolltype("vertical");
            this.tabLog.Tabpage1.addChild(obj.name, obj);

            obj = new Tabpage("Tabpage2",this.tabLog);
            obj.set_text("log text");
            this.tabLog.addChild(obj.name, obj);

            obj = new TextArea("ta_logtext","1","2","703","599",null,null,null,null,null,null,this.tabLog.Tabpage2.form);
            obj.set_taborder("0");
            obj.set_wordWrap("char");
            obj.set_scrolltype("vertical");
            this.tabLog.Tabpage2.addChild(obj.name, obj);

            obj = new PopupDiv("pDivReplace","760","87","437","153",null,null,null,null,null,null,this);
            obj.set_text("PopupDiv00");
            obj.set_visible("false");
            obj.set_url("debugging::replace.xfdl");
            this.addChild(obj.name, obj);

            obj = new Button("btnReplace","1039","48","79","31",null,null,null,null,null,null,this);
            obj.set_taborder("6");
            obj.set_text("replace");
            this.addChild(obj.name, obj);

            obj = new Button("Button00","1148","15","33","23",null,null,null,null,null,null,this);
            obj.set_taborder("7");
            obj.set_text("Button00");
            this.addChild(obj.name, obj);
            // Layout Functions
            //-- Default Layout : this
            obj = new Layout("default","",1260,850,this,function(p){});
            this.addLayout(obj.name, obj);
            
            // BindItem Information
            obj = new BindItem("item0","tabLog.Tabpage1.form.ta_log","value","ds_log","msg");
            this.addChild(obj.name, obj);
            obj.bind();
            
            // TriggerItem Information

        };
        
        this.loadPreloadList = function()
        {
            this._addPreloadList("fdl","debugging::replace.xfdl");
        };
        
        // User Script
        this.registerScript("traceLog.xfdl", function() {

        this.readLog = function() {

        	var rtn = this.vf_log.readLine();
        //	trace(rtn);
        }

        var lv_data = "";
        var lv_rownum = 0;
        this.processLog = function(text) {
        //	trace(text);
        	var pos = text.indexOf("$%");
        	if ( pos < 0 ) {
        		lv_data += text;
        		lv_rownum++;
        	} else {
        		if ( lv_data.length > 0 ) {
        			var msgdata = this.ds_log.getColumn(this.ds_log.rowposition,"msg");
        			this.ds_log.setColumn(this.ds_log.rowposition,"msg",msgdata+lv_data);
        			lv_data = "";
        		}
        		var arrtext = text.split("_,_");

        		var row = this.ds_log.addRow();
        		lv_rownum = lv_rownum + 1;
        		var ymd = arrtext[0];
        		var pos = ymd.indexOf("$%");
        		ymd = ymd.substr(pos+2);
        		var time = arrtext[1];
        		this.ds_log.setColumn(row,"ymd",ymd);
        		this.ds_log.setColumn(row,"time",time);
        		this.ds_log.setColumn(row,"level",arrtext[2]);
        		this.ds_log.setColumn(row,"treestate",arrtext[3]);
        		this.ds_log.setColumn(row,"total_time",arrtext[4]);
        		this.ds_log.setColumn(row,"title",arrtext[5]);
        		this.ds_log.setColumn(row,"rownum",lv_rownum);
        		var str = arrtext[6];

        		if ( str ) 	str = str.split("$$").join("\n");

        		this.ds_log.setColumn(row,"msg",str);

        		if ( row != 0 ) {
        			var arrymd = ymd.split("-");
        			var arrtime = time.split(":");
        			var objDate2 = new nexacro.Date(arrymd[0],arrymd[1],arrymd[2],arrtime[0],arrtime[1],arrtime[2],arrtime[3]);
        			ymd  = this.ds_log.getColumn(row-1,"ymd");
        			time = this.ds_log.getColumn(row-1,"time");
        			arrymd = ymd.split("-");
        			arrtime = time.split(":");
        			var objDate1 = new nexacro.Date(arrymd[0],arrymd[1],arrymd[2],arrtime[0],arrtime[1],arrtime[2],arrtime[3]);
        			var totaltime = nexacro.round((objDate2 - objDate1) / 1000, 3);

        			this.ds_log.setColumn(row,"checktime",totaltime);
        		}
        	}
        }

        this.btn_fileopen_onclick = function(obj,e)
        {
        	lv_rownum = 0;
        	this.vf_log.open(this.edt_path.value, VirtualFile.openRead );

        };



        this.vf_log_onerror = function(obj,e)
        {
        	trace(e.errormsg);
        };

        this.vf_log_onsuccess = function(obj,e)
        {
        	if ( e.reason == 1 ) {
        		this.ds_log.clearData();
        		this.ds_log.set_enableevent(false);
        		this.readLog();
        	} else if ( e.reason == 4 ) {
        		if( e.textdata == "" )
        		{
        			this.ds_log.set_enableevent(true);
        			this.ds_log.set_rowposition(0);
        			obj.close();
        		} else {
        			this.processLog(e.textdata);
        			this.readLog();
        		}
        	}
        //	trace(e.textdata);
        };

        this.fd_log_onclose = function(obj,e)
        {
        	this.edt_path.set_value(e.virtualfiles[0].fullpath);
        };

        this.btn_file_onclick = function(obj,e)
        {
        	var varRet;
        	varRet = this.fd_log.open( "FileOpen", FileDialog.LOAD );
        };

        this.btnLoadText_onclick = function(obj,e)
        {
        	var logstr = this.tabLog.Tabpage2.form.ta_logtext.value;
        	var arrlog = logstr.split("\n");

        	lv_rownum = 0;
        	this.ds_log.clearData();
        	this.ds_log.set_enableevent(false);
        	for ( var i = 0 ; i < arrlog.length ; i++ )
        	{
        		this.processLog(arrlog[i]);
        	}
        	this.ds_log.set_enableevent(true);
        	this.ds_log.set_rowposition(0);
        };

        this.btnReplace_onclick = function(obj,e)
        {
        	this.pDivReplace.trackPopup( obj.left-200,obj.bottom+3 );
        };

        this.Button00_onclick = function(obj,e)
        {
        	trace(nexacro._OS);
        };

        });
        
        // Regist UI Components Event
        this.on_initEvent = function()
        {
            this.btn_fileopen.addEventHandler("onclick",this.btn_fileopen_onclick,this);
            this.btn_file.addEventHandler("onclick",this.btn_file_onclick,this);
            this.btnLoadText.addEventHandler("onclick",this.btnLoadText_onclick,this);
            this.btnReplace.addEventHandler("onclick",this.btnReplace_onclick,this);
            this.Button00.addEventHandler("onclick",this.Button00_onclick,this);
            this.vf_log.addEventHandler("onerror",this.vf_log_onerror,this);
            this.vf_log.addEventHandler("onsuccess",this.vf_log_onsuccess,this);
            this.fd_log.addEventHandler("onclose",this.fd_log_onclose,this);
        };
        this.loadIncludeScript("traceLog.xfdl");
        this.loadPreloadList();
        
        // Remove Reference
        obj = null;
    };
}
)();
