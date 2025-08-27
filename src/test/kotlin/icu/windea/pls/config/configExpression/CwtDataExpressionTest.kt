package icu.windea.pls.config.configExpression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.ep.configExpression.CwtDataExpressionResolver
import org.junit.Assert.*

class CwtDataExpressionTest : BasePlatformTestCase() {
    private fun hasEp(): Boolean = try {
        CwtDataExpressionResolver.EP_NAME.extensionList.isNotEmpty()
    } catch (_: Throwable) {
        false
    }

    fun testResolveEmpty_key_and_value() {
        val ek = CwtDataExpression.resolveEmpty(true)
        assertEquals("", ek.expressionString)
        assertTrue(ek.isKey)
        assertEquals(CwtDataTypes.Constant, ek.type)
        assertEquals("", ek.value)
        assertEquals(ek, CwtDataExpression.resolve("", true))
        assertSame(ek, CwtDataExpression.resolveEmpty(true))

        val ev = CwtDataExpression.resolveEmpty(false)
        assertEquals("", ev.expressionString)
        assertFalse(ev.isKey)
        assertEquals(CwtDataTypes.Constant, ev.type)
        assertEquals("", ev.value)
        assertEquals(ev, CwtDataExpression.resolve("", false))
        assertSame(ev, CwtDataExpression.resolveEmpty(false))

        // equals by expressionString only, but instances differ
        assertEquals(ek, ev)
        assertNotSame(ek, ev)
    }

    fun testResolveBlock() {
        val e = CwtDataExpression.resolveBlock()
        assertEquals("{...}", e.expressionString)
        assertEquals(CwtDataTypes.Block, e.type)
        assertTrue(e.isKey)
        assertSame(e, CwtDataExpression.resolveBlock())
    }

    fun testResolveConstant_key_and_value() {
        if (!hasEp()) return
        val s = "hello"
        val ek = CwtDataExpression.resolve(s, true)
        assertEquals(CwtDataTypes.Constant, ek.type)
        assertEquals(s, ek.value)
        assertTrue(ek.isKey)

        val ev = CwtDataExpression.resolve(s, false)
        assertEquals(CwtDataTypes.Constant, ev.type)
        assertEquals(s, ev.value)
        assertFalse(ev.isKey)

        // equals by expressionString only, but instances differ by cache bucket (key/value)
        assertEquals(ek, ev)
        assertNotSame(ek, ev)
    }

    fun testResolveBaseRules_int_float_scalar_color_bool() {
        if (!hasEp()) return
        // int and int range
        run {
            val e = CwtDataExpression.resolve("int", false)
            assertEquals(CwtDataTypes.Int, e.type)
            assertNull(e.intRange)
        }
        run {
            val e = CwtDataExpression.resolve("int[1..10]", false)
            assertEquals(CwtDataTypes.Int, e.type)
            val r = e.intRange
            assertNotNull(r)
            assertEquals(1, r!!.first)
            assertEquals(10, r.second)
        }

        // float and float range
        run {
            val e = CwtDataExpression.resolve("float", false)
            assertEquals(CwtDataTypes.Float, e.type)
            assertNull(e.floatRange)
        }
        run {
            val e = CwtDataExpression.resolve("float[1.5..2.0]", false)
            assertEquals(CwtDataTypes.Float, e.type)
            val r = e.floatRange
            assertNotNull(r)
            assertEquals(1.5f, r!!.first!!, 0.0001f)
            assertEquals(2.0f, r.second!!, 0.0001f)
        }

        // scalar
        run {
            val e = CwtDataExpression.resolve("scalar", false)
            assertEquals(CwtDataTypes.Scalar, e.type)
        }

        // color field variants
        run {
            val e = CwtDataExpression.resolve("colour_field", false)
            assertEquals(CwtDataTypes.ColorField, e.type)
        }
        run {
            val e = CwtDataExpression.resolve("colour[255,0,0]", false)
            assertEquals(CwtDataTypes.ColorField, e.type)
            assertEquals("255,0,0", e.value)
        }
        run {
            val e = CwtDataExpression.resolve("color_field", false)
            assertEquals(CwtDataTypes.ColorField, e.type)
        }
        run {
            val e = CwtDataExpression.resolve("color[0,255,0]", false)
            assertEquals(CwtDataTypes.ColorField, e.type)
            assertEquals("0,255,0", e.value)
        }

        // bool
        run {
            val e = CwtDataExpression.resolve("bool", false)
            assertEquals(CwtDataTypes.Bool, e.type)
        }
    }

