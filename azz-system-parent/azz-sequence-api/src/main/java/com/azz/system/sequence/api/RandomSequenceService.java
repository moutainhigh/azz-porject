/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月24日 下午4:20:38
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.system.sequence.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <P>TODO</P>
 * @version 1.0
 * @author 刘建麟  2018年10月24日 下午4:20:38
 */
@FeignClient("azz-sequence-service")
public interface RandomSequenceService {

	@RequestMapping(value="/azz/api/sequence/getClientCustomerNumber",method=RequestMethod.GET)
	String getClientNumber();
	
	@RequestMapping(value="/azz/api/sequence/getClientCompanyNumber",method=RequestMethod.GET)
	String getCompanyNumber();
	
	@RequestMapping(value="/azz/api/sequence/getClientEmployeeNumber",method=RequestMethod.GET)
	String getEmployeeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getClientDepartmentNumber",method=RequestMethod.GET)
	String getDepartmentNumber();
	
	@RequestMapping(value="/azz/api/sequence/getClientPowerNumber",method=RequestMethod.GET)
	String getPowermentNumber();
	
	@RequestMapping(value="/azz/api/sequence/getClassificationNumber",method=RequestMethod.GET)
	String getClassificationNumber();
	
	@RequestMapping(value="/azz/api/sequence/getParameterItemCodeNumber",method=RequestMethod.GET)
	String getParameterItemCodeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getProductParameterCodeNumber",method=RequestMethod.GET)
	String getProductParameterCodeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getModuleCodeNumber",method=RequestMethod.GET)
	String getModuleCodeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getProductCodeNumber",method=RequestMethod.GET)
	String getProductCodeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getBrandCodeNumber",method=RequestMethod.GET)
	String getBrandCodeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getCaseCodeNumber",method=RequestMethod.GET)
    String getCaseCodeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getCombinationCodeNumber",method=RequestMethod.GET)
    String getCombinationCodeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getPayCodeNumber",method=RequestMethod.GET)
    String getPayCodeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getClientPersonalOrderCodeNumber",method=RequestMethod.GET)
    String getClientPersonalOrderCodeNumber();
	
	@RequestMapping(value="/azz/api/sequence/getMerchantOrderCodeNumber",method=RequestMethod.GET)
    String getMerchantOrderCodeNumber();
}

