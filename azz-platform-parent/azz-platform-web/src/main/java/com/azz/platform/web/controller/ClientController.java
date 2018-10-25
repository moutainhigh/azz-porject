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
import com.azz.platform.client.api.ClientService;
import com.azz.platform.client.pojo.ClientUser;
import com.azz.platform.client.pojo.bo.SearchClientManagerParam;

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
	 * <p>平台 客户管理 禁用 启用</p>
	 * @param param
	 * @return
	 * @author 刘建麟  2018年10月23日 下午2:02:33
	 */
	@RequestMapping("/updateClientUserStatus")
	public JsonResult<String> updateClientUserStatus(@RequestParam("code") String code,@RequestParam("status") Integer status) {
		return clientService.updateClientUserStatus(code, status);
	}
}
