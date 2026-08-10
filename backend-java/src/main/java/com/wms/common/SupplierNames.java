package com.wms.common;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 供应商主数据：商品初始化或新增时随机分配一个供应商名称。
 */
public final class SupplierNames {

    public static final List<String> ALL = List.of(
            "供应商A", "供应商B", "供应商C", "供应商D", "供应商E");

    private SupplierNames() {
    }

    /** 随机返回一个供应商名称 */
    public static String random() {
        return ALL.get(ThreadLocalRandom.current().nextInt(ALL.size()));
    }
}
