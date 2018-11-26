/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年11月26日 上午10:57:20
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azz.controller.utils.WebUtils;
import com.azz.core.common.JsonResult;
import com.azz.core.common.QueryPage;
import com.azz.core.common.page.Pagination;
import com.azz.order.api.client.SelectionService;
import com.azz.order.selection.bo.AddSelectionRecordParam;
import com.azz.order.selection.bo.AddToShoppingCartParam;
import com.azz.order.selection.bo.DelSelectionRecordParam;
import com.azz.order.selection.bo.OrderParam;
import com.azz.order.selection.bo.SearchCombinationInitParamsParam;
import com.azz.order.selection.bo.SearchInitParamsParam;
import com.azz.order.selection.bo.SearchSelectionRecordParam;
import com.azz.order.selection.vo.CombinationDetail;
import com.azz.order.selection.vo.CombinationInfo;
import com.azz.order.selection.vo.CombinationInitParams;
import com.azz.order.selection.vo.InitParams;
import com.azz.order.selection.vo.ProductInfomation;
import com.azz.order.selection.vo.ProductPrice;
import com.azz.order.selection.vo.SelectionCaseInfo;
import com.azz.order.selection.vo.SelectionRecord;
import com.azz.order.selection.vo.ShoppingCartProductInfo;

/**
 * <P>选型</P>
 * @version 1.0
 * @author 黄智聪  2018年11月26日 上午10:57:20
 */
@RestController
@RequestMapping("/azz/api/client/selection")
public class SelectionController {
	
	@Autowired
	private SelectionService selectionService;
	
	/**
	 * <p>查询选型的所有方案列表 1</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月19日 下午5:57:39
	 */
	@RequestMapping("/getSelectionCaseInfos")
	public JsonResult<Pagination<SelectionCaseInfo>> getSelectionCaseInfos(QueryPage param){
		return selectionService.getSelectionCaseInfos(param);
	}
	
	/**
	 * 
	 * <p>查询初始化参数 2</p>
	 * @param caseCode
	 * @return
	 * @author 黄智聪  2018年11月20日 下午7:32:51
	 */
	@RequestMapping("/getInitParamsByCaseCode")
	public JsonResult<List<InitParams>> getInitParamsByCaseCode(SearchInitParamsParam param){
		return selectionService.getInitParamsByCaseCode(param);
	}
	
	/**
	 * 
	 * <p>根据参数完善页中选中的参数，获取符合这些参数的产品的公共参数   3</p>
	 * @param caseCode
	 * @return
	 * @author 黄智聪  2018年11月20日 下午7:32:14
	 */
	@RequestMapping("/getCombinationInitParams")
	public JsonResult<List<CombinationInitParams>> getCombinationInitParams(SearchInitParamsParam param){
		return selectionService.getCombinationInitParams(param);
	}
	
	/**
	 * 
	 * <p>查询推荐组合列表 4</p>
	 * @param caseCode
	 * @return
	 * @author 黄智聪  2018年11月20日 下午7:32:51
	 */
	@RequestMapping("/getCombinationInfos")
	public JsonResult<Pagination<CombinationInfo>> getCombinationInfos(SearchInitParamsParam param){
		return selectionService.getCombinationInfos(param);
	}
	
	/**
	 * 
	 * <p>查询推荐组合详情（包含推荐组合的信息、所包含的产品的公共选型参数、产品列表）  5</p>
	 * @param combinationCode
	 * @return
	 * @author 黄智聪  2018年11月22日 下午3:12:23
	 */
	@RequestMapping("/getCombinationDetail")
	public JsonResult<CombinationDetail> getCombinationDetail(String combinationCode){
		return selectionService.getCombinationDetail(combinationCode);
	}
	
	/**
	 * 
	 * <p>根据所选参数查询产品列表  6</p>
	 * @param searchParam
	 * @return
	 * @author 黄智聪  2018年11月22日 下午9:15:42
	 */
	@RequestMapping("/getProductInfos")
	public JsonResult<List<List<Object>>> getProductInfos(SearchCombinationInitParamsParam searchParam){
		return selectionService.getProductInfos(searchParam);
	}
	
