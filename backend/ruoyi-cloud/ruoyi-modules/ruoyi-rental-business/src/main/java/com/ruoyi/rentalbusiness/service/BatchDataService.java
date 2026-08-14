package com.ruoyi.rentalbusiness.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.rentalbusiness.model.BatchImportResult;
import com.ruoyi.rentalbusiness.model.BatchImportRow;
import com.ruoyi.rentalbusiness.model.RentalBusinessCommand;
import com.ruoyi.rentalbusiness.model.RentalBusinessComponentCommand;
import com.ruoyi.rentalbusiness.model.RentalBusinessVO;

/**
 * 租赁主数据批量导入导出服务。
 *
 * <p>批量导入只复用现有领域服务，不直接操作数据库，因此仍然执行类目、资源池、仓库、套餐 BOM
 * 和软删除校验；批量导出只读取当前商户范围，文件配额服务负责导入文件审计。</p>
 */
@Service
public class BatchDataService
{
    private static final List<String> RESOURCES = Collections.unmodifiableList(
            java.util.Arrays.asList("warehouse", "pool", "product", "sn", "accessory", "package"));

    private final RentalBusinessService businessService;
    private final FileQuotaService fileQuotaService;
    private final MerchantBusinessAuditRecorder auditRecorder;

    /**
     * 注入主数据领域服务、文件配额服务和商户业务审计记录器。
     *
     * @param businessService 租赁主数据领域服务
     * @param fileQuotaService 文件配额与文件审计服务
     * @param auditRecorder 商户业务审计记录器
     */
    @Autowired
    public BatchDataService(RentalBusinessService businessService, FileQuotaService fileQuotaService,
            MerchantBusinessAuditRecorder auditRecorder)
    {
        this.businessService = businessService;
        this.fileQuotaService = fileQuotaService;
        this.auditRecorder = auditRecorder;
    }

    /**
     * 保留旧构造方法，便于已有测试和离线调用方继续构造批量服务。
     *
     * @param businessService 租赁主数据领域服务
     * @param fileQuotaService 文件配额与文件审计服务
     */
    public BatchDataService(RentalBusinessService businessService, FileQuotaService fileQuotaService)
    {
        this(businessService, fileQuotaService, null);
    }

