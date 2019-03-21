/*******************************************************************************
 * Project Key : CPPII
 * Create on 2019年3月19日 上午11:59:47
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.finance.merchant.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.azz.core.common.JsonResult;
import com.azz.core.common.page.Pagination;
import com.azz.finance.merchant.service.MerchantFinanceService;
import com.azz.order.finance.pojo.bo.SearchMerchantOrderParam;
import com.azz.order.finance.pojo.bo.SearchWithdrawDepositApplyParam;
import com.azz.order.finance.pojo.bo.WithdrawDepositApplyParam;
import com.azz.order.finance.pojo.vo.AccountInfo;
import com.azz.order.finance.pojo.vo.MerchantOrderInfo;
import com.azz.order.finance.pojo.vo.WithdrawDepositApplyDetail;
import com.azz.order.finance.pojo.vo.WithdrawDepositApplyInfo;
import com.azz.order.finance.pojo.vo.WithdrawDepositCount;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 黄智聪  2019年3月19日 上午11:59:47
 */
@RestController
@RequestMapping("/azz/api/merchant/finance")
public class MerchantFinanceController {
	
	@Autowired
	private MerchantFinanceService merchantFinanceService;
	
	/**
	 * 
	 * <p>根据商户查询提现统计</p>
	 * @param merchantCode
	 * @return
	 * @author 黄智聪  2019年3月19日 下午4:26:36
	 */
	@RequestMapping("/getWithdrawDepositCount")
	public JsonResult<WithdrawDepositCount> getWithdrawDepositCount(@RequestParam("merchantCode") String merchantCode){
		return merchantFinanceService.getWithdrawDepositCount(merchantCode);
	}
	
	/**
	 * 
	 * <p>查询提现记录</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2019年3月19日 下午4:18:04
	 */
	@RequestMapping("/getWithdrawDepositApplyInfos")
	public JsonResult<Pagination<WithdrawDepositApplyInfo>> getWithdrawDepositApplyInfos(@RequestBody SearchWithdrawDepositApplyParam param){
		return merchantFinanceService.getWithdrawDepositApplyInfos(param);
	}
	
	/**
	 * 
	 * <p>查询提现申请详情</p>
	 * @param applyCode
	 * @return
	 * @author 黄智聪  2019年3月19日 下午7:28:50
	 */
	@RequestMapping("/getWithdrawDepositApplyDetail")
	public JsonResult<WithdrawDepositApplyDetail> getWithdrawDepositApplyDetail(@RequestParam("applyCode") String applyCode){
		return merchantFinanceService.getWithdrawDepositApplyDetail(applyCode);
	}
	
	/**
	 * 
	 * <p>根据商户编码查询账户信息</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2019年3月19日 下午4:18:04
	 */
	@RequestMapping("/getAccountInfoByMerchantCode")
	public JsonResult<AccountInfo> getAccountInfoByMerchantCode(@RequestParam("merchantCode") String merchantCode){
		return merchantFinanceService.getAccountInfoByMerchantCode(merchantCode);
	}
	
	/**
	 * 
	 * <p>提现申请</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2019年3月19日 下午4:18:04
	 */
	@RequestMapping("/withdrawDepositApply")
	public JsonResult<String> withdrawDepositApply(@RequestBody WithdrawDepositApplyParam param){
		return merchantFinanceService.withdrawDepositApply(param);
	}
	
	/**
	 * 
	 * <p>获取提现申请的商户订单列表</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2019年3月19日 下午4:18:04
	 */
	@RequestMapping("/getMerchantOrders")
	public JsonResult<Pagination<MerchantOrderInfo>> getMerchantOrders(@RequestBody SearchMerchantOrderParam param){
		return merchantFinanceService.getMerchantOrders(param);
	}

}

