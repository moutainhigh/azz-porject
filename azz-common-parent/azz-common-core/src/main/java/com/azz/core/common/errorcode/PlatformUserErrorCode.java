/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月16日 下午6:57:17
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.core.common.errorcode;
/**
 * <P>平台用户模块错误码</P>
 * @version 1.0
 * @author 黄智聪  2018年10月16日 下午6:57:17
 */
public class PlatformUserErrorCode extends BaseErrorCode{
    
    public static final PlatformUserErrorCode PLATFORM_USER_ERROR_INVALID_USER = new PlatformUserErrorCode(20001, "无效用户");
    
    public static final PlatformUserErrorCode PLATFORM_USER_ERROR_WRONG_PHONE_OR_PASSWORD = new PlatformUserErrorCode(20002, "手机号或密码错误");

    public PlatformUserErrorCode(int code, String message) {
	super(code, message);
    }

    @Override
    public String getErrorType() {
	return "平台用户异常";
    }

}

