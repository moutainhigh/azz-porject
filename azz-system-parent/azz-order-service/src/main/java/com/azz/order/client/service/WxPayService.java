/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年11月26日 下午3:15:10
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/

package com.azz.order.client.service;
/**
 * <P>TODO</P>
 * @version 1.0
 * @author 刘建麟  2018年11月26日 下午3:15:10
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSONObject;
import com.azz.core.common.JsonResult;
import com.azz.core.common.errorcode.JSR303ErrorCode;
import com.azz.core.common.errorcode.SystemErrorCode;
import com.azz.core.constants.ClientConstants;
import com.azz.core.constants.ClientConstants.PayMethod;
import com.azz.core.constants.PayConstants;
import com.azz.core.constants.WxActivityConstants;
import com.azz.core.constants.WxCourseConstants.CourseOrderStatus;
import com.azz.exception.JSR303ValidationException;
import com.azz.order.client.mapper.WxPayMapper;
import com.azz.order.client.wx.bo.WxPayOrderInfo;
import com.azz.order.client.wx.pojo.WxPay;
import com.azz.util.HttpClientUtil;
import com.azz.wx.course.api.OrderService;
import com.azz.wx.course.api.SignUpService;
import com.azz.wx.course.pojo.bo.CallBackParam;
import com.azz.wx.course.pojo.bo.SearchActivityInfoParam;
import com.azz.wx.course.pojo.vo.ActivityInfo;
import com.azz.wx.course.pojo.vo.ActivityPayOrderInfo;
import com.azz.wx.course.pojo.vo.CourseOrderDetail;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayConstants.SignType;
import com.github.wxpay.sdk.WXPayUtil;



@Service
public class WxPayService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	private static SignType st;
	
	@Value("${wx.AppId}")
	private String appid;

	@Value("${wx.secrt}")
	private String secrt;

	@Value("${wx.mchId}")
	private String mchid;

	@Value("${wx.Api}")
	private String api;
	
	@Value("${wx.callback}")
	private String callback;
	
	@Value("${wx.activecallback}")
	private String activecallback;
	
	static {
		st = SignType.MD5;
	}

	
	@Autowired
	private WxPayMapper wpm;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private SignUpService signUpService;
	
	/**
	 * 微信支付 统一下单
	 * @param po
	 * @return
	 */
	@Transactional
	public Map<String,String> submitOrderPay(@RequestBody WxPayOrderInfo po) {
		WXPayUtil.getLogger().info("openid--------->" + po.getOpenid());
		//根据课程编号去判断该笔订单是否已支付
		List<WxPay> wxPays = wpm.selectWxOrder(po.getCoursePayNum());
		if(!wxPays.isEmpty()) {
			for (WxPay wxPay2 : wxPays) {
				if(wxPay2.getOrderStatus() == ClientConstants.PayStatus.PAY_CLOSED.getValue()) {
					throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "订单已关闭");
				}
				if(wxPay2.getOrderStatus() == ClientConstants.PayStatus.PAY_SUCCESS.getValue()) {
					throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "订单已完成");
				}
			}
		}
		JsonResult<CourseOrderDetail> detail = orderService.getCourseOrderDetail(po.getCoursePayNum());
		//判断微信课程订单是否关闭
		if(detail != null && detail.getCode() == SystemErrorCode.SUCCESS.getCode()) {
			if(detail.getData().getOrderStatus() == CourseOrderStatus.CLOSED.getValue()) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "订单已失效，请重新下单");
			}
			WXPayUtil.getLogger().info("po.getOrderMoney()=====>"+po.getOrderMoney());
			WXPayUtil.getLogger().info("po.toPlainString()=====>"+detail.getData().getGrandTotal().toPlainString());
			if(!po.getOrderMoney().equals(detail.getData().getGrandTotal().toPlainString())) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "订单金额不一致");
			}
		}
		Map<String, String> reqData = null;
		try {
			reqData = createReqData(detail.getData().getGrandTotal(),detail.getData().getCourseInfo(),po);
		}catch (Exception e) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "创建订单错误");
		}
		if (null == reqData) {
			return null;
		}
		String reqUrl = "https://"+WXPayConstants.DOMAIN_API + WXPayConstants.UNIFIEDORDER_URL_SUFFIX;
		String mapToXml = "";
		try {
			mapToXml = WXPayUtil.mapToXml(reqData);
		} catch (Exception e) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "请求参数有误");
		}
		if (StringUtils.isBlank(mapToXml)) {
			return null;
		}
		WXPayUtil.getLogger().info("reqUrl------------->" + reqUrl);
		WXPayUtil.getLogger().info("mapToXml------------->" + mapToXml);
		String result = "";
		try {
			result = HttpClientUtil.sendPost(reqUrl, mapToXml);
		} catch (Exception e) {
			WXPayUtil.getLogger().info(e.getMessage());
			e.printStackTrace();
		}
		WXPayUtil.getLogger().info("统一下单请求返回结果------------->" + result);
		String prepay_id = "";// 预支付id
		Map<String, String> payMap = new HashMap<String, String>();
		try {
			if (result.indexOf("SUCCESS") != -1) {
				Map<String, String> map = WXPayUtil.xmlToMap(result);
				prepay_id = (String) map.get("prepay_id");
			}
			payMap.put("appId", appid);
			payMap.put("timeStamp", WXPayUtil.getCurrentTimestamp() + "");
			payMap.put("nonceStr", WXPayUtil.generateNonceStr());
			payMap.put("signType", "MD5");
			payMap.put("package", "prepay_id=" + prepay_id);
			String paySign = WXPayUtil.generateSignature(payMap, api);
			payMap.put("paySign", paySign);
			
			return payMap;
		}catch (Exception e) {
			WXPayUtil.getLogger().error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 微信支付 统一下单
	 * @param po
	 * @return
	 */
	@Transactional
	public Map<String,String> submitWxActiveOrderPay(@RequestBody WxPayOrderInfo po) {
		WXPayUtil.getLogger().info("openid--------->" + po.getOpenid());
		//根据课程编号去判断该笔订单是否已支付
		List<WxPay> wxPays = wpm.selectWxOrder(po.getCoursePayNum());
		if(!wxPays.isEmpty()) {
			for (WxPay wxPay2 : wxPays) {
				if(wxPay2.getOrderStatus() == ClientConstants.PayStatus.PAY_CLOSED.getValue()) {
					throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "订单已关闭");
				}
				if(wxPay2.getOrderStatus() == ClientConstants.PayStatus.PAY_SUCCESS.getValue()) {
					throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "订单已完成");
				}
			}
		}
		JsonResult<ActivityPayOrderInfo> detail = signUpService.getActivityOrder(po.getCoursePayNum());
		//WXPayUtil.getLogger().info("ActivityPayOrderInfo=======>" + JSONObject.toJSONString(detail));
		//判断微信课程订单是否关闭
		if(detail != null && detail.getCode() == SystemErrorCode.SUCCESS.getCode()) {
			if(detail.getData().getOrderStatus() == WxActivityConstants.OrderStatus.CLOSED.getValue()) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "订单已失效，请重新下单");
			}
			WXPayUtil.getLogger().info("po.getOrderMoney()=====>"+po.getOrderMoney());
			WXPayUtil.getLogger().info("po.toPlainString()=====>"+detail.getData().getPrice().toPlainString());
			if(!po.getOrderMoney().equals(detail.getData().getPrice().toPlainString())) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "订单金额不一致");
			}
		}
		Map<String, String> reqData = null;
		try {
			reqData = createActiveReqData(detail.getData().getPrice(),detail.getData().getActivityName(),po);
		}catch (Exception e) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "创建订单错误");
		}
		if (null == reqData) {
			return null;
		}
		String reqUrl = "https://"+WXPayConstants.DOMAIN_API + WXPayConstants.UNIFIEDORDER_URL_SUFFIX;
		String mapToXml = "";
		try {
			mapToXml = WXPayUtil.mapToXml(reqData);
		} catch (Exception e) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "请求参数有误");
		}
		if (StringUtils.isBlank(mapToXml)) {
			return null;
		}
		WXPayUtil.getLogger().info("reqUrl------------->" + reqUrl);
		WXPayUtil.getLogger().info("mapToXml------------->" + mapToXml);
		String result = "";
		try {
			result = HttpClientUtil.sendPost(reqUrl, mapToXml);
		} catch (Exception e) {
			WXPayUtil.getLogger().info(e.getMessage());
			e.printStackTrace();
		}
		WXPayUtil.getLogger().info("统一下单请求返回结果------------->" + result);
		String prepay_id = "";// 预支付id
		Map<String, String> payMap = new HashMap<String, String>();
		try {
			if (result.indexOf("SUCCESS") != -1) {
				Map<String, String> map = WXPayUtil.xmlToMap(result);
				prepay_id = (String) map.get("prepay_id");
			}
			payMap.put("appId", appid);
			payMap.put("timeStamp", WXPayUtil.getCurrentTimestamp() + "");
			payMap.put("nonceStr", WXPayUtil.generateNonceStr());
			payMap.put("signType", "MD5");
			payMap.put("package", "prepay_id=" + prepay_id);
			String paySign = WXPayUtil.generateSignature(payMap, api);
			payMap.put("paySign", paySign);
			
			return payMap;
		}catch (Exception e) {
			WXPayUtil.getLogger().error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 创建请求参数
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private Map<String,String> createReqData(BigDecimal totalFee,String info,WxPayOrderInfo po) throws Exception{
		Map<String,String> reqMap = new HashMap<String,String>();
		reqMap.put("appid", appid);
		reqMap.put("mch_id", mchid);
		reqMap.put("nonce_str", WXPayUtil.generateNonceStr());
		reqMap.put("sign_type", WXPayConstants.MD5);
		reqMap.put("body", po.getCourseName());
		reqMap.put("out_trade_no", String.valueOf(WXPayUtil.getCurrentTimestamp()));
		BigDecimal multiply = totalFee.multiply(new BigDecimal(100));
		String money = multiply.toPlainString();
		String fenMoney = money.substring(0,money.indexOf("."));
		reqMap.put("total_fee", fenMoney); //单位分
		reqMap.put("spbill_create_ip",po.getIp());
		reqMap.put("notify_url", callback);
		reqMap.put("trade_type", "JSAPI");
		if(StringUtils.isBlank(po.getOpenid())) {
			return null;
		}
		reqMap.put("openid", po.getOpenid());
		reqMap.put("sign", WXPayUtil.generateSignature(reqMap, api, st));
		WxPay wp = new WxPay();
		wp.setCourseName(po.getCourseName());
		wp.setCourseNum(po.getCourseNum());
		wp.setCoursePayNum(po.getCoursePayNum());
		wp.setCreateIp(po.getIp());
		LocalDateTime now = LocalDateTime.now();
		wp.setCreateOrderTime(now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
		wp.setFeeType("CNY");
		wp.setGoodsBody(po.getCourseName());
		wp.setGoodsInfo(info);
		wp.setOrderStatus(ClientConstants.PayStatus.NOT_PAID.getValue());
		wp.setOutTradeNo(reqMap.get("out_trade_no"));
		wp.setTotalFee(fenMoney);
		int i = wpm.insertPay(wp);
		if(i != 1) {
			return null;
		}
		return reqMap;
	}
	
	
	/**
	 * 创建请求参数
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private Map<String,String> createActiveReqData(BigDecimal totalFee,String info,WxPayOrderInfo po) throws Exception{
		Map<String,String> reqMap = new HashMap<String,String>();
		reqMap.put("appid", appid);
		reqMap.put("mch_id", mchid);
		reqMap.put("nonce_str", WXPayUtil.generateNonceStr());
		reqMap.put("sign_type", WXPayConstants.MD5);
		reqMap.put("body", po.getCourseName());
		reqMap.put("out_trade_no", String.valueOf(WXPayUtil.getCurrentTimestamp()));
		BigDecimal multiply = totalFee.multiply(new BigDecimal(100));
		String money = multiply.toPlainString();
		String fenMoney = money.substring(0,money.indexOf("."));
		reqMap.put("total_fee", fenMoney); //单位分
		reqMap.put("spbill_create_ip",po.getIp());
		reqMap.put("notify_url", activecallback);
		reqMap.put("trade_type", "JSAPI");
		if(StringUtils.isBlank(po.getOpenid())) {
			return null;
		}
		reqMap.put("openid", po.getOpenid());
		reqMap.put("sign", WXPayUtil.generateSignature(reqMap, api, st));
		WxPay wp = new WxPay();
		wp.setCourseName(po.getCourseName());
		wp.setCourseNum(po.getCourseNum());
		wp.setCoursePayNum(po.getCoursePayNum());
		wp.setCreateIp(po.getIp());
		LocalDateTime now = LocalDateTime.now();
		wp.setCreateOrderTime(now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
		wp.setFeeType("CNY");
		wp.setGoodsBody(po.getCourseName());
		wp.setGoodsInfo(info);
		wp.setOrderStatus(ClientConstants.PayStatus.NOT_PAID.getValue());
		wp.setOutTradeNo(reqMap.get("out_trade_no"));
		wp.setTotalFee(fenMoney);
		int i = wpm.insertPay(wp);
		if(i != 1) {
			return null;
		}
		return reqMap;
	}
	
	/**
	 * 微信支付回调
	 * @param xml
	 * @return
	 */
	public String callback(String xml) {
		String resXml = "";
		try {
			//解析XML
			Map<String, String> map = WXPayUtil.xmlToMap(xml);
	        String return_code = map.get("return_code");//状态
	        String out_trade_no = map.get("out_trade_no");//订单号
			if (return_code.equals("SUCCESS")) {
				if (out_trade_no != null) {
					//处理订单逻辑
					String transaction_id = map.get("transaction_id");
					String time_end = map.get("time_end");
					String device_info = map.get("device_info");
					String total_fee = map.get("total_fee");
					String sign = map.get("sign");
					String signature = WXPayUtil.generateSignature(map, api, st);
					Map<String,String> maps = wpm.selectTotalFeeByOrderNum(out_trade_no);
					log.info("微信回调支付金额:{}",total_fee);
					log.info("订单金额:{}",maps.get("total_fee"));
					log.info("微信回调签名:{}",sign);
					log.info("回调签名:{}",signature);
					if(!total_fee.equals(maps.get("total_fee"))) {
						log.info("微信手机支付回调金额不一致:{}",out_trade_no);
						resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
						return resXml;
					}
					if(!sign.equals(signature)) {
						log.info("微信手机支付回调签名不一致:{}",out_trade_no);
						resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
						return resXml;
					}
					int i = wpm.updateWxPayByCallback(transaction_id, ClientConstants.PayStatus.PAY_SUCCESS.getValue(), time_end, device_info, out_trade_no);
					if(i != 1) {
						log.info("微信手机支付回调更新订单失败:{}",out_trade_no);
						resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
						return resXml;
					}else {
						String coursePayNum = wpm.selectWxCourseNum(out_trade_no);
						if(StringUtils.isBlank(coursePayNum)) {
							log.info("微信手机支付回调更新订单失败:{}",out_trade_no);
							resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
							return resXml;
						}
						CallBackParam cbp = new CallBackParam();
		        		cbp.setOrderCode(coursePayNum);
		        		cbp.setPayMethod(PayMethod.ONLINE.getValue());
		        		cbp.setOrderType(PayConstants.PayPlatForm.getNum("WECHAT"));
						orderService.courseOrderPaySuccessOpt(cbp);
						log.info("微信手机支付回调成功订单号:{}",out_trade_no);
						resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>" + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
						return resXml;
					}
				}
			}else{
				log.info("微信手机支付回调失败订单号:{}",out_trade_no);
				resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
				return resXml;
			}
		} catch (Exception e) {
			log.error("手机支付回调通知失败",e);
			 resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
			 return resXml;
		}
		return null;
	}
	
	/**
	 * 微信活动支付回调
	 * @param xml
	 * @return
	 */
	public String activetyCallback(String xml) {
		String resXml = "";
		try {
			//解析XML
			Map<String, String> map = WXPayUtil.xmlToMap(xml);
	        String return_code = map.get("return_code");//状态
	        String out_trade_no = map.get("out_trade_no");//订单号
			if (return_code.equals("SUCCESS")) {
				if (out_trade_no != null) {
					//处理订单逻辑
					String transaction_id = map.get("transaction_id");
					String time_end = map.get("time_end");
					String device_info = map.get("device_info");
					String total_fee = map.get("total_fee");
					String sign = map.get("sign");
					String signature = WXPayUtil.generateSignature(map, api, st);
					Map<String,String> maps = wpm.selectTotalFeeByOrderNum(out_trade_no);
					log.info("微信回调支付金额:{}",total_fee);
					log.info("订单金额:{}",maps.get("total_fee"));
					log.info("微信回调签名:{}",sign);
					log.info("回调签名:{}",signature);
					if(!total_fee.equals(maps.get("total_fee"))) {
						log.info("微信手机支付回调金额不一致:{}",out_trade_no);
						resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
						return resXml;
					}
					if(!sign.equals(signature)) {
						log.info("微信手机支付回调签名不一致:{}",out_trade_no);
						resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
						return resXml;
					}
					int i = wpm.updateWxPayByCallback(transaction_id, ClientConstants.PayStatus.PAY_SUCCESS.getValue(), time_end, device_info, out_trade_no);
					if(i != 1) {
						log.info("微信手机支付回调更新订单失败:{}",out_trade_no);
						resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
						return resXml;
					}else {
						String coursePayNum = wpm.selectWxCourseNum(out_trade_no);
						if(StringUtils.isBlank(coursePayNum)) {
							log.info("微信手机支付回调更新订单失败:{}",out_trade_no);
							resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
							return resXml;
						}
						
						
						CallBackParam cbp = new CallBackParam();
		        		cbp.setOrderCode(coursePayNum);
		        		cbp.setPayMethod(PayMethod.ONLINE.getValue());
		        		cbp.setOrderType(PayConstants.PayPlatForm.getNum("WECHAT"));
		        		signUpService.activityOrderPaySuccessOpt(cbp);
						log.info("微信手机支付回调成功订单号:{}",out_trade_no);
						resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>" + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
						return resXml;
					}
				}
			}else{
				log.info("微信手机支付回调失败订单号:{}",out_trade_no);
				resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
				return resXml;
			}
		} catch (Exception e) {
			log.error("手机支付回调通知失败",e);
			 resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
			 return resXml;
		}
		return null;
	}

}
