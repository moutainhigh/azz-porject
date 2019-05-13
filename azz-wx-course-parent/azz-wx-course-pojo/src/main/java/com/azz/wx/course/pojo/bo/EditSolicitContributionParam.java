package com.azz.wx.course.pojo.bo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class EditSolicitContributionParam {
	
	@NotBlank(message = "请选择征稿")
	private String solicitContributionCode;

	@NotBlank(message = "请填写征稿名称")
    private String solicitContributionName;
	
    @NotNull(message = "请上传征稿主图")
	private SolicitContributionPic solicitContributionPicFile;
    
    @NotBlank(message = "请填写征稿详情")
    private String solicitContributionContent;
    
    private Byte solicitContributionStatus;
    
    private String remark;
    
    @NotNull(message = "缺少请求参数")
	private Integer isChangeSolicitContributionPic;
    
    private String modifier;

}