    /** 导入一个主数据 Excel 文件，并返回逐行成功与失败结果。 */
    public BatchImportResult importData(String resource, MultipartFile file)
    {
        MerchantContextUtils.requireMerchantApproved();
        validateResource(resource);
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("批量导入文件不能为空");
        }
        fileQuotaService.record(file.getOriginalFilename(), null, file.getSize(), "import", resource, null);
        try
        {
            List<BatchImportRow> rows = new ExcelUtil<BatchImportRow>(BatchImportRow.class)
                    .importExcel(file.getInputStream());
            BatchImportResult result = new BatchImportResult();
            result.total = rows == null ? 0 : rows.size();
            if (rows == null)
            {
                audit("IMPORT", resource, file.getOriginalFilename(), "批量导入完成：0 条数据", result);
                return result;
            }
            for (int index = 0; index < rows.size(); index++)
            {
                try
                {
                    saveRow(resource, rows.get(index));
                    result.success++;
                }
                catch (RuntimeException exception)
                {
                    result.failed++;
                    result.errors.add("第" + (index + 2) + "行：" + exception.getMessage());
                }
            }
            audit("IMPORT", resource, file.getOriginalFilename(),
                    "批量导入完成：成功 " + result.success + " 条，失败 " + result.failed + " 条", result);
            return result;
        }
        catch (IOException exception)
        {
            throw new ServiceException("读取批量导入文件失败");
        }
    }

    /** 导出当前商户指定资源的 Excel 文件。 */
    public void exportData(String resource, HttpServletResponse response)
    {
        validateResource(resource);
        List<BatchImportRow> rows = new ArrayList<BatchImportRow>();
        for (RentalBusinessVO item : listResource(resource))
        {
            rows.add(toExportRow(resource, item));
        }
        fileQuotaService.record(resource + "-export.xlsx", null, 0, "export", resource, null);
        new ExcelUtil<BatchImportRow>(BatchImportRow.class).exportExcel(response, rows, resource + "批量导出");
        audit("EXPORT", resource, resource + "-export.xlsx", "批量导出完成：" + rows.size() + " 条数据", rows.size());
    }

    /** 保存一行导入数据，所有写操作转交已有领域服务执行完整校验。 */
    private void saveRow(String resource, BatchImportRow row)
    {
        if (row == null)
        {
            throw new ServiceException("导入行不能为空");
        }
        RentalBusinessCommand command = new RentalBusinessCommand();
        command.remark = row.remark;
        command.status = row.status;
        command.warehouseId = row.warehouseId;
        command.productId = row.productId;
        command.poolId = row.poolId;
        command.classId = row.classId;
        command.modelId = row.modelId;
        command.productType = row.productType;
        command.stockMode = row.stockMode;
        command.productName = row.productName;
        command.brand = row.brand;
        command.modelName = row.modelName;
        command.memorySpec = row.memorySpec;
        command.salePrice = row.salePrice;
        command.snCode = row.snCode;
        command.barcode = row.barcode;
        command.assetStatus = row.assetStatus;
        command.packageName = row.packageName;
        command.enabled = row.enabled;
        if ("warehouse".equals(resource))
        {
            command.name = row.name;
            command.contactName = row.contactName;
            command.contactPhone = row.contactPhone;
            command.city = row.city;
            command.address = row.address;
            businessService.addWarehouse(command);
        }
        else if ("pool".equals(resource))
        {
            command.poolName = StringUtils.defaultIfEmpty(row.poolName, row.name);
            command.resourceType = row.resourceType;
            command.compatibleModelIds = parseIds(row.compatibleModelIds);
            businessService.addPool(command);
        }
        else if ("product".equals(resource))
        {
            businessService.addProduct(command);
        }
        else if ("sn".equals(resource))
        {
            businessService.addSn(command);
        }
        else if ("accessory".equals(resource))
        {
            businessService.addAccessory(command);
        }
        else
        {
            command.components = StringUtils.isBlank(row.components)
                    ? new ArrayList<RentalBusinessComponentCommand>()
                    : JSON.parseArray(row.components, RentalBusinessComponentCommand.class);
            businessService.addPackage(command);
        }
    }

    /** 查询指定资源的商户数据。 */
    private List<RentalBusinessVO> listResource(String resource)
    {
        if ("warehouse".equals(resource)) return businessService.listWarehouses(null, null);
        if ("pool".equals(resource)) return businessService.listPools(null, null);
        if ("product".equals(resource)) return businessService.listProducts(null, null, null, null, null);
        if ("sn".equals(resource)) return businessService.listSns(null, null, null, null);
        if ("accessory".equals(resource)) return businessService.listAccessories(null, null, null, null);
        return businessService.listPackages(null, null);
    }

    /** 将领域对象转换为统一导出行。 */
    private BatchImportRow toExportRow(String resource, RentalBusinessVO item)
    {
        BatchImportRow row = new BatchImportRow();
        row.code = item.code;
        row.name = item.name;
        row.productName = item.productName;
        row.poolName = item.poolName;
        row.resourceType = item.resourceType;
        row.classId = item.classId;
        row.modelId = item.modelId;
        row.poolId = item.poolId;
        row.productType = item.productType;
        row.stockMode = item.stockMode;
        row.brand = item.brand;
        row.modelName = item.modelName;
        row.memorySpec = item.stockStatus;
        row.salePrice = item.salePrice;
        row.productId = item.productId;
        row.warehouseId = item.warehouseId;
        row.snCode = item.snCode;
        row.barcode = item.barcode;
        row.assetStatus = item.assetStatus;
        row.packageName = item.packageName;
        row.enabled = item.enabled;
        row.status = item.status;
        row.remark = item.remark;
        row.components = JSON.toJSONString(item.components);
        return row;
    }

    /** 解析逗号分隔的主键列表。 */
    private List<Long> parseIds(String value)
    {
        List<Long> result = new ArrayList<Long>();
        if (StringUtils.isBlank(value)) return result;
        for (String item : value.split(","))
        {
            try
            {
                Long id = Long.valueOf(item.trim());
                if (id > 0) result.add(id);
            }
            catch (NumberFormatException exception)
            {
                throw new ServiceException("兼容型号ID格式不正确");
            }
        }
        return result;
    }

    /** 校验批量资源类型。 */
    private void validateResource(String resource)
    {
        if (!RESOURCES.contains(resource))
        {
            throw new ServiceException("不支持的批量资源类型");
        }
    }

    /**
     * 记录批量导入导出业务日志。
     *
     * @param operationType 操作类型
     * @param resource 业务资源
     * @param targetName 文件名称
     * @param description 业务描述
     * @param after 变更后摘要
     */
    private void audit(String operationType, String resource, String targetName, String description, Object after)
    {
        if (auditRecorder != null)
        {
            auditRecorder.record("批量管理", operationType, resource.toUpperCase(), null, targetName,
                    description, null, after);
        }
    }
}
