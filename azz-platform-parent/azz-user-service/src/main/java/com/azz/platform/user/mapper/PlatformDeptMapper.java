package com.azz.platform.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.azz.platform.user.pojo.PlatformDept;

@Mapper
public interface PlatformDeptMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PlatformDept record);

    int insertSelective(PlatformDept record);

    PlatformDept selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PlatformDept record);

    int updateByPrimaryKey(PlatformDept record);
    
    PlatformDept selectByDeptName(String deptName);
}