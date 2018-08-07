$(function(){
	window.onload=function(){
	}
    var module = new Module();
    init();

    function init() {
    	module.navigator.init('订单审核');
    }
	var deData = $('#deData').val() ? $('#deData').val().split(',') : null;

    //  地区选择插件
    $('.select-address').addressSelect({
        apiPath: ctx,
        deData : deData,
        callBack: function (res) {
            $('.select-address').find('span').html(res.string);
        }
    });
    
	var  name = '';
	$('.editor1').click(function(){		
		name = $(this).data('name');
		
		var val =  $(this).prev().text().substring(1);
		$('.modal1').attr('name',name);
		$('.modal1').find('input').val(val);           
		if(name == ""){
			
		}		
		$('.modal1').show();
	})
	
	$('.editor2').click(function(){
		name = "modal2";
		var suernameval = $('.suername').text();
		var userphoneval = $('.userphone').text().trim();
		var useradressval = $('.useradress').text();
		var useradressdetailval = $('.useradressdetail').text();
		var zipcode = $('.zipcode').text();
		var time = $('.time').text();
	    
		$('.model2').show();

		$('.model2 .nameedit').val(suernameval);
		$('.model2 .teledit').val(userphoneval);
		$('.model2 .adressedit').val(useradressval);
		$('.model2 .adressdetailedit').val(useradressdetailval);
		$('.model2 .zipcodeedit').val(zipcode);
		$('.model2 .timeedit').val(time);
		
	})
	
	$('.editor3').click(function(){
		name = "modal3";
		var logi_name = $('.logi_name').text();
		var ship_no = $('.ship_no').text().trim();
	    
		$('.model3').show();

		$('.model3 .logi_nameedit').val(logi_name);
		$('.model3 .ship_noedit').val(ship_no);
		
	})
	
	//	取消编辑
	$('.cancel').click(function(){
		if(name!== ''){
			name='';

		};
		$(this).parents('.modalbg').hide();                                                                                                                                                                                    
	})
	
	//	确认编辑
	$('.confirm').click(function(){
		console.log(name)
		if( name == 'fare' ){
			$('.fare').html($(this).parents('.modalbg').find('input').val());
		}else if(name == 'all'){
			//修改价格
			$('.all').html("¥"+$(this).parents('.modalbg').find('input').val());
			$.ajax({ 
				url:  ctx + '/api/store/store-order/save-price.do',
				type:'post',
				data:{
					orderId: $('#order-id').val(),
					payMoney: $('#order-pric').val()
				},
				success: function(result){
					if(result.result==1){
						location.reload();
					}else{
						alert(result.message);
					}
			    },
			    error:function(){
					alert("抱歉，修改价格错误");
				}
		     });
			
		}else if(name == 'paymode'){
			$('.paymode').html($(this).parents('.modalbg').find('input').val());
			
		}else if(name == 'modal2'){
			//修改收货人信息
			$('.suername').text($('.model2 .nameedit').val());
			$('.userphone').text($('.model2 .teledit').val());
			$('.useradress').text($('.model2 .adressedit').val());
			$('.useradressdetail').text( $('.model2 .adressdetailedit').val());
			$('.zipcode').text( $('.model2 .zipcodeedit').val());
			$('.time').text($('.model2 .timeedit').val());
            
			var _ship_name = $('.nameedit').val();
			var _ship_mobile = $('.teledit').val();
			var _ship_zip = $('.zipcodeedit').val();
			var _region_id = $('.select-address').find('input[name="region_id"]').val();
			var _ship_day = $('.model2 select').val();
			
			var re_zip= /^[1-9][0-9]{5}$/;
			if(!_ship_name){
				module.message.error('收货人姓名不能为空！');
                return false;
            }
			if(!_ship_mobile){
				module.message.error('收货人手机号不能为空！');
				return false;
			}
			if(!module.regExp.mobile.test(_ship_mobile)){ 
				module.message.error('手机格式有误！');
		        return false; 
			}
			if(!(re_zip.test(_ship_zip))){ 
				module.message.error('邮政编码有误，请重填！');
				return false; 
			}
			if(!_region_id){
				module.message.error('收货人地区不能为空！');
				return false;
			}
			if(!_ship_day){
				module.message.error('请选择收货时间！');
				return false;
			}
			$.ajax({ 
				url:  ctx + '/api/store/store-order/save-consigee.do',
				type:'post',
				data:{
					orderId: $('#order-id').val(),
					ship_name: _ship_name,
					ship_mobile: _ship_mobile,
					province: $('.adressedit').val(),
					addr: $('.adressdetailedit').val(),
					ship_zip: _ship_zip,
					
					province :  $('.select-address').find('input[name="province"]').val(),
					province_id :  $('.select-address').find('input[name="province_id"]').val(),
					city :  $('.select-address').find('input[name="city"]').val(),
	            	city_id :  $('.select-address').find('input[name="city_id"]').val(),
	            	region :  $('.select-address').find('input[name="region"]').val(),
	            	region_id : _region_id,
					ship_day: _ship_day
				},
				success: function(result){
					if(result.result==1){
						location.reload();
					}else{
						alert(result.message);
					}
			    },
			    error:function(){
					alert("抱歉，修改收货人信息错误");
				}
		     });
			
		}else{
			//修改物流信息
			$('.logi_name').text( $('.model3 .logi_nameedit option:selected').text()).attr("value",$('.model3 .logi_nameedit option:selected').val());
			$('.ship_no').text($('.model3 .ship_noedit').val());
			
			var _logi_id = $('.model3 select').val();
			var _ship_no = $(".ship_noedit").val();
			
			if(!_logi_id){
				module.message.error('请选择快递公司！');
				return false;
			}
			if(!_ship_no){
				module.message.error('请填写快递单号！');
				return false;
			}
			$.ajax({ 
				url:  ctx + '/api/store/store-order/save-logi.do',
				type:'post',
				data:{
					orderId : $('#order-id').val(),
					logi_id : _logi_id,
					ship_no : _ship_no
				},
				success: function(result){
					if(result.result==1){
						location.reload();
					}else{
						alert(result.message);
					}
			    },
			    error:function(){
					alert("抱歉，修改物流错误");
				}
		     });
		}		
		})
	})
	
//订单备注修改	
$("#target").click(function(){
	$.ajax({ 
		url:  ctx + '/api/store/store-order/save-consigee.do',
		type:'post',
		data:{
			orderId: $('#order-id').val(),
			remark:$(".remark").val()
		},
		success: function(result){
			if(result.result==1){
				location.reload();
			}else{
				alert(result.message);
			}
	    },
	    error:function(){
			alert("抱歉，修改订单备注错误");
			}
	     });
})
//特殊处理修改
$("#target2").click(function() {
	$.ajax({
		url : ctx + '/api/store/store-order/save-consigee.do',
		type : 'post',
		data : {
			orderId : $('#order-id').val(),
			admin_remark : $(".admin_remark").val()
		},
		success : function(result) {
			if (result.result == 1) {
				location.reload();
			} else {
				alert(result.message);
			}
		},
		error : function() {
			alert("抱歉，修改订单备注错误");
		}
	});
})
