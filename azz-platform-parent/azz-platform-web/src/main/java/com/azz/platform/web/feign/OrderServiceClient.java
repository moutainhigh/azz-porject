/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月15日 上午9:43:14
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.platform.web.feign;

import org.springframework.cloud.netflix.feign.FeignClient;

import com.azz.order.api.OrderService;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 刘建麟  2018年10月15日 上午9:43:14
 */
@FeignClient(value = "azz_order_service")
public interface OrderServiceClient extends OrderService{

}

