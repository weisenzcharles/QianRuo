package com.ruoyi.system.rental;

import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationCommand;
import com.ruoyi.system.service.rental.PlatformMerchantRegistrationService;

/** 平台商户入驻持久化前的业务校验测试。 */
public class PlatformMerchantRegistrationServiceTest
{
    @Test
    public void submit_EnterpriseLicenseAddressMissing_RejectsApplication()
    {
        MerchantRegistrationApplicationCommand command = new MerchantRegistrationApplicationCommand();
        command.setRegistrationMode(RentalCode.RegistrationMode.PAID.getCode());
        command.setMemberType(RentalCode.MemberType.ENTERPRISE.getCode());
        command.setNickname("测试店铺");
        command.setContactName("测试联系人");
        command.setContactPhone("13800000000");
        command.setLegalIdFrontUrl("front.jpg");
        command.setLegalIdBackUrl("back.jpg");
        command.setLegalSubjectName("测试企业");
        command.setLicenseNo("91310000TEST");
        command.setLicenseImageUrl("license.jpg");
        command.setLegalRepresentativeName("测试法人");
        command.setLegalRepresentativeIdNo("test-id");

        ServiceException exception = Assert.assertThrows(ServiceException.class,
                () -> new PlatformMerchantRegistrationService().submit(command));

        Assert.assertEquals("营业执照地址不能为空", exception.getMessage());
    }
}
