/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年11月23日 下午3:20:33
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.order.selection.bo;

import org.hibernate.validator.constraints.NotBlank;

import com.azz.core.common.QueryPage;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 黄智聪  2018年11月23日 下午3:20:33
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SearchSelectionRecordParam extends QueryPage{
	
	private static final long serialVersionUID = 1301646524761877411L;

	@NotBlank(message = "客户编码不允许为空")
	private String clientUserCode;
	
	private String searchInput;

}

