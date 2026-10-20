package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalCertificationMaterial;
import com.ruoyi.system.domain.rental.RentalMemberCertification;
import com.ruoyi.system.domain.rental.RentalMemberMembershipHistory;
import com.ruoyi.system.domain.rental.RentalMemberQualification;
import com.ruoyi.system.domain.rental.RentalMemberQualificationHistory;

/** 商户认证、会员类型和接单资格数据访问层。 */
public interface RentalMemberCertificationMapper
{
    /**
     * 查询商户最新的未删除认证版本。
     *
     * @param memberId 商户唯一编号
     * @return 最新认证版本，未找到时返回 null
     */
    RentalMemberCertification selectLatestCertification(Long memberId);

    /**
     * 查询商户全部认证版本，按版本倒序返回，供平台追踪认证历史。
     *
     * @param memberId 商户唯一编号
     * @return 认证版本历史
     */
    List<RentalMemberCertification> selectCertificationHistory(Long memberId);

    /**
     * 查询一个认证版本下的全部材料，并按业务排序返回。
     *
     * @param certificationId 认证版本唯一编号
     * @return 认证材料列表
     */
    List<RentalCertificationMaterial> selectMaterials(Long certificationId);

    /**
     * 新增商户认证版本并回写自增唯一编号。
     *
     * @param certification 认证版本快照
     * @return 受影响行数
     */
    int insertCertification(RentalMemberCertification certification);

    /**
     * 新增认证版本材料。
     *
     * @param material 认证材料
     * @return 受影响行数
     */
    int insertMaterial(RentalCertificationMaterial material);

    /**
     * 更新认证版本的审核结果，使用版本和待审核状态作为并发保护条件。
     *
     * @param certification 审核结果
     * @return 受影响行数
     */
    int updateCertificationReview(RentalMemberCertification certification);

    /**
     * 将当前待审核认证版本标记为待补件，并记录平台补件说明和材料类型。
     *
     * @param certification 待补件认证版本
     * @return 受影响行数
     */
    int updateCertificationSupplement(RentalMemberCertification certification);

    /**
     * 查询商户当前全部接单资格快照。
     *
     * @param memberId 商户唯一编号
     * @return 当前资格列表
     */
    List<RentalMemberQualification> selectQualifications(Long memberId);

    /**
     * 查询指定编码的当前接单资格。
     *
     * @param memberId 商户唯一编号
     * @param qualificationCode 资格编码
     * @return 当前资格，未找到时返回 null
     */
    RentalMemberQualification selectQualification(@Param("memberId") Long memberId,
            @Param("qualificationCode") String qualificationCode);

    /**
     * 新增商户接单资格快照。
     *
     * @param qualification 资格快照
     * @return 受影响行数
     */
    int insertQualification(RentalMemberQualification qualification);

    /**
     * 更新商户接单资格快照。
     *
     * @param qualification 资格快照
     * @return 受影响行数
     */
    int updateQualification(RentalMemberQualification qualification);

    /**
     * 新增接单资格变更历史。
     *
     * @param history 资格变更历史
     * @return 受影响行数
     */
    int insertQualificationHistory(RentalMemberQualificationHistory history);

    /**
     * 查询商户接单资格变更历史。
     *
     * @param memberId 商户唯一编号
     * @return 资格变更历史
     */
    List<RentalMemberQualificationHistory> selectQualificationHistory(Long memberId);

    /**
     * 新增会员类型变更历史。
     *
     * @param history 会员类型变更历史
     * @return 受影响行数
     */
    int insertMembershipHistory(RentalMemberMembershipHistory history);

    /**
     * 查询商户会员类型变更历史。
     *
     * @param memberId 商户唯一编号
     * @return 会员类型变更历史
     */
    List<RentalMemberMembershipHistory> selectMembershipHistory(Long memberId);
}
