package com.ruoyi.system.rental;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** 验证平台标准品牌型号 MyBatis 映射始终过滤软删除并保留型号引用保护。 */
public class RentalBrandModelMapperContractTest
{
    /** 验证标准型号列表、内部发布和删除语句的软删除边界。 */
    @Test
    public void brandModelMapperKeepsSoftDeleteBoundary() throws Exception
    {
        Document mapper = load();
        require(sql(mapper, "selectList").contains("m.is_delete = 0"),
                "platform brand model list must exclude deleted rows");
        require(sql(mapper, "selectActiveList").contains("m.status = '0'"),
                "internal brand model list must publish only active rows");
        require(sql(mapper, "countReferences").contains("ry_rental_product"),
                "delete protection must check product references");
        require(sql(mapper, "countReferences").contains("ry_rental_member_common_model"),
                "delete protection must check merchant common model references");
        require(sql(mapper, "softDelete").contains("is_delete = 1"),
                "brand model delete must be a soft delete");
    }

    /** 以禁用外部实体访问的安全配置加载平台品牌型号映射资源。 */
    private Document load() throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("mapper/rental/RentalBrandModelMapper.xml"))
        {
            require(input != null, "missing platform brand model mapper");
            return factory.newDocumentBuilder().parse(input);
        }
    }

    /** 从映射文件提取指定 SQL 语句文本并统一压缩空白。 */
    private String sql(Document mapper, String id)
    {
        for (String tag : new String[] { "select", "insert", "update", "delete" })
        {
            NodeList nodes = mapper.getElementsByTagName(tag);
            for (int index = 0; index < nodes.getLength(); index++)
            {
                Element element = (Element) nodes.item(index);
                if (id.equals(element.getAttribute("id")))
                {
                    return element.getTextContent().replaceAll("\\s+", " ").toLowerCase();
                }
            }
        }
        throw new AssertionError("missing mapper statement: " + id);
    }

    /** 执行无额外测试框架依赖的断言。 */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
