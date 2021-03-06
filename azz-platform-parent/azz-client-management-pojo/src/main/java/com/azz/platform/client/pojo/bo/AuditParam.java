package com.azz.platform.client.pojo.bo;

/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月20日 下午2:50:46
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

/**
 * <P>审核企业信息</P>
 * @version 1.0
 * @author 彭斌  2018年10月20日 下午2:50:46
 */
@Data
public class AuditParam {
    /**
     * 客户编码
     */
    @NotBlank(message = "客户编码不允许为空")
    private String clientUserCode;
    
    /**
     * 企业编码
     */
    @NotBlank(message = "企业编码不允许为空")
    private String companyCode;
    
    /**
     * 审核结果信息
     */
    @NotNull(message = "审核结果不允许为空")
    private Integer status;
    
    /**
     * 审核人
     */
    private String auditor;
}

