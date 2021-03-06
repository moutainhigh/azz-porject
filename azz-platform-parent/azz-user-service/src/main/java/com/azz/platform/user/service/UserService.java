/*******************************************************************************
 * Project Key : CPPII Create on 2018年10月14日 上午9:27:50 Copyright (c) 2018. 爱智造.
 * 注意：本内容仅限于爱智造内部传阅，禁止外泄以及用于其他的商业目的
 ******************************************************************************/

package com.azz.platform.user.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.azz.core.common.JsonResult;
import com.azz.core.common.errorcode.JSR303ErrorCode;
import com.azz.core.common.errorcode.PlatformUserErrorCode;
import com.azz.core.common.errorcode.ShiroAuthErrorCode;
import com.azz.core.common.page.Pagination;
import com.azz.core.constants.EmailConstants;
import com.azz.core.constants.PlatformConstants.PersonalEditType;
import com.azz.core.constants.SmsConstants;
import com.azz.core.constants.SmsConstants.SmsCode;
import com.azz.core.constants.UserConstants;
import com.azz.core.constants.UserConstants.UserStatus;
import com.azz.core.exception.BaseException;
import com.azz.core.exception.ShiroAuthException;
import com.azz.exception.JSR303ValidationException;
import com.azz.model.Password;
import com.azz.platform.user.mapper.PlatformDeptMapper;
import com.azz.platform.user.mapper.PlatformPermissionMapper;
import com.azz.platform.user.mapper.PlatformRoleMapper;
import com.azz.platform.user.mapper.PlatformUserMapper;
import com.azz.platform.user.mapper.PlatformUserRoleMapper;
import com.azz.platform.user.pojo.PlatformDept;
import com.azz.platform.user.pojo.PlatformRole;
import com.azz.platform.user.pojo.PlatformUser;
import com.azz.platform.user.pojo.PlatformUserRole;
import com.azz.platform.user.pojo.bo.AddUserParam;
import com.azz.platform.user.pojo.bo.CheckVerificationCodeParam;
import com.azz.platform.user.pojo.bo.EditPasswordParam;
import com.azz.platform.user.pojo.bo.EditPersonalInfoParam;
import com.azz.platform.user.pojo.bo.EditUserParam;
import com.azz.platform.user.pojo.bo.EnableOrDisableOrDelUserParam;
import com.azz.platform.user.pojo.bo.ImportPlatformUserParam;
import com.azz.platform.user.pojo.bo.LoginParam;
import com.azz.platform.user.pojo.bo.SearchUserParam;
import com.azz.platform.user.pojo.vo.LoginUserInfo;
import com.azz.platform.user.pojo.vo.Menu;
import com.azz.platform.user.pojo.vo.UserInfo;
import com.azz.platform.user.pojo.vo.UserPermission;
import com.azz.system.api.SystemEmailService;
import com.azz.system.api.SystemSmsSendService;
import com.azz.system.bo.MailCheck;
import com.azz.system.bo.MailCodeValidation;
import com.azz.system.bo.MailParam;
import com.azz.system.bo.SmsCheck;
import com.azz.system.bo.SmsCodeValidation;
import com.azz.system.bo.SmsParams;
import com.azz.system.sequence.api.DbSequenceService;
import com.azz.system.vo.SmsInfo;
import com.azz.util.ExcelUtils;
import com.azz.util.JSR303ValidateUtils;
import com.azz.util.ObjectUtils;
import com.azz.util.PasswordHelper;
import com.azz.util.RandomStringUtils;
import com.azz.util.StringUtils;
import com.azz.util.SystemSeqUtils;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;

/**
 * <P>
 * TODO
 * </P>
 * 
 * @version 1.0
 * @author 刘建麟 2018年10月14日 上午9:27:50
 */
@Transactional(rollbackFor = Exception.class)
@Service
@Slf4j
public class UserService {

	@Autowired
	private PlatformUserMapper platformUserMapper;

	@Autowired
	private PlatformPermissionMapper platformPermissionMapper;

	@Autowired
	private PlatformDeptMapper platformDeptMapper;

	@Autowired
	private PlatformRoleMapper platformRoleMapper;

	@Autowired
	private PlatformUserRoleMapper platformUserRoleMapper;

