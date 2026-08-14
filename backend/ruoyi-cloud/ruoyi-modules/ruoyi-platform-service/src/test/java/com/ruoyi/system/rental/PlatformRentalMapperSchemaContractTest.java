package com.ruoyi.system.rental;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** 验证平台端租赁 Mapper 的跨库业务表始终显式声明数据库名称。 */
public class PlatformRentalMapperSchemaContractTest
{
    private static final Path RENTAL_MAPPER_DIRECTORY = Paths.get("src/main/resources/mapper/rental");

    private static final Pattern TABLE_REFERENCE_PATTERN = Pattern.compile(
            "(?i)\\b(?:from|join|into|update|delete\\s+from)\\s+"
                    + "(`?[a-z_][a-z0-9_-]*`?(?:\\s*\\.\\s*`?[a-z_][a-z0-9_-]*`?)?)");

    /** 扫描平台端全部租赁 Mapper，禁止未限定数据库名称的租赁及业务表引用。 */
    @Test
    public void crossDatabaseTablesMustDeclareSchema() throws Exception
    {
        require(Files.isDirectory(RENTAL_MAPPER_DIRECTORY),
                "missing platform rental mapper directory: " + RENTAL_MAPPER_DIRECTORY);
        try (Stream<Path> mapperFiles = Files.list(RENTAL_MAPPER_DIRECTORY))
        {
            mapperFiles.filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .sorted()
                    .forEach(this::verifyMapper);
        }
    }

    /** 加载单个 Mapper，并逐条检查查询、写入、更新和删除语句中的业务表引用。 */
    private void verifyMapper(Path mapperPath)
    {
        try (InputStream input = Files.newInputStream(mapperPath))
        {
            DocumentBuilderFactory factory = secureDocumentBuilderFactory();
            Document mapper = factory.newDocumentBuilder().parse(input);
            for (String tag : new String[] { "select", "insert", "update", "delete" })
            {
                NodeList statements = mapper.getElementsByTagName(tag);
                for (int index = 0; index < statements.getLength(); index++)
                {
                    verifyStatement(mapperPath, (Element) statements.item(index));
                }
            }
        }
        catch (Exception exception)
        {
            throw new AssertionError("failed to inspect mapper: " + mapperPath, exception);
        }
    }

    /** 检查单条 SQL，跨库表必须采用 schema.table 或带反引号的等价格式。 */
    private void verifyStatement(Path mapperPath, Element statement)
    {
        String sql = statement.getTextContent().replaceAll("\\s+", " ");
        Matcher matcher = TABLE_REFERENCE_PATTERN.matcher(sql);
        while (matcher.find())
        {
            String tableReference = matcher.group(1).replace("`", "").replaceAll("\\s+", "");
            String lowerReference = tableReference.toLowerCase(Locale.ROOT);
            boolean crossDatabaseTable = lowerReference.startsWith("ry_rental_")
                    || lowerReference.startsWith("ry_business_");
            require(!crossDatabaseTable,
                    mapperPath.getFileName() + " statement " + statement.getAttribute("id")
                            + " must qualify cross-database table: " + tableReference);
        }
    }

    /** 创建禁用外部实体和外部 DTD 加载的 XML 解析器，避免测试读取网络资源。 */
    private DocumentBuilderFactory secureDocumentBuilderFactory() throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory;
    }

    /** 执行无额外断言库依赖的条件检查，并在失败时输出明确的架构违规信息。 */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
