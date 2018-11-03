/*******************************************************************************
 * Project Key : CPPII
 * Create on 2018年11月1日 下午2:23:23
 * Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/
 
package com.azz.merchant.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.azz.core.common.JsonResult;
import com.azz.core.common.errorcode.JSR303ErrorCode;
import com.azz.core.common.errorcode.SystemErrorCode;
import com.azz.core.common.page.Pagination;
import com.azz.core.constants.FileConstants;
import com.azz.core.constants.MerchantConstants;
import com.azz.core.constants.MerchantConstants.GoodsModuleStatus;
import com.azz.core.constants.MerchantConstants.IsChangeGoodsModulePic;
import com.azz.core.exception.BaseException;
import com.azz.exception.JSR303ValidationException;
import com.azz.merchant.mapper.MerchantGoodsModuleMapper;
import com.azz.merchant.mapper.MerchantMapper;
import com.azz.merchant.mapper.PlatformGoodsClassificationMapper;
import com.azz.merchant.pojo.Merchant;
import com.azz.merchant.pojo.MerchantGoodsModule;
import com.azz.merchant.pojo.PlatformGoodsClassification;
import com.azz.merchant.pojo.bo.AddGoodsModuleParam;
import com.azz.merchant.pojo.bo.EditGoodsModuleParam;
import com.azz.merchant.pojo.bo.GoodsModulePic;
import com.azz.merchant.pojo.bo.PutOnOrPutOffOrDelGoodsModuleParam;
import com.azz.merchant.pojo.bo.SearchGoodsModuleParam;
import com.azz.merchant.pojo.vo.GoodModuleInfo;
import com.azz.merchant.pojo.vo.UploadFileInfo;
import com.azz.system.api.SystemImageUploadService;
import com.azz.system.sequence.api.RandomSequenceService;
import com.azz.util.JSR303ValidateUtils;
import com.azz.util.StringUtils;
import com.github.pagehelper.PageHelper;

/**
 * <P>模组业务</P>
 * @version 1.0
 * @author 黄智聪  2018年11月1日 下午2:23:23
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class GoodsModuleService {
	
	@Autowired
	private MerchantGoodsModuleMapper merchantGoodsModuleMapper;

	@Autowired
	private PlatformGoodsClassificationMapper platformGoodsClassificationMapper;
	
	@Autowired
	private MerchantMapper merchantMapper;
	
	@Autowired
    private SystemImageUploadService systemImageUploadService;
	
	@Autowired
	private RandomSequenceService randomSequenceService;
	
	/**
	 * 
	 * <p>查询模组列表</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月1日 下午3:33:53
	 */
	public JsonResult<Pagination<GoodModuleInfo>> getGoodModuleInfoList(@RequestBody SearchGoodsModuleParam param){
		PageHelper.startPage(param.getPageNum(), param.getPageSize());
		List<GoodModuleInfo> infos = merchantGoodsModuleMapper.getGoodsModuleInfoList(param);
		return JsonResult.successJsonResult(new Pagination<>(infos));
	}
	
	/**
	 * 
	 * <p>查询模组详情</p>
	 * @param moduleCode
	 * @return
	 * @author 黄智聪  2018年11月1日 下午8:45:15
	 */
	public JsonResult<GoodModuleInfo> getGoodModuleInfo(String moduleCode){
		return JsonResult.successJsonResult(merchantGoodsModuleMapper.getGoodsModuleInfo(moduleCode));
	}
	
	/**
	 * 
	 * <p>新增商品模组</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月1日 下午4:08:42
	 */
	public JsonResult<String> addGoodsModule(@RequestBody AddGoodsModuleParam param){
		// 参数校验
		JSR303ValidateUtils.validate(param);
		
		PlatformGoodsClassification goodsClassification = platformGoodsClassificationMapper
				.selectByAssortmentCode(param.getAssortmentCode());
		if(goodsClassification == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "分类不存在");
		}
		if(goodsClassification.getAssortmentTop() != 2) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "只允许选择三级分类");
		}
		String merchantCode = param.getMerchantCode();
		Merchant merchant = merchantMapper.selectByMerchantCode(merchantCode);
		if(merchant == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "商户不存在");
		}
		int count = merchantGoodsModuleMapper.countGoodsModule(merchant.getId(), param.getModuleName(), null);
		if(count > 0) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "已存在相同模组请修改模组名称");
		}
		GoodsModulePic pic = param.getGoodsModulePic();
		String moduleCode = randomSequenceService.getModuleCodeNumber();
		// 上传模组图片
		UploadFileInfo fileInfo = uploadModulePic(pic, moduleCode);
		MerchantGoodsModule goodsModuleRecord = MerchantGoodsModule.builder()
				.creator(param.getCreator())
				.createTime(new Date())
				.classificationId(goodsClassification.getId())
				.merchantId(merchant.getId())
				.moduleCode(moduleCode)
				.moduleInfo(param.getModuleInfo())
				.moduleName(param.getModuleName())
				.modulePicName(fileInfo.getOriginalFileName())
				.modulePicUrl(fileInfo.getImgUrl())
				.moduleStatus(param.getModuleStatus())
				.build();
		merchantGoodsModuleMapper.insertSelective(goodsModuleRecord);
		return JsonResult.successJsonResult();
	}
	
	/**
	 * 
	 * <p>修改商品模组</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月1日 下午5:16:03
	 */
	public JsonResult<String> editGoodsModule(@RequestBody EditGoodsModuleParam param){
		// 参数校验
		JSR303ValidateUtils.validate(param);
		String moduleCode = param.getModuleCode();
		MerchantGoodsModule module = merchantGoodsModuleMapper.selectByModuleCode(moduleCode);
		if(module == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "模组不存在");
		}
		PlatformGoodsClassification goodsClassification = platformGoodsClassificationMapper
						.selectByAssortmentCode(param.getAssortmentCode());
		if(goodsClassification == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "分类不存在");
		}
		if(goodsClassification.getAssortmentTop() != 2) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "只允许选择三级分类");
		}
		String merchantCode = param.getMerchantCode();
		Merchant merchant = merchantMapper.selectByMerchantCode(merchantCode);
		if(merchant == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "商户不存在");
		}
		// 查询除自己以外的模组是否存在相同的模组名
		int count = merchantGoodsModuleMapper.countGoodsModule(merchant.getId(), param.getModuleName(), moduleCode);
		if(count > 0) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "已存在相同模组请修改模组名称");
		}
		MerchantGoodsModule merchantGoodsModule = MerchantGoodsModule.builder()
				.modifier(param.getModifier())
				.modifyTime(new Date())
				.moduleCode(moduleCode)
				.moduleInfo(param.getModuleInfo())
				.moduleName(param.getModuleName())
				.classificationId(goodsClassification.getId())
				.moduleStatus(param.getModuleStatus())
				.build();
		// 修改模组图片，则重新上传
		int isChangeGoodsModulePic = param.getIsChangeGoodsModulePic();
		if(isChangeGoodsModulePic == IsChangeGoodsModulePic.Y.getValue()) {
			GoodsModulePic pic = param.getGoodsModulePic();
			// 上传模组图片
			UploadFileInfo fileInfo = uploadModulePic(pic, moduleCode);
			merchantGoodsModule.setModulePicName(fileInfo.getOriginalFileName());
			merchantGoodsModule.setModulePicUrl(fileInfo.getImgUrl());
		}
		merchantGoodsModuleMapper.updateByModuleCode(merchantGoodsModule);
		return JsonResult.successJsonResult();
	}
	
	/**
	 * 
	 * <p>上架、下架或删除模组</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年11月1日 下午8:02:16
	 */
	public JsonResult<String> putOnOrPutOffOrDelGoodsModule(@RequestBody PutOnOrPutOffOrDelGoodsModuleParam param){
		// 参数校验
		JSR303ValidateUtils.validate(param);
		String moduleCode = param.getModuleCode();
		MerchantGoodsModule module = merchantGoodsModuleMapper.selectByModuleCode(moduleCode);
		if(module == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "模组不存在");
		}
		boolean yes = GoodsModuleStatus.checkStatusExist(param.getModuleStatus());
		if(!yes) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "模组状态不存在");
		}
		MerchantGoodsModule merchantGoodsModuleRecord = MerchantGoodsModule.builder()
				.modifier(param.getModifier())
				.modifyTime(new Date())
				.moduleCode(moduleCode)
				.moduleStatus(param.getModuleStatus())
				.build();
		merchantGoodsModuleMapper.updateByModuleCode(merchantGoodsModuleRecord);
		
		return JsonResult.successJsonResult();
	}
	
	/**
	 * 
	 * <p>上传模组主图文件</p>
	 * @param pic
	 * @param moduleCode
	 * @return
	 * @author 黄智聪  2018年11月1日 下午4:49:22
	 */
	public UploadFileInfo uploadModulePic(GoodsModulePic pic, String moduleCode) {
		String originalFileName = pic.getFileName();
	    if(StringUtils.isBlank(originalFileName)) {
	    	throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "模组主图文件名为空");
	    }
	    if(pic.getFileSize() > MerchantConstants.GOODS_MODULE_FILE_SIZE_LIMIT) {
	    	throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "模组主图文件大小不能超过2M");
	    }
	    String filedata = pic.getFileBase64Str();
	    if(StringUtils.isBlank(filedata)) {
	    	throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "模组主图文件内容为空");
	    }
	    int dotIndex = originalFileName.lastIndexOf(".");
	    String fileNameNoSufix = originalFileName.substring(0, dotIndex);
	    String sufix = originalFileName.substring(dotIndex + 1, originalFileName.length());
	    // 新名称为文件名 + 模组编码
	    String newFileName = fileNameNoSufix + "_" + moduleCode;
	    // 图片url  TODO
	    JsonResult<String> jr = systemImageUploadService.uploadImage(FileConstants.IMAGE_BUCKETNAME, newFileName, sufix,
		    filedata, FileConstants.AZZ_MERCHANT, FileConstants.AZZ_BUSINESS_IMAGE_TYPE);
	    UploadFileInfo file = new UploadFileInfo();
	    if(jr.getCode() == SystemErrorCode.SUCCESS.getCode()) {
	    	file.setImgUrl(jr.getData());
	    	file.setOriginalFileName(originalFileName);
	    }else {
	    	throw new BaseException(SystemErrorCode.SYS_ERROR_SERVICE_NOT_USE,"模组主图上传失败，请重试");
	    }
	    return file;
	}
}