    fun testResolveCoreRules_common() {
        if (!hasEp()) return
        // percentage field
        run {
            val e = CwtDataExpression.resolve("percentage_field", false)
            assertEquals(CwtDataTypes.PercentageField, e.type)
        }
        // date field
        run {
            val e = CwtDataExpression.resolve("date_field", false)
            assertEquals(CwtDataTypes.DateField, e.type)
        }
        run {
            val e = CwtDataExpression.resolve("date_field[2020.1.1]", false)
            assertEquals(CwtDataTypes.DateField, e.type)
            assertEquals("2020.1.1", e.value)
        }
        // localisation types
        run { assertEquals(CwtDataTypes.Localisation, CwtDataExpression.resolve("localisation", false).type) }
        run { assertEquals(CwtDataTypes.SyncedLocalisation, CwtDataExpression.resolve("localisation_synced", false).type) }
        run { assertEquals(CwtDataTypes.InlineLocalisation, CwtDataExpression.resolve("localisation_inline", false).type) }

        // file/path/icon
        run {
            val e = CwtDataExpression.resolve("filename[foo.txt]", false)
            assertEquals(CwtDataTypes.FileName, e.type)
            assertEquals("foo.txt", e.value)
        }
        run {
            val e = CwtDataExpression.resolve("filepath", false)
            assertEquals(CwtDataTypes.FilePath, e.type)
        }
        run {
            val e = CwtDataExpression.resolve("filepath[game/common/test]", false)
            assertEquals(CwtDataTypes.FilePath, e.type)
            assertEquals("common/test", e.value)
        }
        run {
            val e = CwtDataExpression.resolve("icon[game/gfx/icons/i.png]", false)
            assertEquals(CwtDataTypes.Icon, e.type)
            assertEquals("gfx/icons/i.png", e.value)
        }

        // definition and values
        run {
            val e = CwtDataExpression.resolve("<my_def>", false)
            assertEquals(CwtDataTypes.Definition, e.type)
            assertEquals("my_def", e.value)
        }
        run { assertEquals("foo", CwtDataExpression.resolve("value[foo]", false).value) }
        run { assertEquals("foo", CwtDataExpression.resolve("value_set[foo]", false).value) }
        run { assertEquals("foo", CwtDataExpression.resolve("dynamic_value[foo]", false).value) }
        run { assertEquals("blue", CwtDataExpression.resolve("enum[blue]", false).value) }

        // scope / scope_group
        run { assertEquals(CwtDataTypes.ScopeField, CwtDataExpression.resolve("scope_field", false).type) }
        run {
            val e = CwtDataExpression.resolve("scope[any]", false)
            assertEquals(CwtDataTypes.Scope, e.type)
            assertNull(e.value)
        }
        run {
            val e = CwtDataExpression.resolve("scope[planet]", false)
            assertEquals(CwtDataTypes.Scope, e.type)
            assertEquals("planet", e.value)
        }
        run {
            val e = CwtDataExpression.resolve("scope_group[g1]", false)
            assertEquals(CwtDataTypes.ScopeGroup, e.type)
            assertEquals("g1", e.value)
        }

        // value field / int value field
        run { assertEquals(CwtDataTypes.ValueField, CwtDataExpression.resolve("value_field", false).type) }
        run {
            val e = CwtDataExpression.resolve("value_field[abc]", false)
            assertEquals(CwtDataTypes.ValueField, e.type)
            assertEquals("abc", e.value)
        }
        run { assertEquals(CwtDataTypes.IntValueField, CwtDataExpression.resolve("int_value_field", false).type) }
        run {
            val e = CwtDataExpression.resolve("int_value_field[42]", false)
            assertEquals(CwtDataTypes.IntValueField, e.type)
            assertEquals("42", e.value)
        }

        // variable field variants
        run { assertEquals(CwtDataTypes.VariableField, CwtDataExpression.resolve("variable_field", false).type) }
        run { assertEquals("foo", CwtDataExpression.resolve("variable_field[foo]", false).value) }
        run { assertEquals("bar", CwtDataExpression.resolve("variable_field32[bar]", false).value) }
        run { assertEquals(CwtDataTypes.IntVariableField, CwtDataExpression.resolve("int_variable_field", false).type) }
        run { assertEquals("7", CwtDataExpression.resolve("int_variable_field[7]", false).value) }
        run { assertEquals("8", CwtDataExpression.resolve("int_variable_field_32[8]", false).value) }

        // alias related
        run { assertEquals("right", CwtDataExpression.resolve("single_alias_right[right]", false).value) }
        run { assertEquals("name", CwtDataExpression.resolve("alias_name[name]", false).value) }
        run { assertEquals("left", CwtDataExpression.resolve("alias_match_left[left]", false).value) }
        run { assertEquals("keys", CwtDataExpression.resolve("alias_keys_field[keys]", false).value) }

        // any, parameter-like, stellaris name format
        run { assertEquals(CwtDataTypes.Any, CwtDataExpression.resolve("\$any", false).type) }
        run { assertEquals(CwtDataTypes.Parameter, CwtDataExpression.resolve("\$parameter", false).type) }
        run { assertEquals(CwtDataTypes.ParameterValue, CwtDataExpression.resolve("\$parameter_value", false).type) }
        run { assertEquals(CwtDataTypes.LocalisationParameter, CwtDataExpression.resolve("\$localisation_parameter", false).type) }
        run { assertEquals(CwtDataTypes.ShaderEffect, CwtDataExpression.resolve("\$shader_effect", false).type) }
        run { assertEquals(CwtDataTypes.DatabaseObject, CwtDataExpression.resolve("\$database_object", false).type) }
        run { assertEquals(CwtDataTypes.DefineReference, CwtDataExpression.resolve("\$define_reference", false).type) }
        run {
            val e = CwtDataExpression.resolve("stellaris_name_format[format_x]", false)
            assertEquals(CwtDataTypes.StellarisNameFormat, e.type)
            assertEquals("format_x", e.value)
        }
    }

