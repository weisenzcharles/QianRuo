package com.ruoyi.rentalbusiness.config;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** 校验租赁业务服务中的 MyBatis XML 资源能够被 XML 解析器正确加载。 */
public class MapperXmlResourceTest
{
    /**
     * 遍历 mapper 目录下的全部 XML 文件并执行严格解析。
     *
     * 该测试用于提前发现未转义的比较符、未闭合标签和非法字符等问题，避免
     * MyBatis 在生产环境创建 SqlSessionFactory 时才发现映射文件损坏，进而
     * 造成整个 rental-business 服务无法启动。
     */
    @Test
    public void allMapperXmlResourcesAreWellFormed() throws Exception
    {
        List<URL> mapperRoots = findMapperRoots();
        require(!mapperRoots.isEmpty(), "mapper resource directory must exist");

        int parsedFileCount = 0;
        for (URL mapperRoot : mapperRoots)
        {
            Path rootPath = Paths.get(mapperRoot.toURI());
            try (var paths = Files.walk(rootPath))
            {
                for (Path file : (Iterable<Path>) paths.filter(path -> path.toString().endsWith(".xml"))::iterator)
                {
                    parseXml(file);
                    parsedFileCount++;
                }
            }
        }

        require(parsedFileCount > 0, "mapper xml resource must exist");
    }

    /**
     * 校验采用标量多参数签名的业务 Mapper 未启用 JDBC 主键回填。
     *
     * RentalBusinessMapper 和 RentalWorkflowMapper 的新增方法均由多个 @Param
     * 标量参数组成，不存在可承接 keyProperty 的实体对象。若配置
     * useGeneratedKeys，MyBatis 会在 SQL 成功后因无法确定主键回填目标而抛出
     * ExecutorException。本测试固定该契约，新增记录需要主键时应使用现有的
     * 唯一业务键查询，或在同一事务连接中显式读取 last_insert_id()。
     */
    @Test
    public void scalarParameterMappersDoNotUseGeneratedKeyBackfill() throws Exception
    {
        for (URL mapperRoot : findMapperRoots())
        {
            Path rootPath = Paths.get(mapperRoot.toURI());
            try (var paths = Files.walk(rootPath))
            {
                for (Path file : (Iterable<Path>) paths.filter(path -> path.toString().endsWith(".xml"))::iterator)
                {
                    Document document = parseXml(file);
                    Element mapper = document.getDocumentElement();
                    if (!isScalarParameterMapper(mapper.getAttribute("namespace")))
                    {
                        continue;
                    }
                    NodeList inserts = mapper.getElementsByTagName("insert");
                    for (int index = 0; index < inserts.getLength(); index++)
                    {
                        Element insert = (Element) inserts.item(index);
                        require(!"true".equalsIgnoreCase(insert.getAttribute("useGeneratedKeys")),
                                "scalar parameter mapper insert must not backfill generated keys: "
                                        + mapper.getAttribute("namespace") + "." + insert.getAttribute("id"));
                    }
                }
            }
        }
    }

    /**
     * 校验仓库查询、新增和修改 SQL 覆盖仓库表的全部可编辑业务字段。
     *
     * 该契约用于防止前端表单新增字段后只停留在请求对象中，最终没有进入查询
     * 或写入 SQL，导致编辑后数据丢失且页面无法及时发现。
     */
    @Test
    public void warehouseMapperMustPersistCompleteEditableFields() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/RentalBusinessMapper.xml").toURI());
        String xml = Files.readString(mapper);
        String[] fields = {"longitude", "latitude", "pickup_rule_json", "return_enabled",
                "alliance_share_enabled"};
        for (String field : fields)
        {
            require(xml.contains(field), "warehouse mapper must contain field: " + field);
        }
        require(xml.contains("#{longitude}"), "warehouse mapper must bind longitude");
        require(xml.contains("#{latitude}"), "warehouse mapper must bind latitude");
        require(xml.contains("#{pickupRuleJson}"), "warehouse mapper must bind pickup rule");
        require(xml.contains("#{returnEnabled}"), "warehouse mapper must bind return flag");
        require(xml.contains("#{allianceShareEnabled}"), "warehouse mapper must bind alliance flag");
    }

    /**
     * 校验仓库响应对象声明了数据库查询返回的完整编辑字段，避免映射层静默丢值。
     */
    @Test
    public void warehouseViewMustExposeCompleteEditableFields()
    {
        Class<?> viewClass = com.ruoyi.rentalbusiness.model.RentalBusinessVO.class;
        String[] fields = {"province", "city", "district", "address", "contactName", "contactPhone", "longitude", "latitude",
                "pickupRuleJson", "returnEnabled", "allianceShareEnabled"};
        for (String field : fields)
        {
            try
            {
                viewClass.getField(field);
            }
            catch (NoSuchFieldException exception)
            {
                throw new AssertionError("warehouse response must expose field: " + field, exception);
            }
        }
    }

    /** 从测试类路径中收集本模块的 mapper 资源目录。 */
    private List<URL> findMapperRoots() throws Exception
    {
        Enumeration<URL> resources = getClass().getClassLoader().getResources("mapper");
        List<URL> roots = new ArrayList<>();
        while (resources.hasMoreElements())
        {
            URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol()))
            {
                roots.add(resource);
            }
        }
        return roots.isEmpty() ? Collections.emptyList() : roots;
    }

    /**
     * 判断 Mapper 是否统一采用无法承接生成主键的标量多参数方法签名。
     *
     * @param namespace MyBatis Mapper 完整命名空间
     * @return 属于本模块标量参数 Mapper 时返回 true
     */
    private boolean isScalarParameterMapper(String namespace)
    {
        return "com.ruoyi.rentalbusiness.mapper.RentalBusinessMapper".equals(namespace)
                || "com.ruoyi.rentalbusiness.mapper.RentalWorkflowMapper".equals(namespace);
    }

    /**
     * 使用禁止外部实体和外部 DTD 的安全配置解析单个 MyBatis 映射文件。
     *
     * @param file 待解析的 Mapper XML 文件
     * @return 完成安全解析的 XML 文档
     */
    private Document parseXml(Path file) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setExpandEntityReferences(false);
        try (InputStream inputStream = Files.newInputStream(file))
        {
            Document document = factory.newDocumentBuilder().parse(inputStream);
            require(document.getDocumentElement() != null, "mapper xml root must exist: " + file);
            return document;
        }
    }

    /** 执行无第三方断言库依赖的测试断言。 */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
