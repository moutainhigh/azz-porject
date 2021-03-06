/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年11月22日 下午2:42:08
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.order.platform.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 彭斌  2018年11月22日 下午2:42:08
 */
@Data
public class ClientOrderRelevanceInvoice {
    private String merchantOrderCode;
    private BigDecimal grandTotal;
    private Integer status;
    private String merchantApplyCode;
    private Integer deliveryType;
    private String companyName;
    private String number;
    private String deliveryPerson;
    private String deliveryPhone;
}

