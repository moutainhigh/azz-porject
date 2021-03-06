/*******************************************************************************
 * Project Key : CPPII
 * Create on 2019年1月4日 下午3:24:52
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.wx.course.pojo.vo;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 黄智聪  2019年1月4日 下午3:24:52
 */
@Data
public class CourseDetail {
	
	private String courseCode;

    private String courseName;
    
    private Date latelyStartClassTime;
    
    private Integer hours;
    
    private Integer buyCount;
    
    private String brandCode;
    
    private String brandName;
    
    private String brandInfo;

    private String coursePicName;

    private String coursePicUrl;
    
    private String courseDescription;
    
    private String classificationCode;
    
    private String classificationName;
    
    private String courseInfo;
    
    private BigDecimal minPrice;
    
    private Byte status;
    
    private Param courseParams;
    
}

