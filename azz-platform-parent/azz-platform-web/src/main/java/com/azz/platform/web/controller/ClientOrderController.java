/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年11月15日 下午2:42:26
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.platform.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.azz.core.common.JsonResult;
import com.azz.core.common.page.Pagination;
import com.azz.order.api.platform.PlatformClientOrderService;
import com.azz.order.client.pojo.vo.ClientOrderDetail;
import com.azz.order.platform.bo.AllocateClientOrderParam;
import com.azz.order.platform.bo.SearchPlatformClientOrderParam;
import com.azz.order.platform.vo.AllocatedMerchantOrderInfo;
import com.azz.order.platform.vo.OrderOperationRecord;
import com.azz.order.platform.vo.PlatformClientOrderInfo;
import com.azz.utils.WebUtils;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 黄智聪  2018年11月15日 下午2:42:26
 */
@RestController
@RequestMapping("/azz/api/platform/client/order")
public class ClientOrderController {
	
	@Autowired
	private PlatformClientOrderService platformClientOrderService;
	
	/**
	 * 
	 * <p>查询平台客户订单列表</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月13日 上午10:54:40
	 */
	@RequestMapping("/getClientOrderInfoList")
	public JsonResult<Pagination<PlatformClientOrderInfo>> getClientOrderInfoList(SearchPlatformClientOrderParam param){
		return platformClientOrderService.getClientOrderInfoList(param);
	}
	
	/**
	 * 
	 * <p>查询客户订单详情</p>
	 * @param clientOrderCode
	 * @return
	 * @author 黄智聪  2018年11月13日 上午10:56:03
	 */
	@RequestMapping("/getClientOrderDetail")
	public JsonResult<ClientOrderDetail> getClientOrderDetail(String clientOrderCode){
		return platformClientOrderService.getClientOrderDetail(clientOrderCode);
	}
	
	/**
	 * 
	 * <p>查询客户订单拆分后的商户订单列表</p>
	 * @param clientOrderCode
	 * @return
	 * @author 黄智聪  2018年11月14日 下午3:58:10
	 */
	@RequestMapping("/getAllocatedMerchantOrder")
	public JsonResult<AllocatedMerchantOrderInfo> getAllocatedMerchantOrder(String clientOrderCode){
		return platformClientOrderService.getAllocatedMerchantOrder(clientOrderCode);
	}
	
	/**
	 * 
	 * <p>查询客户订单拆单后的生成的商户订单</p>
	 * @param clientOrderCode
	 * @return
	 * @author 黄智聪  2018年11月14日 下午3:58:10
	 */
	@RequestMapping("/getGeneratedMerchantOrderInfo")
	public JsonResult<AllocatedMerchantOrderInfo> getGeneratedMerchantOrderInfo(String clientOrderCode){
		return platformClientOrderService.getGeneratedMerchantOrderInfo(clientOrderCode);
	}
	
	/**
	 * 
	 * <p>分配客户订单</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月14日 下午2:31:17
	 */
	@RequestMapping(value="allocateClientOrder",method=RequestMethod.POST,produces="application/json;charset=UTF-8")
	public JsonResult<String> allocateClientOrder(@RequestBody AllocateClientOrderParam param){
		param.setAllocatePerson(WebUtils.getLoginUser().getUserInfo().getUserCode());
		return platformClientOrderService.allocateClientOrder(param);
	}
	
	/**
	 * 
	 * <p>查询客户订单操作记录</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月14日 下午2:31:17
	 */
	@RequestMapping("/getClientOrderOperationRecords")
	public JsonResult<List<OrderOperationRecord>> getClientOrderOperationRecords(String clientOrderCode){
		return platformClientOrderService.getClientOrderOperationRecords(clientOrderCode);
	}

}

