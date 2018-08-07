package com.enation.app.shop.mobile.action.api;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.activemq.ActiveMQInputStream.ReadTimeoutException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.logging.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.ls.LSInput;

import com.enation.app.b2b2c.core.member.service.IStoreCollectManager;
import com.enation.app.b2b2c.core.order.model.StoreOrder;
import com.enation.app.b2b2c.core.order.service.IStoreOrderManager;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.base.core.model.Award;
import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.model.MemberBank;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.app.base.core.service.ISmsManager;
import com.enation.app.base.core.upload.IUploader;
import com.enation.app.base.core.upload.UploadFacatory;
import com.enation.app.shop.core.member.service.IMemberPointManger;
import com.enation.app.shop.core.member.service.IPointHistoryManager;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.service.OrderStatus;
import com.enation.app.shop.mobile.model.SmsLog;
import com.enation.app.shop.mobile.service.ApiFavoriteManager;
import com.enation.app.shop.mobile.service.ApiMemberManager;
import com.enation.app.shop.mobile.service.ApiOrderManager;
import com.enation.app.shop.mobile.utils.ValidateUtils;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.EncryptionUtil1;
import com.enation.framework.util.FileUtil;
import com.enation.framework.util.HttpUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

import net.sf.json.JSONObject;

/**
 * Created by Dawei on 4/28/15.
 */
/**
 * 
 * @ClassName: MemberApiController
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author zwb
 * @version 1.0
 * @since v6.2.1
 * @date 2017年8月27日 上午11:51:31
 *
 */
@Controller("mobileMemberApiController")
@RequestMapping("/api/mobile/member")
public class MemberApiController {
	protected final Logger logger = Logger.getLogger(getClass());
	@Autowired
	private IMemberManager memberManager;

	@Autowired
	private IMemberPointManger memberPointManger;

	@Autowired
	private ISmsManager smsManager;

	@Autowired
	private ApiFavoriteManager apiFavoriteManager;

	@Autowired
	private ApiOrderManager apiOrderManager;

	@Autowired
	private IPointHistoryManager pointHistoryManager;

	@Autowired
	private ApiMemberManager apiMemberManager;

	@Autowired
	private IStoreCollectManager storeCollectManager; // xulipeng

	@Autowired
	private IStoreManager storeManager;
	@Autowired
	private IStoreOrderManager storeOrderManager;

	private final int PAGE_SIZE = 20;

	/**
	 * 短信验证码前缀
	 */
	private static final String SMS_PREFIX = "【Javashop】";

