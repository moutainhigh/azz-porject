/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年11月12日 下午3:55:45
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/

package com.azz.order.platform.bo;

import com.azz.core.common.QueryPage;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <P>
 * TODO
 * </P>
 * 
 * @version 1.0
 * @author 黄智聪 2018年11月12日 下午3:55:45
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SearchPlatformClientOrderParam extends QueryPage {

	private static final long serialVersionUID = -3796726023111848609L;
	
	private Integer orderStatus;
	
	private String searchInput;

}
