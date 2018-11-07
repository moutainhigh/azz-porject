package com.azz.platform.merchant.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.azz.core.common.JsonResult;
import com.azz.core.common.errorcode.JSR303ErrorCode;
import com.azz.core.common.errorcode.MerchantProductErrorCode;
import com.azz.core.common.errorcode.SystemErrorCode;
import com.azz.core.common.page.Pagination;
import com.azz.core.constants.FileConstants;
import com.azz.core.constants.PlatformConstants;
import com.azz.core.exception.BaseException;
import com.azz.exception.JSR303ValidationException;
import com.azz.platform.merchant.mapper.PlatformCaseClassificationParamsMapper;
import com.azz.platform.merchant.mapper.PlatformCaseMapper;
import com.azz.platform.merchant.pojo.PlatformCase;
import com.azz.platform.merchant.pojo.PlatformCaseClassificationParams;
import com.azz.platform.merchant.pojo.bo.AddCaseParam;
import com.azz.platform.merchant.pojo.bo.AddSelectionParams;
import com.azz.platform.merchant.pojo.bo.CasePic;
import com.azz.platform.merchant.pojo.bo.DelSelecttionParams;
import com.azz.platform.merchant.pojo.bo.EditCaseParam;
import com.azz.platform.merchant.pojo.bo.SearchCaseParamList;
import com.azz.platform.merchant.pojo.vo.CaseParams;
import com.azz.platform.merchant.pojo.vo.CaseParamsList;
import com.azz.system.api.SystemImageUploadService;
import com.azz.system.sequence.api.RandomSequenceService;
import com.azz.util.JSR303ValidateUtils;
import com.azz.util.ObjectUtils;
import com.azz.util.StringUtils;
import com.github.pagehelper.PageHelper;

