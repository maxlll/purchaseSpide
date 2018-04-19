
var prevColor;
var tableCss=function(node){
	var trs=$(node).find('tr');
	for(var i=0;i<trs.length;i++){
		var tr=trs[i];
		var color=i%2==0?'#f8f8f8':'#e5e5ff';
		$(tr).css('background-color', color);
		$(tr).hover(
			function(){
				prevColor=$(this).css('background-color');
				$(this).css('background-color', '#ccccff');
			},
			function(){
				$(this).css('background-color', prevColor);
			}
		);
	}
}

var page={pageNo:1,pageSize:10};
var pageObj = {type:'',fillData:function(node, datas){}}
var refreshData=function(){
	$('#headDiv').show();
	$.ajax({
		type: "GET",
        url: "data/"+pageObj.type+"?pageSize="+page.pageSize+"&pageNo="+page.pageNo+'&t='+new Date().getTime(),
        dataType: "json",
        success: function(result){
        	page=result.page;
        	pageObj.fillData($('#tbody'), result.datas);
        	tableCss($('#tbody'));
        	fixPage($('#pageDiv'), result.page);
        	addPageClick($('#pageDiv'));
        	markCur($('#pageDiv'));
        	addPageSizeChange();
        	addExportSelect($('#exportSelect'));
        	addExportClick($('#exportButton'), $('#exportSelect'));
        },
        error: function(e){
        	clearInterval(interval);
        }
	});
}

var fixPage=function(node){
	let pageCount=page.pageCount;
	$(node).empty();
	var html='<span><a id="prevPage">上一页</a></span>';
	if(pageCount<11){
		for(var i=0;i<pageCount;i++){
			html+='<span><a id="'+(i+1)+'">'+(i+1)+'</a></span>';
		}
	}else{
		html+='<span><a id="1">1</a></span>';
		html+='<span><a id="2">2</a></span>';
		html+='<span><a id="3">3</a></span>';
		let pageNo = page.pageNo;
		if(pageNo>3 && pageNo<7){
			for(let i=4;i<=pageNo;i++){
				html+='<span><a id="'+i+'">'+i+'</a></span>';
			}
			html+='...';
		}else if(pageNo>pageCount-6){
			html+='...';
			for(let i=pageNo;i<pageCount-1;i++){
				html+='<span><a id="'+i+'">'+i+'</a></span>';
			}
		}else if(pageNo>=7 && pageNo<=pageCount-6){
			html+='...';
			html+='<span><a id="'+(pageNo-1)+'">'+(pageNo-1)+'</a></span>';
			html+='<span><a id="'+pageNo+'">'+pageNo+'</a></span>';
			html+='<span><a id="'+(pageNo+1)+'">'+(pageNo+1)+'</a></span>';
			html+='...';
		}else{
			html+='...';
		}
		html+='<span><a id="'+(pageCount-1)+'">'+(pageCount-1)+'</a></span>';
		html+='<span><a id="'+pageCount+'">'+pageCount+'</a></span>';
	}
	html+='<span><a id="nextPage">下一页</a></span>';
	html+='<span><input id="pageSize" type="text" value="'+page.pageSize+'" style="width:25px;text-align:center;">条/页</span>';
	html+='<span>共'+page.total+'条</span>';
	$(node).append(html);
}

var addPageClick=function(node){
	var as=$(node).find('a');
	for(var i=0;i<as.length;i++){
		$(as[i]).click(function(){
			var aid=$(this).attr('id');
			var needRefresh = true;
			if(aid=='prevPage'&&page.pageNo>1){
				page.pageNo=page.pageNo-1;
			}else if(aid=='nextPage'&&page.pageNo<page.pageCount){
				page.pageNo=page.pageNo+1;
			}else if(aid=='firstPage'&&page.pageNo>1){
				page.pageNo=1;
			}else if(aid=='lastPage'&&page.pageNo<page.pageCount){
				page.pageNo=page.pageCount;
			}else if(!isNaN(aid)&&aid!=page.pageNo){
				page.pageNo=aid;
			}else{
				needRefresh = false;
			}
			if(needRefresh){
				refreshData();
			}
		});
		$(as[i]).css('cursor', 'pointer');
	}
}

var addPageSizeChange=function(){
	$('#pageSize').change(function(node){
		var value=$(this).val();
		if(value){
			value=value<10?10:value>50?50:value;
			page={pageNo:1,pageSize:value};
			refreshData();
		}
	});
}

var addExportSelect=function(node){
	$(node).empty();
	var html = '<option value="1">'+curMenu+' 首页数据</option>';
	html += '<option value="2">所有菜单首页数据</option>';
	html += '<option value="3">所有菜单当月数据</option>';
	$(node).append(html);
}

var addExportClick=function(node, select){
	$(node).click(function(){
		window.open("download/"+pageObj.type+"?option="+$(select).val()+'&pageSize='+page.pageSize);
	});
}

var markCur=function(node){
	var as=$(node).find('a');
	for(var i=0;i<as.length;i++){
		var aid=$(as[i]).attr('id');
		if(aid==page.pageNo){
			$(as[i]).css('background-color', '#6666ff');
			break;
		}
	}
}

var interval;
var startInterval=function(){
	if(interval){
		clearInterval(interval);
	}
	interval=setInterval(function(){
		page={pageNo:1,pageSize:10};
		refreshData();
	}, 60*1000, 10*1000);
}