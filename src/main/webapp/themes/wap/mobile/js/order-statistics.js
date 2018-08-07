
var global_select_year_val = 0;//用户选择的年
var global_select_month_val = 0;//用户选择的月

$(function() {  
    FastClick.attach(document.body);  
    
    var $filteritem = $('.filter-item');
    var $filteritemspan = $('.filter-item span');
    
    var $searchgo =$('.searchgo');
    
    var $cancel = $('.cancel');
    
    $filteritemspan.on('click',function(){

    	
    	if($(this).parent().hasClass('on')){
    		$(this).parent().removeClass('on');
    	}else{
    	   $(this).parent().addClass('on').siblings().removeClass('on');
    		
    	}
    	
    })
    
    initFilter();//初始化年和月
    
    /**
     * 点击年或月的时候执行
     * 		函数功能：根据用户选择的年和月查询订单
     */
    $('.filternav').on('click','li',function(){
    	$(this).addClass('active').siblings().removeClass('active');
    	$(this).parents('.filter-item').removeClass('on');
    	var val = $(this).text();
    	var my = $(this).attr("data-val");
    	
    	if(my=="年"){
    		global_select_year_val = val;//用户选择的年
    		$('#year-val').text(val+my);
    		$('#month-val').text(global_select_month_val+"月");
    	}else{
    		global_select_month_val = val;//用户选择的月
    		$('#year-val').text(global_select_year_val+"年");
    		$('#month-val').text(val+my);
    	}
    	ajaxGetOrder();
    });
    
});

/**
 * 初始化过滤时的年和月
 * @returns
 */
function initFilter(){
	var date = new Date();
	var y = date.getFullYear();//获取年(四位)
	var m = date.getMonth()+1;//获得月份 
	var wk = date.getDay();//获取星期
	var d = date.getDate()+1;//获取日
	
	global_select_year_val = y;//用户选择的年
	global_select_month_val = m;//用户选择的月
	
	var url = "javascript:;";
	
	var yearHtml = "";
	var newY = y-2;
	for(var i=0; i<12; i++){
		if((i+newY)==y){
			yearHtml += '<li class="active" data-val="年"><a href="'+url+'">'+(i+newY)+'</a></li>';
		}else{
			yearHtml += '<li data-val="年"><a href="'+url+'">'+(i+newY)+'</a></li>';
		}
	}
	
	var monthHtml = "";
	for(var j=1; j<=12; j++){
		if(j==m){
			monthHtml += '<li class="active" data-val="月"><a href="'+url+'">'+j+'</a></li>';
		}else{
			monthHtml += '<li data-val="月"><a href="'+url+'">'+j+'</a></li>';
		}
	}
	
	$("#year-ul").html(yearHtml);
	$("#month-ul").html(monthHtml);
	
	ajaxGetOrder();//初始化请求当前月的订单销售
}