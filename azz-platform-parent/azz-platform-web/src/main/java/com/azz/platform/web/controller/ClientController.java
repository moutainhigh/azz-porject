/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月25日 下午3:23:51
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.platform.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.azz.core.common.JsonResult;
import com.azz.core.common.page.Pagination;
import com.azz.platform.client.api.AuditService;
import com.azz.platform.client.api.ClientService;
import com.azz.platform.client.pojo.ClientUser;
import com.azz.platform.client.pojo.bo.AuditParam;
import com.azz.platform.client.pojo.bo.SearchClientManagerParam;
import com.azz.platform.client.pojo.bo.SearchClientMerchantManagerParam;
import com.azz.platform.client.pojo.bo.SearchClientParam;
import com.azz.platform.client.pojo.vo.ClientAccountInfo;
import com.azz.platform.client.pojo.vo.ClientCertification;
import com.azz.platform.client.pojo.vo.ClientCompanyEmployee;
import com.azz.platform.client.pojo.vo.ClientCompanyInfo;
import com.azz.platform.client.pojo.vo.ClientInfo;
import com.azz.platform.client.pojo.vo.ClientMerchantInfo;
import com.azz.utils.WebUtils;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 刘建麟  2018年10月25日 下午3:23:51
 */
@RestController
@RequestMapping("/azz/api/client")
public class ClientController {
	
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
    private AuditService auditService;
	/**
	 * <p>平台 客户管理</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月23日 下午2:02:33
	 */
	@RequestMapping("/selectClientUserList")
	public JsonResult<Pagination<ClientUser>> selectClientUserList(SearchClientManagerParam param) {
		return clientService.selectClientUserList(param);
	}
	
	/**
	 * <p>平台 客户管理 企业用户</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月23日 下午2:02:33
	 */
	@RequestMapping("/selectClientMerchantList")
	public JsonResult<Pagination<ClientMerchantInfo>> selectClientMerchantList(SearchClientMerchantManagerParam param) {
		return clientService.selectClientMerchantList(param);
	}
	
	/**
	 * <p>平台 客户管理 禁用 启用</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月23日 下午2:02:33
	 */
	@RequestMapping("/updateClientUserStatus")
	public JsonResult<String> updateClientUserStatus(@RequestParam("code") String code,@RequestParam("status") Integer status) {
		return clientService.updateClientUserStatus(code, status);
	}
	
	
	/**
	 * <p>平台 客户  企业管理 禁用 启用</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月23日 下午2:02:33
	 */
	@RequestMapping("/updateClientCompnayStatus")
	public JsonResult<String> updateClientCompnayStatus(@RequestParam("code") String code,@RequestParam("status") Integer status) {
		return clientService.updateClientCompnayStatus(code, status);
	}
	
	
	
	/**
	 * <p>平台 客户管理 用户详情</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月23日 下午2:02:33
	 */
	@RequestMapping("/selectClientUserInfo")
	public JsonResult<ClientAccountInfo> selectClientUserInfo(@RequestParam("code") String code) {
		return clientService.selectClientUserInfo(code);
	}
	
	/**
	 * <p>平台 客户管理 企业详情</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月23日 下午2:02:33
	 */
	@RequestMapping("/selectClientCompanyDetail")
	public JsonResult<ClientCompanyInfo> selectClientCompanyDetail(@RequestParam("code") String code) {
		return clientService.selectClientCompanyDetail(code);
	}
	
	/**
	 * <p>平台 客户管理 企业成员</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月23日 下午2:02:33
	 */
	@RequestMapping("/selectClientCompanyEmployeeList")
	public JsonResult<Pagination<ClientCompanyEmployee>> selectClientCompanyEmployeeList(SearchClientManagerParam param) {
		return clientService.selectClientCompanyEmployeeList(param);
	}
	
	
	/**
	 * <p>获取审批列表</p>
	 * @param param
	 * @return
	 * @author 彭斌  2018年10月25日 下午5:38:32
	 */
	@RequestMapping("/searchClientCertificationList")
    public JsonResult<Pagination<ClientCertification>> searchClientList(SearchClientParam param) {
        return clientService.searchClientList(param);
    }
	
	/**
	 * <p>获取客户详情</p>
	 * @param clientUserCode
	 * @return
	 * @author 彭斌  2018年10月25日 下午5:38:48
	 */
	@RequestMapping("/searchClientInfo")
    public JsonResult<ClientInfo> searchClientInfo(@RequestParam("companyCode") String companyCode){
	    return clientService.selectDetailsClientInfo(companyCode);
	}
	
	/**
	 * <p>审批客户信息</p>
	 * @param param
	 * @return
	 * @author 彭斌  2018年10月25日 下午5:39:03
	 */
	@RequestMapping("/auditClient")
    JsonResult<String> auditClient(AuditParam param){
	    param.setAuditor(WebUtils.getLoginUser().getUserInfo().getUserCode());
	    return auditService.auditClient(param);
	}
}

