/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月31日 下午5:19:40
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
 * @author 彭斌  2018年10月31日 下午5:19:40
 */
@Data
public class ClassificationList {
    
    private Long id;
    
    /**
     * 分类图片url
     *
     * @mbg.generated
     */
    private String assortmentPicUrl;
    
    /**
     * 分类编码
     */
    private String assortmentCode;
    
    /**
     * 分类名称
     *
     * @mbg.generated
     */
    private String assortmentName;
    
    /**
     * 分类排序(默认0)
     *
     * @mbg.generated
     */
    private Integer assortmentSort;
    
    /**
     * 父级编码
     */
    private String assortmentParentCode;
    
    /**
     * 分类层级
     *
     * @mbg.generated
     */
    private Byte assortmentTop;
    
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;
    
    List<ClassificationList> childList;
    
}

