/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月16日 下午7:33:31
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.platform.user.api;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.azz.core.common.JsonResult;
import com.azz.platform.user.pojo.bo.AddDeptParam;
import com.azz.platform.user.pojo.bo.EditDeptParam;
import com.azz.platform.user.pojo.bo.SearchDeptParam;
import com.azz.platform.user.pojo.vo.Dept;

/**
 * <P>部门服务管理</P>
 * @version 1.0
 * @author 彭斌  2018年10月16日 下午7:33:31
 */
@FeignClient("azz-user-service")
public interface DeptService {
    
    /**
     * <p>新增部门信息</p>
     * @param param
     * @return
     * @author 彭斌  2018年10月17日 下午2:50:20
     */
    @PostMapping("addDeptInfo")
    JsonResult<String> addDeptInfo(@RequestBody AddDeptParam param);
    
    /**
     * <p>修改部门信息</p>
     * @param param
     * @return
     * @author 彭斌  2018年10月17日 下午2:50:24
     */
    @PostMapping("editDeptInfo")
    JsonResult<String> editDeptInfo(@RequestBody EditDeptParam param);
    
    /**
     * <p>获取部门信息列表</p>
     * @param param
     * @return
     * @author 彭斌  2018年10月17日 下午2:50:28
     */
    @PostMapping("getDeptList")
    JsonResult<List<Dept>> getDeptList(@RequestBody SearchDeptParam param);
    
    /**
     * <p>逻辑删除部门信息</p>
     * @param id
     * @return
     * @author 彭斌  2018年10月17日 下午2:50:31
     */
    @GetMapping("delDeptInfo")
    JsonResult<String> delDeptInfo(@RequestParam("deptCode") String deptCode, @RequestParam("modifier") String modifier);
    
    /**
     * <p>获取该父级下的部门信息</p>
     * @param id
     * @return
     * @author 彭斌  2018年10月17日 下午2:50:34
     */
    @GetMapping("getDeptInfo")
    JsonResult<List<Dept>> getDeptParentInfo(@RequestParam("deptCode") String deptCode);
    
    /**
     * <p>禁用部门</p>
     * @param deptCode
     * @param modifier
     * @return
     * @author 彭斌  2018年10月20日 下午4:48:26
     */
    @GetMapping("disableDeptInfo")
    JsonResult<String> disableDeptInfo(@RequestParam("deptCode") String deptCode, @RequestParam("modifier") String modifier);
}

