package com.ruoyi.merchant.mapper;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.domain.MerchantAccount;
import com.ruoyi.merchant.domain.MerchantAdminRole;
import com.ruoyi.merchant.domain.MerchantBinding;
import com.ruoyi.merchant.model.MerchantUserQuery;
import com.ruoyi.merchant.model.MerchantUserVO;
import com.ruoyi.merchant.api.model.MerchantUserSummary;

/** 商户身份数据访问。所有归属查询均包含可信商户条件。 */
public interface MerchantIdentityMapper
{
    /** 在当前商户下创建用户并回填数据库生成的唯一编号。 */
    int insertMerchantUser(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("user") MerchantUserVO user, @Param("password") String password, @Param("operator") Long operator);
    /** 创建或恢复当前商户用户绑定，并写入仓库范围类型。 */
    int upsertMerchantUserBinding(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId, @Param("memberRole") String memberRole,
            @Param("warehouseScopeType") Integer warehouseScopeType, @Param("operator") Long operator);
    /** 软删除用户已有的仓库范围关系。 */
    int deleteMerchantUserWarehouseScope(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId);
    /** 批量写入用户指定仓库范围。 */
    int insertMerchantUserWarehouseScope(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId, @Param("warehouseIds") List<Long> warehouseIds,
            @Param("operator") Long operator);
    /** 查询当前商户的用户列表。 */
    List<MerchantUserVO> selectMerchantUsers(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("query") MerchantUserQuery query);

    /** 查询当前商户用户详情。 */
    MerchantUserVO selectMerchantUser(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("userId") Long userId);

    /** 修改当前商户用户基础资料和状态。 */
    int updateMerchantUser(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId, @Param("nickname") String nickname, @Param("phone") String phone,
            @Param("email") String email, @Param("status") Integer status, @Param("operator") Long operator);

    /** 将当前商户用户密码重置为初始密码状态。 */
    int resetMerchantUserPassword(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId, @Param("password") String password, @Param("operator") Long operator);
    /**
     * 按商户用户名查询未删除账号。
     *
     * @param username 商户用户名
     * @return 商户账号，不存在时返回 {@code null}
     */
    MerchantAccount selectByUsername(String username);

    /**
     * 按手机号查询未删除商户账号。
     *
     * @param phone 商户手机号
     * @return 商户账号，不存在时返回 {@code null}
     */
    MerchantAccount selectByPhone(String phone);

    /**
     * 按用户唯一编号查询未删除商户账号。
     *
     * @param userId 商户用户唯一编号
     * @return 商户账号，不存在时返回 {@code null}
     */
    MerchantAccount selectById(@Param("userId") Long userId);

    /**
     * 按邮箱查询未删除商户账号。
     *
     * @param email 商户联系邮箱
     * @return 商户账号，不存在时返回 {@code null}
     */
    MerchantAccount selectByEmail(String email);

    /**
     * 按申请手机号查询平台商户审核状态，用于在商户登录态中确定当前功能模式。
     *
     * @param phone 商户申请手机号
     * @return 商户审核状态，不存在申请时返回 {@code null}
     */
    Integer selectMemberAuthStatusByPhone(@Param("phone") String phone);

