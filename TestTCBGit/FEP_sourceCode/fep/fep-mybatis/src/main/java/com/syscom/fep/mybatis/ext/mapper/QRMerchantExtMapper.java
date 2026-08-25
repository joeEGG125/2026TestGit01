package com.syscom.fep.mybatis.ext.mapper;
import com.syscom.fep.mybatis.mapper.QRMerchantMapper;
import com.syscom.fep.mybatis.model.QRMerchant;
import jakarta.annotation.Resource;

import java.util.List;

@Resource
public interface QRMerchantExtMapper extends QRMerchantMapper {
    void deleteQRMERCHANT();
    int batchInsertQRMERCHANT(List<QRMerchant> list);
}