/**
 * 
 * <P>方案管理</P>
 * @version 1.0
 * @author 彭斌  2018年11月6日 下午2:07:01
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class CaseService {

    @Autowired
    SystemImageUploadService systemImageUploadService;
    
    @Autowired
    PlatformCaseMapper platformCaseMapper;
    
    @Autowired
    PlatformCaseClassificationParamsMapper platformCaseClassificationParamsMapper;
    
    @Autowired
    private RandomSequenceService randomSequenceService;
   
    /**
     * <p>新增方案</p>
     * @param param
     * @return
     * @author 彭斌  2018年11月5日 下午7:23:52
     */
    public JsonResult<String> addCase(@RequestBody AddCaseParam param) {
        JSR303ValidateUtils.validate(param);

        // 主图基础校验
        CasePic cp = param.getCasePic();
        String originalFileName = cp.getFileName();
        
        PlatformCase pcObj = new PlatformCase();
        
        
        // 编辑的方案名称和原始的方案名不一致校验方案名称是否唯一
        PlatformCase platformCase = platformCaseMapper.selectByCaseName(param.getCaseName());
        if(ObjectUtils.isNotNull(platformCase)) {
            // 方案名称存在
            throw new BaseException(
                    MerchantProductErrorCode.MERCHANT_PRODUCT_CASE_EXIST);
        }
        
        // 编辑方案分类校验是否唯一
        PlatformCase platformCaseByClassification = platformCaseMapper.selectByClassificationId(param.getClassificationId());
        if(ObjectUtils.isNotNull(platformCaseByClassification)) {
            // 方案分类存在
            throw new BaseException(
                    MerchantProductErrorCode.MERCHANT_PRODUCT_CASE_CLASSIFICATION_EXIST);
        }
        
        // 新增方案编码 TODO 系统生成
        String caseCode = randomSequenceService.getClassificationNumber();
        
        if (StringUtils.isBlank(originalFileName)) {
            throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
                    "主图文件名为空");
        }
        if (cp.getFileSize() > PlatformConstants.CLASSIFICATION_FILE_SIZE_LIMIT) {
            throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
                    "主图文件大小不能超过2M");
        }
        String filedata = cp.getFileBase64Str();
        if (StringUtils.isBlank(filedata)) {
            throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
                    "主图文件内容为空");
        }

        int dotIndex = originalFileName.lastIndexOf(".");
        String fileNameNoSufix = originalFileName.substring(0, dotIndex);
        String sufix = originalFileName.substring(dotIndex + 1, originalFileName.length());
        // 新名称为文件名 + 文件后缀
        String newFileName = fileNameNoSufix +"_"+ caseCode;

        // 图片url
        JsonResult<String> jr = systemImageUploadService.uploadImage(FileConstants.IMAGE_BUCKETNAME, newFileName, sufix, filedata, FileConstants.AZZ_PLATFORM, FileConstants.AZZ_CASE_IMAGE_TYPE);
        if (jr.getCode() != SystemErrorCode.SUCCESS.getCode()) {
            throw new BaseException(SystemErrorCode.SYS_ERROR_SERVICE_NOT_USE, "主图上传失败，请重试");
        }
        
        
        
        pcObj.setCaseCode(caseCode);
        pcObj.setCaseName(param.getCaseName());
        pcObj.setCasePicName(originalFileName);
        pcObj.setCasePicUrl(jr.getData());
        pcObj.setCaseStatus(param.getCaseStatus());
        pcObj.setClassificationId(param.getClassificationId());
        pcObj.setCreateTime(new Date());
        pcObj.setCreator(param.getCreator());
        pcObj.setRemark(param.getRemark());
       
        platformCaseMapper.insertSelective(pcObj);
        return JsonResult.successJsonResult();
    }

    /**
     * 
     * <p>编辑方案</p>
     * @param param
     * @return
     * @author 彭斌  2018年11月6日 下午4:11:31
     */
    public JsonResult<String> editCase(@RequestBody EditCaseParam param){
        
        PlatformCase pcObj = platformCaseMapper.selectByCaseCode(param.getCaseCode());
        
        if(!pcObj.getCaseName().equals(param.getCaseName())) {
            // 编辑的方案名称和原始的方案名不一致校验方案名称是否唯一
            PlatformCase platformCase = platformCaseMapper.selectByCaseName(param.getCaseName());
            if(ObjectUtils.isNotNull(platformCase)) {
                // 方案名称存在
                throw new BaseException(
                        MerchantProductErrorCode.MERCHANT_PRODUCT_CASE_EXIST);
            }
        }
        
        if(!pcObj.getClassificationId().equals(param.getClassificationId())) {
            // 编辑方案分类校验是否唯一
            PlatformCase platformCaseByClassification = platformCaseMapper.selectByClassificationId(param.getClassificationId());
            if(ObjectUtils.isNotNull(platformCaseByClassification)) {
                // 方案分类存在
                throw new BaseException(
                        MerchantProductErrorCode.MERCHANT_PRODUCT_CASE_CLASSIFICATION_EXIST);
            }
        }
        
        if(param.getIsEditPic() == 1) {
            // 主图基础校验
            CasePic cp = param.getCasePic();
            String originalFileName = cp.getFileName();
            if (StringUtils.isBlank(originalFileName)) {
                throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
                        "主图文件名为空");
            }
            if (cp.getFileSize() > PlatformConstants.CLASSIFICATION_FILE_SIZE_LIMIT) {
                throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
                        "主图文件大小不能超过2M");
            }
            String filedata = cp.getFileBase64Str();
            if (StringUtils.isBlank(filedata)) {
                throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
                        "主图文件内容为空");
            }
            
            int dotIndex = originalFileName.lastIndexOf(".");
            String fileNameNoSufix = originalFileName.substring(0, dotIndex);
            String sufix = originalFileName.substring(dotIndex + 1, originalFileName.length());
            // 新名称为文件名 + 文件后缀
            String newFileName = fileNameNoSufix + "_" + param.getCaseCode();
            
            // 图片url
            JsonResult<String> jr = systemImageUploadService.uploadImage(FileConstants.IMAGE_BUCKETNAME, newFileName, sufix, filedata, FileConstants.AZZ_PLATFORM, FileConstants.AZZ_CASE_IMAGE_TYPE);
            
            if (jr.getCode() != SystemErrorCode.SUCCESS.getCode()) {
                throw new BaseException(SystemErrorCode.SYS_ERROR_SERVICE_NOT_USE, "主图上传失败，请重试");
            }
            
            pcObj.setCasePicName(originalFileName);
            pcObj.setCasePicUrl(jr.getData());
        }
        
        
        pcObj.setCaseName(param.getCaseName());
        pcObj.setCaseStatus(param.getCaseStatus());
        pcObj.setClassificationId(param.getClassificationId());
        pcObj.setRemark(param.getRemark());
        platformCaseMapper.updateByPrimaryKeySelective(pcObj);
        return JsonResult.successJsonResult();
    }
    
    /**
     * <p>根据分类编码获取参数信息</p>
     * @param assortmentId
     * @return
     * @author 彭斌  2018年11月6日 下午4:11:44
     */
    public JsonResult<Pagination<CaseParams>> getCaseParamList(@RequestBody SearchCaseParamList param){
        if(null == param.getAssortmentId()) {
            throw new BaseException(
                    MerchantProductErrorCode.MERCHANT_PRODUCT_CASE_CLASSIFICATION_IS_NULL);
        }
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        List<CaseParams> list = platformCaseMapper.selectParamsByAssortmentId(param);
        return JsonResult.successJsonResult(new Pagination<>(list));
    }
    
    
    /**
     * <p>添加选型参数</p>
     * @param param
     * @return
     * @author 彭斌  2018年11月6日 下午4:40:14
     */
    public JsonResult<String> addParam(@RequestBody AddSelectionParams param){
        if(null == param.getCaseId() || param.getParamsId().size() == 0) {
            throw new BaseException(
                    MerchantProductErrorCode.MERCHANT_PRODUCT_CASE_ID_OR_PARAMS_IS_NULL);
        }
        
        for (int i = 0; i < param.getParamsId().size(); i++) {
            PlatformCaseClassificationParams record = new PlatformCaseClassificationParams();
            record.setCaseId(param.getCaseId());
            record.setCreateTime(new Date());
            record.setCreator(param.getCreator());
            record.setParamsId(param.getParamsId().get(i));
            platformCaseClassificationParamsMapper.insertSelective(record);
        }
        return JsonResult.successJsonResult();
    }
    
    /**
     * <p>根据方案编码获取选型参数</p>
     * @param caseCode
     * @return
     * @author 彭斌  2018年11月6日 下午5:22:17
     */
    public JsonResult<List<CaseParamsList>> getCaseSelectionParameter(@RequestParam("caseCode") String caseCode){
        List<CaseParamsList> list = new ArrayList<>();
        if(!"".equals(caseCode)) {
            list = platformCaseMapper.selectParamsByCaseCode(caseCode);
        }
        return JsonResult.successJsonResult(list);
    }
    
    /**
     * <p>移除选型参数</p>
     * @param param
     * @return
     * @author 彭斌  2018年11月6日 下午5:55:11
     */
    public JsonResult<String> delSelectionParameter(@RequestBody DelSelecttionParams param){
        PlatformCaseClassificationParams pccp = platformCaseClassificationParamsMapper.selectParam(param.getCaseId(), param.getParamsId());
        if(ObjectUtils.isNotNull(pccp)) {
            pccp.setLastModifyTime(new Date());
            pccp.setModifier(param.getModifier());
            pccp.setStatus(0);
            platformCaseClassificationParamsMapper.updateByPrimaryKeySelective(pccp);
        }
        return JsonResult.successJsonResult();
    }
}

