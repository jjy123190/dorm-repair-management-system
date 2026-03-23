package com.scau.dormrepair.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 编号生成器的最小单元测试。
 */
class BusinessNumberGeneratorTest {

    @Test
    void shouldGenerateRepairRequestNoWithPrefix() {
        String repairRequestNo = BusinessNumberGenerator.nextRepairRequestNo();
        assertTrue(repairRequestNo.startsWith("RR"));
    }

    @Test
    void shouldGenerateWorkOrderNoWithPrefix() {
        String workOrderNo = BusinessNumberGenerator.nextWorkOrderNo();
        assertTrue(workOrderNo.startsWith("WO"));
    }
}
