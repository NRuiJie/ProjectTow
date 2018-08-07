package com.enation.app.shop.mobile.action.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.core.goods.service.StoreCartContainer;
import com.enation.app.b2b2c.core.goods.service.StoreCartKeyEnum;
import com.enation.app.b2b2c.core.order.model.cart.StoreCartItem;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.model.Regions;
import com.enation.app.base.core.service.IRegionsManager;
import com.enation.app.shop.core.member.model.MemberAddress;
import com.enation.app.shop.core.member.service.IMemberAddressManager;
import com.enation.app.shop.mobile.utils.ValidateUtils;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonMessageUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * Created by Dawei on 5/11/15.
 */
@Controller("mobileMemberAddressApiController")
@RequestMapping("/api/mobile/address")
public class MemberAddressApiController {

	@Autowired
	private IMemberAddressManager memberAddressManager;

	@Autowired
	private IRegionsManager regionsManager;

	/**
	 * 获取会员的默认收货地址
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/default", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult defaultAddress() {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
		}
		List<MemberAddress> addressList = memberAddressManager.listAddress();
		if (addressList == null || addressList.size() == 0) {
			return JsonResultUtil.getErrorJson("您还没有添加地址！");
		}

		MemberAddress defaultAddress = null;
		for (MemberAddress address : addressList) {
			if (address.getDef_addr() != null
					&& address.getDef_addr() == 1) {
				defaultAddress = address;
				break;
			}
		}
		if (defaultAddress == null) {
			defaultAddress = addressList.get(0);
		}
		return JsonResultUtil.getObjectJson(defaultAddress);
	}

	/**
	 * 获取会员地址
	 *
	 * @return json字串
	 */
	@ResponseBody
	@RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult list() {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
		}
		List<MemberAddress> memeberAddressList = memberAddressManager.listAddress();
		return JsonResultUtil.getObjectJson(memeberAddressList);
	}

	/**
	 * 添加会员地址
	 *
	 * @return json字串
	 */
	@ResponseBody
	@RequestMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult add() {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
		}

		if (memberAddressManager.addressCount(member.getMember_id()) >= 10) {
			return JsonResultUtil.getErrorJson("添加失败，您最多可以维护10个收货地址！");
		}
		MemberAddress address = new MemberAddress();
		try {
			address = this.fillAddressFromReq(address);			
		} catch (RuntimeException e) {
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
		int addr_id = memberAddressManager.addAddress(address);

		return JsonResultUtil.getObjectJson(memberAddressManager.getAddress(addr_id));
	}

	/**
	 * 修改收货地址
	 *
	 * @return json字串 result 为1表示添加成功，0表示失败 ，int型 message 为提示信息 ，String型
	 */
	@ResponseBody
	@RequestMapping(value = "/edit", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult edit() {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
		}

		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		int addr_id = NumberUtils.toInt(request.getParameter("addr_id"), 0);

		MemberAddress address = memberAddressManager.getAddress(addr_id);
		if (address == null
				|| !address.getMember_id().equals(member.getMember_id())) {
			return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
		}

		try {
			address = this.fillAddressFromReq(address);
		} catch (RuntimeException e) {
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
		memberAddressManager.updateAddress(address);
		if(address.getDef_addr() != null && address.getDef_addr().intValue() == 1){
			memberAddressManager.updateMemberAddress(member.getMember_id(), addr_id);
		}
		return JsonResultUtil.getObjectJson(address);
	}

	/**
	 * 设置当前地址为默认地址
	 *
	 * @param addr_id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/set-default", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult setDefaultAddress(Integer addr_id) {
		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
			}

			MemberAddress memberAddress = memberAddressManager
					.getAddress(addr_id);
			if (memberAddress == null
					|| !memberAddress.getMember_id().equals(
							member.getMember_id())) {
				return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
			}

			memberAddressManager.updateAddressDefult();
			memberAddressManager.addressDefult("" + addr_id);
			return JsonResultUtil.getSuccessJson("设置为默认地址成功！");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("设置为默认地址失败，请您重试！");
		}
	}

	/**
	 * 删除一个收货地址
	 *
	 * @param addr_id
	 *            ：要删除的收货地址id,int型 result 为1表示添加成功，0表示失败 ，int型 message 为提示信息
	 *            ，String型
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult delete(Integer addr_id) {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
		}

		MemberAddress memberAddress = memberAddressManager.getAddress(addr_id);
		if (memberAddress == null
				|| !memberAddress.getMember_id().equals(member.getMember_id())) {
			return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
		}

		try {
			memberAddressManager.deleteAddress(addr_id);
			return JsonResultUtil.getSuccessJson("删除收货地址成功！");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("删除收货地址失败！");
		}
	}
	
	/**
	 * 根据parentid获取地区列表
	 * 
	 * @param parentid
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/region-list", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult list(Integer parentid) {
		Map data = new HashMap();
		List regionList = regionsManager.listChildrenByid(parentid);
		return JsonResultUtil.getObjectJson(regionList);
	}

	/**
	 * 从request中填充address信息
	 *
	 * @param address
	 * @return
	 */
	private MemberAddress fillAddressFromReq(MemberAddress address) {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();

		address.setDef_addr(0);
		String name = request.getParameter("name");
		if (StringUtil.isEmpty(name)) {
			throw new RuntimeException("收货人姓名不能为空！");
		}
		address.setName(name);
		Pattern p = Pattern.compile("^[0-9A-Za-z一-龥]{0,20}$");
		Matcher m = p.matcher(name);

		if (!m.matches()) {
			throw new RuntimeException("收货人姓名格式不正确！");
		}

		address.setTel("");

		String mobile = request.getParameter("mobile");
		address.setMobile(mobile);
		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			throw new RuntimeException("请输入正确的手机号码！");
		}

		String province_id = request.getParameter("province_id");
		if (province_id == null || province_id.equals("")) {
			throw new RuntimeException("请选择所在地区中的省！");
		}
		address.setProvince_id(Integer.valueOf(province_id));

		Regions province = regionsManager.get(address.getProvince_id());
		if (province == null)
			throw new RuntimeException("系统参数错误！");
		address.setProvince(province.getLocal_name());

		String city_id = request.getParameter("city_id");
		if (city_id == null || city_id.equals("")) {
			throw new RuntimeException("请选择所在地区中的市！");
		}
		address.setCity_id(Integer.valueOf(city_id));

		Regions city = regionsManager.get(address.getCity_id());
		if (city == null)
			throw new RuntimeException("系统参数错误！");
		address.setCity(city.getLocal_name());

		String region_id = request.getParameter("region_id");
		if (region_id != null && !region_id.equals("0")) {
			address.setRegion_id(Integer.valueOf(region_id));
			Regions region = regionsManager.get(address.getRegion_id());	
			if (region == null)
				throw new RuntimeException("系统参数错误");
			address.setRegion(region.getLocal_name());
		}
		String town_id = request.getParameter("town_id");
		if(town_id!=null && !town_id.equals("0")){
			address.setTown_id(Integer.valueOf(town_id));
			Regions town = regionsManager.get(address.getTown_id());
			if(town!=null){
				address.setTown(town.getLocal_name());
			}else{
				throw new RuntimeException("系统参数错误");
			}
		}


		String addr = request.getParameter("addr");
		if (addr == null || addr.equals("")) {
			throw new RuntimeException("详细地址不能为空！");
		}
		address.setAddr(addr);
		address.setZip("");
		address.setDef_addr(NumberUtils.toInt(request.getParameter("def"), 0));
		if(region_id.equals("0")){
			address.setRegion("");
			address.setRegion_id(0);
		}
		if(StringUtils.isEmpty(town_id) || town_id.equals("0")){
			address.setTown("");
			address.setTown_id(0);
		}
		//判断是否有保税仓商品
//		Integer flag = 0;
//		List<Map> storeCartList = StoreCartContainer.getSelectStoreCartListFromSession();
//		for (Map map : storeCartList) {
//			List<StoreCartItem> goodslist  =(List<StoreCartItem>) map.get(StoreCartKeyEnum.goodslist.toString());
//			for (StoreCartItem storeCartItem : goodslist) {
//				if(storeCartItem.getFlag() == 3){
//					flag = 1;
//				}
//			}
//		}
//		if(flag == 1){
//			//保税仓必填字段
//			String idf_img = request.getParameter("idf_img")==null?"":request.getParameter("idf_img");
//			String idz_img = request.getParameter("idz_img")==null?"":request.getParameter("idz_img");
//			String id_number = request.getParameter("id_number")==null?"":request.getParameter("id_number");
//			if(!StringUtil.isEmpty(id_number) && !StringUtil.isEmpty(idz_img) && !StringUtil.isEmpty(idf_img)){
//				address.setId_number(id_number);
//				address.setIdf_img(idf_img);
//				address.setIdz_img(idz_img);
//			}else{
//				throw new RuntimeException("身份证号、身份证正反面都不能为空！");
//			}
//		}
		String id_number = request.getParameter("id_number")==null?"":request.getParameter("id_number");
		address.setId_number(id_number);
		return address;
	}

	
}
