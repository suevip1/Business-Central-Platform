package atnibam.space.auth.service.impl;

import atnibam.space.auth.factory.SendCodeStrategyFactory;
import atnibam.space.auth.model.dto.AccountVerificationDTO;
import atnibam.space.auth.service.CodeService;
import atnibam.space.auth.strategy.SendCodeStrategy;
import atnibam.space.common.core.domain.R;
import atnibam.space.common.core.enums.CertificateMethodEnum;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static atnibam.space.common.core.constant.Constants.RANDOM_LENGTH;

/**
 * CodeService实现类
 */
@Slf4j
@Service
public class CodeServiceImpl implements CodeService {
    @Autowired
    private SendCodeStrategyFactory sendCodeStrategyFactory;

    /**
     * 发送验证码
     *
     * @param accountVerificationDTO 包含账号、验证码类型等信息的数据传输对象
     * @return 返回验证码发送结果
     */
    @Override
    public R sendCode(AccountVerificationDTO accountVerificationDTO) {
        // 生成验证码
        String code = RandomUtil.randomNumbers(RANDOM_LENGTH);
        // 根据登录方式决定策略
        SendCodeStrategy strategy = sendCodeStrategyFactory.getStrategy(CertificateMethodEnum.fromCode(accountVerificationDTO.getCodeType()));
        // 发送验证码
        strategy.sendCodeHandler(accountVerificationDTO, code);
        // 把验证码加入到缓存中
        strategy.saveVerificationCodeToRedis(accountVerificationDTO.getAccountNumber(), code);
        return R.ok();
    }
}