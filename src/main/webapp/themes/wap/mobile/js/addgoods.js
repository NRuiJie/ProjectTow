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
    
    $searchgo.click(function(){
    	$('.search').show();
    })
    
    $cancel.click(function(){
       $('.search').hide();  	
    })
    
    
    
});  