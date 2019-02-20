/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年10月15日 下午3:05:46
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/

package com.azz.wx.course.api;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.azz.core.common.JsonResult;
import com.azz.wx.course.pojo.bo.AddCourseApplyParam;
import com.azz.wx.course.pojo.bo.AddCourseSuggestionsParam;
import com.azz.wx.course.pojo.bo.BindingPhomeParam;
import com.azz.wx.course.pojo.bo.EditCourseApplyParam;
import com.azz.wx.course.pojo.vo.CourseSignUpInfo;
import com.azz.wx.course.pojo.vo.PersonalCenterInfo;

/**
 * <P>个人中心相关</P>
 * @version 1.0
 * @author 彭斌  2019年1月24日 下午4:22:09
 */
@FeignClient("azz-wx-course-service")
public interface CoursePersonalCenterService {


    /**
     * <p>课程个人中心首页</p>
     * @param openId
     * @return
     * @author 彭斌  2019年1月24日 下午2:28:06
     */
	@RequestMapping(value="/azz/api/client/center/getIndexCenter",method=RequestMethod.POST)
	public JsonResult<PersonalCenterInfo> getPersonlCenter(@RequestParam("openId") String openId);

	/**
     * <p>绑定手机号码</p>
     * @param param
     * @return
     * @author 彭斌  2019年1月24日 下午2:28:10
     */
    @RequestMapping(value = "/azz/api/client/center/bindingPhone", method = RequestMethod.POST)
    public JsonResult<String> bindingPhone(@RequestBody BindingPhomeParam param);

    /**
     * <p>解绑手机号码</p>
     * @param param
     * @return
     * @author 彭斌  2019年1月24日 下午2:29:43
     */
    @RequestMapping(value = "/azz/api/client/center/untiedPhone", method = RequestMethod.POST)
    public JsonResult<String> untiedPhone(@RequestBody BindingPhomeParam param);


    /**
     * <p>获取课程报名信息</p>
     * @param userCode
     * @return
     * @author 彭斌  2019年1月24日 下午2:31:41
     */
    @RequestMapping(value = "/azz/api/client/center/getCourseSignUp", method = RequestMethod.POST)
    public JsonResult<List<CourseSignUpInfo>> getCourseSignUp(@RequestParam("userCode") String userCode);


    /**
     * <p>新增课程报名信息</p>
     * @param param
     * @return
     * @author 彭斌  2019年1月24日 下午2:32:25
     */
    @RequestMapping(value = "/azz/api/client/center/addCourseApply", method = RequestMethod.POST)
    public JsonResult<String> addCourseApply(@RequestBody AddCourseApplyParam param);

    /**
     * <p>编辑申请课程信息</p>
     * @param param
     * @return
     * @author 彭斌  2019年1月24日 下午2:33:25
     */
    @RequestMapping(value = "/azz/api/client/center/editCourseApply", method = RequestMethod.POST)
    public JsonResult<String> editCourseApply(@RequestBody EditCourseApplyParam param);
    
    /**
     * <p>删除申请信息</p>
     * @param applyCode
     * @return
     * @author 彭斌  2019年1月24日 下午2:34:27
     */
    @RequestMapping(value = "/azz/api/client/center/deletCourseApply", method = RequestMethod.POST)
    public JsonResult<String> deletCourseApply(@RequestParam("applyCode") String applyCode);
    
    
    /**
     * <p>设置默认</p>
     * @param applyCode
     * @return
     * @author 彭斌  2019年1月24日 下午2:35:11
     */
    @RequestMapping(value = "/azz/api/client/center/setDefault", method = RequestMethod.POST)
    public JsonResult<String> setDefault(@RequestParam("applyCode") String applyCode);
    
    
    /**
     * <p>获取课程申请详情</p>
     * @param applyCode
     * @return
     * @author 彭斌  2019年1月24日 下午2:36:02
     */
    @RequestMapping(value = "/azz/api/client/center/getCourseSignUpInfo", method = RequestMethod.POST)
    public JsonResult<CourseSignUpInfo> getCourseSignUpInfo(@RequestParam("applyCode") String applyCode);
    
    
    /**
     * <p>新增投诉建议</p>
     * @param param
     * @return
     * @author 彭斌  2019年1月24日 下午3:47:53
     */
    @RequestMapping(value = "/azz/api/client/center/addSuggestions", method = RequestMethod.POST)
    public JsonResult<String> addSuggestions(@RequestBody AddCourseSuggestionsParam param);
}
