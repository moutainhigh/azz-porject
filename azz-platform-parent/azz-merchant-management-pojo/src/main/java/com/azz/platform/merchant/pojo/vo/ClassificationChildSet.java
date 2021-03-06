/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月31日 下午6:36:03
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.platform.merchant.pojo.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 彭斌  2018年10月31日 下午6:36:03
 */
@Data
public class ClassificationChildSet {
    /**
     * 分类图片url
     *
     * @mbg.generated
     */
    private String cAssortmentPicUrl;
    
    /**
     * 分类编码
     */
    private String cAssortmentCode;
    
    /**
     * 分类名称
     *
     * @mbg.generated
     */
    private String cAssortmentName;
    
    /**
     * 分类排序(默认0)
     *
     * @mbg.generated
     */
    private Integer cAssortmentSort;
    
    /**
     * 父级编码
     */
    private String cAssortmentParentCode;
    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd hh:mm:ss")
    private Date cCreateTime;
    
}