	@Autowired
	private DbSequenceService dbSequenceService;

	@Autowired
	private SystemSmsSendService systemSmsSendService;
	
	@Autowired
	private SystemEmailService systemEmailService;

	public JsonResult<String> loginAuth(@RequestBody LoginParam param) {
		log.debug("————身份认证方法————");
		String phoneNumber = param.getPhoneNumber();
		String password = param.getPassword();
		PlatformUser platformUser = platformUserMapper.getUserByPhoneNumber(phoneNumber, null);
		if (platformUser == null) {// 无效用户
			throw new ShiroAuthException(ShiroAuthErrorCode.SHIRO_AUTH_ERROR_LOGIN_ERROR, "请输入正确的账号或密码");
		}
		if (platformUser.getStatus() == UserStatus.INVALID.getValue()) {
			throw new ShiroAuthException(ShiroAuthErrorCode.SHIRO_AUTH_ERROR_LOGIN_ERROR, "账号已被禁用，请联系管理员解除");
		}
		boolean isRight = PasswordHelper.checkPassword(password, platformUser.getSalt(), platformUser.getPassword());
		if (!isRight) {// 与盐值加密的密码不匹配
			throw new ShiroAuthException(ShiroAuthErrorCode.SHIRO_AUTH_ERROR_LOGIN_ERROR, "请输入正确的账号或密码");
		}
		return JsonResult.successJsonResult();
	}

	public JsonResult<LoginUserInfo> getLoginUserInfoByPhoneNumber(String phoneNumber) {
		LoginUserInfo info = new LoginUserInfo();
		UserInfo userInfo = platformUserMapper.getUserInfoByPhoneNumber(phoneNumber);
		List<UserPermission> userPermissions = platformPermissionMapper.getUserPermissionInfoByPhoneNumber(phoneNumber);
		info.setUserInfo(userInfo);
		info.setUserPermissions(userPermissions);
		info.setMenus(generateMenuTree(phoneNumber));
		return JsonResult.successJsonResult(info);
	}

	/**
	 * 
	 * <p>
	 * 导入平台端成员
	 * </p>
	 * 
	 * @param param
	 * @return
	 * @throws IOException
	 * @author 黄智聪 2018年12月11日 下午3:16:08
	 */
	public JsonResult<String> importPlatformUser(@RequestBody ImportPlatformUserParam param) throws IOException {
		// 记录出错行数
		int errorRowNum = 1;
		String creator = param.getCreator();

		String base64Str = param.getBase64Str();
		// 将字符串转换为byte数组
		byte[] bytes = new BASE64Decoder().decodeBuffer(base64Str);
		// 转化为输入流
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

		@Cleanup
		HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
		HSSFSheet sheet = workbook.getSheetAt(0);
		// 手机号码以及对应的密码
		Map<String, String> phoneNumbers = new HashMap<>();
		int lastRowNum = sheet.getLastRowNum();
		if (lastRowNum == 0) {// 未填写数据
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "导入数据不可为空");
		}
		for (int i = 1; i <= lastRowNum; i++) {
			HSSFRow row = sheet.getRow(i);
			errorRowNum++;
			if (ObjectUtils.isNotNull(row)) {
				// 获取当前行的元素信息
				ArrayList<Cell> rowCells = Lists.newArrayList(row.cellIterator());
				String userName = null;
				String phoneNumber = null;
				String email = null;
				String postName = null;
				Long roleId = null;
				Long deptId = null;
				String randomPwd = null;

				// 成员姓名校验
				if (Cell.CELL_TYPE_BLANK != rowCells.get(0).getCellType()) {
					String cell_1 = ExcelUtils.getStringValue(row.getCell(0));
					if (StringUtils.isNotBlank(cell_1)) {
						userName = cell_1;
					} else {
						throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
								"第" + errorRowNum + "行成员姓名不允许为空");
					}
				} else {
					throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
							"第" + errorRowNum + "行成员姓名不允许为空");
				}

