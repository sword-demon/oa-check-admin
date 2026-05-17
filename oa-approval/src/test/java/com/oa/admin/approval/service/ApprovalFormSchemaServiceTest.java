package com.oa.admin.approval.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.admin.approval.dto.form.ApprovalFormSchema;
import com.oa.admin.approval.service.impl.ApprovalFormSchemaServiceImpl;
import com.oa.admin.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author wxvirus
 */
class ApprovalFormSchemaServiceTest {
    private ApprovalFormSchemaService service;

    @BeforeEach
    void setUp() {
        service = new ApprovalFormSchemaServiceImpl(new ObjectMapper());
    }

    @Test
    void parse_legacyFields_normalizesToCurrentSchema() {
        String config = """
            {"fields":[{"name":"leave_days","type":"Integer","label":"请假天数","required":true}]}
            """;

        ApprovalFormSchema schema = service.parse(config);

        assertEquals(1, schema.getVersion());
        assertEquals(1, schema.getFields().size());
        assertEquals("leave_days", schema.getFields().get(0).getFieldKey());
        assertEquals("number", schema.getFields().get(0).getType());
    }

    @Test
    void validateForPublish_emptyFields_throws() {
        BusinessException ex = assertThrows(BusinessException.class,
            () -> service.validateForPublish("{\"version\":1,\"fields\":[]}"));

        assertEquals(1002, ex.getCode());
    }

    @Test
    void validateForSave_duplicateFieldKey_throws() {
        String config = """
            {"version":1,"fields":[
              {"fieldKey":"amount","type":"number","label":"金额"},
              {"fieldKey":"amount","type":"number","label":"金额2"}
            ]}
            """;

        assertThrows(BusinessException.class, () -> service.validateForSave(config));
    }

    @Test
    void validateForSave_optionFieldWithoutOptions_throws() {
        String config = """
            {"version":1,"fields":[{"fieldKey":"type","type":"select","label":"类型"}]}
            """;

        assertThrows(BusinessException.class, () -> service.validateForSave(config));
    }

    @Test
    void validateSubmission_requiredFieldMissing_throws() {
        String config = """
            {"version":1,"fields":[{"fieldKey":"reason","type":"textarea","label":"原因","required":true}]}
            """;

        assertThrows(BusinessException.class, () -> service.validateSubmission(config, Map.of()));
    }

    @Test
    void validateSubmission_numericRange_throwsWhenOutOfRange() {
        String config = """
            {"version":1,"fields":[{"fieldKey":"days","type":"number","label":"天数","min":1,"max":5}]}
            """;

        assertThrows(BusinessException.class, () -> service.validateSubmission(config, Map.of("days", 6)));
    }

    @Test
    void validateSubmission_optionValue_allowsKnownValue() {
        String config = """
            {"version":1,"fields":[
              {"fieldKey":"type","type":"select","label":"类型","options":[{"label":"事假","value":"personal"}]}
            ]}
            """;

        assertDoesNotThrow(() -> service.validateSubmission(config, Map.of("type", "personal")));
    }

    @Test
    void validateSubmission_checkboxValue_rejectsUnknownValue() {
        String config = """
            {"version":1,"fields":[
              {"fieldKey":"items","type":"checkbox","label":"事项","options":[{"label":"A","value":"a"}]}
            ]}
            """;

        assertThrows(BusinessException.class,
            () -> service.validateSubmission(config, Map.of("items", List.of("a", "b"))));
    }
}
