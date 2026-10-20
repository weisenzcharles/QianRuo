package com.ruoyi.merchant.application;

import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.model.MerchantRegistrationCommand;

/** 商户公开入驻申请校验测试。 */
public class MerchantRegistrationServiceTest
{
    @Test
    public void submit_EnterpriseLicenseAddressMissing_RejectsApplication()
    {
        MerchantRegistrationCommand command = new MerchantRegistrationCommand();
        command.setCode("1234");
        command.setRegistrationMode(RentalCode.RegistrationMode.PAID.getCode());
        command.setMemberType(RentalCode.MemberType.ENTERPRISE.getCode());
        command.setLegalIdFrontUrl("front.jpg");
        command.setLegalIdBackUrl("back.jpg");
        command.setLegalSubjectName("测试企业");
        command.setLicenseNo("91310000TEST");
        command.setLicenseImageUrl("license.jpg");
        command.setLegalRepresentativeName("测试法人");
        command.setLegalRepresentativeIdNo("test-id");

        ServiceException exception = Assert.assertThrows(ServiceException.class,
                () -> new MerchantRegistrationService(null, null).submit(command));

        Assert.assertEquals("营业执照地址不能为空", exception.getMessage());
    }
}