				// 手机号校验
				if (Cell.CELL_TYPE_BLANK != rowCells.get(1).getCellType()) {
					String cell_2 = ExcelUtils.getStringValue(row.getCell(1));
					if (StringUtils.isNotBlank(cell_2)) {
						phoneNumber = cell_2;
						if (!StringUtils.isPhoneNumber(phoneNumber)) {
							throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
									"第" + errorRowNum + "行手机号格式不正确");
						}
						PlatformUser u = platformUserMapper.getUserByPhoneNumber(phoneNumber, null);
						if (u != null) {
							throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
									"第" + errorRowNum + "行手机号已存在");
						}
					} else {
						throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
								"第" + errorRowNum + "行手机号不允许为空");
					}
				} else {
					throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
							"第" + errorRowNum + "行手机号不允许为空");
				}

				// 邮箱校验
				if (Cell.CELL_TYPE_BLANK != rowCells.get(2).getCellType()) {
					String cell_3 = ExcelUtils.getStringValue(row.getCell(2));
					if (StringUtils.isNotBlank(cell_3)) {
						email = cell_3;
						if (!StringUtils.isEmail(email)) {
							throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
									"第" + errorRowNum + "行邮箱格式不正确");
						}
						PlatformUser u = platformUserMapper.getUserByEmail(email, null);
						if (u != null) {
							throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
									"第" + errorRowNum + "行邮箱已存在");
						}
					}
				}

				// 岗位名称校验
				if (Cell.CELL_TYPE_BLANK != rowCells.get(3).getCellType()) {
					String cell_4 = ExcelUtils.getStringValue(row.getCell(3));
					if (StringUtils.isNotBlank(cell_4)) {
						postName = cell_4;
					}
				}

				// 部门编码校验
				if (Cell.CELL_TYPE_BLANK != rowCells.get(4).getCellType()) {
					String cell_5 = ExcelUtils.getStringValue(row.getCell(4));
					if (StringUtils.isNotBlank(cell_5)) {
						String deptCode = cell_5;
						PlatformDept dept = platformDeptMapper.selectByDeptCode(deptCode);
						if (dept == null) {
							throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
									"第" + errorRowNum + "行部门编码不存在");
						}
						deptId = dept.getId();
					} else {
						throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
								"第" + errorRowNum + "行部门编码不允许为空");
					}
				} else {
					throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
							"第" + errorRowNum + "行部门编码不允许为空");
				}

				// 角色编码校验
				if (Cell.CELL_TYPE_BLANK != rowCells.get(5).getCellType()) {
					String cell_6 = ExcelUtils.getStringValue(row.getCell(5));
					if (StringUtils.isNotBlank(cell_6)) {
						String roleCode = cell_6;
						PlatformRole role = platformRoleMapper.selectByRoleCode(roleCode);
						if (role == null) {
							throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
									"第" + errorRowNum + "行角色编码不存在");
						}
						roleId = role.getId();
					} else {
						throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
								"第" + errorRowNum + "行角色编码不允许为空");
					}
				} else {
					throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM,
							"第" + errorRowNum + "行角色编码不允许为空");
				}

				// 随机6位数字密码
				randomPwd = RandomStringUtils.randomNumeric(6);
				// 生成盐值加密的密码
				Password pwd = PasswordHelper.encryptPasswordByModel(randomPwd);
				// 将手机号和密码存起来
				phoneNumbers.put(phoneNumber, randomPwd);

				Date nowDate = new Date();
				String code = dbSequenceService.getPlatEmployeeNumber();
				PlatformUser userRecord = PlatformUser.builder().createTime(nowDate).creator(creator).deptId(deptId)
						.email(email).password(pwd.getPassword()).phoneNumber(phoneNumber).postName(postName)
						.userCode(SystemSeqUtils.getSeq(code)).userName(userName).salt(pwd.getSalt()).build();
				platformUserMapper.insertSelective(userRecord);
				// 用户与角色绑定
				PlatformUserRole userRoleRecord = PlatformUserRole.builder().createTime(nowDate).creator(creator)
						.userId(userRecord.getId()).roleId(roleId).build();
				platformUserRoleMapper.insertSelective(userRoleRecord);

			}
		}

		// 向成员发送密码
		Set<String> set = phoneNumbers.keySet();
		for (String phoneNumber : set) {
			String pwd = phoneNumbers.get(phoneNumber);
			try {
				// 发送短信
				this.sendPasswordMsg(phoneNumber, pwd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return JsonResult.successJsonResult();
	}

	// 发送短信通知成员
	private void sendPasswordMsg(String phoneNumber, String password) {
		SmsParams sms = new SmsParams();
		sms.setPhone(phoneNumber);
		sms.setMsgType(SmsConstants.ACCOUNT_CREATE_SUCCESS.getMsgType());
		sms.setCode(password);
		systemSmsSendService.sendSmsCode(sms);
	}
	
	

	/**
	 * 
	 * <p>修改个人资料</p>
	 * @param param
	 * @return
	 * @author 黄智聪  2018年12月12日 下午2:56:38
	 */
	public JsonResult<String> editPersonalInfo(@RequestBody EditPersonalInfoParam param){
		JSR303ValidateUtils.validate(param);
		PlatformUser platformUserRecord = null;
		PlatformUser u = null;
		Date nowDate = new Date();
		switch (param.getEditType()) {
		case PersonalEditType.NAME:
			String platformUserName = param.getUserName();
			if(StringUtils.isBlank(platformUserName)) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "姓名不允许为空");
			}
			platformUserRecord = PlatformUser.builder()
					.userCode(param.getModifier())
					.userName(platformUserName)
					.modifier(param.getModifier())
					.lastModifyTime(nowDate)
					.build();
			break;
		case PersonalEditType.PHONE_NUMBER:
			String phoneNumber = param.getPhoneNumber();
			// 手机格式校验
			if(StringUtils.isBlank(phoneNumber)) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "手机号不允许为空");
			}
			if (!StringUtils.isPhoneNumber(phoneNumber)) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "请输入正确的手机号");
			}
			// 手机是否已被客户成员所使用
			u = platformUserMapper.getUserByPhoneNumber(phoneNumber, param.getModifier());
			if (u != null) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "手机号已被使用，请更改");
			}
			
			// 校验验证码
			CheckVerificationCodeParam checkParam = new CheckVerificationCodeParam();
			checkParam.setPhoneNumber(phoneNumber);
			checkParam.setVerificationCode(param.getVerificationCode());
			this.checkEditVerificationCode(checkParam);

			platformUserRecord = PlatformUser.builder()
					.userCode(param.getModifier())
					.phoneNumber(phoneNumber)
					.modifier(param.getModifier())
					.lastModifyTime(nowDate)
					.build();
			break;
		case PersonalEditType.EMAIL:
			String email = param.getEmail();
			// 邮箱格式校验
			if(StringUtils.isBlank(email)) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "邮箱不允许为空");
			}
			if (!StringUtils.isEmail(email)) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "请输入正确的邮箱");
			}
			// 邮箱是否已被客户成员所使用
			u = platformUserMapper.getUserByEmail(email, param.getModifier());
			if (u != null) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "邮箱已被使用，请更改");
			}
			
			// 校验邮箱验证码
			this.checkEditEmailVerificationCode(param.getVerificationCode(), email);

			platformUserRecord = PlatformUser.builder()
					.userCode(param.getModifier())
					.email(email)
					.modifier(param.getModifier())
					.lastModifyTime(nowDate)
					.build();
			break;
		case PersonalEditType.PASSWORD:
			String password = param.getPassword();
			String confirmPassword = param.getConfirmPassword();
			if(StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "密码或确认密码不允许为空");
			}
			// 密码与确认密码一致性校验
			if (!password.equals(confirmPassword)) {
			    throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "密码与确认密码不一致");
			}
			
			// 生成盐值加密的密码
			Password pwd = PasswordHelper.encryptPasswordByModel(password);
			platformUserRecord = PlatformUser.builder()
					.userCode(param.getModifier())
					.password(pwd.getPassword())
					.salt(pwd.getSalt())
					.modifier(param.getModifier())
					.lastModifyTime(nowDate)
					.build();
			break;
		default:
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "修改类型不存在");
		}
		platformUserMapper.updateByUserCode(platformUserRecord);
		return JsonResult.successJsonResult();
	}
	
	/**
	 * 
	 * <p>
	 * 发送修改个人信息的邮箱验证码
	 * </p>
	 * 
	 * @param phoneNumber
	 * @return
	 * @author 黄智聪 2018年10月22日 下午5:37:30
	 */
	public JsonResult<String> sendEditEmailVerificationCode(String email) {
		MailParam m = new MailParam();
		m.setTo(email);
		JsonResult<SmsInfo> jr = systemEmailService.sendMail(m);
		if (jr.getData() == null || !jr.getData().getCode().equals(EmailConstants.EMAIL_SEND_SUCCESS)) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "邮箱验证码发送失败，请重试");
		}
		return JsonResult.successJsonResult();
	}
	
	
	
	/**
	 * 
	 * <p>校验邮箱验证码 </p>
	 * @param verificationCode
	 * @param email
	 * @author 黄智聪  2018年12月12日 下午4:27:39
	 */
	public void checkEditEmailVerificationCode(String verificationCode, String email) {
		// 先校验验证码是否已失效
		JsonResult<SmsInfo> jr = null;
		MailCodeValidation mcv = new MailCodeValidation();
		mcv.setMail(email);
		mcv.setSec(UserConstants.CHANGE_DATA_EMAIL_TIME_OUT);
		jr = systemEmailService.validationMailCodeTime(mcv);
		if (jr.getData() == null || !jr.getData().getCode().equals(EmailConstants.EMAIL_SEND_SUCCESS)) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "邮箱验证码已失效，请重新获取");
		}
		// 再校验邮箱验证码是否正确
		MailCheck mailCheck = new MailCheck();
		mailCheck.setCode(verificationCode);
		mailCheck.setMail(email);
		jr = systemEmailService.checkMailCode(mailCheck);
		if (jr.getData() == null || !jr.getData().getCode().equals(EmailConstants.EMAIL_SEND_SUCCESS)) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "验证码错误");
		}
	}
	
	/**
	 * 
	 * <p>
	 * 发送修改个人信息的验证码
	 * </p>
	 * 
	 * @param phoneNumber
	 * @return
	 * @author 黄智聪 2018年10月22日 下午5:37:30
	 */
	public JsonResult<String> sendEditVerificationCode(String phoneNumber) {
		SmsParams sms = new SmsParams();
		sms.setPhone(phoneNumber);
		sms.setMsgType(SmsConstants.CHANGE_DATA.getMsgType());
		return systemSmsSendService.sendSmsCode(sms);
	}
	
	/**
	 * 
	 * <p>
	 * 校验验证码 
	 * </p>
	 * 
	 * @param param
	 * @return
	 * @author 黄智聪 2018年11月26日 下午7:10:22
	 */
	public JsonResult<String> checkEditVerificationCode(@RequestBody CheckVerificationCodeParam param) {
		JSR303ValidateUtils.validate(param);
		String phoneNumber = param.getPhoneNumber();
		String verificationCode = param.getVerificationCode();
		// 先校验验证码是否已失效
		SmsCodeValidation sv = new SmsCodeValidation();
		sv.setPhone(phoneNumber);
		sv.setSec(UserConstants.CHANGE_DATA_SMS_TIME_OUT);
		JsonResult<SmsInfo> jr = systemSmsSendService.checkMsgCodeTime(sv);
		if (jr.getData() == null || !jr.getData().getCode().equals(SmsCode.SUCCESS.getCode())) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "短信验证码已失效，请重新获取");
		}
		// 再校验验证码是否正确
		SmsCheck sc = new SmsCheck();
		sc.setCode(verificationCode);
		sc.setPhone(phoneNumber);
		jr = systemSmsSendService.checkMsgCode(sc);
		if (jr.getData() == null || !jr.getData().getCode().equals(SmsCode.SUCCESS.getCode())) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "验证码错误");
		}
		return JsonResult.successJsonResult();
	}
	
	
	

	/**
	 * 
	 * <p>
	 * 根据手机号查询当前用户角色并生成菜单树
	 * </p>
	 * 
	 * @param phoneNumber
	 *            手机号
	 * @return
	 * @author 黄智聪 2018年10月19日 上午10:36:34
	 */
	private List<Menu> generateMenuTree(String phoneNumber) {
		// 根据手机号查询所有一级菜单权限
		List<UserPermission> oneMenuPermissions = platformPermissionMapper
				.getUserPermissionByPhoneNumberAndLevel(phoneNumber, 1);
		// 根据手机号查询所有二级菜单权限
		List<UserPermission> twoMenuPermissions = platformPermissionMapper
				.getUserPermissionByPhoneNumberAndLevel(phoneNumber, 2);
		List<Menu> oneLevelMenus = new ArrayList<>();
		for (UserPermission oneMenuPermission : oneMenuPermissions) {
			// 一级菜单的权限编码
			String oneLevelPermissionCode = oneMenuPermission.getPermissionCode();
			List<Menu> twoLevelMenus = new ArrayList<>();
			for (UserPermission twoMenuPermission : twoMenuPermissions) {
				// 二级菜单的父级权限编码
				String twoLevelPermissionCode = twoMenuPermission.getParentPermissionCode();
				if (twoLevelPermissionCode.equals(oneLevelPermissionCode)) {// 一二级菜单进行分类
					Menu twoLevelMenu = new Menu(twoMenuPermission.getPermissionName(), twoMenuPermission.getPageUrl(),
							twoMenuPermission.getIcon(), null);
					twoLevelMenus.add(twoLevelMenu);
				}
			}
			Menu oneLevelMenu = new Menu(oneMenuPermission.getPermissionName(), oneMenuPermission.getPageUrl(),
					oneMenuPermission.getIcon(), twoLevelMenus);
			oneLevelMenus.add(oneLevelMenu);
		}
		return oneLevelMenus;
	}

	public JsonResult<String> editPassword(@RequestBody EditPasswordParam param) {
		JSR303ValidateUtils.validate(param);

		// 密码一致性校验
		if (!param.getFirstPassword().equals(param.getSecondPassword())) {
			throw new BaseException(PlatformUserErrorCode.PLATFORM_USER_ERROR_INCONSISTENT_PASSWORD);
		}

		// 根据用户编码获取用户信息
		PlatformUser platformUser = platformUserMapper.getUserByUserCode(param.getUserCode());
		if (platformUser == null) {// 无效用户
			throw new ShiroAuthException(ShiroAuthErrorCode.SHIRO_AUTH_ERROR_LOGIN_ERROR, "无效用户");
		}

		// 用户设置的新密码信息
		Password pwd = PasswordHelper.encryptPasswordByModel(param.getSecondPassword());
		platformUser.setPassword(pwd.getPassword());
		platformUser.setSalt(pwd.getSalt());
		platformUser.setModifier(param.getUserInfo().getUserCode());
		platformUser.setLastModifyTime(new Date());

		platformUserMapper.updateByPrimaryKeySelective(platformUser);

		return JsonResult.successJsonResult();
	}

	public JsonResult<String> addUser(@RequestBody AddUserParam param) {
		// 参数校验
		JSR303ValidateUtils.validate(param);

		PlatformDept dept = platformDeptMapper.selectByDeptCode(param.getDeptCode());
		if (dept == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "部门不存在");
		}
		PlatformRole role = platformRoleMapper.selectByRoleCode(param.getRoleCode());
		if (role == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "角色不存在");
		}
		String email = param.getEmail();
		if (!StringUtils.isBlank(email)) {
			PlatformUser user = platformUserMapper.getUserByEmail(email, null);
			if (user != null) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "邮箱已存在");
			}
		}
		PlatformUser u = platformUserMapper.getUserByPhoneNumber(param.getPhoneNumber(), null);
		if (u != null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "手机号已存在");
		}

		// 随机6位数字密码
		String randomPwd = RandomStringUtils.randomNumeric(6);

		// 生成盐值加密的密码
		Password pwd = PasswordHelper.encryptPasswordByModel(randomPwd);
		Date nowDate = new Date();
		String creator = param.getCreator();
		String code = dbSequenceService.getPlatEmployeeNumber();
		PlatformUser userRecord = PlatformUser.builder().createTime(nowDate).creator(creator).deptId(dept.getId())
				.email(param.getEmail()).password(pwd.getPassword()).phoneNumber(param.getPhoneNumber())
				.postName(param.getPostName()).userCode(SystemSeqUtils.getSeq(code)).userName(param.getUserName())
				.salt(pwd.getSalt()).build();
		platformUserMapper.insertSelective(userRecord);
		// 用户与角色绑定
		PlatformUserRole userRoleRecord = PlatformUserRole.builder().createTime(nowDate).creator(creator)
				.userId(userRecord.getId()).roleId(role.getId()).build();
		platformUserRoleMapper.insertSelective(userRoleRecord);

		try {
			// 发送短信
			this.sendPasswordMsg(param.getPhoneNumber(), randomPwd);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return JsonResult.successJsonResult();
	}

	public JsonResult<String> editUser(@RequestBody EditUserParam param) {
		// 参数校验
		JSR303ValidateUtils.validate(param);

		PlatformDept dept = platformDeptMapper.selectByDeptCode(param.getDeptCode());
		if (dept == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "部门不存在");
		}
		PlatformRole role = platformRoleMapper.selectByRoleCode(param.getRoleCode());
		if (role == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "角色不存在");
		}
		if (UserConstants.PLATFORM_ADMIN_ROLE_NAME.equals(role.getRoleName())) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "该用户不允许修改");
		}

		String userCode = param.getUserCode();
		PlatformUser user = platformUserMapper.getUserByUserCode(userCode);
		if (user == null) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "用户不存在");
		}
		String email = param.getEmail();
		if (!StringUtils.isBlank(email)) {
			// 带上用户编码是为了排除当前用户以外是否存在邮箱了
			PlatformUser u = platformUserMapper.getUserByEmail(email, userCode);
			if (u != null) {
				throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "邮箱已存在");
			}
		}
		PlatformUser u = platformUserMapper.getUserByPhoneNumber(param.getPhoneNumber(), userCode);
		if (u != null) {
			// 带上用户编码是为了排除当前用户以外是否存在手机了
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "手机号已存在");
		}

		Date nowDate = new Date();
		String modifier = param.getModifier();
		Long userId = user.getId();
		PlatformUser userRecord = PlatformUser.builder().modifier(modifier).lastModifyTime(nowDate).deptId(dept.getId())
				.email(param.getEmail()).phoneNumber(param.getPhoneNumber()).postName(param.getPostName())
				.userName(param.getUserName()).id(userId).build();
		platformUserMapper.updateByPrimaryKeySelective(userRecord);

		// 先删除原先的用户与角色的绑定
		platformUserRoleMapper.deleteByUserId(userId);

		// 重新对用户与角色进行新的绑定
		PlatformUserRole userRoleRecord = PlatformUserRole.builder().createTime(nowDate).creator(modifier)
				.userId(userId).roleId(role.getId()).build();
		platformUserRoleMapper.insertSelective(userRoleRecord);
		return JsonResult.successJsonResult();
	}

	public JsonResult<Pagination<UserInfo>> getUserList(@RequestBody SearchUserParam param) {
		PageHelper.startPage(param.getPageNum(), param.getPageSize());
		List<UserInfo> users = platformUserMapper.getUserInfoBySearchParam(param);
		return JsonResult.successJsonResult(new Pagination<>(users));
	}

	public JsonResult<String> enableOrDisableOrDelUser(@RequestBody EnableOrDisableOrDelUserParam param) {
		// 参数校验
		JSR303ValidateUtils.validate(param);
		int status = param.getStatus();
		this.checkStatusExist(status);
		PlatformUser userRecord = PlatformUser.builder().userCode(param.getUserCode()).status(status)
				.modifier(param.getModifier()).lastModifyTime(new Date()).build();
		platformUserMapper.updateByUserCode(userRecord);
		return JsonResult.successJsonResult();
	}

	public JsonResult<UserInfo> getUserInfo(String userCode) {
		if (StringUtils.isBlank(userCode)) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "用户编码不允许为空");
		}
		UserInfo userInfo = platformUserMapper.getUserInfoByUserCode(userCode);
		if (userInfo == null) {
			throw new BaseException(PlatformUserErrorCode.PLATFORM_USER_ERROR_INVALID_USER);
		}
		return JsonResult.successJsonResult(userInfo);
	}
	
	/**
	 * 
	 * <p>
	 * 校验是否存在该状态
	 * </p>
	 * 
	 * @param value
	 * @return
	 * @author 黄智聪 2018年10月20日 上午11:29:37
	 */
	public void checkStatusExist(int value) {
		if (!UserStatus.checkStatusExist(value)) {
			throw new JSR303ValidationException(JSR303ErrorCode.SYS_ERROR_INVALID_REQUEST_PARAM, "用户状态不存在");
		}
	}

}
