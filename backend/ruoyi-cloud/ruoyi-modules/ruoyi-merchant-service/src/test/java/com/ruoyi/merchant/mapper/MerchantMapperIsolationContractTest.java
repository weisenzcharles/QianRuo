package com.ruoyi.merchant.mapper;

import java.io.InputStream;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** 验证所有商户数据 SQL 都不会丢失可信租户和商户约束。 */
public class MerchantMapperIsolationContractTest
{
    /** 验证角色查询和写操作始终包含租户及商户条件。 */
    @Test
    public void roleQueriesAndMutationsAreMerchantScoped() throws Exception
    {
        Document mapper = load("mapper/merchant/MerchantRoleMapper.xml");
        assertScoped(mapper,
                "selectList", "selectById", "selectByName", "selectByKey",
                "update", "updateStatus", "softDelete", "countUsers", "selectUserIdsByRole",
                "deleteRoleMenus", "insertRoleMenus", "selectAllocatedUsers", "selectUnallocatedUsers",
                "countUsersInMerchant", "deleteUserRole", "insertUserRole");
    }

    /** 验证菜单授权查询始终包含租户及商户条件。 */
    @Test
    public void menuAssignmentsAreMerchantScoped() throws Exception
    {
        Document mapper = load("mapper/merchant/MerchantMenuMapper.xml");
        assertScoped(mapper, "selectByUser", "selectIdsByRole");
        String previewSql = statement(mapper, "selectDefaultPreviewMenus").replaceAll("\\s+", " ").toLowerCase();
        require(previewSql.contains("tenant_id = 0") && previewSql.contains("member_id = 0"),
                "default preview menus must only read global merchant menu templates");
    }

    /** 验证个人资料与仓库范围操作始终包含租户及商户条件。 */
    @Test
    public void profileAndWarehouseOperationsAreMerchantScoped() throws Exception
    {
        Document mapper = load("mapper/merchant/MerchantIdentityMapper.xml");
        assertScoped(mapper, "selectWarehouseIds", "selectRoleKeys", "selectPermissions", "selectWarehouses",
                "updateProfile", "updateAvatar", "updatePassword");
    }

    /** 验证登录必须关联有效商户绑定，未审核账号使用默认只读权限，且平台开户写操作带有商户归属约束。 */
    @Test
    public void loginBindingAndDefaultReadPermissionsAreScoped() throws Exception
    {
        Document mapper = load("mapper/merchant/MerchantIdentityMapper.xml");
        String bindingSql = statement(mapper, "selectActiveBinding").replaceAll("\\s+", " ").toLowerCase();
        require(bindingSql.contains("m.status = '0'"), "merchant login binding must require active member");
        require(bindingSql.contains("m.is_delete = '0'"), "merchant login binding must exclude deleted member");
        String defaultPermissionSql = statement(mapper, "selectDefaultReadPermissions")
                .replaceAll("\\s+", " ").toLowerCase();
        require(defaultPermissionSql.contains("perms like 'merchant:%:list'"),
                "default merchant permissions must include list permissions");
        require(defaultPermissionSql.contains("perms like 'merchant:%:query'"),
                "default merchant permissions must include query permissions");
        require(!defaultPermissionSql.contains(":edit") && !defaultPermissionSql.contains(":add")
                && !defaultPermissionSql.contains(":remove") && !defaultPermissionSql.contains(":export"),
                "default merchant permissions must not include write permissions");
        assertScoped(mapper, "countOwnerBinding", "insertOwnerBinding", "selectAdminRoleId",
                "insertAdminRoleMenus", "insertAdminUserRole");
    }

    /** 验证商户操作日志读写始终包含租户及商户条件。 */
    @Test
    public void operationLogReadsAndWritesAreMerchantScoped() throws Exception
    {
        Document mapper = load("mapper/merchant/MerchantOperLogMapper.xml");
        assertScoped(mapper, "insert", "selectList");
    }

    /** 验证商户产品类目选择的查询、删除和恢复操作始终包含租户及商户条件。 */
    @Test
    public void productClassSelectionsAreMerchantScoped() throws Exception
    {
        Document mapper = load("mapper/merchant/MerchantProductClassMapper.xml");
        assertScoped(mapper, "selectClassIds", "countSelected", "softDeleteAll", "upsertSelections");
    }

    /** 验证商户常用型号关系的查询、删除和恢复操作始终包含租户及商户条件。 */
    @Test
    public void brandModelSelectionsAreMerchantScoped() throws Exception
    {
        Document mapper = load("mapper/merchant/MerchantBrandModelMapper.xml");
        assertScoped(mapper, "selectSelectedModelIds", "countSelected", "softDeleteAll", "upsertSelections");
    }

    /**
     * 断言指定映射语句同时包含租户和商户参数，防止跨商户读写。
     *
     * @param mapper 已解析的 MyBatis 映射文档
     * @param statementIds 需要检查的语句标识
     */
    private void assertScoped(Document mapper, String... statementIds)
    {
        for (String statementId : statementIds)
        {
            String sql = statement(mapper, statementId).replaceAll("\\s+", " ").toLowerCase();
            require(sql.contains("tenant_id = #{tenantid}") || sql.contains("#{tenantid}")
                    || sql.contains("#{record.tenantid}"),
                    statementId + " must include tenantId");
            require(sql.contains("member_id = #{merchantid}") || sql.contains("#{merchantid}")
                    || sql.contains("#{record.merchantid}"),
                    statementId + " must include merchantId");
        }
    }

    /**
     * 以禁用外部实体的安全配置加载 MyBatis XML 测试资源。
     *
     * @param resource 类路径资源名称
     * @return 解析后的 XML 文档
     */
    private Document load(String resource) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource))
        {
            require(input != null, "Missing mapper resource: " + resource);
            return factory.newDocumentBuilder().parse(input);
        }
    }

    /**
     * 从 MyBatis XML 中提取指定语句的 SQL 文本。
     *
     * @param mapper 已解析的 MyBatis 映射文档
     * @param id 语句标识
     * @return SQL 文本
     */
    private String statement(Document mapper, String id)
    {
        for (String tag : Arrays.asList("select", "insert", "update", "delete"))
        {
            NodeList nodes = mapper.getElementsByTagName(tag);
            for (int index = 0; index < nodes.getLength(); index++)
            {
                Element element = (Element) nodes.item(index);
                if (id.equals(element.getAttribute("id")))
                {
                    return element.getTextContent();
                }
            }
        }
        throw new AssertionError("Missing mapper statement: " + id);
    }

    /**
     * 执行无第三方依赖的测试断言。
     *
     * @param condition 期望成立的条件
     * @param message 断言失败提示
     */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
