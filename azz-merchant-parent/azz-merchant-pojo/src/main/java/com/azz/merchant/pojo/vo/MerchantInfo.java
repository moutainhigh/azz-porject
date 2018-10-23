/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月22日 上午11:18:29
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.merchant.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 黄智聪  2018年10月22日 上午11:18:29
 */
@Data
public class MerchantInfo {
    
    private String merchantCode;
    
    private String companyName;
    
    private String phoneNumber;
    
    private String registeredPerson;
    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd hh:mm:ss")
    private String registeredTime;
    
    private String status;
    
}

