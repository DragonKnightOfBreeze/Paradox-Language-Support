package icu.windea.pls.config.configExpression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.test.dsl.ExpectScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * [CwtDataExpression] 的解析逻辑测试。
 *
 * 用例按照数据类型（[CwtDataTypes]）组织，每个数据类型至少覆盖一个用例。
 * 平台测试环境已加载数据表达式支持的扩展点，因此无需额外判断扩展点是否可用。
 *
 * @see CwtDataExpression
 * @see CwtDataExpressionMetadata
 * @since 3.0.4
 */
@RunWith(JUnit4::class)
class CwtDataExpressionTest : BasePlatformTestCase() {
    // region Test Helpers

    /**
     * 解析 [expressionString] 并校验表达式字符串、数据类型和角色，随后执行 [block] 以便校验元数据。
     */
    private fun expectDataExpression(
        expressionString: String,
        type: CwtDataType,
        role: CwtDataExpressionRole = CwtDataExpressionRole.Other,
        block: ExpectScope.(CwtDataExpression) -> Unit = {},
    ) {
        expectScope {
            val expression = CwtDataExpression.resolve(expressionString, role)
            expression.expressionString.expectEquals(expressionString)
            expression.type.expectSame(type)
            expression.role.expectEquals(role)
            block(expression)
        }
    }

    /**
     * 校验表达式不携带任何元数据（即仅由裸字面量规则解析得到）。
     */
    private fun ExpectScope.expectNoMetadata(expression: CwtDataExpression) {
        val metadata = expression.metadata
        metadata.value.expectNull()
        metadata.condition.expectFalse()
        metadata.ignoreCase.expectFalse()
        metadata.intRange.expectNull()
        metadata.floatRange.expectNull()
        metadata.suffixes.expectNull()
    }

    /**
     * 校验整数区间元数据，并同时确认浮点数区间元数据为空。
     */
    private fun ExpectScope.expectIntRange(
        expression: CwtDataExpression,
        start: Int?,
        end: Int?,
        openStart: Boolean = false,
        openEnd: Boolean = false,
    ) {
        expression.metadata.floatRange.expectNull()
        val range = expression.metadata.intRange.expectNotNull()
        range.start.expectEquals(start)
        range.end.expectEquals(end)
        range.openStart.expectEquals(openStart)
        range.openEnd.expectEquals(openEnd)
    }

    /**
     * 校验浮点数区间元数据，并同时确认整数区间元数据为空。
     */
    private fun ExpectScope.expectFloatRange(
        expression: CwtDataExpression,
        start: Float?,
        end: Float?,
        openStart: Boolean = false,
        openEnd: Boolean = false,
    ) {
        expression.metadata.intRange.expectNull()
        val range = expression.metadata.floatRange.expectNotNull()
        range.start.expectEquals(start)
        range.end.expectEquals(end)
        range.openStart.expectEquals(openStart)
        range.openEnd.expectEquals(openEnd)
    }

    private fun ExpectScope.expectSuffixes(expression: CwtDataExpression, vararg suffixes: String) {
        expression.metadata.suffixes.expectEquals(suffixes.toSet())
    }

    // endregion

    // region Basic Data Types

    @Test
    fun testAny() {
        expectDataExpression("\$any", CwtDataTypes.Any) { expectNoMetadata(it) }
    }

    @Test
    fun testLiteral() {
        expectDataExpression("\$literal", CwtDataTypes.Literal) { expectNoMetadata(it) }
    }

    @Test
    fun testScalar() {
        expectDataExpression("scalar", CwtDataTypes.Scalar) { expectNoMetadata(it) }
    }

    @Test
    fun testBool() {
        expectDataExpression("bool", CwtDataTypes.Bool) { expectNoMetadata(it) }
    }