	/**
	 * 会员登录
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult login(String username, String password) {
		if (memberManager.login(username, password) != 1) {
			return JsonResultUtil.getErrorJson("账号密码错误");
		}

		String cookieValue = EncryptionUtil1.authcode(
				"{username:\"" + username + "\",password:\"" + StringUtil.md5(password) + "\"}", "ENCODE", "", 0);
		HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(), "JavaShopUser", cookieValue, 60 * 60 * 24 * 14);

		Member member = UserConext.getCurrentMember();

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", member.getUname());
		map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
		map.put("level", member.getLvname());

		map.put("imuser", "");
		map.put("impass", "");

		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 是否已登录
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/islogin", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult isLogin() {
		if (UserConext.getCurrentMember() == null) {
			return JsonResultUtil.getErrorJson("尚未登陆");
		}
		return JsonResultUtil.getSuccessJson("已经登录");
	}

	/**
	 * 退出登录
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult logout() {
		if (UserConext.getCurrentMember() != null) {
			this.memberManager.logout();
		}
		HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(), "JavaShopUser", null, 0);
		return JsonResultUtil.getSuccessJson("注销成功");
	}

	/**
	 * 修改密码
	 * 
	 * @param oldpass
	 *            旧密码
	 * @param password
	 *            新密码
	 * @param repass
	 *            重复密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult changePassword(String oldpass, String password, String repass) {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("请您登录后再修改密码！");
		}

		oldpass = StringUtil.md5(oldpass);
		if (!oldpass.equals(member.getPassword())) {
			return JsonResultUtil.getErrorJson("您的旧密码不正确！");
		}

		if (!repass.equals(password)) {
			return JsonResultUtil.getErrorJson("您两次输入的密码不一致！");
		}

		try {
			memberManager.updatePassword(password);
			return JsonResultUtil.getSuccessJson("修改密码成功！");
		} catch (Exception e) {
		}
		return JsonResultUtil.getErrorJson("修改密码失败！");
	}

	/**
	 * 使用手机修改密码
	 * 
	 * @param mobile
	 *            手机号码
	 * @param mobilecode
	 *            手机验证码
	 * @param password
	 *            新密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/mobile-change-pass", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult mobileChangePassword(String mobile, String mobilecode, String password) {
		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("请输入正确的手机号码！");
		}
		if (StringUtil.isEmpty(mobilecode)) {
			return JsonResultUtil.getErrorJson("请验证手机号码后再修改密码！");
		}
		if (StringUtil.isEmpty(password) || password.length() < 6 || password.length() > 12) {
			return JsonResultUtil.getErrorJson("新密码长度为6到12位！");
		}

		// 验证短信验证码
		SmsLog smsLog = (SmsLog) SmsLog.cache.get(mobile);
		if (smsLog == null || smsLog.getTimeList().size() <= 0) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}
		if (StringUtils.isEmpty(smsLog.getMobileCode()) || !smsLog.getMobileCode().equals(mobilecode)) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}
		if (!mobile.equals(smsLog.getMobile())) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}
		if (DateUtil.getDateline() - smsLog.getTimeList().get(smsLog.getTimeList().size() - 1) > 10 * 60) {
			return JsonResultUtil.getErrorJson("短信验证码已过期，请您重新发送！");
		}

		// 根据手机号码获取用户
		Member member = memberManager.getMemberByMobile(mobile);
		if (member == null) {
			return JsonResultUtil.getErrorJson("不存在此手机号码！");
		}

		try {
			memberManager.updatePassword(member.getMember_id(), password);
			return JsonResultUtil.getSuccessJson("修改密码成功");
		} catch (Exception e) {
		}
		return JsonResultUtil.getErrorJson("修改密码失败");
	}

	/**
	 * 注册
	 * 
	 * @param username
	 *            要注册的用户名
	 * @param password
	 *            要注册的密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult register(String username, String password) {
		Member member = new Member();
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String registerip = request.getRemoteAddr();

		if (StringUtil.isEmpty(username)) {
			return JsonResultUtil.getErrorJson("用户名不能为空！");
		}
		if (username.length() < 4 || username.length() > 20) {
			return JsonResultUtil.getErrorJson("用户名的长度为4-20个字符！");
		}
		if (username.contains("@")) {
			return JsonResultUtil.getErrorJson("用户名中不能包含@等特殊字符！");
		}
		if (StringUtil.isEmpty(password)) {
			return JsonResultUtil.getErrorJson("密码不能为空！");
		}
		if (memberManager.checkname(username) > 0) {
			return JsonResultUtil.getErrorJson("此用户名已经存在，请您选择另外的用户名!");
		}

		member.setMobile("");
		member.setUname(username);
		member.setPassword(password);
		member.setEmail(username);
		member.setRegisterip(registerip);

		if (memberManager.register(member) != 1) {
			return JsonResultUtil.getErrorJson("用户名[" + member.getUname() + "]已存在!");
		}

		this.memberManager.login(username, password);
		String cookieValue = EncryptionUtil1.authcode(
				"{username:\"" + username + "\",password:\"" + StringUtil.md5(password) + "\"}", "ENCODE", "", 0);
		HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(), "JavaShopUser", cookieValue, 60 * 60 * 24 * 14);
		member = UserConext.getCurrentMember();

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", member.getUname());
		map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
		map.put("level", member.getLvname());

		map.put("imuser", "");
		map.put("impass", "");

		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 使用手机注册
	 * 
	 * @param mobile
	 *            手机号码
	 * @param mobilecode
	 *            手机验证码
	 * @param password
	 *            密码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/mobile-register", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult mobileRegister(String mobile, String mobilecode, String password) {
		Member member = new Member();
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String registerip = request.getRemoteAddr();

		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("请输入正确的手机号码！");
		}

		// 验证短信验证码
		if (StringUtil.isEmpty(mobilecode)) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}

		SmsLog smsLog = (SmsLog) SmsLog.cache.get(mobile);
		if (smsLog == null || smsLog.getTimeList().size() <= 0) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}
		if (StringUtils.isEmpty(smsLog.getMobileCode()) || !smsLog.getMobileCode().equals(mobilecode)) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}
		if (!mobile.equals(smsLog.getMobile())) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}
		if (DateUtil.getDateline() - smsLog.getTimeList().get(smsLog.getTimeList().size() - 1) > 10 * 60) {
			return JsonResultUtil.getErrorJson("短信验证码已过期，请您重新发送！");
		}

		member.setUname(mobile);
		member.setMobile(mobile);

		if (StringUtil.isEmpty(password)) {
			return JsonResultUtil.getErrorJson("密码不能为空！");
		}
		if (password.length() < 6 || password.length() > 12) {
			return JsonResultUtil.getErrorJson("密码长度为6到12位！");
		}
		if (memberManager.checkname(mobile) > 0 || memberManager.checkMobile(mobile) > 0) {
			return JsonResultUtil.getErrorJson("此手机号码已被注册，请您选择另外的用户名!");
		}
		member.setPassword(password);
		member.setEmail(mobile);
		member.setRegisterip(registerip);

		if (memberManager.register(member) != 1) {
			return JsonResultUtil.getErrorJson("手机号码 [" + member.getUname() + "] 已被注册!");
		}
		ThreadContextHolder.getHttpRequest().getSession().removeAttribute("mobileCode");
		ThreadContextHolder.getHttpRequest().getSession().removeAttribute("mobile");

		this.memberManager.login(mobile, password);
		String cookieValue = EncryptionUtil1.authcode(
				"{username:\"" + mobile + "\",password:\"" + StringUtil.md5(password) + "\"}", "ENCODE", "", 0);
		HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(), "JavaShopUser", cookieValue, 60 * 60 * 24 * 14);

		member = UserConext.getCurrentMember();

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("username", member.getUname());
		map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
		map.put("level", member.getLvname());

		map.put("imuser", "");
		map.put("impass", "");

		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 发送注册短信验证码
	 * 
	 * @param mobile
	 *            手机号码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/send-register-code", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult sendRegisterCode(String mobile) {
		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("系统参数错误！");
		}

		// 验证是否已被注册
		if (memberManager.checkname(mobile) > 0 || memberManager.checkMobile(mobile) > 0) {
			return JsonResultUtil.getErrorJson("此手机号码已被注册!");
		}

		// 验证是否发送过频
		SmsLog smsLog = (SmsLog) SmsLog.cache.get(mobile);
		if (smsLog != null) {
			long lasttime = smsLog.getTimeList().get(smsLog.getTimeList().size() - 1);
			if (DateUtil.getDateline() - lasttime < 60) {
				return JsonResultUtil.getErrorJson("发送短信太频繁，请您稍后再试！");
			}
			if (smsLog.getTimeList().size() >= 6) {
				long pretime = smsLog.getTimeList().get(smsLog.getTimeList().size() - 6);
				if (DateUtil.getDateline() - pretime < 60 * 60) {
					return JsonResultUtil.getErrorJson("发送短信太频繁，请您稍后再试！");
				}
			}
		}

		try {
			String mobileCode = "" + (int) ((Math.random() * 9 + 1) * 100000);

			if (smsLog != null) {
				smsLog.setMobileCode(mobileCode);
			} else {
				smsLog = new SmsLog(mobile, mobileCode);
			}
			smsLog.getTimeList().add(DateUtil.getDateline());
			SmsLog.cache.put(mobile, smsLog);

			String content = SMS_PREFIX + "您的验证码为：【" + mobileCode + "】";
			System.out.println(content);
			try {
				this.smsManager.send(mobile, content, new HashMap());
			} catch (Exception ex) {
			}
			return JsonResultUtil.getSuccessJson("发送成功");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("发送失败");
		}
	}

	/**
	 * 发送找回密码短信验证码
	 * 
	 * @param mobile
	 *            手机号码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/send-find-code", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult sendFindPassCode(String mobile) {
		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("请您输入正确的手机号码！");
		}

		// 验证是否已被注册
		if (memberManager.checkMobile(mobile) <= 0) {
			return JsonResultUtil.getErrorJson("此手机号码不存在!");
		}

		// 验证是否发送过频
		SmsLog smsLog = (SmsLog) SmsLog.cache.get(mobile);
		if (smsLog != null) {
			long lasttime = smsLog.getTimeList().get(smsLog.getTimeList().size() - 1);
			if (DateUtil.getDateline() - lasttime < 60) {
				return JsonResultUtil.getErrorJson("发送短信太频繁，请您稍后再试！");
			}
			if (smsLog.getTimeList().size() >= 6) {
				long pretime = smsLog.getTimeList().get(smsLog.getTimeList().size() - 6);
				if (DateUtil.getDateline() - pretime < 60 * 60) {
					return JsonResultUtil.getErrorJson("发送短信太频繁，请您稍后再试！");
				}
			}
		}

		try {
			String mobileCode = "" + (int) ((Math.random() * 9 + 1) * 100000);
			if (smsLog != null) {
				smsLog.setMobileCode(mobileCode);
			} else {
				smsLog = new SmsLog(mobile, mobileCode);
			}
			smsLog.getTimeList().add(DateUtil.getDateline());
			SmsLog.cache.put(mobile, smsLog);

			String content = SMS_PREFIX + "您的验证码为：【" + mobileCode + "】";
			System.out.println(content);
			try {
				this.smsManager.send(mobile, content, new HashMap());
			} catch (Exception ex) {
			}
			return JsonResultUtil.getSuccessJson("发送成功");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("发送失败");
		}
	}

	/**
	 * 验证手机短信验证码
	 * 
	 * @param mobile
	 *            手机号码
	 * @param mobilecode
	 *            手机验证码
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/valid-mobile", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult validMobile(String mobile, String mobilecode) {
		if (StringUtil.isEmpty(mobile) || !ValidateUtils.isMobile(mobile)) {
			return JsonResultUtil.getErrorJson("请输入正确的手机号码！");
		}

		// 验证短信验证码
		if (StringUtil.isEmpty(mobilecode)) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}

		SmsLog smsLog = (SmsLog) SmsLog.cache.get(mobile);
		if (smsLog == null || smsLog.getTimeList().size() <= 0) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}
		if (StringUtils.isEmpty(smsLog.getMobileCode()) || !smsLog.getMobileCode().equals(mobilecode)) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}
		if (!mobile.equals(smsLog.getMobile())) {
			return JsonResultUtil.getErrorJson("短信验证码不正确！");
		}
		if (DateUtil.getDateline() - smsLog.getTimeList().get(smsLog.getTimeList().size() - 1) > 10 * 60) {
			return JsonResultUtil.getErrorJson("短信验证码已过期，请您重新发送！");
		}
		return JsonResultUtil.getSuccessJson("验证成功");
	}

	/**
	 * 获取当前登录的用户信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult info() {

		Map map = new HashMap<String, String>();
		;
		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("请您登录后再进行此项操作！");
			}
			// map = new HashMap<String, String>();
			// 基本信息
			map.put("nick_name", member.getNickname());
			map.put("username", member.getUname());
			map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
			map.put("level_id", member.getLv_id());
			map.put("level", member.getLvname());
			map.put("name", member.getName());
			map.put("sex", member.getSex());
			map.put("birthday", member.getBirthday());
			map.put("province", member.getProvince());
			map.put("province_id", member.getProvince_id());
			map.put("city", member.getCity());
			map.put("city_id", member.getCity_id());
			map.put("region", member.getRegion());
			map.put("region_id", member.getRegion_id());
			map.put("address", member.getAddress());
			map.put("zip", member.getZip());
			map.put("mobile", member.getMobile());
			map.put("tel", member.getTel());
			map.put("account_blance", member.getAccount_balance()); // 账户余额
			map.put("unique_code", member.getUnique_code()); // 唯一邀请码
			map.put("recommend_unique_code", member.getRecommend_unique_code()); // 推荐人唯一邀请码
			map.put("member_level", member.getMember_level()); // 会员级别 0 暂无 1
																// 小蜜蜂 2 大黄蜂 3
																// 蜂王
			map.put("unique_time", member.getUnique_time()); // 邀请成功日期
			map.put("new_point", member.getNew_point());

			// 扩展信息
			map.put("favoriteCount", apiFavoriteManager.count(member.getMember_id())); // 收藏的商品
			map.put("favoriteStoreCount", apiFavoriteManager.storeCount(member.getMember_id())); // 关注的店铺
			map.put("point", member.getPoint()); // 等级积分
			map.put("mp", member.getMp()); // 消费积分
			map.put("paymentOrderCount", apiOrderManager.count(OrderStatus.ORDER_NOT_PAY, member.getMember_id())); // 待付款订单数
			map.put("shippingOrderCount", apiOrderManager.count(OrderStatus.ORDER_SHIP, member.getMember_id())); // 待收货订单数
			map.put("commentOrderCount", apiOrderManager.commentGoodsCount(member.getMember_id())); // 待评论订单数
			map.put("returnedOrderCount", apiOrderManager.returnedCount(member.getMember_id())); // 退换货订单数

			// 仁济
			map.put("type", member.getType().toString());// 0:大号、1:资源方、2:创客、3:店主、4:分享客

			map.put("fenxiaoUrl", "/mobile/withdraw/withdraw-index.html");
			map.put("inviteUrl", "/mobile/storeopen/open_store.html?is_used=-1");

			Store store = storeManager.getStoreByMember(member.getMember_id());
			if (store != null) {
				Boolean isPay = true;
				if (store.getPay_order_id() != null) {
					StoreOrder storeOrder = storeOrderManager.get(store.getPay_order_id());
					if (storeOrder != null && storeOrder.getPay_status() == 0) {
						map.put("order_id", store.getPay_order_id());
						isPay = false;
					}
				}
				if (store.getDisabled() == -1) {
					map.put("disabled", 2);
				} else if (store.getDisabled() == 0 && isPay) {
					map.put("disabled", 0);
				} else {
					map.put("disabled", 1);
				}
			}

			if (store == null) {
				map.put("storeId", "");
			} else {
				map.put("storeId", store.getStore_id().toString());
				map.put("storeUrl", "/mobile/storenav.html");
			}
			map.put("centerUrl", "/mobile/member_and/huiyuanzhongxin.html?uname=" + member.getUname());
			Page webPage = this.storeCollectManager.getList(member.getMember_id(), 1, 10);
			map.put("collectNum", webPage.getTotalCount()); // xulipeng 关注店铺的数量
		} catch (Exception e) {
			logger.error("获取用户信息出错：" + e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 修改用户信息
	 * 
	 * @param member
	 *            会员实体
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult save(@ModelAttribute Member member,
			@RequestParam(value = "photo", required = false) MultipartFile photo) {
		if (UserConext.getCurrentMember() == null) {
			return JsonResultUtil.getErrorJson("请您先登录后再修改资料！");
		}
		Member dbMember = memberManager.get(UserConext.getCurrentMember().getMember_id());

		// 先上传图片
		String faceField = "faceFile";
		if (photo != null) {
			// 判断文件类型
			if (!FileUtil.isAllowUpImg(photo.getOriginalFilename())) {
				return JsonResultUtil.getErrorJson("头像文件格式不正确，请上传jpg或png的格式文件！");
			}

			// 判断文件大小
			if (photo.getSize() > 2000 * 1024) {
				return JsonResultUtil.getErrorJson("头像图片不能大于2Mb！");
			}

			InputStream stream = null;
			try {
				stream = photo.getInputStream();
			} catch (Exception e) {
				e.printStackTrace();
				return JsonResultUtil.getErrorJson("保存资料失败，请您重试！");
			}

			IUploader uploader = UploadFacatory.getUploaer();
			String imgPath = uploader.upload(stream, faceField, photo.getOriginalFilename());
			dbMember.setFace(imgPath);
		}

		if (member.getBirthday() == null || member.getBirthday() == 0) {
			dbMember.setBirthday(19700101L);
		}

		if (StringUtil.isEmpty(member.getMobile())) {
			return JsonResultUtil.getErrorJson("手机号码不能为空！");
		}

		if (!ValidateUtils.isMobile(member.getMobile())) {
			return JsonResultUtil.getErrorJson("手机号码格式不正确！");
		}

		dbMember.setName(member.getName());
		dbMember.setSex(member.getSex());
		dbMember.setBirthday(member.getBirthday());
		dbMember.setProvince(member.getProvince());
		dbMember.setProvince_id(member.getProvince_id());
		dbMember.setCity(member.getCity());
		dbMember.setCity_id(member.getCity_id());
		dbMember.setRegion(member.getRegion());
		dbMember.setRegion_id(member.getRegion_id());
		dbMember.setAddress(member.getAddress());
		dbMember.setMobile(member.getMobile());
		dbMember.setZip(member.getZip());
		dbMember.setTel(member.getTel());

		try {
			this.memberManager.edit(dbMember);
			return JsonResultUtil.getSuccessJson("保存资料成功！");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("保存资料失败，请您重试！");
		}
	}

	/**
	 * 积分明细
	 * 
	 * @param page
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/point-history", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult pointHistory(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
		}
		if (page == null || page <= 0)
			page = 1;
		Page pointHistoryPage = pointHistoryManager.pagePointHistory(Integer.valueOf(page), PAGE_SIZE);
		List list = (List) pointHistoryPage.getResult();
		return JsonResultUtil.getObjectJson(list);
	}

	/**
	 * 优惠券列表
	 * 
	 * @param type
	 *            1:未使用; 2:已使用; 3:已过期
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/bonus", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult bonus(@RequestParam(value = "type", required = false, defaultValue = "1") Integer type,
			@RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
		}
		Page webPage = apiMemberManager.getBonusListByMemberid(member.getMember_id(), type, page, PAGE_SIZE);
		return JsonResultUtil.getObjectJson(webPage.getResult());
	}

	/**
	 * 获取优惠券数量
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/bonus-count", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult bonusCount() {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("unused_count", apiMemberManager.getBounsCountByMemberId(member.getMember_id(), 1));
		map.put("used_count", apiMemberManager.getBounsCountByMemberId(member.getMember_id(), 2));
		map.put("expired_count", apiMemberManager.getBounsCountByMemberId(member.getMember_id(), 3));
		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 立即开店说明
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/shop", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult shop() {
		/*
		 * Member member =UserConext.getCurrentMember(); if (member == null) {
		 * return JsonResultUtil.getErrorJson("请登录后再进行此项操作！"); }
		 */
		Map<String, Object> map = new HashMap<>();
		map.put("advUrl", "http://static.b2b2cv2.javamall.com.cn/attachment/adv/201509111204145384.jpg");
		map.put("contentA", "会员在商城开店后，可在自己的店铺内购买商品，可获得店铺利润。");
		map.put("contentB", "会员在商城开店后，可在自己的店铺内购买商品，可获得店铺利润。");
		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 积分转余额 - 曹梦林 pointToadvance()
	 * 
	 * @Param Double point 积分
	 **/
	@ResponseBody
	@RequestMapping(value = "/pointoadvance", produces = MediaType.APPLICATION_JSON_VALUE)

