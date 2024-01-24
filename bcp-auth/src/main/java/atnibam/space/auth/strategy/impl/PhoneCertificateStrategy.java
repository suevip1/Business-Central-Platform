package atnibam.space.auth.strategy.impl;

import atnibam.space.api.user.RemoteUserCredentialsService;
import atnibam.space.api.user.RemoteUserInfoService;
import atnibam.space.auth.strategy.CertificateStrategy;
import atnibam.space.auth.utils.SmsUtil;
import atnibam.space.common.core.domain.AuthCredentials;
import atnibam.space.common.core.domain.UserInfo;
import atnibam.space.common.core.domain.dto.LoginRequestDTO;
import atnibam.space.common.core.enums.ResultCode;
import atnibam.space.common.core.exception.UserOperateException;
import atnibam.space.common.core.utils.StringUtils;
import atnibam.space.common.redis.service.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static atnibam.space.auth.constant.AuthConstants.LOGIN_PHONE_CODE_KEY;

/**
 * 手机验证码认证策略类
 */
@Component
public class PhoneCertificateStrategy implements CertificateStrategy {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private RemoteUserCredentialsService remoteUserCredentialsService;
    @Autowired
    private RemoteUserInfoService remoteUserInfoService;
    @Autowired
    private SmsUtil smsUtil;

    /**
     * 根据手机号获取认证凭证
     *
     * @param phoneNumber 手机号
     * @return 认证凭证
     */
    @Override
    public AuthCredentials getAuthCredentialsByCertificate(String phoneNumber) {
        // 判断手机号格式是否正确
        smsUtil.isValidPhoneNumberFormat(phoneNumber);
        return remoteUserCredentialsService.getAuthCredentialsByPhone(phoneNumber).getData();
    }

    /**
     * 根据手机号和登录请求获取用户信息
     *
     * @param loginRequestDTO 登录请求
     * @return 用户信息
     */
    @Override
    public UserInfo getUserInfoByCertificate(LoginRequestDTO loginRequestDTO) {
        // 判断手机号格式是否正确
        smsUtil.isValidPhoneNumberFormat(loginRequestDTO.getCertificate());
        return remoteUserInfoService.getUserInfoByPhone(loginRequestDTO.getCertificate(), loginRequestDTO.getAppCode()).getData();
    }

    /**
     * 根据手机号创建用户凭证
     *
     * @param phoneNumber 手机号
     */
    @Override
    public void createCredentialsByCertificate(String phoneNumber) {
        // 判断手机号格式是否正确
        smsUtil.isValidPhoneNumberFormat(phoneNumber);
        remoteUserCredentialsService.createUserCredentialsByPhone(phoneNumber);
    }

    /**
     * 从Redis中获取手机验证码
     *
     * @param phoneNumber 手机号
     * @return 手机验证码
     * @throws UserOperateException 用户操作异常
     */
    @Override
    public String getCodeFromRedis(String phoneNumber) {
        String phoneCode = redisCache.getCacheObject(LOGIN_PHONE_CODE_KEY + phoneNumber);
        if (!StringUtils.hasText(phoneCode)) {
            throw new UserOperateException(ResultCode.USER_VERIFY_ERROR);
        }
        return phoneCode;
    }
}