    @Test
    fun testInt() {
        // without range
        expectDataExpression("int", CwtDataTypes.Int) { expectNoMetadata(it) }
        // closed range
        expectDataExpression("int[1..10]", CwtDataTypes.Int) { expectIntRange(it, 1, 10) }
        // open start, closed end
        expectDataExpression("int(0..1]", CwtDataTypes.Int) { expectIntRange(it, 0, 1, openStart = true) }
        // closed start, open end
        expectDataExpression("int[-5..5)", CwtDataTypes.Int) { expectIntRange(it, -5, 5, openEnd = true) }
        // unbounded end (`inf`)
        expectDataExpression("int[0..inf]", CwtDataTypes.Int) { expectIntRange(it, 0, null) }
        // unbounded on both ends
        expectDataExpression("int[..]", CwtDataTypes.Int) { expectIntRange(it, null, null) }
        // a malformed range does not match the ranged rule
        // the leading `int` is then treated as a dynamic segment and the expression is split into a template
        expectDataExpression("int[1..10", CwtDataTypes.Template) { it.metadata.value.expectNull() }
    }

    @Test
    fun testFloat() {
        // without range
        expectDataExpression("float", CwtDataTypes.Float) { expectNoMetadata(it) }
        // closed range
        expectDataExpression("float[0.0..1.0]", CwtDataTypes.Float) { expectFloatRange(it, 0.0f, 1.0f) }
        // open start, closed end
        expectDataExpression("float(1.5..2.0]", CwtDataTypes.Float) { expectFloatRange(it, 1.5f, 2.0f, openStart = true) }
        // negative bounds
        expectDataExpression("float[-100.0..100.0)", CwtDataTypes.Float) { expectFloatRange(it, -100.0f, 100.0f, openEnd = true) }
        // integer bounds are also accepted for a float range
        expectDataExpression("float[1..2]", CwtDataTypes.Float) { expectFloatRange(it, 1.0f, 2.0f) }
    }

    @Test
    fun testColorField() {
        expectDataExpression("colour_field", CwtDataTypes.ColorField) { expectNoMetadata(it) }
        expectDataExpression("color_field", CwtDataTypes.ColorField) { expectNoMetadata(it) }
        expectDataExpression("colour[255,0,0]", CwtDataTypes.ColorField) { it.metadata.value.expectEquals("255,0,0") }
        expectDataExpression("color[0,255,0]", CwtDataTypes.ColorField) { it.metadata.value.expectEquals("0,255,0") }
        expectDataExpression("colour[rgb]", CwtDataTypes.ColorField) { it.metadata.value.expectEquals("rgb") }
        // empty argument
        expectDataExpression("colour[]", CwtDataTypes.ColorField) { it.metadata.value.expectNull() }
    }

    @Test
    fun testBlock() {
        // the block type is not resolved via `resolve`, but via `resolveBlock`
        expectScope {
            val expression = CwtDataExpression.resolveBlock()
            expression.expressionString.expectEquals("{...}")
            expression.type.expectSame(CwtDataTypes.Block)
            expression.role.expectEquals(CwtDataExpressionRole.Value)
            expression.metadata.value.expectNull()
            expression.expectSame(CwtDataExpression.resolveBlock())
        }
    }

    // endregion

    // region Extra Basic Data Types

    @Test
    fun testPercentageField() {
        expectDataExpression("percentage_field", CwtDataTypes.PercentageField) { expectNoMetadata(it) }
    }

    @Test
    fun testIntPercentageField() {
        expectDataExpression("int_percentage_field", CwtDataTypes.IntPercentageField) { expectNoMetadata(it) }
    }

    @Test
    fun testDateField() {
        expectDataExpression("date_field", CwtDataTypes.DateField) { expectNoMetadata(it) }
        expectDataExpression("date_field[2020.1.1]", CwtDataTypes.DateField) { it.metadata.value.expectEquals("2020.1.1") }
        expectDataExpression("date_field[y.M.d]", CwtDataTypes.DateField) { it.metadata.value.expectEquals("y.M.d") }
        // empty format
        expectDataExpression("date_field[]", CwtDataTypes.DateField) { it.metadata.value.expectNull() }
    }

    // endregion

    // region Core Data Types

    @Test
    fun testDefinition() {
        expectDataExpression("<my_def>", CwtDataTypes.Definition) { it.metadata.value.expectEquals("my_def") }
        // dotted subtype reference
        expectDataExpression("<event.country>", CwtDataTypes.Definition) { it.metadata.value.expectEquals("event.country") }
        // empty type
        expectDataExpression("<>", CwtDataTypes.Definition) { it.metadata.value.expectNull() }
    }

    @Test
    fun testLocalisation() {
        expectDataExpression("localisation", CwtDataTypes.Localisation) { expectNoMetadata(it) }
    }

