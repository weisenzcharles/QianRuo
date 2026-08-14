package com.ruoyi.merchant.application;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.api.RemoteMerchantRegistrationService;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationCommand;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationVO;
import com.ruoyi.merchant.model.MerchantRegistrationAccessCommand;
import com.ruoyi.merchant.model.MerchantRegistrationCommand;
import com.ruoyi.merchant.model.MerchantRegistrationCodeCommand;
import com.ruoyi.merchant.model.MerchantRegistrationVO;

/** 商户端公开入驻应用服务。 */
@Service
public class MerchantRegistrationService
{
    private static final String ENTERPRISE = "enterprise";

    @Autowired
    private RemoteMerchantRegistrationService registrationService;

    /**
     * 校验获取验证码的手机号。短信服务接入前不实际发送短信，接口仅保留完整调用契约，
     * 后续可在此处替换为真实验证码发送与频率限制实现。
     *
     * @param command 获取验证码请求
     * @return 手机号校验通过时返回 {@code true}
     */
    public boolean requestCode(MerchantRegistrationCodeCommand command)
    {
        if (StringUtils.isBlank(command.getPhone()) || !command.getPhone().trim().matches("^1[3-9]\\d{9}$"))
        {
            throw new ServiceException("手机号格式不正确");
        }
        return true;
    }

    /**
     * 提交新的商户入驻申请，并通过内部契约交由平台服务持久化。
     *
     * @param command 公开注册页面提交的商户资料和验证码
     * @return 已进入待审核状态的申请资料
     */
    public MerchantRegistrationVO submit(MerchantRegistrationCommand command)
    {
        validate(command);
        return unwrap(registrationService.submit(toApplicationCommand(command), SecurityConstants.INNER));
    }

    /**
     * 使用手机号和四位验证码查询当前申请状态及已提交资料。
     *
     * @param command 手机号验证码凭证
     * @return 对应手机号的商户申请资料
     */
    public MerchantRegistrationVO lookup(MerchantRegistrationAccessCommand command)
    {
        validateCode(command.getCode());
        return unwrap(registrationService.getByPhone(command.getPhone().trim(), SecurityConstants.INNER));
    }

    /**
     * 将商户修改后的驳回申请重新提交平台审核，非驳回状态由平台服务拒绝。
     *
     * @param command 修改后的完整申请资料和验证码
     * @return 重新进入待审核状态的申请资料
     */
    public MerchantRegistrationVO resubmit(MerchantRegistrationCommand command)
    {
        validate(command);
        return unwrap(registrationService.resubmit(toApplicationCommand(command), SecurityConstants.INNER));
    }

    /**
     * 确认付费注册支付完成。当前为支付渠道接入前的统一确认入口，正式接入后由支付回调调用同一平台状态机。
     *
     * @param paymentNo 注册支付单号
     * @return 注册完成后的申请资料
     */
    public MerchantRegistrationVO confirmPayment(String paymentNo)
    {
        if (StringUtils.isBlank(paymentNo))
        {
            throw new ServiceException("注册支付单号不能为空");
        }
        return unwrap(registrationService.confirmPayment(paymentNo, SecurityConstants.INNER));
    }

    /**
     * 校验公开申请的业务规则，企业商户必须补齐主体、执照和法人证件资料。
     *
     * @param command 待校验的公开申请命令
     */
    private void validate(MerchantRegistrationCommand command)
    {
        validateCode(command.getCode());
        if (!StringUtils.equalsAny(command.getRegistrationMode(), "paid", "invite"))
        {
            throw new ServiceException("请选择正确的注册方式");
        }
        if ("invite".equals(command.getRegistrationMode()) && StringUtils.isBlank(command.getInviteCode()))
        {
            throw new ServiceException("邀请码注册必须填写邀请码");
        }
        if (!StringUtils.equalsAny(command.getMemberType(), "personal", ENTERPRISE))
        {
            throw new ServiceException("商户类型不正确");
        }
        if (ENTERPRISE.equals(command.getMemberType()))
        {
            require(command.getLegalSubjectName(), "企业名称不能为空");
            require(command.getLicenseNo(), "营业执照号不能为空");
            require(command.getLicenseImageUrl(), "请上传营业执照");
            require(command.getLegalRepresentativeName(), "法人姓名不能为空");
            require(command.getLegalRepresentativeIdNo(), "法人身份证号不能为空");
            require(command.getLegalIdFrontUrl(), "请上传法人身份证人像面");
            require(command.getLegalIdBackUrl(), "请上传法人身份证国徽面");
        }
    }

    /**
     * 校验当前临时验证码规则。短信能力接入前允许任意四位数字，接入后应替换为服务端验证码核销。
     *
     * @param code 待校验验证码
     */
    private void validateCode(String code)
    {
        if (StringUtils.isBlank(code) || !code.matches("\\d{4}"))
        {
            throw new ServiceException("请输入四位数字验证码");
        }
    }

    /**
     * 将商户端公开命令转换为平台内部契约，只复制申请资料并规范化手机号。
     *
     * @param source 公开注册命令
     * @return 平台服务内部申请命令
     */
    private MerchantRegistrationApplicationCommand toApplicationCommand(MerchantRegistrationCommand source)
    {
        MerchantRegistrationApplicationCommand target = new MerchantRegistrationApplicationCommand();
        BeanUtils.copyProperties(source, target);
        target.setContactPhone(source.getContactPhone().trim());
        return target;
    }

    /**
     * 统一处理平台服务响应并转换为商户端视图对象，平台调用失败时抛出明确业务异常。
     *
     * @param result 平台服务返回结果
     * @return 商户端公开申请视图
     */
    private MerchantRegistrationVO unwrap(R<MerchantRegistrationApplicationVO> result)
    {
        if (result == null || R.FAIL == result.getCode() || result.getData() == null)
        {
            throw new ServiceException(result == null ? "平台商户入驻服务无响应" : result.getMsg());
        }
        MerchantRegistrationVO target = new MerchantRegistrationVO();
        BeanUtils.copyProperties(result.getData(), target);
        target.setCode(null);
        return target;
    }

    /**
     * 校验企业申请中的必填字符串不是空值或纯空白。
     *
     * @param value 待校验字符串
     * @param message 校验失败时返回的业务提示
     */
    private void require(String value, String message)
    {
        if (StringUtils.isBlank(value))
        {
            throw new ServiceException(message);
        }
    }
}
