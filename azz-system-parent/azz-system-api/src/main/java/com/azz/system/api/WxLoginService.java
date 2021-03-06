package com.azz.system.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.azz.core.common.JsonResult;
import com.azz.system.bo.WxClientRegistParam;
import com.azz.system.bo.WxLoginParam;
import com.azz.system.pojo.ClientWxUser;
import com.azz.system.vo.WxCallBackInfo;
import com.azz.system.vo.WxInfo;
import com.azz.system.vo.WxLoginInfo;

@FeignClient("azz-system-service")
public interface WxLoginService {

	/**
	 * 去到微信扫码页面
	 * @return
	 */
	@RequestMapping(value="/azz/api/wechat/goWxScanPage",method=RequestMethod.POST)
	public JsonResult<WxInfo> goWxScanPage();
		
	
	/**
	 * 微信回调
	 * @param code
	 * @param state
	 * @param key
	 * @return
	 */
	@RequestMapping(value="/azz/api/wechat/callback",method=RequestMethod.POST)
	public JsonResult<WxCallBackInfo> callback(@RequestParam("code")String code,@RequestParam("state") String state) ;
	
	/**
	 * 绑定或者注册
	 * @param wcbi
	 * @return
	 */
	@RequestMapping(value="/azz/api/wechat/goBindOrReg",method=RequestMethod.POST)
	public JsonResult<WxCallBackInfo> goBindOrReg(@RequestBody WxCallBackInfo wcbi) ;
	
	/**
	 * @param wsc
	 * @return
	 */
	@RequestMapping(value="/azz/api/wechat/insert",method=RequestMethod.POST)
	public Integer insert(@RequestBody ClientWxUser wsc) ;
	
	/**
	 * 注册并绑定
	 * @param wcrp
	 * @return
	 */
	@RequestMapping(value="/azz/api/wechat/regAndBind",method=RequestMethod.POST)
	public JsonResult<WxLoginInfo> regAndBind(@RequestBody WxClientRegistParam wcrp) ;
}