    @Test
    fun testSyncedLocalisation() {
        expectDataExpression("localisation_synced", CwtDataTypes.SyncedLocalisation) { expectNoMetadata(it) }
    }

    @Test
    fun testInlineLocalisation() {
        expectDataExpression("localisation_inline", CwtDataTypes.InlineLocalisation) { expectNoMetadata(it) }
    }

    @Test
    fun testModifier() {
        expectDataExpression("<modifier>", CwtDataTypes.Modifier) { expectNoMetadata(it) }
    }

    @Test
    fun testEnumValue() {
        expectDataExpression("enum[blue]", CwtDataTypes.EnumValue) { it.metadata.value.expectEquals("blue") }
        // empty name
        expectDataExpression("enum[]", CwtDataTypes.EnumValue) { it.metadata.value.expectNull() }
    }

    @Test
    fun testValue() {
        expectDataExpression("value[foo]", CwtDataTypes.Value) { it.metadata.value.expectEquals("foo") }
        expectDataExpression("value[]", CwtDataTypes.Value) { it.metadata.value.expectNull() }
    }

    @Test
    fun testValueSet() {
        expectDataExpression("value_set[foo]", CwtDataTypes.ValueSet) { it.metadata.value.expectEquals("foo") }
        expectDataExpression("value_set[]", CwtDataTypes.ValueSet) { it.metadata.value.expectNull() }
    }

    @Test
    fun testDynamicValue() {
        expectDataExpression("dynamic_value[foo]", CwtDataTypes.DynamicValue) { it.metadata.value.expectEquals("foo") }
        expectDataExpression("dynamic_value[]", CwtDataTypes.DynamicValue) { it.metadata.value.expectNull() }
    }

    @Test
    fun testScopeField() {
        expectDataExpression("scope_field", CwtDataTypes.ScopeField) { expectNoMetadata(it) }
    }

    @Test
    fun testScope() {
        // `scope[any]` is equivalent to `scope_field`, so the value is dropped
        expectDataExpression("scope[any]", CwtDataTypes.Scope) { it.metadata.value.expectNull() }
        expectDataExpression("scope[planet]", CwtDataTypes.Scope) { it.metadata.value.expectEquals("planet") }
        // empty type
        expectDataExpression("scope[]", CwtDataTypes.Scope) { it.metadata.value.expectNull() }
    }

    @Test
    fun testScopeGroup() {
        expectDataExpression("scope_group[g1]", CwtDataTypes.ScopeGroup) { it.metadata.value.expectEquals("g1") }
        expectDataExpression("scope_group[]", CwtDataTypes.ScopeGroup) { it.metadata.value.expectNull() }
    }

    @Test
    fun testValueField() {
        expectDataExpression("value_field", CwtDataTypes.ValueField) { expectNoMetadata(it) }
        expectDataExpression("value_field[0.0..1.0]", CwtDataTypes.ValueField) { expectFloatRange(it, 0.0f, 1.0f) }
        expectDataExpression("value_field(0.0..1.0]", CwtDataTypes.ValueField) { expectFloatRange(it, 0.0f, 1.0f, openStart = true) }
    }

    @Test
    fun testIntValueField() {
        expectDataExpression("int_value_field", CwtDataTypes.IntValueField) { expectNoMetadata(it) }
        expectDataExpression("int_value_field(0..1)", CwtDataTypes.IntValueField) { expectIntRange(it, 0, 1, openStart = true, openEnd = true) }
        expectDataExpression("int_value_field[1..10]", CwtDataTypes.IntValueField) { expectIntRange(it, 1, 10) }
    }

    @Test
    fun testVariableField() {
        expectDataExpression("variable_field", CwtDataTypes.VariableField) { expectNoMetadata(it) }
        expectDataExpression("variable_field[0.0..1.0]", CwtDataTypes.VariableField) { expectFloatRange(it, 0.0f, 1.0f) }
        // 32-bit variant
        expectDataExpression("variable_field_32", CwtDataTypes.VariableField) { expectNoMetadata(it) }
        expectDataExpression("variable_field_32(0.0..1.0]", CwtDataTypes.VariableField) { expectFloatRange(it, 0.0f, 1.0f, openStart = true) }
    }

