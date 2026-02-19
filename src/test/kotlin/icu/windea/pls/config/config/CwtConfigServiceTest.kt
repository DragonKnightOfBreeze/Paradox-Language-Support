package icu.windea.pls.config.config

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.cwt.psi.CwtMember
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.atomic.AtomicInteger

@RunWith(JUnit4::class)
class CwtConfigServiceTest : BasePlatformTestCase() {
    private val counter = AtomicInteger()

    private fun assertResolveConfigType(text: String, expected: CwtConfigType?) {
        val name = "resolve_config_type_${counter.getAndIncrement()}.test.cwt"
        myFixture.configureByText(name, text)
        val elementAtCaret = myFixture.file.findElementAt(myFixture.caretOffset)
            ?: error("No element at caret")
        val member = PsiTreeUtil.getParentOfType(elementAtCaret, CwtMember::class.java, false)
            ?: error("No CwtMember at caret")
        val result = CwtConfigService.resolveConfigType(member, myFixture.file)
        assertEquals(expected, result)
    }

    // region resolveConfigType

    @Test
    fun testResolveConfigType_type() {
        assertResolveConfigType("""
            types = {
                <caret>type[army] = {}
            }
        """.trimIndent(), CwtConfigTypes.Type)
    }

    @Test
    fun testResolveConfigType_subtype() {
        assertResolveConfigType("""
            types = {
                type[army] = {
                    <caret>subtype[has_species] = {}
                }
            }
        """.trimIndent(), CwtConfigTypes.Subtype)
    }

    @Test
    fun testResolveConfigType_modifier_inType() {
        // types/type[*]/modifiers/mod_name (length 4, no subtype) → Modifier
        assertResolveConfigType("""
            types = {
                type[army] = {
                    modifiers = {
                        <caret>some_modifier = {}
                    }
                }
            }
        """.trimIndent(), CwtConfigTypes.Modifier)
    }

    @Test
    fun testResolveConfigType_modifier_inType_withSubtype() {
        // types/type[*]/modifiers/subtype[*]/mod_name (length 5, with subtype) → Modifier
        assertResolveConfigType("""
            types = {
                type[army] = {
                    modifiers = {
                        subtype[has_species] = {
                            <caret>some_modifier = {}
                        }
                    }
                }
            }
        """.trimIndent(), CwtConfigTypes.Modifier)
    }

    @Test
    fun testResolveConfigType_modifier_inType_wrongDepth() {
        // types/type[*]/modifiers/mod_name/nested (length 5, no subtype at index 3) → null
        assertResolveConfigType("""
            types = {
                type[army] = {
                    modifiers = {
                        some_modifier = {
                            <caret>nested = {}
                        }
                    }
                }
            }
        """.trimIndent(), null)
    }

    @Test
    fun testResolveConfigType_modifier_inType_subtypeContainerOnly() {
        // types/type[*]/modifiers/subtype[*] (length 4, subtype at index 3 but length != 5) → null
        assertResolveConfigType("""
            types = {
                type[army] = {
                    modifiers = {
                        <caret>subtype[has_species] = {}
                    }
                }
            }
        """.trimIndent(), null)
    }

    @Test
    fun testResolveConfigType_row() {
        assertResolveConfigType("""
            rows = {
                <caret>row[my_row] = {}
            }
        """.trimIndent(), CwtConfigTypes.Row)
    }

    @Test
    fun testResolveConfigType_enum() {
        assertResolveConfigType("""
            enums = {
                <caret>enum[my_enum] = {}
            }
        """.trimIndent(), CwtConfigTypes.Enum)
    }

    @Test
    fun testResolveConfigType_enumValue() {
        // 值元素，位于 enum 块中
        assertResolveConfigType("""
            enums = {
                enum[my_enum] = {
                    <caret>value1
                }
            }
        """.trimIndent(), CwtConfigTypes.EnumValue)
    }

    @Test
    fun testResolveConfigType_complexEnum() {
        assertResolveConfigType("""
            enums = {
                <caret>complex_enum[my_ce] = {}
            }
        """.trimIndent(), CwtConfigTypes.ComplexEnum)
    }

    @Test
    fun testResolveConfigType_dynamicValueType() {
        assertResolveConfigType("""
            values = {
                <caret>value[my_value] = {}
            }
        """.trimIndent(), CwtConfigTypes.DynamicValueType)
    }

    @Test
    fun testResolveConfigType_dynamicValue() {
        // 值元素，位于 value 块中
        assertResolveConfigType("""
            values = {
                value[my_value] = {
                    <caret>some_val
                }
            }
        """.trimIndent(), CwtConfigTypes.DynamicValue)
    }

    @Test
    fun testResolveConfigType_singleAlias() {
        assertResolveConfigType("""
            <caret>single_alias[my_sa] = something
        """.trimIndent(), CwtConfigTypes.SingleAlias)
    }

    @Test
    fun testResolveConfigType_alias() {
        assertResolveConfigType("""
            <caret>alias[other:my_alias] = something
        """.trimIndent(), CwtConfigTypes.Alias)
    }

