/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年8月30日 上午10:54:04
 * Copyright (c) 2008 - 2011.深圳市齐采科技有限公司版权所有. 粤ICP备16034195号
 * 注意：本内容仅限于深圳市齐采科技服务有限公司内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.azz.core.common.errorcode.JSR303ErrorCode;
import com.azz.exception.JSR303ValidationException;

import lombok.experimental.UtilityClass;

/**
 * <P>JSR303校验</P>
 * @version 1.0
 * @author 黄智聪（13510946256）  2018年8月30日 上午10:54:04
 */
@UtilityClass
public class JSR303ValidateUtils {

    /**
     * 
     * <p>验证符合JSR303规范的对象</p>
     * @param input 需被校验的参数
     * @throws JSR303ValidationException 验证不通过时,抛出此异常
     * @author 黄智聪（13510946256）  2018年8月30日 上午10:57:59
     */
    public static <T> void validate(T input) throws JSR303ValidationException {
        validateInput(input);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(input);
        if (0 < violations.size()) {
            for (ConstraintViolation<T> violation : violations) {
                throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, violation.getMessage());
            }
        }
    }

    /**
     * 
     * <p>验证符合JSR303规范的对象 </p>
     * @param input 需被校验的参数
     * @param ignoreFields  忽略校验的字段
     * @throws JSR303ValidationException   验证不通过时,抛出此异常
     * @author 黄智聪（13510946256）  2018年8月30日 上午11:29:22
     */
    public static <T> void validate(T input, String[] ignoreFields) throws JSR303ValidationException {
        validateInput(input);
        if (ignoreFields == null || ignoreFields.length == 0) {
            validate(input);
        } else {
            Collection<String> coll = Arrays.asList(ignoreFields);
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<T>> violations = validator.validate(input);
            if (0 < violations.size()) {
                for (ConstraintViolation<T> violation : violations) {
                    if (!coll.contains(violation.getPropertyPath().toString())) {
                        throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, violation.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * 
     * <p>输入参数校验</p>
     * @param input 需被校验的参数
     * @throws JSR303ValidationException   验证不通过时,抛出此异常
     * @author 黄智聪（13510946256）  2018年8月30日 上午11:13:28
     */
    private static <T> void validateInput(T input) throws JSR303ValidationException{
        if (input == null) {
            throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "输入参数为空");
        }
    }
    
}