	public String pointToadvance(Double point) {
		Member member = UserConext.getCurrentMember();
		return this.memberPointManger.newPointToAdvance(member.getMember_id(), point);
	}

	/**
	 * 获取用户余额 - 曹梦林 getBalance()
	 */
	@ResponseBody
	@RequestMapping(value = "/balance", produces = MediaType.APPLICATION_JSON_VALUE)

	public JsonResult getBalance() {
		try {
			Member member = UserConext.getCurrentMember();
			if (null == member) {
				return JsonResultUtil.getErrorJson("未登录");
			}
			Member newmember = this.memberManager.get(member.getMember_id());
			return JsonResultUtil.getSuccessJson("" + newmember.getAccount_balance());
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("获取用户余额信息失败");
		}

	}

	/**
	 * 提现 - 曹梦林 forward()
	 * 
	 * @param num
	 *            提现金额
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/forward", produces = MediaType.APPLICATION_JSON_VALUE)

	public JsonResult forward(Double num) {
		try {

			Member member = UserConext.getCurrentMember();
			if (null == member) {
				return JsonResultUtil.getErrorJson("未登录");
			}
			if (num < 100) {
				return JsonResultUtil.getErrorJson("提现金额不足100元");
			}
			String result = this.memberManager.forward(member.getMember_id(), num);
			if (result == "操作失败") {
				return JsonResultUtil.getErrorJson("操作失败，余额不足");
			}
			return JsonResultUtil.getSuccessJson("操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("获取用户银行卡信息失败");
		}

	}

	/**
	 * 设置用户取现银行卡 - 曹梦林 setBank()
	 * 
	 * @param num
	 *            银行卡号
	 * @param bank_name
	 *            银行名称
	 * @param bank_icon
	 *            银行图标
	 * @param zh_name
	 *            支行名称
	 * @param cardholder
	 *            持卡人
	 * @param bank_id
	 *            银行id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/setBank", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult setBank(String num, String bank_name, String bank_icon, String zh_name, String cardholder,
			Integer bank_id) {

		try {
			Member member = UserConext.getCurrentMember();
			if (null == member || null == num || null == bank_name || null == cardholder || null == zh_name) {
				return JsonResultUtil.getErrorJson("请填写完整信息！");
			}
			if (null == bank_id) {
				bank_id = 0;
			}
			Map datas = new HashMap();
			datas.put("member_id", member.getMember_id());
			datas.put("num", num);
			datas.put("bank_icon", bank_icon);
			datas.put("zh_name", zh_name);
			datas.put("bank_name", bank_name);
			datas.put("cardholder", cardholder);
			datas.put("bank_id", bank_id);
			this.memberManager.setBank(datas);
			return JsonResultUtil.getSuccessJson("操作成功");

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("设置银行失败");
		}
	}

	/**
	 * 获取用户银行卡信息 - 曹梦林 getBank()
	 * 
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/getBank", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getBank() {
		try {
			Member member = UserConext.getCurrentMember();
			// System.out.println(member.getMember_id());
			if (null == member) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			Integer memberId = member.getMember_id();
			MemberBank list = this.memberManager.getBank(memberId);
			// System.out.println(list);
			return JsonResultUtil.getObjectJson(list);
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("获取用户银行卡信息失败");
		}

	}

	/**
	 * 获取银行列表 - 曹梦林 getBankList()
	 **/
	@ResponseBody
	@RequestMapping(value = "/getBankList", produces = MediaType.APPLICATION_JSON_VALUE)