	/**
	 * 
	 * <p>根据产品编码查询该产品的价格信息 7</p>
	 * @param productCode
	 * @return
	 * @author 黄智聪  2018年11月23日 上午11:11:52
	 */
	@RequestMapping("/getProductPrice")
	public JsonResult<ProductPrice> getProductPrice(String productCode){
		return selectionService.getProductPrice(productCode);
	}
	
	/**
	 * 
	 * <p>查询选型记录 8</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月23日 上午11:11:52
	 */
	@RequestMapping("/getSelectionRecord")
	public JsonResult<Pagination<SelectionRecord>> getSelectionRecord(SearchSelectionRecordParam param){
		param.setClientUserCode(WebUtils.getLoginClientUser().getClientUserInfo().getClientUserCode());
		return selectionService.getSelectionRecord(param);
	}
	
	/**
	 * 
	 * <p>保存选型记录 9</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月23日 下午3:48:46
	 */
	@RequestMapping("/addSelectionRecord")
	public JsonResult<String> addSelectionRecord(AddSelectionRecordParam param){
		param.setClientUserCode(WebUtils.getLoginClientUser().getClientUserInfo().getClientUserCode());
		return selectionService.addSelectionRecord(param);
	}
	
	/**
	 * 
	 * <p>删除选型记录 10</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月23日 下午6:32:17
	 */
	@RequestMapping("/delSelectionRecord")
	public JsonResult<String> delSelectionRecord(DelSelectionRecordParam param){
		param.setClientUserCode(WebUtils.getLoginClientUser().getClientUserInfo().getClientUserCode());
		return selectionService.delSelectionRecord(param);
	}
	
	/**
	 * 
	 * <p>添加产品到购物车 11</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月23日 下午3:48:46
	 */
	@RequestMapping("/addProductsToShoppingCart")
	public JsonResult<String> addProductsToShoppingCart(AddToShoppingCartParam param){
		param.setClientUserCode(WebUtils.getLoginClientUser().getClientUserInfo().getClientUserCode());
		return selectionService.addProductsToShoppingCart(param);
	}
	
	/**
	 * 
	 * <p>查询购物车产品信息 12</p>
	 * @param clientUserCode
	 * @return
	 * @author 黄智聪  2018年11月23日 下午6:23:58
	 */
	@RequestMapping("/getShoppingCartProductInfos")
	public JsonResult<List<ShoppingCartProductInfo>> getShoppingCartProductInfos(){
		return selectionService.getShoppingCartProductInfos(WebUtils.getLoginClientUser().getClientUserInfo().getClientUserCode());
	}
	
	/**
	 * 
	 * <p>移除购物车中的产品 13</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月23日 下午6:49:03
	 */
	@RequestMapping("/removeShoppingCartProduct")
	public JsonResult<String> removeShoppingCartProduct(Long shoppingCartId){
		return selectionService.removeShoppingCartProduct(shoppingCartId);
	}
	
	/**
	 * 
	 * <p>查询确认订单页面的商品信息 14</p>
	 * @param clientUserCode
	 * @return
	 * @author 黄智聪  2018年11月24日 下午3:03:52
	 */
	@RequestMapping("/getConfirmOrderProductInfos")
	public JsonResult<List<ProductInfomation>> getConfirmOrderProductInfos(){
		return selectionService.getConfirmOrderProductInfos(WebUtils.getLoginClientUser().getClientUserInfo().getClientUserCode());
	}
	
	/**
	 * 
	 * <p>查询用户是否能对购物车的产品进行下单操作 15</p>
	 * @param clientUserCode
	 * @return
	 * @author 黄智聪  2018年11月23日 下午6:57:21
	 */
	@RequestMapping("/checkOrderOpt")
	public JsonResult<String> checkOrderOpt(){
		return selectionService.checkOrderOpt(WebUtils.getLoginClientUser().getClientUserInfo().getClientUserCode());
	}
	
	/**
	 * 
	 * <p>下单 16</p>
	 * @return
	 * @author 黄智聪  2018年11月24日 上午10:58:45
	 */
	@RequestMapping("/addOrder")
	public JsonResult<String> addOrder(OrderParam param){
		param.setClientUserCode(WebUtils.getLoginClientUser().getClientUserInfo().getClientUserCode());
		return selectionService.addOrder(param);
	}

}