    @Test
    fun testIntVariableField() {
        expectDataExpression("int_variable_field", CwtDataTypes.IntVariableField) { expectNoMetadata(it) }
        expectDataExpression("int_variable_field(0..1)", CwtDataTypes.IntVariableField) { expectIntRange(it, 0, 1, openStart = true, openEnd = true) }
        // 32-bit variant
        expectDataExpression("int_variable_field_32", CwtDataTypes.IntVariableField) { expectNoMetadata(it) }
        expectDataExpression("int_variable_field_32(0..1]", CwtDataTypes.IntVariableField) { expectIntRange(it, 0, 1, openStart = true) }
    }

    @Test
    fun testCommand() {
        expectDataExpression("\$command", CwtDataTypes.Command) { expectNoMetadata(it) }
    }

    @Test
    fun testScriptValueReference() {
        expectDataExpression("\$script_value_reference", CwtDataTypes.ScriptValueReference) { expectNoMetadata(it) }
    }

    @Test
    fun testDefineReference() {
        expectDataExpression("\$define_reference", CwtDataTypes.DefineReference) { expectNoMetadata(it) }
    }

    @Test
    fun testArrayDefineReference() {
        expectDataExpression("\$array_define_reference", CwtDataTypes.ArrayDefineReference) { expectNoMetadata(it) }
    }

    @Test
    fun testTags() {
        expectDataExpression("\$tags[some_tag]", CwtDataTypes.Tags) {
            it.metadata.value.expectEquals("some_tag")
            it.metadata.condition.expectFalse()
        }
        // condition variant
        expectDataExpression("\$tags_condition[some_tag]", CwtDataTypes.Tags) {
            it.metadata.value.expectEquals("some_tag")
            it.metadata.condition.expectTrue()
        }
        // empty name
        expectDataExpression("\$tags[]", CwtDataTypes.Tags) {
            it.metadata.value.expectNull()
            it.metadata.condition.expectFalse()
        }
    }

    @Test
    fun testDatabaseObject() {
        expectDataExpression("\$database_object", CwtDataTypes.DatabaseObject) { expectNoMetadata(it) }
    }

    @Test
    fun testNameFormat() {
        expectDataExpression("name_format[format_x]", CwtDataTypes.NameFormat) { it.metadata.value.expectEquals("format_x") }
        expectDataExpression("name_format[]", CwtDataTypes.NameFormat) { it.metadata.value.expectNull() }
    }

    @Test
    fun testTechnologyWithLevel() {
        expectDataExpression("\$technology_with_level", CwtDataTypes.TechnologyWithLevel) { expectNoMetadata(it) }
    }

    @Test
    fun testParameter() {
        expectDataExpression("\$parameter", CwtDataTypes.Parameter) { expectNoMetadata(it) }
    }

    @Test
    fun testParameterValue() {
        expectDataExpression("\$parameter_value", CwtDataTypes.ParameterValue) { expectNoMetadata(it) }
    }

    @Test
    fun testLocalisationParameter() {
        expectDataExpression("\$localisation_parameter", CwtDataTypes.LocalisationParameter) { expectNoMetadata(it) }
    }

    @Test
    fun testTemplate() {
        // the template string can only be obtained via `expressionString`, not `metadata.value`
        expectDataExpression("a_value[foo]_b", CwtDataTypes.Template) { it.metadata.value.expectNull() }
        expectDataExpression("job_<foo>_add", CwtDataTypes.Template) { it.metadata.value.expectNull() }
        expectDataExpression("a_icon[game/ui/icon.dds]_b", CwtDataTypes.Template) { it.metadata.value.expectNull() }
        // a single segment does not form a template: it degrades to the segment type
        expectDataExpression("value[foo]", CwtDataTypes.Value) { it.metadata.value.expectEquals("foo") }
        expectDataExpression("abc", CwtDataTypes.Constant) { expectNoMetadata(it) }
    }

    // endregion

    // region Path Reference Data Types

    @Test
    fun testIcon() {
        // the `game/` prefix is stripped
        expectDataExpression("icon[game/gfx/icons/i.png]", CwtDataTypes.Icon) { it.metadata.value.expectEquals("gfx/icons/i.png") }
        // the path is normalized (backslashes and repeated separators)
        expectDataExpression("icon[gfx\\icons\\\\i.png]", CwtDataTypes.Icon) { it.metadata.value.expectEquals("gfx/icons/i.png") }
        // empty path
        expectDataExpression("icon[]", CwtDataTypes.Icon) { it.metadata.value.expectNull() }
    }

