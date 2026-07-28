package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalMerchantUser;

/**
 * 商户端用户 数据层
 *
 * @author qianruo
 */
public interface RentalMerchantUserMapper
{
    public List<RentalMerchantUser> selectMerchantUserList(RentalMerchantUser merchantUser);

    public RentalMerchantUser selectById(@Param("merchantUserId") Long merchantUserId);

    public RentalMerchantUser selectByUserName(String userName);

    public RentalMerchantUser selectByPhone(String phone);

    public RentalMerchantUser selectByEmail(String email);

    public int insertMerchantUser(RentalMerchantUser merchantUser);

    public int updateMerchantUser(RentalMerchantUser merchantUser);

    public int deleteMerchantUserByIds(@Param("merchantUserIds") Long[] merchantUserIds);

    public int updateLoginInfo(RentalMerchantUser merchantUser);

    public int resetUserPwd(@Param("merchantUserId") Long merchantUserId, @Param("password") String password);

    public int updateUserPwd(@Param("merchantUserId") Long merchantUserId, @Param("password") String password);

    public int initUserPwd(@Param("merchantUserId") Long merchantUserId, @Param("password") String password);
}
