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
import com.azz.core.common.errorcode.PlatformUserErrorCode;
import com.azz.core.common.errorcode.SystemErrorCode;
import com.azz.core.common.page.Pagination;
import com.azz.core.constants.FileConstants;
import com.azz.core.constants.PlatformConstants;
import com.azz.core.exception.BaseException;
import com.azz.exception.JSR303ValidationException;
import com.azz.platform.merchant.mapper.PlatformGoodsClassificationMapper;
import com.azz.platform.merchant.mapper.PlatformGoodsParamsMapper;
import com.azz.platform.merchant.pojo.PlatformGoodsClassification;
import com.azz.platform.merchant.pojo.bo.AddClassificationParam;
import com.azz.platform.merchant.pojo.bo.ClassificationPic;
import com.azz.platform.merchant.pojo.bo.DelClassificationParam;
import com.azz.platform.merchant.pojo.bo.EditClassificationParam;
import com.azz.platform.merchant.pojo.bo.SearchChildClassificationParam;
import com.azz.platform.merchant.pojo.bo.SearchClassificationListParam;
import com.azz.platform.merchant.pojo.bo.SearchSameLevelClassification;
import com.azz.platform.merchant.pojo.vo.ChildClassification;
import com.azz.platform.merchant.pojo.vo.Classification;
import com.azz.platform.merchant.pojo.vo.ClassificationList;
import com.azz.platform.merchant.pojo.vo.ClassificationParams;
import com.azz.system.api.SystemImageUploadService;
import com.azz.system.bo.UploadImageParam;
import com.azz.system.sequence.api.DbSequenceService;
import com.azz.util.JSR303ValidateUtils;
import com.azz.util.ObjectUtils;
import com.azz.util.StringUtils;
import com.azz.util.SystemSeqUtils;
import com.github.pagehelper.PageHelper;