    @Test
    fun testFilePath() {
        expectDataExpression("filepath", CwtDataTypes.FilePath) { expectNoMetadata(it) }
        // the `game/` prefix is stripped
        expectDataExpression("filepath[game/common/test]", CwtDataTypes.FilePath) { it.metadata.value.expectEquals("common/test") }
        // relative to the current script file
        expectDataExpression("filepath[./]", CwtDataTypes.FilePath) { it.metadata.value.expectEquals("./") }
        expectDataExpression("filepath[./foo]", CwtDataTypes.FilePath) { it.metadata.value.expectEquals("./foo") }
    }

    @Test
    fun testFileName() {
        expectDataExpression("filename", CwtDataTypes.FileName) { expectNoMetadata(it) }
        expectDataExpression("filename[foo.txt]", CwtDataTypes.FileName) { it.metadata.value.expectEquals("foo.txt") }
        expectDataExpression("filename[]", CwtDataTypes.FileName) { it.metadata.value.expectNull() }
    }

    @Test
    fun testAbsoluteFilePath() {
        expectDataExpression("abs_filepath", CwtDataTypes.AbsoluteFilePath) { expectNoMetadata(it) }
    }

    // endregion

    // region External Reference Data Types

    @Test
    fun testShaderEffect() {
        expectDataExpression("\$shader_effect", CwtDataTypes.ShaderEffect) { expectNoMetadata(it) }
    }

    @Test
    fun testMeshLocator() {
        expectDataExpression("\$mesh_locator", CwtDataTypes.MeshLocator) { expectNoMetadata(it) }
    }

    // endregion

    // region Expandable Data Types

    @Test
    fun testUnionValue() {
        expectDataExpression("union[loc_or_text]", CwtDataTypes.UnionValue) { it.metadata.value.expectEquals("loc_or_text") }
        expectDataExpression("union[]", CwtDataTypes.UnionValue) { it.metadata.value.expectNull() }
    }

    @Test
    fun testAliasKeysField() {
        expectDataExpression("alias_keys_field[keys]", CwtDataTypes.AliasKeysField) { it.metadata.value.expectEquals("keys") }
        expectDataExpression("alias_keys_field[]", CwtDataTypes.AliasKeysField) { it.metadata.value.expectNull() }
    }

    @Test
    fun testAliasName() {
        expectDataExpression("alias_name[name]", CwtDataTypes.AliasName) { it.metadata.value.expectEquals("name") }
        expectDataExpression("alias_name[]", CwtDataTypes.AliasName) { it.metadata.value.expectNull() }
    }

    @Test
    fun testAliasMatchLeft() {
        expectDataExpression("alias_match_left[left]", CwtDataTypes.AliasMatchLeft) { it.metadata.value.expectEquals("left") }
        expectDataExpression("alias_match_left[]", CwtDataTypes.AliasMatchLeft) { it.metadata.value.expectNull() }
    }

    @Test
    fun testSingleAliasRight() {
        expectDataExpression("single_alias_right[right]", CwtDataTypes.SingleAliasRight) { it.metadata.value.expectEquals("right") }
        expectDataExpression("single_alias_right[]", CwtDataTypes.SingleAliasRight) { it.metadata.value.expectNull() }
    }

    // endregion

    // region Pattern Data Types

    @Test
    fun testConstant() {
        // a plain literal falls back to constant
        expectDataExpression("hello", CwtDataTypes.Constant) { expectNoMetadata(it) }
        // boolean-like and numeric literals are still constants at the resolution level
        expectDataExpression("yes", CwtDataTypes.Constant) { expectNoMetadata(it) }
        expectDataExpression("123", CwtDataTypes.Constant) { expectNoMetadata(it) }
        // a literal containing excluded characters also falls back to constant
        expectDataExpression("foo.bar", CwtDataTypes.Constant) { expectNoMetadata(it) }
        // a literal matching the `type[x]` shape is still treated as a constant
        expectDataExpression("foo[bar]", CwtDataTypes.Constant) { expectNoMetadata(it) }
    }

