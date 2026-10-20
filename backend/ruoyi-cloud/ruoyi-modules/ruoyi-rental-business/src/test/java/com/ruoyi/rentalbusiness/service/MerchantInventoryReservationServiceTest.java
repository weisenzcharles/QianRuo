package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;

/** 外部库存预占自动过期释放测试。 */
public class MerchantInventoryReservationServiceTest
{
    /** 到期外部预占必须释放库存并把预占状态改为已过期。 */
    @Test
    public void expireExternalReservations_releasesExpiredReservation()
    {
        AtomicInteger reservationStatus = new AtomicInteger();
        MerchantInventoryMapper mapper = (MerchantInventoryMapper) Proxy.newProxyInstance(
                MerchantInventoryMapper.class.getClassLoader(), new Class<?>[] {MerchantInventoryMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName())
                    {
                        case "selectExpiredExternalReservationsForUpdate":
                            return List.of(reservationRow());
                        case "selectReservationForUpdate":
                            return new HashMap<String, Object>(reservationRow());
                        case "selectReservationUnits":
                            return new ArrayList<Map<String, Object>>(List.of(unitRow()));
                        case "selectReservationQuantities":
                            return Collections.emptyList();
                        case "updateUnitStatus":
                            require(Integer.valueOf(2).equals(args[3]), "only reserved units can be released");
                            require(Integer.valueOf(1).equals(args[4]), "expired units must return to available");
                            return 1;
                        case "updateReservationUnitStatus":
                            return 1;
                        case "updateReservationQuantityStatus":
                            return 0;
                        case "updateReservationStatus":
                            reservationStatus.set((Integer) args[4]);
                            return 1;
                        default:
                            return null;
                    }
                });

        int changed = new MerchantInventoryReservationService(mapper, null, null).expireExternalReservations();

        require(changed == 1, "one expired reservation must be processed");
        require(reservationStatus.get() == 4, "reservation must be marked expired");
    }

    private Map<String, Object> reservationRow()
    {
        return Map.of("tenantId", 10001L, "memberId", 27L, "reservationId", 800L,
                "reservationSource", 2, "reservationStatus", 1);
    }

    private Map<String, Object> unitRow()
    {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("unitId", 801L);
        row.put("inventoryId", 900L);
        row.put("unitStatus", 2);
        return row;
    }

    private static void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
