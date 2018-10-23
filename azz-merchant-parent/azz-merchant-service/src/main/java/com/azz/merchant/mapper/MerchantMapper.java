package com.azz.merchant.mapper;


import org.apache.ibatis.annotations.Mapper;

import com.azz.merchant.pojo.Merchant;
import com.azz.merchant.pojo.vo.MerchantInfo;

@Mapper
public interface MerchantMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Merchant record);

    int insertSelective(Merchant record);

    Merchant selectByPrimaryKey(Long id);
    
    Merchant selectByMerchantCode(String merchantCode);

    int updateByPrimaryKeySelective(Merchant record);

    int updateByPrimaryKey(Merchant record);
    
    int updateByMerchantCode(Merchant record);
    
    /**
     * 
     * <p>根据手机号查询商户</p>
     * @param phoneNumber 手机号
     * @return
     * @author 黄智聪  2018年10月17日 下午7:14:23
     */
    Merchant getMerchantByPhoneNumber(String phoneNumber);
    

    /**
     * 
     * <p>根据信用代码查询商户信息</p>
     * @param creditCode 信用代码
     * @return
     * @author 黄智聪  2018年10月23日 下午7:58:00
     */
    Merchant getMerchantByCreditCode(String creditCode);
    
    /**
     * 
     * <p>根据手机号查询商户信息</p>
     * @param userId
     * @return
     * @author 黄智聪  2018年10月17日 下午7:14:20
     */
    MerchantInfo getMerchantInfoByPhoneNumber(String phoneNumber);
    
}