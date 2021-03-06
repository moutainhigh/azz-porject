/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月22日 下午5:18:06
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.merchant.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.azz.core.common.JsonResult;
import com.azz.merchant.pojo.bo.AddRoleParam;
import com.azz.merchant.pojo.bo.DelRoleParam;
import com.azz.merchant.pojo.bo.EditRoleParam;
import com.azz.merchant.pojo.bo.SearchRoleParam;
import com.azz.merchant.pojo.bo.SetRolePermissionParam;
import com.azz.merchant.pojo.vo.Permission;
import com.azz.merchant.pojo.vo.RoleInfo;
import com.azz.merchant.service.PermissionService;


/**
 * <P>TODO</P>
 * @version 1.0
 * @author 黄智聪  2018年10月22日 下午5:18:06
 */
@RestController
@RequestMapping("/azz/api/merchant/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;
    
    @RequestMapping("/getPermissionList")
    public JsonResult<List<Permission>> getPermissionList(@RequestParam("merchantCode")String merchantCode, @RequestParam("roleCode")String roleCode) {
	return permissionService.getPermissionList(merchantCode, roleCode);
    }
    
    @RequestMapping("/addRole")
    public JsonResult<String> addRole(@RequestBody AddRoleParam param) {
	return permissionService.addRole(param);
    }
    
    @RequestMapping("/editRole")
    public JsonResult<String> editRole(@RequestBody EditRoleParam param) {
	return permissionService.editRole(param);
    }
    
    @RequestMapping("/delRole")
    public JsonResult<String> delRole(@RequestBody DelRoleParam param) {
	return permissionService.delRole(param);
    }
    
    @RequestMapping("/getRoleList")
    public JsonResult<List<RoleInfo>> getRoleList(@RequestBody SearchRoleParam param) {
	return permissionService.getRoleList(param);
    }
    
    @RequestMapping("/getRolePermissions")
    public JsonResult<List<String>> getRolePermissions(@RequestParam("merchantCode")String merchantCode, @RequestParam("roleCode")String roleCode) {
	return permissionService.getRolePermissions(merchantCode, roleCode);
    }
    
    @RequestMapping("/setRolePermissions")
    public JsonResult<String> setRolePermissions(@RequestBody SetRolePermissionParam param) {
	return permissionService.setRolePermissions(param);
    }
}