    @Test
    fun testGlob() {
        expectDataExpression("glob:fo*", CwtDataTypes.Glob) {
            it.metadata.value.expectEquals("fo*")
            it.metadata.ignoreCase.expectFalse()
        }
        expectDataExpression("glob.i:fo*", CwtDataTypes.Glob) {
            it.metadata.value.expectEquals("fo*")
            it.metadata.ignoreCase.expectTrue()
        }
        // empty pattern
        expectDataExpression("glob:", CwtDataTypes.Glob) { it.metadata.value.expectEquals("") }
        expectDataExpression("glob.i:", CwtDataTypes.Glob) {
            it.metadata.value.expectEquals("")
            it.metadata.ignoreCase.expectTrue()
        }
        // an unknown pattern prefix falls back to constant
        expectDataExpression("globx:foo", CwtDataTypes.Constant) { expectNoMetadata(it) }
    }

    @Test
    fun testAnt() {
        expectDataExpression("ant:foo/*", CwtDataTypes.Ant) {
            it.metadata.value.expectEquals("foo/*")
            it.metadata.ignoreCase.expectFalse()
        }
        expectDataExpression("ant.i:foo/*", CwtDataTypes.Ant) {
            it.metadata.value.expectEquals("foo/*")
            it.metadata.ignoreCase.expectTrue()
        }
        expectDataExpression("ant:", CwtDataTypes.Ant) { it.metadata.value.expectEquals("") }
    }

    @Test
    fun testRegex() {
        expectDataExpression("re:foo.*bar", CwtDataTypes.Regex) {
            it.metadata.value.expectEquals("foo.*bar")
            it.metadata.ignoreCase.expectFalse()
        }
        expectDataExpression("re.i:foo.*bar", CwtDataTypes.Regex) {
            it.metadata.value.expectEquals("foo.*bar")
            it.metadata.ignoreCase.expectTrue()
        }
        // `regex:` / `regex.i:` are provided for compatibility
        expectDataExpression("regex:foo.*bar", CwtDataTypes.Regex) {
            it.metadata.value.expectEquals("foo.*bar")
            it.metadata.ignoreCase.expectFalse()
        }
        expectDataExpression("regex.i:foo.*bar", CwtDataTypes.Regex) {
            it.metadata.value.expectEquals("foo.*bar")
            it.metadata.ignoreCase.expectTrue()
        }
        expectDataExpression("re:", CwtDataTypes.Regex) { it.metadata.value.expectEquals("") }
    }

    // endregion

    // region Suffix Aware Data Types

    @Test
    fun testSuffixAwareDefinition() {
        expectDataExpression("<event>|country,planet", CwtDataTypes.SuffixAwareDefinition) {
            it.metadata.value.expectEquals("event")
            expectSuffixes(it, "country", "planet")
        }
        expectDataExpression("<event>|country", CwtDataTypes.SuffixAwareDefinition) {
            it.metadata.value.expectEquals("event")
            expectSuffixes(it, "country")
        }
        // suffixes are trimmed and empty items are ignored
        expectDataExpression("<event>| country , planet ", CwtDataTypes.SuffixAwareDefinition) {
            it.metadata.value.expectEquals("event")
            expectSuffixes(it, "country", "planet")
        }
        // an empty suffix list degrades to a plain definition
        expectDataExpression("<event>|", CwtDataTypes.Definition) { it.metadata.value.expectEquals("event") }
        // an unsupported base falls back to constant
        expectDataExpression("foo|bar", CwtDataTypes.Constant) { expectNoMetadata(it) }
    }

    @Test
    fun testSuffixAwareLocalisation() {
        expectDataExpression("localisation|key,desc", CwtDataTypes.SuffixAwareLocalisation) {
            it.metadata.value.expectNull()
            expectSuffixes(it, "key", "desc")
        }
        expectDataExpression("localisation|key", CwtDataTypes.SuffixAwareLocalisation) {
            it.metadata.value.expectNull()
            expectSuffixes(it, "key")
        }
        // an empty suffix list degrades to a plain localisation
        expectDataExpression("localisation|", CwtDataTypes.Localisation) { it.metadata.value.expectNull() }
    }

