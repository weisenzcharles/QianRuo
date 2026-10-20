package com.ruoyi.rentalbusiness.controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.rentalbusiness.mapper.RegionMapper;

/** 为商户表单提供本地行政区划级联查询接口。 */
@RestController
@RequestMapping("/merchant/region")
public class MerchantRegionController extends BaseController
{
    private final RegionMapper regionMapper;

    /** 注入行政区划数据访问对象。 */
    public MerchantRegionController(RegionMapper regionMapper)
    {
        this.regionMapper = regionMapper;
    }

    /** 查询指定父级下的省、市或区县；省级使用 0。 */
    @GetMapping("/children")
    public AjaxResult children(@RequestParam(required = false) String parentCode)
    {
        List<Map<String, Object>> rows = regionMapper.selectChildren(
                parentCode == null ? "0" : parentCode.trim());
        return success(rows);
    }
}