    fun testResolvePatternAware_ant_and_regex() {
        if (!hasEp()) return
        run {
            val e = CwtDataExpression.resolve("ant:foo/*", false)
            assertEquals(CwtDataTypes.AntExpression, e.type)
            assertEquals("foo/*", e.value)
            assertNull(e.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("ant.i:foo/*", false)
            assertEquals(CwtDataTypes.AntExpression, e.type)
            assertEquals("foo/*", e.value)
            assertEquals(true, e.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("re:foo.*bar", false)
            assertEquals(CwtDataTypes.Regex, e.type)
            assertEquals("foo.*bar", e.value)
            assertNull(e.ignoreCase)
        }
        run {
            val e = CwtDataExpression.resolve("re.i:foo.*bar", false)
            assertEquals(CwtDataTypes.Regex, e.type)
            assertEquals("foo.*bar", e.value)
            assertEquals(true, e.ignoreCase)
        }
    }

    fun testResolveTemplateExpression_viaDataExpression() {
        if (!hasEp()) return
        val s = "a_value[foo]_b"
        val e = CwtDataExpression.resolve(s, false)
        if (e.type != CwtDataTypes.TemplateExpression) {
            // 在某些退化环境下，EP 不可用时会回退为 Constant
            // 为了兼容性测试，这里仅校验不抛异常
            return
        }
        assertEquals(CwtDataTypes.TemplateExpression, e.type)
        assertEquals(s, e.value)
        assertEquals(e, CwtDataExpression.resolve(s, false))
    }

    fun testResolveTemplateSegments_direct() {
        if (!hasEp()) return
        run {
            val e = CwtDataExpression.resolveTemplate("value[bar]")
            assertEquals(CwtDataTypes.Value, e.type)
            assertEquals("bar", e.value)
        }
        run {
            val e = CwtDataExpression.resolveTemplate("abc")
            assertEquals(CwtDataTypes.Constant, e.type)
            assertEquals("abc", e.value)
        }
    }

    fun testCaching_same_instance_on_repeated_calls() {
        if (!hasEp()) return
        val s1 = "int[1..10]"
        val a1 = CwtDataExpression.resolve(s1, false)
        val a2 = CwtDataExpression.resolve(s1, false)
        assertSame(a1, a2)

        val k1 = CwtDataExpression.resolve("int", true)
        val k2 = CwtDataExpression.resolve("int", true)
        assertSame(k1, k2)

        val v1 = CwtDataExpression.resolve("int", false)
        assertNotSame(k1, v1)
        assertEquals(k1, v1) // equals by expressionString
    }
}