	public JsonResult getBankList() {

		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			// 设置默认值，没用
			// pageNo=pageNo==null?1:pageNo;
			// pageSize=pageSize==null?12:pageSize;
			return JsonResultUtil.getObjectJson(this.memberManager.getBankList());
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("获取记录失败");
		}
		// return null;
	}

	/**
	 * 获取提现记录 - 曹梦林 getForwardList()
	 **/
	@ResponseBody
	@RequestMapping(value = "/getFowardList", produces = MediaType.APPLICATION_JSON_VALUE)

	public JsonResult getFowardList(Integer pageNo, Integer pageSize) {

		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			// 设置默认值，没用
			// pageNo=pageNo==null?1:pageNo;
			// pageSize=pageSize==null?12:pageSize;
			return JsonResultUtil
					.getObjectJson(this.memberManager.getForwardList(member.getMember_id(), pageNo, pageSize));
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("获取记录失败");
		}
		// return null;
	}

	/**
	 * 登录送积分
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/login-add-point", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult loginAddPoint() {
		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			Map<String, Object> pointMap = this.memberPointManger.addPoint(member.getMember_id());
			return JsonResultUtil.getObjectJson(pointMap);
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("登录送积分失败");
		}
	}

	/**
	 * 积分记录
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list-point-record", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult pointRecord(int pageNo, int pageSize) {
		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			Page mobilePage = this.memberPointManger.getPointRecord(member.getMember_id(), pageNo, pageSize);
			return JsonResultUtil.getObjectJson(mobilePage);
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("积分记录获取失败");
		}
	}

	/**
	 * 余额列表-财务中心
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/balanceList", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult balanceList(Integer pageNo, Integer pageSize) {

		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			List datas = this.memberManager.getBalanceRecord(member.getMember_id(), pageNo, pageSize);
			return JsonResultUtil.getObjectJson(datas);
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("获取数据失败");
		}
	}

	/**
	 * 抽奖操作 - 曹梦林 lottery() return
	 */

	@ResponseBody
	@RequestMapping(value = "/lottery", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult lottery(Integer l_type) {
		if (l_type == null) {
			return JsonResultUtil.getErrorJson("抽奖失败");
		}
		Member member = UserConext.getCurrentMember();
		if (null == member) {
			return JsonResultUtil.getErrorJson("请先登录");
		}
		Object resNum = "0";
		Map result;
		if (l_type == 1) {
			// member.getMember_id()
			result = this.memberManager.lottery(member.getMember_id());
			resNum = result.get("result").toString();
		} else {
			result = this.memberManager.lotteryPoint(member.getMember_id());
			resNum = result.get("result").toString();
		}
		if (resNum.equals("0")) {
			return JsonResultUtil.getErrorJson((String) result.get("message"));
		} else {
			result.remove("result");
			return JsonResultUtil.getObjectJson(result);
		}
	}

	/**
	 * @author nieruijie
	 * @date 2018-7-6
	 * @param lottery_type
	 *            奖品状态3，是转盘奖品
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/turntablelottery", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult turntableLottery(HttpServletRequest request) {
		String appphone = request.getParameter("appphone");
		try {
			Member member = UserConext.getCurrentMember();
			// 1.判断用户登陆
			if (member == null) {
				return JsonResultUtil.getErrorJson("未登录!");
			}
			// 2.抽奖
			Object resNum = "0";
			Map result;
			result = this.memberManager.turntablelottery(member.getMember_id(),appphone);
			resNum = result.get("result").toString();
			if (resNum.equals("0")) {
				return JsonResultUtil.getErrorJson((String) result.get("message"));
			} else {
				result.remove("result");
				return JsonResultUtil.getObjectJson(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("获取数据失败");
		}
	}

	/**
	 * 会员剩余积分
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/newpoint", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult newPoint() {
		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			Map<String, Double> newPoint = new HashMap<String, Double>();
			newPoint.put("point", member.getNew_point());
			return JsonResultUtil.getObjectJson(newPoint);
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("积分获取失败");
		}
	}

	/**
	 * 奖品
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/award", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getAward(int lotteryType) {
		List listAward = this.memberManager.getAward(lotteryType);
		return JsonResultUtil.getObjectJson(listAward);
	}

	/**
	 * 转盘奖品，查询全部奖品
	 * 
	 * @author nieruijie
	 * @param lotteryType
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/turntableaward", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getTurntableAward(int lotteryType) {
		List listAward = this.memberManager.getAward(lotteryType);
		return JsonResultUtil.getObjectJson(listAward);
	}

	/**
	 * 查询转盘奖品详细
	 * 
	 * @author nieruijie
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/turntableawardid", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getTurntableAwardID(int aid) {
		List listAward = this.memberManager.getTurntableAwardID(aid);
		return JsonResultUtil.getObjectJson(listAward);
	}

	/**
	 * 中奖记录
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/list-win-record", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult winRecord(int winType, int pageNo, int pageSize) {
		Page mobilePage = this.memberManager.getWinRecord(winType, pageNo, pageSize);
		List<Map> recordList = (List<Map>) mobilePage.getResult();
		for (Map record : recordList) {
			if (!StringUtil.isEmpty(record.get("face").toString())) {
				record.put("face", StaticResourcesUtil.convertToUrl(record.get("face").toString()));
			}
		}
		return JsonResultUtil.getObjectJson(mobilePage);
	}

	/**
	 * 添加收货人信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/consigneeinfo", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult winRecord(Integer winId, String consigneeName, String consigneePhone, String consigneeAddress) {
		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			if (consigneeName == null) {
				return JsonResultUtil.getErrorJson("请填写收货人姓名");
			}
			if (consigneePhone == null) {
				return JsonResultUtil.getErrorJson("请填写收货人电话");
			}
			if (consigneeAddress == null) {
				return JsonResultUtil.getErrorJson("请填写收货人地址");
			}
			this.memberManager.addConsigneeInfo(winId, consigneeName, consigneePhone, consigneeAddress);
			return JsonResultUtil.getSuccessJson("成功");
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("失败");
		}
	}

	/**
	 * 抽奖次数
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/lottery-number", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult lotteryNumberCount() {
		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			int lotteryNumber = this.memberManager.lotteryNumberCount(member.getMember_id());
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("lotteryNumber", lotteryNumber);
			return JsonResultUtil.getObjectJson(map);
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("剩余抽奖次数获取失败");
		}
	}

	/**
	 * 是否有未领取奖品
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/is-get-award", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult isGetAward() {
		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			Map map = this.memberManager.isGetAward(member.getMember_id());

			Integer res = (Integer) map.get("mark");
			if (res.intValue() == 0) {
				return JsonResultUtil.getErrorJson("没有未领取的实物奖品");
			}

			return JsonResultUtil.getObjectJson(map);
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("操作失败");
		}
	}

	/**
	 * 获取抽奖所需积分
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/get-lottery-point", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getLotteryPoint() {
		try {
			Double pointNum = this.memberPointManger.getLotteryPoint();
			Map<String, Double> map = new HashMap<String, Double>();
			map.put("lotterypoint", pointNum);
			return JsonResultUtil.getObjectJson(map);
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("获取失败");
		}

	}

	/**
	 * 获取抽奖次数
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/get-lottery-count", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getLotteryCount(Integer orderId) {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("您没有登录或登录过期！");
		}
		if (orderId == null) {
			return JsonResultUtil.getErrorJson("此订单不存在！");
		}
		try {
			int lotteryCount = this.memberPointManger.getLotteryCount(orderId);
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("lotterycount", lotteryCount);
			return JsonResultUtil.getObjectJson(map);
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("获取失败");
		}

	}

	/**
	 * 抽奖次数(转盘)
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/turntable-lottery-number", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult turntableLotteryNumber() {
		try {
			Member member = UserConext.getCurrentMember();
			if (member == null) {
				return JsonResultUtil.getErrorJson("对不起，会员未登录");
			}
			Integer lotteryNumber = this.memberManager.turntableLotteryNumber(member.getMember_id());
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("turntableLotteryNumber", lotteryNumber);
			return JsonResultUtil.getObjectJson(map);
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("操作失败：", e);
			return JsonResultUtil.getErrorJson("抽奖次数获取失败");
		}
	}
}
