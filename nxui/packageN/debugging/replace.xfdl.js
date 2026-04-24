(function()
{
    return function()
    {
        if (!this._is_form)
            return;
        
        var obj = null;
        
        this.on_create = function()
        {
            this.set_name("replace");
            this.set_titletext("New Form");
            this.set_background("darkgray");
            if (Form == this.constructor)
            {
                this._setFormPosition(430,160);
            }
            
            // Object(Dataset, ExcelExportObject) Initialize

            
            // UI Components Initialize
            obj = new Button("btn_replace","190","107","73","33",null,null,null,null,null,null,this);
            obj.set_taborder("0");
            obj.set_text("replaceAll");
            this.addChild(obj.name, obj);

            obj = new Edit("edt_search","77","21","339","29",null,null,null,null,null,null,this);
            obj.set_taborder("1");
            this.addChild(obj.name, obj);

            obj = new Static("Static00","12","23","53","26",null,null,null,null,null,null,this);
            obj.set_taborder("2");
            obj.set_text("찾을 내용");
            this.addChild(obj.name, obj);

            obj = new Static("Static00_00","12","63","53","26",null,null,null,null,null,null,this);
            obj.set_taborder("3");
            obj.set_text("바꿀 내용");
            this.addChild(obj.name, obj);

            obj = new Edit("edt_change","77","61","339","29",null,null,null,null,null,null,this);
            obj.set_taborder("4");
            this.addChild(obj.name, obj);
            // Layout Functions
            //-- Default Layout : this
            obj = new Layout("default","",430,160,this,function(p){});
            this.addLayout(obj.name, obj);
            
            // BindItem Information

            
            // TriggerItem Information

        };
        
        this.loadPreloadList = function()
        {

        };
        
        // User Script
        this.registerScript("replace.xfdl", function() {

        this.btn_replace_onclick = function(obj,e)
        {
        	var tabObj = this.parent.parent.parent.form.tabLog;
        	trace(tabObj.name);
        	var logstr = tabObj.Tabpage2.form.ta_logtext.value;
        	var sText = this.edt_search.value;
        	var cText = this.edt_change.value?this.edt_change.value:"";

        	if ( logstr ) 	logstr = logstr.split(sText).join(cText);

        	tabObj.Tabpage2.form.ta_logtext.set_value(logstr);
        };

        });
        
        // Regist UI Components Event
        this.on_initEvent = function()
        {
            this.btn_replace.addEventHandler("onclick",this.btn_replace_onclick,this);
        };
        this.loadIncludeScript("replace.xfdl");
        this.loadPreloadList();
        
        // Remove Reference
        obj = null;
    };
}
)();
