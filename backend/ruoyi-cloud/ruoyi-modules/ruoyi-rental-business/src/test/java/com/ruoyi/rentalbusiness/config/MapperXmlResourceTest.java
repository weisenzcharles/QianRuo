package com.ruoyi.rentalbusiness.config;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;

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

    /** 订单列表排序只能选择 XML 中的固定列，禁止把客户端值拼接进 SQL。 */
    @Test
    public void merchantOrderMapperUsesWhitelistedDateAndSortExpressions() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("query.dateTypeCode == 'return'"),
                "merchant order mapper must support return date filtering");
        require(xml.contains("query.dateTypeCode == 'arrival'"),
                "merchant order mapper must support arrival date filtering");
        require(xml.contains("query.sortTypeCode == 'status'"),
                "merchant order mapper must support status sorting");
        require(xml.contains("query.sortDirectionCode == 'asc'"),
                "merchant order mapper must support ascending sorting");
        require(!xml.contains("order by ${"),
                "merchant order mapper must not interpolate client sort values");
    }

    /** 订单归还验机必须保存并读取独立的验机记录，不能只改变订单状态。 */
    @Test
    public void merchantOrderMapperMustPersistReturnInspection() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("selectLatestReturnInspection"),
                "merchant order mapper must query the latest return inspection");
        require(xml.contains("insertReturnInspection"),
                "merchant order mapper must insert return inspection records");
        require(xml.contains("ry_rental_order_return_inspection"),
                "return inspection must use the dedicated business table");
        require(xml.contains("#{command.result}"),
                "return inspection mapper must bind the submitted inspection result");
        require(xml.contains("selectOrderReturnReadiness") && xml.contains("json_table"),
                "merchant order mapper must check bound serial device return readiness");
        require(xml.contains("returned_unit_count"),
                "return readiness mapper must expose returned device count");
    }

    /** 平台订单详情必须回传最近一次归还验机记录。 */
    @Test
    public void platformOrderMapperMustReadReturnInspection() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/PlatformOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("selectPlatformOrderReturnInspection"),
                "platform order mapper must query return inspection");
        require(xml.contains("ry_business.ry_rental_order_return_inspection"),
                "platform order mapper must read the business inspection table");
        require(xml.contains("date_format(create_time, '%Y-%m-%d %H:%i:%s') as inspectedTime"),
                "platform order mapper must map the inspection creation time");
        require(xml.contains("order by create_time desc, inspection_id desc"),
                "platform order mapper must return the latest inspection");
        require(!xml.contains("inspected_time"),
                "platform order mapper must use the actual inspection timestamp column");
    }

    /** 平台订单物流单号必须按数字方向查询，避免迁移后的订单物流单号被查为空。 */
    @Test
    public void platformOrderMapperMustUseNumericLogisticsDirections() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/PlatformOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("l.direction = 1"), "shipping logistics must use numeric direction 1");
        require(xml.contains("l.direction = 2"), "return logistics must use numeric direction 2");
        require(!xml.contains("l.direction = 'ship'"), "shipping logistics must not use the retired text direction");
        require(!xml.contains("l.direction = 'return'"), "return logistics must not use the retired text direction");
    }

    /** 平台物流详情必须显式映射下划线列，保证订单详情能展示物流单号和路线关联编号。 */
    @Test
    public void platformLogisticsMapperMustUseBrowserPropertyAliases() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/PlatformLogisticsMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("l.tracking_no as trackingNo"),
                "platform logistics mapper must map tracking_no to trackingNo");
        require(xml.contains("l.logistics_id as logisticsId"),
                "platform logistics mapper must map logistics_id to logisticsId");
        require(xml.contains("l.route_status as routeStatus"),
                "platform logistics mapper must map route_status to routeStatus");
        require(xml.contains("as routeQueryTime"),
                "platform logistics mapper must map route_query_time to routeQueryTime");
    }

    /** 收发趋势需分别统计发货仓和归还仓，并严格限制商户用户可访问范围。 */
    @Test
    public void merchantInboundOutboundTrendMapperMustUseScheduleDatesAndWarehouseScope() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantInventoryMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int trendStart = xml.indexOf("<select id=\"selectInboundOutboundTrend\"");
        int trendEnd = xml.indexOf("</select>", trendStart);
        require(trendStart >= 0 && trendEnd > trendStart, "inbound/outbound trend query must exist");
        String trendQuery = xml.substring(trendStart, trendEnd);
        require(trendQuery.contains("o.ship_due_time"), "shipment trend must use scheduled ship date");
        require(trendQuery.contains("o.return_due_time"), "return trend must use scheduled return date");
        require(trendQuery.contains("o.warehouse_id = #{warehouseId}"),
                "shipment trend must filter by outbound warehouse");
        require(trendQuery.contains("o.return_warehouse_id = #{warehouseId}"),
                "return trend must filter by return warehouse");
        require(trendQuery.contains("ry_rental_logistics_order")
                && trendQuery.contains("l.pickup_time")
                && trendQuery.contains("l.estimated_arrive_time"),
                "trend must include logistics schedule data");
        require(trendQuery.contains("l.direction = 1") && trendQuery.contains("l.direction = 2"),
                "logistics trend must separate shipment and return directions");
        require(trendQuery.contains("estimatedShipQuantity") && trendQuery.contains("estimatedReturnQuantity"),
                "trend must expose logistics estimated quantities separately from order quantities");
        require(trendQuery.contains("warehouseIds") && trendQuery.contains("and 1 = 0"),
                "trend must enforce a restricted user's warehouse scope");
        require(trendQuery.contains("test=\"showAll\""),
                "show-all mode must add catalog entries to the trend result");
        require(trendQuery.contains("ry_rental_merchant_package p")
                && trendQuery.contains("ry_rental_product p"),
                "show-all mode must support both package and model catalogs");

        int pendingStart = xml.indexOf("<select id=\"countPendingShipOrders\"");
        int pendingEnd = xml.indexOf("</select>", pendingStart);
        require(pendingStart >= 0 && pendingEnd > pendingStart, "pending shipment summary query must exist");
        String pendingQuery = xml.substring(pendingStart, pendingEnd);
        require(pendingQuery.contains("count(distinct o.order_id)"),
                "pending shipment summary must count orders once");
        require(pendingQuery.contains("o.warehouse_id = #{warehouseId}"),
                "pending shipment summary must respect outbound warehouse filter");
    }

    /** 库存列表必须返回库存模式，数量库存详情才能切换到预占订单视图。 */
    @Test
    public void merchantInventoryListMustReturnStockMode() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantInventoryMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<select id=\"selectInventory\"");
        int end = xml.indexOf("</select>", start);
        require(start >= 0 && end > start, "merchant inventory list mapper must exist");
        String inventoryQuery = xml.substring(start, end);
        require(inventoryQuery.contains("p.stock_mode as stockMode"),
                "merchant inventory list must return stock mode for detail rendering");
    }

    /** 收发趋势数字的订单弹窗必须沿用物流预计和订单计划的准确筛选条件。 */
    @Test
    public void merchantInboundOutboundOrderMapperMustMatchTrendSources() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantInventoryMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<select id=\"selectInboundOutboundOrders\"");
        int end = xml.indexOf("</select>", start);
        require(start >= 0 && end > start, "trend order detail query must exist");
        String detailQuery = xml.substring(start, end);
        require(detailQuery.contains("route.pickup_time")
                && detailQuery.contains("route.estimated_arrive_time"),
                "trend order detail must support logistics estimated dates");
        require(detailQuery.contains("coalesce(o.ship_due_time, o.lease_start_date)")
                && detailQuery.contains("coalesce(o.return_due_time, o.lease_end_date)"),
                "trend order detail must support order schedule dates");
        require(detailQuery.contains("oi.package_id = #{itemId}")
                && detailQuery.contains("coalesce(p.model_id, package_row.model_id) = #{itemId}"),
                "trend order detail must filter the selected package or model");
        require(detailQuery.contains("warehouseIds") && detailQuery.contains("and 1 = 0"),
                "trend order detail must enforce the user's warehouse scope");
    }

    /** 外部库存预占必须持久化来源、令牌、请求商品和数量，释放后才能返回稳定结果。 */
    @Test
    public void merchantInventoryMapperMustPersistExternalReservationContext() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantInventoryMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<insert id=\"insertExternalReservation\">");
        int end = xml.indexOf("</insert>", start);
        require(start >= 0 && end > start, "external reservation insert must exist");
        String statement = xml.substring(start, end);
        require(statement.contains("reservation_source"), "external reservation must persist its source");
        require(statement.contains("external_token_id"), "external reservation must persist its token");
        require(statement.contains("external_product_id"), "external reservation must persist its product");
        require(statement.contains("external_quantity"), "external reservation must persist its quantity");
        require(statement.contains("external_payload_hash"), "external reservation must persist payload hash");
        require(xml.contains("external_product_id as externalProductId")
                && xml.contains("external_quantity as externalQuantity"),
                "reservation queries must expose external context");
        require(xml.contains("selectExpiredExternalReservationsForUpdate")
                && xml.contains("expire_time &lt;= now()"),
                "external reservation expiry task must select only expired active reservations");

        Path governanceMapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantInventoryGovernanceMapper.xml").toURI());
        String governanceXml = Files.readString(governanceMapper);
        require(governanceXml.contains("request_path as requestPath"),
                "sync log idempotency checks must know the original request path");
    }

    /** 执行面工作台必须只汇总未完成履约、售后、物流和库存告警。 */
    @Test
    public void platformExecutionWorkbenchMapperMustCoverOpenExecutionTasks() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/PlatformExecutionWorkbenchMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("countOpenFulfillmentTasks"), "execution workbench must count fulfillment tasks");
        require(xml.contains("order_status in (10, 15, 45, 60, 65)"),
                "fulfillment tasks must use actionable order statuses");
        require(xml.contains("case_status = 0"), "execution workbench must only include processing after-sales cases");
        require(xml.contains("abnormal_flag = 1") && xml.contains("route_status not in (2, 4)"),
                "execution workbench must only include unresolved abnormal logistics");
        require(xml.contains("alert_status = 0"), "execution workbench must only include pending inventory alerts");
        require(xml.contains("ry_rental.ry_rental_member"),
                "execution workbench must return merchant names instead of member identifiers");
    }

    /** 原订单展示子订单物流时，轨迹查询必须使用同一条联盟物流记录。 */
    @Test
    public void merchantLogisticsRouteMapperResolvesAllianceChildLogistics() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantLogisticsRouteMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<select id=\"selectCurrentLogistics\"");
        int end = xml.indexOf("</select>", start);
        require(start >= 0 && end > start, "current logistics query must exist");
        String statement = xml.substring(start, end);
        require(statement.contains("ry_rental_order_alliance"),
                "current logistics query must resolve alliance child orders");
        require(statement.contains("alliance_link.source_order_id = #{orderId}"),
                "alliance logistics query must be scoped to the requested source order");
        require(statement.contains("alliance_link.source_member_id = #{memberId}"),
                "alliance logistics query must be scoped to the current merchant");
        require(statement.contains("union all"),
                "current logistics query must retain a direct-order fallback");
    }

    /** 平台单条物流刷新必须能解析完整的供应商查询投影。 */
    @Test
    public void merchantLogisticsRouteMapperRefreshStatementParses() throws Exception
    {
        String resource = "mapper/merchant/MerchantLogisticsRouteMapper.xml";
        Configuration configuration = new Configuration();
        try (var input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("logisticsId", Long.valueOf(11L));
        configuration.getMappedStatement(
                "com.ruoyi.rentalbusiness.mapper.MerchantLogisticsRouteMapper.selectLogisticsForRefresh")
                .getBoundSql(parameters);
    }

    /**
     * 校验采用标量多参数签名的业务 Mapper 未启用 JDBC 唯一编号回填。
     *
     * RentalBusinessMapper 和 RentalWorkflowMapper 的新增方法均由多个 @Param
     * 标量参数组成，不存在可承接 keyProperty 的实体对象。若配置
     * useGeneratedKeys，MyBatis 会在 SQL 成功后因无法确定唯一编号回填目标而抛出
     * ExecutorException。本测试固定该契约，新增记录需要唯一编号时应使用现有的
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

    /** 校验备货语句只更新订单状态和审计字段，防止误复用整单编辑 SQL。 */
    @Test
    public void merchantOrderPrepareMapperMustBeNarrowUpdate() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<update id=\"updateMerchantOrderStatus\">");
        int end = xml.indexOf("</update>", start);
        require(start >= 0 && end > start, "merchant order prepare mapper must exist");
        String statement = xml.substring(start, end);
        for (String field : new String[] {"channel_id", "customer_name", "customer_phone", "recipient_name",
                "recipient_phone", "recipient_address",
                "warehouse_id", "return_warehouse_id", "lease_start_date", "lease_end_date", "rent_amount",
                "deposit_amount", "package_id", "remark"})
        {
            require(!statement.contains(field + " ="),
                    "prepare mapper must not update order field: " + field);
        }
        require(statement.contains("order_status = #{toStatus}"),
                "prepare mapper must update the target status");
        require(statement.contains("order_status = #{fromStatus}"),
                "prepare mapper must guard the source status");
        require(statement.contains("version = version + 1"),
                "prepare mapper must increment the order version");
    }

    /** 售后单使用统一业务单号回查，不依赖多参数 Mapper 的主键回填。 */
    @Test
    public void merchantAfterSalesMapperMustUseExplicitCaseNoLookup() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantAfterSalesMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<insert id=\"insertCase\">");
        int end = xml.indexOf("</insert>", start);
        require(start >= 0 && end > start, "after sales insert mapper must exist");
        String statement = xml.substring(start, end);
        require(!statement.contains("useGeneratedKeys"),
                "after sales mapper must not use generated-key backfill for scalar parameters");
        require(statement.contains("#{caseNo}"),
                "after sales mapper must persist the generated business case number");
        require(statement.contains("case_status"),
                "after sales mapper must persist the initial processing status");
    }

    /**
     * 校验联盟结算汇总在某一状态没有记录时仍返回数值 0，避免基础数值类型映射 NULL。
     */
    @Test
    public void allianceSettlementSummaryMustReturnZeroForEmptyStatusBuckets() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/PlatformAllianceOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        String[] statusExpressions = {
                "coalesce(sum(case when settlement_status = 0 then 1 else 0 end), 0)",
                "coalesce(sum(case when settlement_status = 1 then 1 else 0 end), 0)",
                "coalesce(sum(case when settlement_status = 2 then 1 else 0 end), 0)",
                "coalesce(sum(case when settlement_status = 3 then 1 else 0 end), 0)"
        };
        for (String expression : statusExpressions)
        {
            require(xml.contains(expression),
                    "alliance settlement summary must coalesce empty status bucket: " + expression);
        }
    }

    /** 平台共享范围查询必须同时覆盖共享仓库和共享商品范围。 */
    @Test
    public void platformAllianceScopeMapperMustExposeWarehouseAndProductScopes() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantAllianceScopeMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("selectPlatformScopes"), "platform scope query must exist");
        require(xml.contains("ry_rental_alliance_warehouse_scope"), "platform scope query must include warehouse scopes");
        require(xml.contains("ry_rental_alliance_product_scope"), "platform scope query must include product scopes");
        require(xml.contains("warehouse_name as scope_name"), "warehouse scope must expose warehouse name");
        require(xml.contains("product_name as scope_name"), "product scope must expose product name");
    }

    /**
     * 校验接受协助订单时能取得任务所属联盟，避免公众池订单被跨联盟领取。
     */
    @Test
    public void allianceTaskQueryMustExposeAllianceId() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderAllianceMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("alliance_id as allianceId"),
                "alliance task query must expose the task alliance id");
    }

    /** 公众池只能展示接收方共享范围、套餐和库存均可承接的订单。 */
    @Test
    public void alliancePublicPoolMustRecheckReceiverScopeAndInventory() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderAllianceMapper.xml").toURI());
        String xml = Files.readString(mapper).toLowerCase(Locale.ROOT);
        require(xml.contains("<sql id=\"publicpoolcandidatescope\">")
                && xml.contains("selecteligibleTargetMemberIds".toLowerCase(Locale.ROOT)),
                "alliance mapper must define receiver eligibility checks");
        require(xml.contains("ry_rental_alliance_product_scope")
                && xml.contains("ry_rental_alliance_warehouse_scope")
                && xml.contains("target_inventory.available_quantity"),
                "public pool eligibility must check product scope, warehouse scope and inventory");
        require(xml.contains("<include refid=\"publicpoolcandidatescope\" />"),
                "public pool queries must reuse the receiver eligibility fragment");
    }

    /**
     * 校验协助订单换仓同时具备子订单和联盟主表的更新语句，避免只改页面显示而未持久化。
     */
    @Test
    public void allianceWarehouseChangeMapperMustUpdateChildAndAlliance() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderAllianceMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("<update id=\"updateChildOrderWarehouse\">"),
                "alliance mapper must update child order warehouse");
        require(xml.contains("<update id=\"updateTargetWarehouse\">"),
                "alliance mapper must update alliance target warehouse");
        require(xml.contains("order_status in (10, 15)"),
                "warehouse change must be limited to pre-fulfillment child orders");
        require(xml.contains("target_warehouse_id = #{warehouseId}"),
                "warehouse change must persist target warehouse");
    }

    /**
     * 校验天气任务使用市级行政区划 adcode，而不是导入数据中的 citycode。
     *
     * 高德天气接口的 city 参数只接受 adcode；两类编码混用时接口可能返回成功状态但没有
     * casts，任务会把所有记录写成失败快照，页面最终只能显示“待查询”。
     */
    @Test
    public void weatherMapperMustUseRegionAdcode() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/RegionMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("city.region_code as weatherAdcode"),
                "weather mapper must use the level-two region adcode");
        require(!xml.contains("district.region_code as weatherAdcode"),
                "weather mapper must not request weather for level-three districts");
        require(xml.contains("where city.region_level = 2"),
                "weather mapper must query level-two administrative regions only");
    }

    /**
     * 校验区县天气查询会沿父级关系解析到市级天气快照，不会按区县编码读取独立天气记录。
     */
    @Test
    public void regionWeatherMapperMustReuseCityWeatherForLowerLevels() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/RegionWeatherMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("requested.parent_code"),
                "region weather mapper must resolve lower-level regions through parent_code");
        require(xml.contains("city.region_level = 2"),
                "region weather mapper must resolve to a level-two region");
        require(xml.contains("ry_business.ry_rental_region_weather"),
                "region weather mapper must read the unified weather table");
    }

    /**
     * 校验天气快照使用原子创建或更新语义：无唯一键记录时插入，已有记录时更新且恢复软删除状态。
     */
    @Test
    public void regionWeatherMapperMustCreateOrUpdateByRegionAndDate() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/RegionWeatherMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("on duplicate key update"),
                "region weather mapper must update an existing region-date snapshot atomically");
        require(xml.contains("update_time = now()"),
                "region weather mapper must refresh update time when an existing snapshot is updated");
        require(xml.contains("is_delete = 0"),
                "region weather mapper must restore a soft-deleted snapshot when it is refreshed");

        Path migration = findRepositoryFile("database/migrations/V20260815_002__region_weather.sql");
        String migrationSql = Files.readString(migration);
        require(migrationSql.contains("uk_region_weather_region_date"),
                "region weather table must enforce one snapshot per region and date");
    }

    /** 校验订单批注历史严格按商户范围过滤，并按最新创建时间优先返回。 */
    @Test
    public void merchantOrderNoteMapperMustReturnNewestFirstWithinMerchantScope() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("<select id=\"selectMerchantOrderNotes\""),
                "merchant order mapper must provide order note history query");
        require(xml.contains("n.tenant_id = #{tenantId}") && xml.contains("n.member_id = #{merchantId}"),
                "order note history must be scoped by tenant and merchant");
        require(xml.contains("n.is_delete = 0"),
                "order note history must exclude soft-deleted records");
        require(xml.contains("order by n.create_time desc, n.note_id desc"),
                "order note history must return newest records first");
        require(xml.contains("n.create_by as operator_id"),
                "order note history must return operator user ids for service-side name resolution");
        require(!xml.contains("ry_rental.ry_rental_merchant_user"),
                "rental-business mapper must not cross database boundary to resolve merchant users");
    }

    /** 工作台只能查询业务库，商户用户名称必须通过商户身份内部服务解析。 */
    @Test
    public void merchantWorkTaskMapperMustNotCrossDatabaseBoundary() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantWorkTaskMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(!xml.contains("ry_rental.ry_rental_merchant_user"),
                "merchant work task mapper must not cross database boundary to resolve merchant users");
        require(xml.contains("t.create_by as creatorUserId"),
                "merchant work task mapper must return creator user id for service-side name resolution");
        require(xml.contains("a.assignee_user_id as userId"),
                "daily work report must return assignee user id for service-side name resolution");
    }

    /** 校验订单列表同时返回录入人和对接人的商户用户唯一编号，名称由应用服务批量解析。 */
    @Test
    public void merchantOrderListMapperMustReturnCreatorAndCoordinatorIds() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("<result property=\"createBy\" column=\"create_by\" />"),
                "merchant order mapper must map the creator user id");
        require(xml.contains("<result property=\"updateBy\" column=\"update_by\" />"),
                "merchant order mapper must map the coordinator user id");
        require(xml.contains("o.create_by as create_by") && xml.contains("o.update_by as update_by"),
                "merchant order list must select both audit user ids");
    }

    /** 校验订单列表同时返回归还仓库名称，避免前端只能拿到仓库唯一编号。 */
    @Test
    public void merchantOrderListMapperMustReturnReturnWarehouseName() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<select id=\"selectMerchantOrderList\"");
        int end = xml.indexOf("</select>", start);
        require(start >= 0 && end > start, "merchant order list mapper must exist");
        String listSql = xml.substring(start, end);
        require(xml.contains("<result property=\"returnWarehouseName\" column=\"return_warehouse_name\" />"),
                "merchant order mapper must map the return warehouse name");
        require(listSql.contains("return_warehouse.warehouse_name as return_warehouse_name"),
                "merchant order list must select the return warehouse name");
        require(listSql.contains("ry_business.ry_rental_warehouse return_warehouse"),
                "merchant order list must join the return warehouse by order return warehouse id");
    }

    /** 校验订单列表只从订单主表读取套餐详情快照。 */
    @Test
    public void merchantOrderListMapperMustExposeCompletePackageItems() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<select id=\"selectMerchantOrderList\"");
        int end = xml.indexOf("</select>", start);
        require(start >= 0 && end > start, "merchant order list mapper must exist");
        String listSql = xml.substring(start, end).toLowerCase(Locale.ROOT);
        require(listSql.contains("o.package_details_json"),
                "merchant order list must read package details from the order table");
        require(!listSql.contains("ry_rental_merchant_package_version")
                && !listSql.contains("ry_rental_merchant_package_item")
                && !listSql.contains("ry_rental_order_package_snapshot"),
                "merchant order list must not query package detail tables");

        int detailStart = xml.indexOf("<select id=\"selectMerchantOrderById\"");
        int detailEnd = xml.indexOf("</select>", detailStart);
        require(detailStart >= 0 && detailEnd > detailStart,
                "merchant order detail mapper must exist");
        String detailSql = xml.substring(detailStart, detailEnd).toLowerCase(Locale.ROOT);
        require(detailSql.contains("o.package_details_json"),
                "merchant order detail must read package details from the order table");
        require(!detailSql.contains("ry_rental_merchant_package_version")
                && !detailSql.contains("ry_rental_merchant_package_item")
                && !detailSql.contains("ry_rental_order_package_snapshot"),
                "merchant order detail must not query package detail tables");

        int insertStart = xml.indexOf("<insert id=\"insertMerchantOrder\"");
        int insertEnd = xml.indexOf("</insert>", insertStart);
        require(insertStart >= 0 && insertEnd > insertStart,
                "merchant order insert mapper must exist");
        String insertSql = xml.substring(insertStart, insertEnd).toLowerCase(Locale.ROOT);
        require(insertSql.contains("source_text_snapshot, share_snapshot_json, package_details_json")
                && insertSql.contains("nullif(trim(#{command.sharesnapshotjson}), ''), null,"),
                "merchant order insert must initialize the package snapshot only on the server");
    }

    /** 校验联盟子订单识别和原订单物流展示都以联盟关系表为准。 */
    @Test
    public void merchantOrderMapperMustSynchronizeAllianceSourceView() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper).toLowerCase(Locale.ROOT);
        require(xml.contains("<select id=\"selectalliancesourceorderlink\""),
                "merchant order mapper must resolve the alliance source order link");
        require(xml.contains("<update id=\"updatealliancesourceorderstatus\""),
                "merchant order mapper must update the source order status");

        int listStart = xml.indexOf("<select id=\"selectmerchantorderlist\"");
        int listEnd = xml.indexOf("</select>", listStart);
        String listSql = xml.substring(listStart, listEnd);
        require(listSql.contains("coalesce(o.source_order_id")
                && listSql.contains("alliance_child.source_order_id"),
                "merchant order list must recognize children from the alliance relation");
        require(listSql.contains("alliance_logistics.tracking_no"),
                "merchant order list must show the canonical child logistics for a source order");

        int detailStart = xml.indexOf("<select id=\"selectmerchantorderbyid\"");
        int detailEnd = xml.indexOf("</select>", detailStart);
        String detailSql = xml.substring(detailStart, detailEnd);
        require(detailSql.contains("coalesce(o.source_order_id")
                && detailSql.contains("alliance_logistics.carrier"),
                "merchant order detail must use the same alliance source and logistics rules");
    }

    /** 校验订单套餐详情直接写回订单主表，避免列表依赖独立快照表。 */
    @Test
    public void merchantOrderMapperMustDeclareOrderPackageDetailsUpdate() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("<update id=\"updateOrderPackageDetails\">")
                && xml.contains("package_details_json = #{packageDetailsJson}"),
                "order package details must be stored on the order table");
    }

    /** 校验联盟列表和明细快照复用订单主表 JSON，不再依赖旧订单套餐快照表。 */
    @Test
    public void allianceMapperMustUseOrderPackageDetails() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderAllianceMapper.xml").toURI());
        String xml = Files.readString(mapper).toLowerCase(Locale.ROOT);
        require(xml.contains("json_extract(o.package_details_json, '$.packagename')"),
                "alliance task list must read package name from order details");
        require(xml.contains("json_extract(o.package_details_json, '$.items')"),
                "alliance item snapshot must read package items from order details");
        require(!xml.contains("ry_rental_order_package_snapshot"),
                "alliance mapper must not read the legacy order package snapshot table");
    }

    /** 校验订单分页总数使用独立轻量 SQL，不让 PageHelper 解析套餐明细投影。 */
    @Test
    public void merchantOrderCountMapperMustAvoidPackageProjection() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<select id=\"selectMerchantOrderCount\"");
        int end = xml.indexOf("</select>", start);
        require(start >= 0 && end > start, "merchant order count mapper must exist");
        String countSql = xml.substring(start, end).replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        require(countSql.contains("count(distinct o.order_id)"),
                "merchant order count must count distinct orders");
        require(countSql.contains("merchantorderfilters"),
                "merchant order count must reuse the list filter fragment");
        require(!countSql.contains("json_table") && !countSql.contains("ry_rental_order_package_snapshot"),
                "merchant order count must not include package detail projection");
    }

    /** 校验订单详情只取首条明细但不写固定 LIMIT，避免被分页拦截器拼成两个 LIMIT。 */
    @Test
    public void merchantOrderDetailMapperMustAvoidFixedLimitAfterOrderBy() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderMapper.xml").toURI());
        String xml = Files.readString(mapper);
        int start = xml.indexOf("<select id=\"selectMerchantOrderById\"");
        int end = xml.indexOf("</select>", start);
        require(start >= 0 && end > start, "merchant order detail mapper must exist");
        String detailSql = xml.substring(start, end).replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        require(detailSql.contains("select min(first_item.order_item_id)"),
                "merchant order detail must select the first order item without a row limit");
        require(!detailSql.contains("order by oi.order_item_id limit 1"),
                "merchant order detail must not append a fixed limit that conflicts with PageHelper");
    }

    /** 校验纠纷列表联表查询限定纠纷主表字段，避免 is_delete 和 create_time 产生歧义。 */
    @Test
    public void merchantAllianceDisputeListMustQualifyJoinedColumns() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantAllianceDisputeMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("and d.is_delete = 0"),
                "alliance dispute list must qualify the dispute soft-delete column");
        require(xml.contains("d.initiator_member_id = #{memberId}")
                && xml.contains("d.respondent_member_id = #{memberId}"),
                "alliance dispute list must qualify joined member columns");
        require(xml.contains("and d.dispute_status = #{disputeStatus}"),
                "alliance dispute list must qualify the status column");
        require(xml.contains("order by d.create_time desc, d.dispute_id desc"),
                "alliance dispute list must qualify ordering columns");
    }

    /** 校验转单弹窗提交的结算与配送字段确实写入联盟订单主表。 */
    @Test
    public void allianceTransferMapperMustPersistSettlementAndShippingFields() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderAllianceMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("settlement_price") && xml.contains("shipping_mode")
                && xml.contains("shipping_payer"),
                "alliance transfer insert must persist settlement and shipping columns");
        require(xml.contains("#{command.settlementPrice}") && xml.contains("#{command.shippingMode}")
                && xml.contains("#{command.shippingPayer}"),
                "alliance transfer insert must bind settlement and shipping values");
    }

    /** 校验接受协助订单时保存接收商户实际选择的履约仓库。 */
    @Test
    public void allianceAcceptMapperMustPersistTargetWarehouse() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderAllianceMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("target_warehouse_id = #{warehouseId}"),
                "alliance acceptance must persist the selected target warehouse");
    }

    /** 校验协助订单列表展示保存后的履约仓库名称，而不是技术编号。 */
    @Test
    public void allianceTaskQueryMustReturnTargetWarehouseName() throws Exception
    {
        Path mapper = Paths.get(getClass().getClassLoader().getResource(
                "mapper/merchant/MerchantOrderAllianceMapper.xml").toURI());
        String xml = Files.readString(mapper);
        require(xml.contains("target_warehouse.warehouse_name as targetWarehouseName"),
                "alliance task query must return the target warehouse name");
        require(xml.contains("target_warehouse.warehouse_id = a.target_warehouse_id"),
                "alliance task query must join the persisted target warehouse");
    }

    /** 从当前 Maven 或 IDE 工作目录向上查找仓库文件，避免测试依赖固定执行目录。 */
    private Path findRepositoryFile(String relativePath)
    {
        Path current = Paths.get("").toAbsolutePath().normalize();
        while (current != null)
        {
            Path candidate = current.resolve(relativePath);
            if (Files.isRegularFile(candidate))
            {
                return candidate;
            }
            current = current.getParent();
        }
        throw new AssertionError("repository file must exist: " + relativePath);
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
     * 判断 Mapper 是否统一采用无法承接生成唯一编号的标量多参数方法签名。
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
