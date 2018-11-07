/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年11月6日 下午4:15:09
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.platform.merchant.pojo.bo;

import java.util.List;

import lombok.Data;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 彭斌  2018年11月6日 下午4:15:09
 */
@Data
public class AddSelectionParams {
      
      private Long caseId;
      
      private List<Long> paramsId;
      
      private String creator;
}

