/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月24日 下午6:19:44
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.platform.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.azz.core.common.JsonResult;
import com.azz.core.common.page.Pagination;
import com.azz.platform.client.api.AuditService;
import com.azz.platform.client.pojo.ClientUser;
import com.azz.platform.client.pojo.bo.SearchClientManagerParam;
import com.azz.platform.client.pojo.bo.SearchClientParam;
import com.azz.platform.client.pojo.vo.ClientCertification;
import com.azz.platform.client.service.ClientService;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 彭斌  2018年10月25日 上午12:03:12
 */
@RestController
@RequestMapping("/azz/api/client")
public class ClientController {
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
	private AuditService auditService;
	
	@RequestMapping(value="searchClientList",method=RequestMethod.POST)
	 public JsonResult<Pagination<ClientCertification>> searchClientList(@RequestBody SearchClientParam param) {
		return null;
	}

	
	/**
	 * <p>平台 客户管理</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月25日 下午3:22:40
	 */
	@RequestMapping(value="selectClientUserList",method=RequestMethod.POST)
	 public JsonResult<Pagination<ClientUser>> selectClientUserList(@RequestBody SearchClientManagerParam param) {
		return clientService.selectClientUserList(param);
	}
	
	/**
	 * <p>客户管理 启用 禁用  1启用 0 禁用</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月24日 下午7:35:48
	 */
	@RequestMapping(value="updateClientUserStatus",method=RequestMethod.POST)
	 public JsonResult<String> updateClientUserStatus(String code,Integer status) {
		return clientService.updateClientUserStatus(code,status);
	}
	
}

