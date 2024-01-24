package atnibam.space.auth.strategy.impl;

import atnibam.space.api.user.RemoteUserCredentialsService;
import atnibam.space.api.user.RemoteUserInfoService;
import atnibam.space.auth.strategy.CertificateStrategy;
import atnibam.space.auth.utils.EmailUtil;
import atnibam.space.common.core.domain.AuthCredentials;
import atnibam.space.common.core.domain.UserInfo;
import atnibam.space.common.core.domain.dto.LoginRequestDTO;
import atnibam.space.common.core.exception.UserOperateException;
import atnibam.space.common.core.utils.StringUtils;
import atnibam.space.common.redis.service.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static atnibam.space.auth.constant.AuthConstants.LOGIN_EMAIL_CODE_KEY;
import static atnibam.space.common.core.enums.ResultCode.USER_VERIFY_ERROR;

/**
 * 邮件证书策略组件
 */
@Component
public class EmailCertificateStrategy implements CertificateStrategy {
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private RemoteUserCredentialsService remoteUserCredentialsService;
    @Autowired
    private RemoteUserInfoService remoteUserInfoService;
    @Resource
    private EmailUtil emailUtil;

    /**
     * 从Redis中获取邮箱的验证码
     *
     * @param email 邮箱
     * @return 验证码
     * @throws UserOperateException 用户操作异常
     */
    @Override
    public String getCodeFromRedis(String email) {
        // 检查邮箱是否合法
        emailUtil.isValidEmailFormat(email);
        String emailCode = redisCache.getCacheObject(LOGIN_EMAIL_CODE_KEY + email);
        if (!StringUtils.hasText(emailCode)) {
            //todo log
            throw new UserOperateException(USER_VERIFY_ERROR);
        }
        return emailCode;
    }

    /**
     * 根据邮箱获取认证凭证
     *
     * @param email 邮箱
     * @return 认证凭证
     */
    @Override
    public AuthCredentials getAuthCredentialsByCertificate(String email) {
        // 检查邮箱是否合法
        emailUtil.isValidEmailFormat(email);
        return remoteUserCredentialsService.getAuthCredentialsByEmail(email).getData();
    }

    /**
     * 根据证书获取用户信息
     *
     * @param loginRequestDTO 登录请求数据
     * @return 用户信息
     */
    @Override
    public UserInfo getUserInfoByCertificate(LoginRequestDTO loginRequestDTO) {
        // 检查邮箱是否合法
        emailUtil.isValidEmailFormat(loginRequestDTO.getCertificate());
        return remoteUserInfoService.getUserInfoByEmail(loginRequestDTO.getCertificate(), loginRequestDTO.getAppCode()).getData();
    }

    /**
     * 创建凭证
     *
     * @param email 邮箱地址
     */
    @Override
    public void createCredentialsByCertificate(String email) {
        // 检查邮箱是否合法
        emailUtil.isValidEmailFormat(email);
        remoteUserCredentialsService.createUserCredentialsByEmail(email);
    }
}