    @Test
    fun testResolveConfigType_alias_modifier() {
        assertResolveConfigType("""
            <caret>alias[modifier:my_modifier] = something
        """.trimIndent(), CwtConfigTypes.Modifier)
    }

    @Test
    fun testResolveConfigType_alias_trigger() {
        assertResolveConfigType("""
            <caret>alias[trigger:my_trigger] = something
        """.trimIndent(), CwtConfigTypes.Trigger)
    }

    @Test
    fun testResolveConfigType_alias_effect() {
        assertResolveConfigType("""
            <caret>alias[effect:my_effect] = something
        """.trimIndent(), CwtConfigTypes.Effect)
    }

    @Test
    fun testResolveConfigType_directive() {
        assertResolveConfigType("""
            <caret>directive[my_dir] = something
        """.trimIndent(), CwtConfigTypes.Directive)
    }

    @Test
    fun testResolveConfigType_link() {
        assertResolveConfigType("""
            links = {
                <caret>my_link = something
            }
        """.trimIndent(), CwtConfigTypes.Link)
    }

    @Test
    fun testResolveConfigType_localisationLink() {
        assertResolveConfigType("""
            localisation_links = {
                <caret>my_link = something
            }
        """.trimIndent(), CwtConfigTypes.LocalisationLink)
    }

    @Test
    fun testResolveConfigType_localisationPromotion() {
        assertResolveConfigType("""
            localisation_promotions = {
                <caret>my_promo = something
            }
        """.trimIndent(), CwtConfigTypes.LocalisationPromotion)
    }

    @Test
    fun testResolveConfigType_localisationCommand() {
        assertResolveConfigType("""
            localisation_commands = {
                <caret>my_cmd = something
            }
        """.trimIndent(), CwtConfigTypes.LocalisationCommand)
    }

    @Test
    fun testResolveConfigType_modifierCategory() {
        assertResolveConfigType("""
            modifier_categories = {
                <caret>my_cat = something
            }
        """.trimIndent(), CwtConfigTypes.ModifierCategory)
    }

    @Test
    fun testResolveConfigType_modifier_standalone() {
        assertResolveConfigType("""
            modifiers = {
                <caret>my_modifier = something
            }
        """.trimIndent(), CwtConfigTypes.Modifier)
    }

    @Test
    fun testResolveConfigType_scope() {
        assertResolveConfigType("""
            scopes = {
                <caret>my_scope = something
            }
        """.trimIndent(), CwtConfigTypes.Scope)
    }

    @Test
    fun testResolveConfigType_scopeGroup() {
        assertResolveConfigType("""
            scope_groups = {
                <caret>my_group = something
            }
        """.trimIndent(), CwtConfigTypes.ScopeGroup)
    }

    @Test
    fun testResolveConfigType_databaseObjectType() {
        assertResolveConfigType("""
            database_object_types = {
                <caret>my_type = something
            }
        """.trimIndent(), CwtConfigTypes.DatabaseObjectType)
    }

    @Test
    fun testResolveConfigType_systemScope() {
        assertResolveConfigType("""
            system_scopes = {
                <caret>my_scope = something
            }
        """.trimIndent(), CwtConfigTypes.SystemScope)
    }

    @Test
    fun testResolveConfigType_locale() {
        assertResolveConfigType("""
            locales = {
                <caret>my_locale = something
            }
        """.trimIndent(), CwtConfigTypes.Locale)
    }

    @Test
    fun testResolveConfigType_extendedScriptedVariable() {
        assertResolveConfigType("""
            scripted_variables = {
                <caret>my_var = something
            }
        """.trimIndent(), CwtConfigTypes.ExtendedScriptedVariable)
    }

    @Test
    fun testResolveConfigType_extendedDefinition() {
        assertResolveConfigType("""
            definitions = {
                <caret>my_def = something
            }
        """.trimIndent(), CwtConfigTypes.ExtendedDefinition)
    }

    @Test
    fun testResolveConfigType_extendedGameRule() {
        assertResolveConfigType("""
            game_rules = {
                <caret>my_rule = something
            }
        """.trimIndent(), CwtConfigTypes.ExtendedGameRule)
    }

    @Test
    fun testResolveConfigType_extendedOnAction() {
        assertResolveConfigType("""
            on_actions = {
                <caret>my_action = something
            }
        """.trimIndent(), CwtConfigTypes.ExtendedOnAction)
    }

    @Test
    fun testResolveConfigType_extendedInlineScript() {
        assertResolveConfigType("""
            inline_scripts = {
                <caret>my_script = something
            }
        """.trimIndent(), CwtConfigTypes.ExtendedInlineScript)
    }

    @Test
    fun testResolveConfigType_extendedParameter() {
        assertResolveConfigType("""
            parameters = {
                <caret>my_param = something
            }
        """.trimIndent(), CwtConfigTypes.ExtendedParameter)
    }

    @Test
    fun testResolveConfigType_extendedComplexEnumValue() {
        assertResolveConfigType("""
            complex_enum_values = {
                my_type = {
                    <caret>my_value = something
                }
            }
        """.trimIndent(), CwtConfigTypes.ExtendedComplexEnumValue)
    }

