/*******************************************************************************
 * Project Key : CPPII Create on 2018年10月17日 上午9:17:00 Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/

package com.azz.platform.user.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.azz.core.common.JsonResult;
import com.azz.core.common.errorcode.PlatformUserErrorCode;
import com.azz.core.exception.BaseException;
import com.azz.platform.user.mapper.PlatformDeptMapper;
import com.azz.platform.user.pojo.PlatformDept;
import com.azz.platform.user.pojo.bo.AddDeptParam;
import com.azz.platform.user.pojo.bo.EditDeptParam;
import com.azz.platform.user.pojo.bo.SearchDeptParam;
import com.azz.platform.user.pojo.vo.Dept;
import com.azz.util.JSR303ValidateUtils;
import com.azz.util.ObjectUtils;

/**
 * <P>
 * 部门服务接口
 * </P>
 * 
 * @version 1.0
 * @author 彭斌 2018年10月17日 上午9:17:00
 */
@Service
public class DeptService{

    @Autowired
    private PlatformDeptMapper deptMapper;

    public JsonResult<String> addDeptInfo(@RequestBody AddDeptParam param) {
        // 部门信息非空校验
        JSR303ValidateUtils.validate(param);
        String deptParentCode = param.getParentCode();
        
        
        // 部门名称/父级编码校验
        PlatformDept deptObj = deptMapper.selectByDeptName(param.getDeptName());
        
        if (ObjectUtils.isNotNull(deptObj)) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_EXIST);
        }
        
        PlatformDept dept = new PlatformDept();
        
        if(!"".equals(deptParentCode)) {
            PlatformDept deptObjCode = deptMapper.selectByDeptCode(deptParentCode);
            if(ObjectUtils.isNull(deptObjCode)) {
                throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_NO_EXIST);
            }
            dept.setParentCode(deptParentCode);
        } else {
            // 系统自动生成部门编码
            dept.setParentCode("0");
        }
        dept.setDeptCode("D"+System.currentTimeMillis()); // TODO 部门编码生成
        dept.setDeptName(param.getDeptName());
        dept.setStatus(param.getStatus());
        dept.setCreator(param.getCreator());
        dept.setCreateTime(new Date());

        deptMapper.insertSelective(dept);
        return JsonResult.successJsonResult();
    }

    public JsonResult<String> editDeptInfo(@RequestBody EditDeptParam param) {
        JSR303ValidateUtils.validate(param);

        PlatformDept dept = deptMapper.selectByDeptCode(param.getDeptCode());
        if (ObjectUtils.isNull(dept)) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_NO_EXIST);
        }

        if (!dept.getDeptName().equals(param.getDeptName().trim())) {
            PlatformDept deptObj = deptMapper.selectByDeptName(param.getDeptName());
            if(ObjectUtils.isNotNull(deptObj)) {
                throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_EXIST);
            }
        }
        dept.setParentCode(param.getParentCode());
        dept.setDeptName(param.getDeptName());
        dept.setLastModifyTime(new Date());
        dept.setModifier(param.getModifier());
        dept.setStatus(param.getStatus());

        deptMapper.updateByPrimaryKeySelective(dept);
        return JsonResult.successJsonResult();
    }

    public JsonResult<List<Dept>> getDeptList(@RequestBody SearchDeptParam param) {
        List<Dept> infos = deptMapper.selectDeptList(param);
        return JsonResult.successJsonResult(infos);
    }

    public JsonResult<String> delDeptInfo(String deptCode, String modifier) {
        if (ObjectUtils.isNull(deptCode) ) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_NO_EXIST);
        }
        PlatformDept dept = deptMapper.selectByDeptCode(deptCode);
        if (ObjectUtils.isNull(dept)) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_NO_EXIST);
        }

        dept.setStatus(0);
        dept.setModifier(modifier);
        dept.setLastModifyTime(new Date());
        
        deptMapper.updateByPrimaryKeySelective(dept);
        return JsonResult.successJsonResult();
    }

    public JsonResult<List<Dept>> getDeptParentInfo(String deptCode) {
        if (ObjectUtils.isNull(deptCode)) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_NO_EXIST);
        }
        List<Dept> dept = deptMapper.selectDeptParentList(deptCode);
        return JsonResult.successJsonResult(dept);
    }
    
    public JsonResult<String> disableDeptInfo(String deptCode, String modifier) {
        if (ObjectUtils.isNull(deptCode) ) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_NO_EXIST);
        }
        PlatformDept dept = deptMapper.selectByDeptCode(deptCode);
        if (ObjectUtils.isNull(dept)) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_NO_EXIST);
        }

        dept.setStatus(2);
        dept.setModifier(modifier);
        dept.setLastModifyTime(new Date());
        
        deptMapper.updateByPrimaryKeySelective(dept);
        return JsonResult.successJsonResult();
    }

    public JsonResult<String> enableDeptInfo(String deptCode, String modifier) {
        if (ObjectUtils.isNull(deptCode) ) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_NO_EXIST);
        }
        PlatformDept dept = deptMapper.selectByDeptCode(deptCode);
        if (ObjectUtils.isNull(dept)) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_DEPT_ERROR_NO_EXIST);
        }

        dept.setStatus(1);
        dept.setModifier(modifier);
        dept.setLastModifyTime(new Date());
        
        deptMapper.updateByPrimaryKeySelective(dept);
        return JsonResult.successJsonResult();
    }

}
