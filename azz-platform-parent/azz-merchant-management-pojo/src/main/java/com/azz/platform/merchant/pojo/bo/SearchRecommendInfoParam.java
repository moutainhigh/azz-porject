/*******************************************************************************
 * Project Key : CPPII
 * Create on 2019年1月7日 上午11:18:15
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.platform.merchant.pojo.bo;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 黄智聪  2019年1月7日 上午11:18:15
 */
@Data
public class SearchRecommendInfoParam {
	
	@NotBlank(message = "请选择专场")
	private String specialPerformanceCode;
	
	private String recommendName;

}