    /**
     * 按可信商户主体查询平台审核状态，确保商户所有子账号使用相同的认证结果。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @return 商户审核状态
     */
    Integer selectMemberAuthStatus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId);

    /** 查询指定用户的最小展示摘要，禁止返回认证敏感字段。 */
    List<MerchantUserSummary> selectUserSummaries(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("userIds") Set<Long> userIds);

    /** 查询当前商户仍可被指派的用户摘要。 */
    List<MerchantUserSummary> selectActiveUserSummaries(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("userIds") Set<Long> userIds);

    /**
     * 查询账号唯一有效的商户主体绑定。绑定有效不等于审核通过，审核状态单独控制功能权限。
     *
     * @param userId 商户用户唯一编号
     * @return 有效主体绑定，不存在时返回 {@code null}
     */
    MerchantBinding selectActiveBinding(@Param("userId") Long userId);

    /**
     * 统计账号关联的唯一有效商户主体数量。待审核和驳回状态也必须保留登录绑定。
     *
     * @param userId 商户用户唯一编号
     * @return 有效主体绑定数量
     */
    int countActiveBindings(@Param("userId") Long userId);

    /**
     * 查询当前商户用户可以访问的仓库唯一编号集合。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param userId 商户用户唯一编号
     * @return 仓库唯一编号集合
     */
    List<Long> selectWarehouseIds(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId);

    /**
     * 查询当前商户用户在所属商户内的有效角色标识。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param userId 商户用户唯一编号
     * @return 角色标识集合
     */
    List<String> selectRoleKeys(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId);

    /**
     * 查询当前商户用户的权限标识。普通用户通过角色和直接授权获得权限；
     * 商户管理员角色始终获得平台当前有效的全部商户权限，确保平台新增可显示菜单后无需依赖历史角色菜单快照。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param userId 商户用户唯一编号
     * @return 权限标识集合
     */
    List<String> selectPermissions(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId);

    /**
     * 查询未通过认证商户的默认只读权限。该集合只包含列表和查询权限，严禁包含写入、删除、导出及授权权限。
     *
     * @return 全局商户页面默认只读权限集合
     */
    List<String> selectDefaultReadPermissions();

    /**
     * 更新商户用户最近一次登录地址和时间。
     *
     * @param userId 商户用户唯一编号
     * @param loginIp 登录来源地址
     * @param loginDate 登录时间
     * @return 更新记录数
     */
    int updateLogin(@Param("userId") Long userId, @Param("loginIp") String loginIp,
            @Param("loginDate") Date loginDate);

    /**
     * 在可信租户和商户范围内更新商户用户基础资料。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param userId 商户用户唯一编号
     * @param nickname 用户昵称
     * @param phone 用户手机号
     * @param email 用户邮箱
     * @param updatedBy 操作人员
     * @return 更新记录数
     */
    int updateProfile(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId, @Param("nickname") String nickname, @Param("phone") String phone,
            @Param("email") String email, @Param("updatedBy") Long updatedBy);

    /**
     * 在可信租户和商户范围内更新商户用户头像。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param userId 商户用户唯一编号
     * @param avatar 头像地址
     * @param updatedBy 操作人员
     * @return 更新记录数
     */
    int updateAvatar(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId, @Param("avatar") String avatar,
            @Param("updatedBy") Long updatedBy);

    /**
     * 在可信租户和商户范围内更新商户用户密码，并可限定仅初始化状态允许修改。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param userId 商户用户唯一编号
     * @param password 加密后的新密码
     * @param initialOnly 是否仅允许初始密码状态更新
     * @return 更新记录数
     */
    int updatePassword(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId, @Param("password") String password,
            @Param("initialOnly") boolean initialOnly);
    /**
     * 创建尚未设置密码的商户所有者账号，首次验证码登录后必须初始化密码。
     *
     * @param account 商户账号及可信归属信息
     * @param operator 平台审核操作人员
     * @return 新增记录数
     */
    int insertAccount(@Param("account") MerchantAccount account, @Param("operator") Long operator);

    /**
     * 激活已属于同一商户的所有者账号，并保留已经设置的密码状态。
     *
     * @param account 商户账号及可信归属信息
     * @param operator 平台审核操作人员
     * @return 更新记录数
     */
    int activateOwnerAccount(@Param("account") MerchantAccount account, @Param("operator") Long operator);

    /**
     * 统计指定账号在当前商户下是否已有有效所有者绑定。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param userId 商户用户唯一编号
     * @return 所有者绑定数量
     */
    int countOwnerBinding(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId);

    /**
     * 创建商户所有者主体绑定，并明确使用当前租户和商户条件。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param userId 商户用户唯一编号
     * @param operator 平台审核操作人员
     * @return 新增记录数
     */
    int insertOwnerBinding(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("userId") Long userId, @Param("operator") Long operator);

    /**
     * 为审核通过的商户幂等复制平台启用的全局角色模板。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param operator 执行复制的系统操作人
     * @return 新增的商户运行角色数量
     */
    int insertMerchantRolesFromTemplates(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("operator") Long operator);

    /**
     * 为商户运行角色幂等复制全局角色模板的菜单权限。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @return 新增或恢复的角色菜单数量
     */
    int insertMerchantRoleMenusFromTemplates(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId);

    /**
     * 查询当前商户已经存在的管理员角色。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @return 管理员角色唯一编号，不存在时返回 {@code null}
     */
    Long selectAdminRoleId(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId);

    /**
     * 为审核通过的商户创建独立管理员角色。
     *
     * @param role 包含可信租户和商户归属的管理员角色
     * @return 新增记录数
     */
    int insertAdminRole(MerchantAdminRole role);

    /**
     * 幂等授予商户管理员当前全部启用的商户菜单。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param roleId 商户管理员角色唯一编号
     * @return 新增的角色菜单记录数
     */
    int insertAdminRoleMenus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId);

    /**
     * 幂等绑定商户所有者账号与当前商户的管理员角色。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param roleId 商户管理员角色唯一编号
     * @param userId 商户所有者用户唯一编号
     * @return 新增的用户角色记录数
     */
    int insertAdminUserRole(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("roleId") Long roleId, @Param("userId") Long userId);
}
