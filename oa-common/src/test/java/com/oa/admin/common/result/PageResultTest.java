package com.oa.admin.common.result;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    void constructor_setsAllFields() {
        List<String> items = List.of("a", "b", "c");
        PageResult<String> result = new PageResult<>(items, 100L, 2L, 10L);

        assertEquals(items, result.getList());
        assertEquals(100L, result.getTotal());
        assertEquals(2L, result.getPage());
        assertEquals(10L, result.getPageSize());
    }

    @Test
    void constructor_withEmptyList_works() {
        PageResult<String> result = new PageResult<>(Collections.emptyList(), 0L, 1L, 10L);

        assertTrue(result.getList().isEmpty());
        assertEquals(0L, result.getTotal());
    }

    @Test
    void constructor_withNullList_works() {
        PageResult<String> result = new PageResult<>(null, 0L, 1L, 10L);
        assertNull(result.getList());
    }
}