/**
 * <P>
 * 分类管理
 * </P>
 * 
 * @version 1.0
 * @author 彭斌 2018年10月20日 下午2:54:40
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class ClassificationService {

    @Autowired
    PlatformGoodsClassificationMapper platformGoodsClassificationMapper;

    @Autowired
    PlatformGoodsParamsMapper platformGoodsParamsMapper;

    @Autowired
    private DbSequenceService dbSequenceService;

    @Autowired
    private SystemImageUploadService systemImageUploadService;

    /**
     * <p>
     * 新增分类
     * </p>
     * 
     * @param param
     * @return
     * @author 彭斌 2018年10月31日 下午4:16:31
     */
    public JsonResult<String> addClassification(@RequestBody AddClassificationParam param) {
        JSR303ValidateUtils.validate(param);

        // 主图基础校验
        ClassificationPic cp = param.getClassificationPic();
        String originalFileName = cp.getFileName();
        // 分类编码
        String classificationCode = dbSequenceService.getClassificationNumber();
        
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
        String newFileName = fileNameNoSufix +"_"+ SystemSeqUtils.getSeq(classificationCode);

        // 图片url
        UploadImageParam up = new UploadImageParam();
	    up.setBucketname(FileConstants.IMAGE_BUCKETNAME);
	    up.setFiledata(filedata);
	    up.setFilename(newFileName);
	    up.setImagetype(FileConstants.AZZ_CLASSIFICATION_IMAGE_TYPE);
	    up.setPlattype(FileConstants.AZZ_PLATFORM);
	    up.setSuffix(sufix);
        JsonResult<String> jr = systemImageUploadService.uploadImage(up);
        if (jr.getCode() != SystemErrorCode.SUCCESS.getCode()) {
            throw new BaseException(SystemErrorCode.SYS_ERROR_SERVICE_NOT_USE, "主图上传失败，请重试");
        }

        PlatformGoodsClassification record = new PlatformGoodsClassification();
        if (null != param.getAssortmentParentCode() && !"0".equals(param.getAssortmentParentCode())) {
            PlatformGoodsClassification pgc = platformGoodsClassificationMapper
                    .selectByAssortmentCode(param.getAssortmentParentCode());
            if (ObjectUtils.isNull(pgc)) {
                throw new BaseException(
                        PlatformUserErrorCode.PLATFORM_PRODUCT_CLASSIFICATION_NO_EXIST);
            }

            if (pgc.getAssortmentTop() == 0) {
                record.setAssortmentTop((byte) 1);
            } else if (pgc.getAssortmentTop() == 1) {
                record.setAssortmentTop((byte) 2);
            }
        } else {
            record.setAssortmentTop((byte) 0);
        }
        // 校验分类等级不允许超过三级分类
        List<ClassificationList> clList = platformGoodsClassificationMapper.selectByParam(param.getAssortmentParentCode());
        if(clList.size()>0) {
            if(clList.get(0).getAssortmentTop() == 2) {
                throw new BaseException(PlatformUserErrorCode.PLATFORM_PRODUCT_THIRD_LEVEL_CLASSIFICATION);
            }
        }
        // 校验分类名称
        SearchSameLevelClassification searchSameLevelClassification = new SearchSameLevelClassification();
        searchSameLevelClassification.setAssortmentName(param.getAssortmentName());
        searchSameLevelClassification.setAssortmentTop(record.getAssortmentTop());
        searchSameLevelClassification.setAssortmentParentCode(param.getAssortmentParentCode());
        int count = platformGoodsClassificationMapper.selectSameLevelClassification(searchSameLevelClassification);
        
        if (count > 0) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_SAME_CLASSIFICATION_ID);
        }
       
        record.setAssortmentCode(SystemSeqUtils.getSeq(classificationCode));
        record.setAssortmentParentCode(
                null == param.getAssortmentParentCode() ? "0" : param.getAssortmentParentCode());
        record.setAssortmentName(param.getAssortmentName());
        record.setAssortmentSort(param.getAssortmentSort());
        record.setAssortmentPicName(originalFileName);
        record.setAssortmentPicUrl(jr.getData());
        record.setCreateTime(new Date());
        record.setCreator(param.getCreator());
        record.setStatus(1);
        platformGoodsClassificationMapper.insertSelective(record);

        return JsonResult.successJsonResult();
    }


    /**
     * <p>
     * 修改分类
     * </p>
     * 
     * @param param
     * @return
     * @author 彭斌 2018年10月31日 下午4:36:55
     */
    public JsonResult<String> editClassification(@RequestBody EditClassificationParam param) {
        JSR303ValidateUtils.validate(param);
        
        PlatformGoodsClassification pgcObj = platformGoodsClassificationMapper
                .selectByAssortmentCode(param.getAssortmentCode());
        
        if(param.getIsEditPic() == 1) {
            // 主图基础校验
            ClassificationPic cp = param.getClassificationPic();
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
            String newFileName = fileNameNoSufix + "_" + param.getAssortmentCode();
            
            // 图片url
            UploadImageParam up = new UploadImageParam();
    	    up.setBucketname(FileConstants.IMAGE_BUCKETNAME);
    	    up.setFiledata(filedata);
    	    up.setFilename(newFileName);
    	    up.setImagetype(FileConstants.AZZ_CLASSIFICATION_IMAGE_TYPE);
    	    up.setPlattype(FileConstants.AZZ_PLATFORM);
    	    up.setSuffix(sufix);
            JsonResult<String> jr = systemImageUploadService.uploadImage(up);
            
            if (jr.getCode() != SystemErrorCode.SUCCESS.getCode()) {
                throw new BaseException(SystemErrorCode.SYS_ERROR_SERVICE_NOT_USE, "主图上传失败，请重试");
            }
            
            pgcObj.setAssortmentPicName(originalFileName);
            pgcObj.setAssortmentPicUrl(jr.getData());
        }

       

        if (null != param.getAssortmentParentCode() && !"0".equals(param.getAssortmentParentCode())) {
            PlatformGoodsClassification pgc = platformGoodsClassificationMapper
                    .selectByAssortmentCode(param.getAssortmentParentCode());
            if (ObjectUtils.isNull(pgc)) {
                throw new BaseException(
                        PlatformUserErrorCode.PLATFORM_PRODUCT_CLASSIFICATION_NO_EXIST);
            }
            if(pgc.getAssortmentTop() == 0) {
                pgcObj.setAssortmentTop((byte) 1);
            }else if (pgc.getAssortmentTop() == 1) {
                pgcObj.setAssortmentTop((byte) 2);
            }
        } else {
            pgcObj.setAssortmentTop((byte) 0);
        }

        // 校验分类等级不允许超过三级分类
        List<ClassificationList> clList = platformGoodsClassificationMapper.selectByParam(param.getAssortmentParentCode());
        if(clList.size()>0) {
            if(clList.get(0).getAssortmentTop() == 2) {
                throw new BaseException(PlatformUserErrorCode.PLATFORM_PRODUCT_THIRD_LEVEL_CLASSIFICATION);
            }
        }
        
        // 校验分类名称
        if (pgcObj.getAssortmentName().equals(param.getAssortmentName().trim())) {
            pgcObj.setAssortmentName(param.getAssortmentName());
        } else {
            SearchSameLevelClassification searchSameLevelClassification = new SearchSameLevelClassification();
            searchSameLevelClassification.setAssortmentName(param.getAssortmentName());
            searchSameLevelClassification.setAssortmentTop(pgcObj.getAssortmentTop());
            searchSameLevelClassification.setAssortmentParentCode(param.getAssortmentParentCode());
            int count = platformGoodsClassificationMapper.selectSameLevelClassification(searchSameLevelClassification);
            
            if (count > 0) {
                throw new BaseException(PlatformUserErrorCode.PLATFORM_SAME_CLASSIFICATION_ID);
            }
            pgcObj.setAssortmentName(param.getAssortmentName());
        }
        // 分类编码
        pgcObj.setAssortmentParentCode(
                null == param.getAssortmentParentCode() ? "0" : param.getAssortmentParentCode());
        pgcObj.setAssortmentSort(param.getAssortmentSort());
        pgcObj.setModifyTime(new Date());
        pgcObj.setModifier(param.getModifier());
        platformGoodsClassificationMapper.updateByPrimaryKeySelective(pgcObj);
        return JsonResult.successJsonResult();
    }

    /**
     * <p>
     * 删除分类
     * </p>
     * 
     * @param assortmentCode
     * @return
     * @author 彭斌 2018年10月31日 下午4:52:11
     */
    public JsonResult<String> delClassification(@RequestBody DelClassificationParam param) {
        JSR303ValidateUtils.validate(param);
        PlatformGoodsClassification pgcObj =
                platformGoodsClassificationMapper.selectByAssortmentCode(param.getAssortmentCode());
        if (ObjectUtils.isNull(pgcObj)) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_PRODUCT_CLASSIFICATION_NO_EXIST);
        }
        // 校验 分类删除前判断分类下是否存在模组和参数项被关联使用，存在报错“分类下存在模组和参数项，请先处理后删除！
        int countGoodsParam =
                platformGoodsParamsMapper.selectCountByParams(pgcObj.getId());
        int countModuleParam = platformGoodsClassificationMapper.selectCountById(pgcObj.getId());
        if (countGoodsParam > 0 || countModuleParam > 0) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_PRODUCT_CLASSIFICATION_EXIST);
        }
        //状态(0:无效 1：有效 2:禁用)
        PlatformGoodsClassification pgc = platformGoodsClassificationMapper.selectByAssortmentParentCode(param.getAssortmentCode());
        if(ObjectUtils.isNotNull(pgc)) {
            throw new BaseException(PlatformUserErrorCode.PLATFORM_PRODUCT_CHILD_CLASSIFICATION_EXIST);
        }
        
        pgcObj.setModifyTime(new Date());
        pgcObj.setModifier(param.getModifier());
        pgcObj.setStatus(0);
        platformGoodsClassificationMapper.updateByPrimaryKeySelective(pgcObj);
        return JsonResult.successJsonResult();
    }

    /**
     * <p>
     * 获取详情信息
     * </p>
     * 
     * @param assortmentCode
     * @return
     * @author 彭斌 2018年10月31日 下午5:17:08
     */
    public JsonResult<Classification> getClassificationInfo(
            @RequestParam("assortmentCode") String assortmentCode) {
        if (null == assortmentCode) {
            throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
                    "分类编码不存在");
        }
        Classification classification =
                platformGoodsClassificationMapper.selectDetailByAssortmentCode(assortmentCode);
        return JsonResult.successJsonResult(classification);
    }

    /**
     * <p>
     * 获取分类列表
     * </p>
     * 
     * @param param
     * @return
     * @author 彭斌 2018年11月1日 上午11:12:52
     */
    public JsonResult<List<ClassificationList>> getClassificationList(
            @RequestBody SearchClassificationListParam param) {
        List<ClassificationList> list = new ArrayList<>();
        List<ClassificationList> classificationSetList =
                platformGoodsClassificationMapper.selectByParam(param.getParam());

        if (null != classificationSetList && !classificationSetList.isEmpty()) {
            for (ClassificationList classificationSet : classificationSetList) {
                // 有效并且为父级分类
                if (classificationSet.getAssortmentTop() == 0) {
                    list.add(classificationSet);
                }
            }
        }

        // 递归填充二级三级分类
        for (ClassificationList classificationList : list) {
            // 根据分类编码查询下级分类信息
            classificationList.setChildList(
                    selectClassificationSubList(classificationList.getAssortmentCode()));
        }

        return JsonResult.successJsonResult(list);
    }

    /**
     * <p>
     * 根据分类编码查询下级分类
     * </p>
     * 
     * @param childCode
     * @return
     * @author 彭斌 2018年11月1日 下午9:23:43
     */
    @SuppressWarnings("unused")
    private List<ClassificationList> selectClassificationSubList(String childCode) {
        List<ClassificationList> result = new ArrayList<>();
        List<ClassificationList> childList =
                platformGoodsClassificationMapper.selectParentByParam(childCode);
        for (ClassificationList classificationList : childList) {
            classificationList.setChildList(
                    selectClassificationSubList(classificationList.getAssortmentCode()));
            result.add(classificationList);
        }
        return result;
    }

    
    public PlatformGoodsClassification getPlatformGoodsClassificationById(Long id) {
    	return platformGoodsClassificationMapper.selectByPrimaryKey(id);
    }
    
    /**
     * <p>获取一级分类</p>
     * @param param
     * @return
     * @author 彭斌  2018年11月6日 上午10:35:48
     */
    public JsonResult<List<ClassificationParams>> getClassificationParent(@RequestBody SearchClassificationListParam param){
        List<ClassificationParams> list = platformGoodsClassificationMapper.selectParentByAssortmentName(param);
        return JsonResult.successJsonResult(list);
    }
    
    /**
     * <p>根据分类编码获取子级的分类</p>
     * @param parentCode
     * @return
     * @author 彭斌  2018年11月6日 上午10:49:44
     */
    public JsonResult<List<ClassificationParams>> getClassificationChild(@RequestParam("parentCode") String parentCode){
        if(null == parentCode || "".equals(parentCode)) {
            throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
                    "分类编码不存在");
        }
        List<ClassificationParams> list = platformGoodsClassificationMapper.selectChild(parentCode);
        return JsonResult.successJsonResult(list);
    }
    
    /**
     * <p>客户端选型获取当前分类下的子级分类信息 </p>
     * @param param
     * @return
     * @author 彭斌  2018年12月20日 上午11:28:53
     */
    public JsonResult<Pagination<ChildClassification>> getClassificationChildPagination(@RequestBody SearchChildClassificationParam param){
        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        if(ObjectUtils.isNull(param.getAssortmentCode())) {
            throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
                    "参数异常");
        }
        List<ChildClassification> list = platformGoodsClassificationMapper.getChildClassification(param);
        return JsonResult.successJsonResult(new Pagination<>(list));
    }
}