    @Test
    fun testSuffixAwareSyncedLocalisation() {
        expectDataExpression("localisation_synced|key", CwtDataTypes.SuffixAwareSyncedLocalisation) {
            it.metadata.value.expectNull()
            expectSuffixes(it, "key")
        }
        // an empty suffix list degrades to a plain synced localisation
        expectDataExpression("localisation_synced|", CwtDataTypes.SyncedLocalisation) { it.metadata.value.expectNull() }
    }

    // endregion

    // region General Behaviors

    @Test
    fun testResolveEmpty() {
        for (role in CwtDataExpressionRole.entries) {
            expectDataExpression("", CwtDataTypes.Constant, role) { expectNoMetadata(it) }
        }
        expectScope {
            val key = CwtDataExpression.resolveEmpty(CwtDataExpressionRole.Key)
            val other = CwtDataExpression.resolveEmpty(CwtDataExpressionRole.Other)
            key.expectSame(CwtDataExpression.resolveEmpty(CwtDataExpressionRole.Key))
            // equality is based on `expressionString` only, but instances differ by role
            key.expectEquals(other)
            key.expectNotSame(other)
        }
    }

    @Test
    fun testResolveRole() {
        // the default role is `Other`
        expectDataExpression("hello", CwtDataTypes.Constant) { it.role.expectEquals(CwtDataExpressionRole.Other) }
        expectDataExpression("hello", CwtDataTypes.Constant, CwtDataExpressionRole.Key) { it.role.expectEquals(CwtDataExpressionRole.Key) }
        expectDataExpression("hello", CwtDataTypes.Constant, CwtDataExpressionRole.Value) { it.role.expectEquals(CwtDataExpressionRole.Value) }
        expectScope {
            val key = CwtDataExpression.resolve("hello", CwtDataExpressionRole.Key)
            val value = CwtDataExpression.resolve("hello", CwtDataExpressionRole.Value)
            // equality is based on `expressionString` only, but instances differ by role
            key.expectEquals(value)
            key.expectNotSame(value)
        }
    }

    @Test
    fun testResolveTemplateSegment() {
        // `resolveTemplate` resolves a segment of a template expression, with the role of `Other`
        expectScope {
            val value = CwtDataExpression.resolveTemplate("value[bar]")
            value.type.expectSame(CwtDataTypes.Value)
            value.role.expectEquals(CwtDataExpressionRole.Other)
            value.metadata.value.expectEquals("bar")

            val constant = CwtDataExpression.resolveTemplate("abc")
            constant.type.expectSame(CwtDataTypes.Constant)
            constant.role.expectEquals(CwtDataExpressionRole.Other)
            constant.expressionString.expectEquals("abc")
            constant.metadata.value.expectNull()
        }
    }

    @Test
    fun testCreate() {
        expectScope {
            val expression = CwtDataExpression.create("hello", CwtDataTypes.Value)
            expression.expressionString.expectEquals("hello")
            expression.type.expectSame(CwtDataTypes.Value)
            expression.role.expectEquals(CwtDataExpressionRole.Other)
            // `create` always produces a new instance (not cached)
            expression.expectNotSame(CwtDataExpression.create("hello", CwtDataTypes.Value))

            // empty expression falls back to `resolveEmpty`
            CwtDataExpression.create("", CwtDataTypes.Constant).expectSame(CwtDataExpression.resolveEmpty())

            // metadata can be provided
            val withMetadata = CwtDataExpression.create("hello", CwtDataTypes.Value) { value = "hello" }
            withMetadata.metadata.value.expectEquals("hello")
        }
    }

    @Test
    fun testResolveCachingAndEquality() {
        expectScope {
            val expression = CwtDataExpression.resolve("hello", CwtDataExpressionRole.Other)
            // cached by (role, expressionString)
            expression.expectSame(CwtDataExpression.resolve("hello", CwtDataExpressionRole.Other))
            // equality is based on `expressionString` only
            val another = CwtDataExpression.resolve("hello", CwtDataExpressionRole.Value)
            expression.expectEquals(another)
            another.expectNotSame(expression)
            expression.hashCode().expectEquals(another.hashCode())
            // different expression strings are not equal
            expression.expectNotEquals(CwtDataExpression.resolve("world", CwtDataExpressionRole.Other))
            // `toString` is the expression string
            expression.toString().expectEquals("hello")
        }
    }

    // endregion
}
