package ${ctx.packageName}.enums;

/**
 * ${ctx.enumDef.name} enum
 * @author ${ctx.config.author!'generator'}
 */
public enum ${ctx.enumDef.name} {
<#list ctx.enumDef.values as v>
    ${v.name}(<#if ctx.enumDef.isIntType()>${v.code}<#else>"${v.code}"</#if><#if ctx.enumDef.hasLabels()>, "${v.label}"</#if>)<#if v_has_next>,<#else>;</#if>
</#list>

<#if ctx.enumDef.isIntType()>
    private final int code;
<#else>
    private final String code;
</#if>
<#if ctx.enumDef.hasLabels()>
    private final String label;
</#if>

    <#if ctx.enumDef.hasLabels()>
    ${ctx.enumDef.name}(<#if ctx.enumDef.isIntType()>int code<#else>String code</#if>, String label) {
        this.code = code;
        this.label = label;
    }
    <#else>
    ${ctx.enumDef.name}(<#if ctx.enumDef.isIntType()>int code<#else>String code</#if>) {
        this.code = code;
    }
    </#if>

<#if ctx.enumDef.isIntType()>
    public int getCode() {
        return code;
    }
<#else>
    public String getCode() {
        return code;
    }
</#if>
<#if ctx.enumDef.hasLabels()>
    public String getLabel() {
        return label;
    }
</#if>

    public static ${ctx.enumDef.name} fromCode(<#if ctx.enumDef.isIntType()>int<#else>String</#if> code) {
        for (${ctx.enumDef.name} value : values()) {
<#if ctx.enumDef.isIntType()>
            if (value.code == code) {
<#else>
            if (value.code.equals(code)) {
</#if>
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown ${ctx.enumDef.name} code: " + code);
    }
}
