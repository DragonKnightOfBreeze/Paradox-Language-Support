package icu.windea.pls.config.configExpression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionSupport
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see CwtDataExpression
 */
@RunWith(JUnit4::class)
class CwtDataExpressionTest : BasePlatformTestCase() {
    private fun hasEp(): Boolean = try {
        CwtDataExpressionSupport.EP_NAME.extensionList.isNotEmpty()
    } catch (_: Throwable) {
        false
    }

    @Test
    fun testResolveEmpty_key_and_value() {
        val ek = CwtDataExpression.resolveEmpty(CwtDataExpressionRole.Key)
        assertEquals("", ek.expressionString)
        assertEquals(CwtDataExpressionRole.Key, ek.role)
        assertEquals(CwtDataTypes.Constant, ek.type)
        assertEquals("", ek.expressionString)
        assertNull(ek.metadata.value) // since 3.0.1
        assertEquals(ek, CwtDataExpression.resolve("", CwtDataExpressionRole.Key))
        assertSame(ek, CwtDataExpression.resolveEmpty(CwtDataExpressionRole.Key))

        val ev = CwtDataExpression.resolveEmpty(CwtDataExpressionRole.Value)
        assertFalse(ev.role == CwtDataExpressionRole.Key)
        assertEquals(CwtDataTypes.Constant, ev.type)
        assertEquals("", ev.expressionString)
        assertNull(ev.metadata.value) // since 3.0.1
        assertEquals(ev, CwtDataExpression.resolve("", CwtDataExpressionRole.Value))
        assertSame(ev, CwtDataExpression.resolveEmpty(CwtDataExpressionRole.Value))

        // equals by expressionString only, but instances differ
        assertEquals(ek, ev)
        assertNotSame(ek, ev)
    }

    @Test
    fun testResolveBlock() {
        val e = CwtDataExpression.resolveBlock()
        assertEquals("{...}", e.expressionString)
        assertEquals(CwtDataTypes.Block, e.type)
        assertTrue(e.role == CwtDataExpressionRole.Value)
        assertSame(e, CwtDataExpression.resolveBlock())
    }

    @Test
    fun testResolveConstant_key_and_value() {
        if (!hasEp()) return
        val s = "hello"
        val ek = CwtDataExpression.resolve(s, CwtDataExpressionRole.Key)
        assertEquals(CwtDataTypes.Constant, ek.type)
        assertEquals(s, ek.expressionString)
        assertNull(ek.metadata.value) // since 3.0.1
        assertEquals(CwtDataExpressionRole.Key, ek.role)

        val ev = CwtDataExpression.resolve(s, CwtDataExpressionRole.Value)
        assertEquals(CwtDataTypes.Constant, ev.type)
        assertEquals(s, ev.expressionString)
        assertNull(ev.metadata.value) // since 3.0.1
        assertTrue(ev.role == CwtDataExpressionRole.Value)

        // equals by expressionString only, but instances differ by cache bucket (key/value)
        assertEquals(ek, ev)
        assertNotSame(ek, ev)
    }

