package com.azz.platform.merchant.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.azz.platform.merchant.pojo.MerchantUserRole;

@Mapper
public interface MerchantUserRoleMapper {
    int deleteByPrimaryKey(Long id);

    int insert(MerchantUserRole record);

    int insertSelective(MerchantUserRole record);

    MerchantUserRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MerchantUserRole record);

    int updateByPrimaryKey(MerchantUserRole record);
    
    int deleteByMerchantUserId(Long merchantUserId);
}