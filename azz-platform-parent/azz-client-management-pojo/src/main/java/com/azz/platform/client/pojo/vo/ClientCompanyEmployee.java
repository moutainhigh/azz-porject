/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月26日 上午11:35:48
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.platform.client.pojo.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 刘建麟  2018年10月26日 上午11:35:48
 */
@Data
public class ClientCompanyEmployee {

	private String employeeCode;
	private String employeeName;
	private String phone;
	private String deptName;
	private String postName;
	private Integer status;
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd hh:mm:ss")
	private Date createTime;
}