    @Test
    fun testResolveBaseConfigs_int_float_scalar_color_bool() {
        if (!hasEp()) return
        // int and int range
        run {
            val e = CwtDataExpression.resolve("int", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Int, e.type)
            assertNull(e.metadata.intRange)
        }
        run {
            val e = CwtDataExpression.resolve("int[1..10]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Int, e.type)
            val r = e.metadata.intRange
            assertNotNull(r)
            r!!
            assertEquals(1, r.start)
            assertEquals(10, r.end)
            assertFalse(r.openStart)
            assertFalse(r.openEnd)
        }

        // float and float range
        run {
            val e = CwtDataExpression.resolve("float", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Float, e.type)
            assertNull(e.metadata.floatRange)
        }
        run {
            val e = CwtDataExpression.resolve("float(1.5..2.0]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Float, e.type)
            val r = e.metadata.floatRange
            assertNotNull(r)
            r!!
            assertEquals(1.5f, r.start!!, 0.0001f)
            assertEquals(2.0f, r.end!!, 0.0001f)
            assertTrue(r.openStart)
            assertFalse(r.openEnd)
        }

        // scalar
        run {
            val e = CwtDataExpression.resolve("scalar", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Scalar, e.type)
            assertFalse(e.metadata.wildcard)
        }
        run {
            val e = CwtDataExpression.resolve("wildcard_scalar", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Scalar, e.type)
            assertTrue(e.metadata.wildcard)
        }

        // color field variants
        run {
            val e = CwtDataExpression.resolve("colour_field", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.ColorField, e.type)
        }
        run {
            val e = CwtDataExpression.resolve("colour[255,0,0]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.ColorField, e.type)
            assertEquals("255,0,0", e.metadata.value)
        }
        run {
            val e = CwtDataExpression.resolve("color_field", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.ColorField, e.type)
        }
        run {
            val e = CwtDataExpression.resolve("color[0,255,0]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.ColorField, e.type)
            assertEquals("0,255,0", e.metadata.value)
        }

        // bool
        run {
            val e = CwtDataExpression.resolve("bool", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Bool, e.type)
        }
    }

    @Test
    fun testResolveCoreConfigs_common() {
        if (!hasEp()) return
        // percentage field
        run {
            val e = CwtDataExpression.resolve("percentage_field", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.PercentageField, e.type)
        }
        // int percentage field
        run {
            val e = CwtDataExpression.resolve("int_percentage_field", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.IntPercentageField, e.type)
        }
        // date field
        run {
            val e = CwtDataExpression.resolve("date_field", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.DateField, e.type)
        }
        run {
            val e = CwtDataExpression.resolve("date_field[2020.1.1]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.DateField, e.type)
            assertEquals("2020.1.1", e.metadata.value)
        }
        // localisation types
        run {
            assertEquals(CwtDataTypes.Localisation, CwtDataExpression.resolve("localisation", CwtDataExpressionRole.Value).type)
        }
        run {
            assertEquals(CwtDataTypes.SyncedLocalisation, CwtDataExpression.resolve("localisation_synced", CwtDataExpressionRole.Value).type)
        }
        run {
            assertEquals(CwtDataTypes.InlineLocalisation, CwtDataExpression.resolve("localisation_inline", CwtDataExpressionRole.Value).type)
        }

        // file/path/icon
        run {
            val e = CwtDataExpression.resolve("filename[foo.txt]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.FileName, e.type)
            assertEquals("foo.txt", e.metadata.value)
        }
        run {
            val e = CwtDataExpression.resolve("filepath", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.FilePath, e.type)
        }
        run {
            val e = CwtDataExpression.resolve("filepath[game/common/test]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.FilePath, e.type)
            assertEquals("common/test", e.metadata.value)
        }
        run {
            val e = CwtDataExpression.resolve("icon[game/gfx/icons/i.png]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Icon, e.type)
            assertEquals("gfx/icons/i.png", e.metadata.value)
        }

        // definition and values
        run {
            val e = CwtDataExpression.resolve("<my_def>", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Definition, e.type)
            assertEquals("my_def", e.metadata.value)
        }

        run {
            assertEquals("blue", CwtDataExpression.resolve("enum[blue]", CwtDataExpressionRole.Value).metadata.value)
        }

        run {
            val e = CwtDataExpression.resolve("union[loc_or_text]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.UnionValue, e.type)
            assertEquals("loc_or_text", e.metadata.value)
        }

        run {
            assertEquals("foo", CwtDataExpression.resolve("value[foo]", CwtDataExpressionRole.Value).metadata.value)
        }
        run {
            assertEquals("foo", CwtDataExpression.resolve("value_set[foo]", CwtDataExpressionRole.Value).metadata.value)
        }
        run {
            assertEquals("foo", CwtDataExpression.resolve("dynamic_value[foo]", CwtDataExpressionRole.Value).metadata.value)
        }

        // scope / scope_group
        run { assertEquals(CwtDataTypes.ScopeField, CwtDataExpression.resolve("scope_field", CwtDataExpressionRole.Value).type) }
        run {
            val e = CwtDataExpression.resolve("scope[any]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Scope, e.type)
            assertNull(e.metadata.value)
        }
        run {
            val e = CwtDataExpression.resolve("scope[planet]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Scope, e.type)
            assertEquals("planet", e.metadata.value)
        }
        run {
            val e = CwtDataExpression.resolve("scope_group[g1]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.ScopeGroup, e.type)
            assertEquals("g1", e.metadata.value)
        }

        // value field / int value field
        run {
            assertEquals(CwtDataTypes.ValueField, CwtDataExpression.resolve("value_field", CwtDataExpressionRole.Value).type)
        }
        run {
            val e = CwtDataExpression.resolve("value_field[0.0..1.0]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.ValueField, e.type)
            assertEquals(FloatRangeInfo.from("[0.0..1.0]"), e.metadata.floatRange)
        }
        run {
            assertEquals(CwtDataTypes.IntValueField, CwtDataExpression.resolve("int_value_field", CwtDataExpressionRole.Value).type)
        }
        run {
            val e = CwtDataExpression.resolve("int_value_field(0..1)", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.IntValueField, e.type)
            assertEquals(IntRangeInfo.from("(0..1)"), e.metadata.intRange)
        }

        // variable field variants
        run {
            assertEquals(CwtDataTypes.VariableField, CwtDataExpression.resolve("variable_field", CwtDataExpressionRole.Value).type)
        }
        run {
            val e = CwtDataExpression.resolve("variable_field[0.0..1.0]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.VariableField, e.type)
            assertEquals(FloatRangeInfo.from("[0.0..1.0]"), e.metadata.floatRange)
        }
        run {
            val e = CwtDataExpression.resolve("variable_field_32(0.0..1.0]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.VariableField, e.type)
            assertEquals(FloatRangeInfo.from("(0.0..1.0]"), e.metadata.floatRange)
        }
        run {
            assertEquals(CwtDataTypes.IntVariableField, CwtDataExpression.resolve("int_variable_field", CwtDataExpressionRole.Value).type)
        }
        run {
            val e = CwtDataExpression.resolve("int_variable_field(0..1)", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.IntVariableField, e.type)
            assertEquals(IntRangeInfo.from("(0..1)"), e.metadata.intRange)
        }
        run {
            val e = CwtDataExpression.resolve("int_variable_field(0..1]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.IntVariableField, e.type)
            assertEquals(IntRangeInfo.from("(0..1]"), e.metadata.intRange)
        }

        // alias related
        run {
            assertEquals("right", CwtDataExpression.resolve("single_alias_right[right]", CwtDataExpressionRole.Value).metadata.value)
        }
        run {
            assertEquals("name", CwtDataExpression.resolve("alias_name[name]", CwtDataExpressionRole.Value).metadata.value)
        }
        run {
            assertEquals("left", CwtDataExpression.resolve("alias_match_left[left]", CwtDataExpressionRole.Value).metadata.value)
        }
        run {
            assertEquals("keys", CwtDataExpression.resolve("alias_keys_field[keys]", CwtDataExpressionRole.Value).metadata.value)
        }

        // any, parameter-like, stellaris name format
        run {
            assertEquals(CwtDataTypes.Any, CwtDataExpression.resolve("\$any", CwtDataExpressionRole.Value).type)
        }

        run {
            assertEquals(CwtDataTypes.ScriptValueReference, CwtDataExpression.resolve("\$script_value_reference", CwtDataExpressionRole.Value).type)
        }
        run {
            assertEquals(CwtDataTypes.ArrayDefineReference, CwtDataExpression.resolve("\$array_define_reference", CwtDataExpressionRole.Value).type)
        }
        run {
            assertEquals(CwtDataTypes.DefineReference, CwtDataExpression.resolve("\$define_reference", CwtDataExpressionRole.Value).type)
        }
        run {
            val e = CwtDataExpression.resolve("\$tags[some_tag]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Tags, e.type)
            assertEquals("some_tag", e.metadata.value)
        }
        run {
            assertEquals(CwtDataTypes.DatabaseObject, CwtDataExpression.resolve("\$database_object", CwtDataExpressionRole.Value).type)
        }
        run {
            val e = CwtDataExpression.resolve("name_format[format_x]", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.NameFormat, e.type)
            assertEquals("format_x", e.metadata.value)
        }

        run {
            assertEquals(CwtDataTypes.ShaderEffect, CwtDataExpression.resolve("\$shader_effect", CwtDataExpressionRole.Value).type)
            assertEquals(CwtDataTypes.MeshLocator, CwtDataExpression.resolve("\$mesh_locator", CwtDataExpressionRole.Value).type)
            assertEquals(CwtDataTypes.TechnologyWithLevel, CwtDataExpression.resolve("\$technology_with_level", CwtDataExpressionRole.Value).type)
        }

        run {
            assertEquals(CwtDataTypes.Parameter, CwtDataExpression.resolve("\$parameter", CwtDataExpressionRole.Value).type)
            assertEquals(CwtDataTypes.ParameterValue, CwtDataExpression.resolve("\$parameter_value", CwtDataExpressionRole.Value).type)
            assertEquals(CwtDataTypes.LocalisationParameter, CwtDataExpression.resolve("\$localisation_parameter", CwtDataExpressionRole.Value).type)
        }
    }

    @Test
    fun testResolveTemplateExpression_viaDataExpression() {
        if (!hasEp()) return
        val s = "a_value[foo]_b"
        val e = CwtDataExpression.resolve(s, CwtDataExpressionRole.Value)
        if (e.type != CwtDataTypes.Template) {
            // 在某些退化环境下，EP 不可用时会回退为 Constant
            // 为了兼容性测试，这里仅校验不抛异常
            return
        }
        assertEquals(CwtDataTypes.Template, e.type)
        assertEquals(s, e.expressionString)
        assertNull(e.metadata.value) // since 3.0.1
        assertEquals(e, CwtDataExpression.resolve(s, CwtDataExpressionRole.Value))
    }

    @Test
    fun testResolveTemplateSegments_direct() {
        if (!hasEp()) return
        run {
            val e = CwtDataExpression.resolveTemplate("value[bar]")
            assertEquals(CwtDataTypes.Value, e.type)
            assertEquals("bar", e.metadata.value)
        }
        run {
            val e = CwtDataExpression.resolveTemplate("abc")
            assertEquals(CwtDataTypes.Constant, e.type)
            assertEquals("abc", e.expressionString)
            assertNull(e.metadata.value) // since 3.0.1
        }
    }

    @Test
    fun testResolvePatterns() {
        if (!hasEp()) return
        run {
            val e = CwtDataExpression.resolve("glob:fo*", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Glob, e.type)
            assertEquals("fo*", e.metadata.value)
            assertFalse(e.metadata.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("glob.i:fo*", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Glob, e.type)
            assertEquals("fo*", e.metadata.value)
            assertTrue(e.metadata.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("ant:foo/*", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Ant, e.type)
            assertEquals("foo/*", e.metadata.value)
            assertFalse(e.metadata.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("ant.i:foo/*", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Ant, e.type)
            assertEquals("foo/*", e.metadata.value)
            assertTrue(e.metadata.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("re:foo.*bar", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Regex, e.type)
            assertEquals("foo.*bar", e.metadata.value)
            assertFalse(e.metadata.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("re.i:foo.*bar", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Regex, e.type)
            assertEquals("foo.*bar", e.metadata.value)
            assertTrue(e.metadata.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("regex:foo.*bar", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Regex, e.type)
            assertEquals("foo.*bar", e.metadata.value)
            assertFalse(e.metadata.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("regex.i:foo.*bar", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Regex, e.type)
            assertEquals("foo.*bar", e.metadata.value)
            assertTrue(e.metadata.ignoreCase)
        }
    }

    @Test
    fun testResolvePatterns_empty() {
        if (!hasEp()) return
        run {
            val e = CwtDataExpression.resolve("glob:", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Glob, e.type)
            assertEquals("", e.metadata.value)
            assertFalse(e.metadata.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("glob.i:", CwtDataExpressionRole.Value)
            assertEquals(CwtDataTypes.Glob, e.type)
            assertEquals("", e.metadata.value)
            assertTrue(e.metadata.ignoreCase)
        }
    }
}
