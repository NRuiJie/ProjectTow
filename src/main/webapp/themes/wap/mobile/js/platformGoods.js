/**
 * Created by Andste on 2016/6/6.
 */

$(function(){
// 添加商品操作
    (function(){
        var addGoods = $('.add-goods');
        addGoods.unbind('click').on('click', function(){
            var _this = $(this), goods_id = _this.attr('goods_id');
           
                setTimeout(function(){
                   
                	  $.ajax({
                          url : ctx + "/api/b2b2c/store_platform-goods/add_platform_goods.do?goods_id=" + goods_id,
                          cache : false,
                          dataType : 'json',
                          success : function(result) {
                              if(result.result==1){
                            	  alert("添加成功!");
                                  window.location.reload();
                              }else {
                            	  alert(result.message);
                              };
                          },
                          error : function() {
                        	  alert("出现错误，请重试！");
                          }
                      });
                   
                }, 500);
            });

           
      
    }());
    // 移除商品操作
    (function(){
    	var deleteBtn = $('.mv-goods');
    	deleteBtn.unbind('click').on('click', function(){
    		var _this = $(this), goods_id = _this.attr('goods_id');
    		
    			
    				
    				$.ajax({
    					url : ctx + "/api/b2b2c/store_platform-goods/mv_platform_goods.do?goods_id=" + goods_id,
    					cache : false,
    					dataType : 'json',
    					success : function(result) {
    						if(result.result==1){
    							alert("移除成功");
    							window.location.reload();
    							
    						}else {
    							alert(result.message);
    						};
    					},
    					error : function() {
    						alert("出现错误，请重试！");
    					}
    				});
    		
    	});
    }());



  

   
    //  筛选
    (function(){
        var seach_keyword_val = '';

        //  代理商品搜索关键词时
        $('#key_seach_btn').on('click', function(){
            var  _this = $(this),
                seach_keyword_val = $('#goodsName').val();
            //暂时去掉特殊字符的验证 by fengkun
//            if(!testStr(seach_keyword_val)){
//                $.message.error('不能包含特殊字符！');
//                return false;
//            };
            location.href="goodsadd.html?goodsName="+seach_keyword_val;
        });
        //  平台商品搜索关键词时
        $('#addkey_seach_btn').on('click', function(){
        	var  _this = $(this),
        	seach_keyword_val = $('#goodsName').val();
        	//暂时去掉特殊字符的验证 by fengkun
//            if(!testStr(seach_keyword_val)){
//                $.message.error('不能包含特殊字符！');
//                return false;
//            };
        	location.href="addgoods.html?goodsName="+seach_keyword_val;
        });
        //  平台商品搜索分类时
        $('.search-cat-id').on('click', function(){
        	var  _this = $(this);
        	var catId = _this.attr("cat-id");
        	if(catId==""||catId==null){
        		location.href="addgoods.html";
        	}else{
            	location.href="addgoods.html?catid="+catId;
        	}
        });
        //  平台商品搜索品牌时
        $('.search-brand-id').on('click', function(){
        	var  _this = $(this);
        	var brandId = _this.attr("brand-id");
        	if(brandId==""||brandId==null){
        		location.href="addgoods.html";
        	}else{
        		location.href="addgoods.html?brandId="+brandId;
        	}
        });

        //  如果之前有搜索过，则获取url中的搜索关键词赋值给搜索框
        (function(){
            var reg = new RegExp("(^|&)" + "goodsName" + "=([^&]*)(&|$)", "i");
        	var r = window.location.search.substr(1).match(reg);
        	var context = "";
        	if(r != null)
        		context = r[2];
        	reg = null;
        	r = null;
        	context=context == null || context == "" || context == "undefined" ? "" : decodeURI(context);
            if(context){
                $('#goodsName').val(context)
            };
        })();
        //  如果之前有搜索过，则获取url中的搜索分类，对应选中
        (function(){
        	var reg = new RegExp("(^|&)" + "catid" + "=([^&]*)(&|$)", "i");
        	var r = window.location.search.substr(1).match(reg);
        	var context = "";
        	if(r != null)
        		context = r[2];
        	reg = null;
        	r = null;
        	context=context == null || context == "" || context == "undefined" ? "" : decodeURI(context);
        	if(context){
        		$('.search-cat-id').each(function(index, element) {
        			var catId = $(element).attr("cat-id");
        		   		if (catId==context) {
        					$(element).addClass('active');
        				} else {
        					$(element).removeClass('active');
        				}
               		
        	    });
        	};
        })();
        //  如果之前有搜索过，则获取url中的搜索品牌，对应选中
        (function(){
        	var reg = new RegExp("(^|&)" + "brandId" + "=([^&]*)(&|$)", "i");
        	var r = window.location.search.substr(1).match(reg);
        	var context = "";
        	if(r != null)
        		context = r[2];
        	reg = null;
        	r = null;
        	context=context == null || context == "" || context == "undefined" ? "" : decodeURI(context);
        	if(context){
        		$('.search-brand-id').each(function(index, element) {
        			var brandId = $(element).attr("brand-id");
        			if (brandId==context) {
        				$(element).addClass('active');
        			} else {
        				$(element).removeClass('active');
        			}
        			
        		});
        	};
        })();
        function testStr(str){
            var pattern	= /[`~!@#\$%\^\&\*\(\)_\+<>\?:"\{\},\.\\\/;'\[\]]/im;
            if(pattern.test(str)){
                return false
            }else {
                return true;
            };
        };
    })();

 

});