/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月15日 下午3:05:46
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/

package com.azz.platform.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.azz.core.common.JsonResult;
import com.azz.core.common.page.Pagination;
import com.azz.platform.merchant.api.AuditService;
import com.azz.platform.merchant.api.MerchantService;
import com.azz.platform.merchant.pojo.bo.AuditParam;
import com.azz.platform.merchant.pojo.bo.SearchMerchantParam;
import com.azz.platform.merchant.pojo.vo.MerchantApproval;
import com.azz.platform.merchant.pojo.vo.MerchantInfo;

/**
 * <P>商户管理</P>
 * @version 1.0
 * @author 彭斌  2018年10月17日 下午3:04:47
 */
@RestController
@RequestMapping("/azz/api/merchant")
public class MerchantController {

	private static final Logger LOG = LoggerFactory.getLogger(MerchantController.class);

	@Autowired
	MerchantService merchantService;
	
	@Autowired
	AuditService auditService;
	
	/**
	 * <p>商户审批列表</p>
	 * @param param
	 * @return
	 * @author 彭斌  2018年10月23日 下午2:02:33
	 */
	@RequestMapping("/searchMerchantList")
	public JsonResult<Pagination<MerchantApproval>> searchMerchantList(SearchMerchantParam param) {
		return merchantService.searchMerchantList(param);
	}

	/**
	 * <p>获取审批详情信息</p>
	 * @param merchantCode
	 * @return
	 * @author 彭斌  2018年10月23日 下午2:04:29
	 */
	@RequestMapping("/searchMerchantInfo")
	public JsonResult<MerchantInfo> searchMerchantInfo(@RequestParam("merchantCode") String merchantCode){
	    return merchantService.searchMerchantInfo(merchantCode);
	}
	
	/**
	 * <p>审批操作</p>
	 * @param param
	 * @return
	 * @author 彭斌  2018年10月23日 下午2:06:24
	 */
    @RequestMapping("/auditEnterprise")
    public JsonResult<String> auditEnterprise(AuditParam param) {
        return auditService.auditEnterprise(param);
    }
	
}