    @Test
    fun testResolveConfigType_extendedDynamicValue() {
        assertResolveConfigType("""
            dynamic_values = {
                my_type = {
                    <caret>my_value = something
                }
            }
        """.trimIndent(), CwtConfigTypes.ExtendedDynamicValue)
    }

    @Test
    fun testResolveConfigType_noMatch() {
        // 不匹配任何已知模式
        assertResolveConfigType("""
            unknown = {
                <caret>something = value
            }
        """.trimIndent(), null)
    }

    @Test
    fun testResolveConfigType_topLevelProperty_noMatch() {
        // 顶层属性，不匹配任何已知容器模式（depth 1）
        assertResolveConfigType("""
            <caret>unknown = value
        """.trimIndent(), null)
    }

    // endregion

    // region resolveNameByConfigType

    @Test
    fun testResolveNameByConfigType_type() {
        assertEquals("army", CwtConfigService.resolveNameByConfigType("type[army]", CwtConfigTypes.Type))
    }

    @Test
    fun testResolveNameByConfigType_subtype() {
        assertEquals("has_species", CwtConfigService.resolveNameByConfigType("subtype[has_species]", CwtConfigTypes.Subtype))
    }

    @Test
    fun testResolveNameByConfigType_row() {
        assertEquals("my_row", CwtConfigService.resolveNameByConfigType("row[my_row]", CwtConfigTypes.Row))
    }

    @Test
    fun testResolveNameByConfigType_enum() {
        assertEquals("my_enum", CwtConfigService.resolveNameByConfigType("enum[my_enum]", CwtConfigTypes.Enum))
    }

    @Test
    fun testResolveNameByConfigType_complexEnum() {
        assertEquals("my_ce", CwtConfigService.resolveNameByConfigType("complex_enum[my_ce]", CwtConfigTypes.ComplexEnum))
    }

    @Test
    fun testResolveNameByConfigType_dynamicValueType() {
        assertEquals("my_value", CwtConfigService.resolveNameByConfigType("value[my_value]", CwtConfigTypes.DynamicValueType))
    }

    @Test
    fun testResolveNameByConfigType_singleAlias() {
        assertEquals("my_sa", CwtConfigService.resolveNameByConfigType("single_alias[my_sa]", CwtConfigTypes.SingleAlias))
    }

    @Test
    fun testResolveNameByConfigType_alias() {
        assertEquals("my_alias", CwtConfigService.resolveNameByConfigType("alias[my_alias]", CwtConfigTypes.Alias))
    }

    @Test
    fun testResolveNameByConfigType_trigger() {
        assertEquals("my_trigger", CwtConfigService.resolveNameByConfigType("alias[trigger:my_trigger]", CwtConfigTypes.Trigger))
    }

    @Test
    fun testResolveNameByConfigType_effect() {
        assertEquals("my_effect", CwtConfigService.resolveNameByConfigType("alias[effect:my_effect]", CwtConfigTypes.Effect))
    }

    @Test
    fun testResolveNameByConfigType_modifier_aliasForm() {
        // alias[modifier:*] 格式
        assertEquals("my_modifier", CwtConfigService.resolveNameByConfigType("alias[modifier:my_modifier]", CwtConfigTypes.Modifier))
    }

    @Test
    fun testResolveNameByConfigType_modifier_plainText() {
        // 纯文本格式（来自 modifiers/* 或 types/type[*]/modifiers/**）
        assertEquals("my_modifier", CwtConfigService.resolveNameByConfigType("my_modifier", CwtConfigTypes.Modifier))
    }

    @Test
    fun testResolveNameByConfigType_directive() {
        assertEquals("my_dir", CwtConfigService.resolveNameByConfigType("directive[my_dir]", CwtConfigTypes.Directive))
    }

    @Test
    fun testResolveNameByConfigType_otherType_passthrough() {
        // 其他类型直接返回原文本
        assertEquals("my_link", CwtConfigService.resolveNameByConfigType("my_link", CwtConfigTypes.Link))
        assertEquals("my_scope", CwtConfigService.resolveNameByConfigType("my_scope", CwtConfigTypes.Scope))
        assertEquals("my_locale", CwtConfigService.resolveNameByConfigType("my_locale", CwtConfigTypes.Locale))
    }

    @Test
    fun testResolveNameByConfigType_wrongPrefix() {
        // 前缀不匹配时返回 null
        assertNull(CwtConfigService.resolveNameByConfigType("wrong[army]", CwtConfigTypes.Type))
        assertNull(CwtConfigService.resolveNameByConfigType("type[army]", CwtConfigTypes.Subtype))
    }

    @Test
    fun testResolveNameByConfigType_emptyName() {
        // 空名称返回 null
        assertNull(CwtConfigService.resolveNameByConfigType("type[]", CwtConfigTypes.Type))
        assertNull(CwtConfigService.resolveNameByConfigType("", CwtConfigTypes.Link))
    }

    // endregion
}
