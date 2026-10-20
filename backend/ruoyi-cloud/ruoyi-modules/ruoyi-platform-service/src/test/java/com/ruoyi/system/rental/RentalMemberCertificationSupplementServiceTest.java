package com.ruoyi.system.rental;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.api.model.MerchantCertificationMaterialCommand;
import com.ruoyi.system.api.model.MerchantCertificationSupplementCommand;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.domain.rental.RentalMemberCertification;
import com.ruoyi.system.mapper.rental.RentalMemberCertificationMapper;
import com.ruoyi.system.model.rental.RentalMemberSupplementCommand;
import com.ruoyi.system.service.rental.RentalMemberCertificationService;

/** 验证认证补件只能从待审核进入待补件，并在商户提交后重新生成认证版本。 */
public class RentalMemberCertificationSupplementServiceTest
{
    @Test
    public void requestSupplement_marksPendingVersionAndStoresRequirements() throws Exception
    {
        RentalMemberCertification latest = certification(RentalCode.CertificationStatus.PENDING.getCode());
        AtomicReference<RentalMemberCertification> updated = new AtomicReference<RentalMemberCertification>();
        RentalMemberCertificationMapper mapper = mapper(latest, updated);
        RentalMemberCertificationService service = service(mapper);

        RentalMemberSupplementCommand command = new RentalMemberSupplementCommand();
        command.setMaterialTypes(List.of(RentalCode.CertificationMaterialType.LICENSE.getCode(),
                RentalCode.CertificationMaterialType.LEGAL_ID_BACK.getCode()));
        command.setAuditRemark("请补交清晰的营业执照和身份证国徽面");

        service.requestSupplement(member(), command, "platform-admin", 1L);

        require(updated.get() != null, "certification update must be persisted");
        require(RentalCode.CertificationStatus.SUPPLEMENT_REQUIRED.getCode()
                .equals(updated.get().getCertificationStatus()), "certification must become supplement required");
        require("1,3".equals(updated.get().getSupplementMaterialTypes()),
                "required material types must be stored in stable order");
        require("请补交清晰的营业执照和身份证国徽面".equals(updated.get().getReviewRemark()),
                "supplement remark must be visible to merchant");
    }

    @Test(expected = ServiceException.class)
    public void submitSupplement_withoutAllRequiredMaterials_isRejected() throws Exception
    {
        RentalMemberCertification latest = certification(
                RentalCode.CertificationStatus.SUPPLEMENT_REQUIRED.getCode());
        latest.setSupplementMaterialTypes("1,3");
        RentalMemberCertificationService service = service(mapper(latest,
                new AtomicReference<RentalMemberCertification>()));

        MerchantCertificationMaterialCommand material = new MerchantCertificationMaterialCommand();
        material.setMaterialType(RentalCode.CertificationMaterialType.LICENSE.getCode());
        material.setFileUrl("https://files.example/license.jpg");
        MerchantCertificationSupplementCommand command = new MerchantCertificationSupplementCommand();
        command.setMaterials(List.of(material));

        service.submitSupplement(member(), command);
    }

    private RentalMemberCertificationMapper mapper(RentalMemberCertification latest,
            AtomicReference<RentalMemberCertification> updated)
    {
        return (RentalMemberCertificationMapper) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { RentalMemberCertificationMapper.class }, (proxy, method, args) -> {
                    if ("selectLatestCertification".equals(method.getName()))
                    {
                        return latest;
                    }
                    if ("updateCertificationSupplement".equals(method.getName()))
                    {
                        updated.set((RentalMemberCertification) args[0]);
                        return 1;
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private RentalMemberCertificationService service(RentalMemberCertificationMapper mapper) throws Exception
    {
        RentalMemberCertificationService service = new RentalMemberCertificationService();
        Field field = RentalMemberCertificationService.class.getDeclaredField("certificationMapper");
        field.setAccessible(true);
        field.set(service, mapper);
        return service;
    }

    private RentalMember member()
    {
        RentalMember member = new RentalMember();
        member.setMemberId(10L);
        member.setTenantId(1L);
        member.setMemberType(RentalCode.MemberType.PERSONAL.getCode());
        return member;
    }

    private RentalMemberCertification certification(Integer status)
    {
        RentalMemberCertification certification = new RentalMemberCertification();
        certification.setCertificationId(100L);
        certification.setMemberId(10L);
        certification.setTenantId(1L);
        certification.setVersionNo(1);
        certification.setCertificationStatus(status);
        return certification;
    }

    private Object defaultValue(Class<?> returnType)
    {
        if (!returnType.isPrimitive())
        {
            return null;
        }
        if (returnType == boolean.class)
        {
            return false;
        }
        if (returnType == byte.class)
        {
            return (byte) 0;
        }
        if (returnType == short.class)
        {
            return (short) 0;
        }
        if (returnType == int.class)
        {
            return 0;
        }
        if (returnType == long.class)
        {
            return 0L;
        }
        if (returnType == float.class)
        {
            return 0F;
        }
        if (returnType == double.class)
        {
            return 0D;
        }
        if (returnType == char.class)
        {
            return '\0';
        }
        return null;
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
