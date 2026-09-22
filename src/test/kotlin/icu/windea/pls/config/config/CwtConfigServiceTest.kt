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

/**
 * @see CwtConfigService
 */
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
        val text = """
            types = {
                <caret>type[army] = {}
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Type)
    }

    @Test
    fun testResolveConfigType_subtype() {
        val text = """
            types = {
                type[army] = {
                    <caret>subtype[has_species] = {}
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Subtype)
    }

    @Test
    fun testResolveConfigType_modifier_inType() {
        // types/type[*]/modifiers/mod_name (length 4, no subtype) → Modifier
        val text = """
            types = {
                type[army] = {
                    modifiers = {
                        <caret>some_modifier = {}
                    }
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Modifier)
    }

    @Test
    fun testResolveConfigType_modifier_inType_withSubtype() {
        // types/type[*]/modifiers/subtype[*]/mod_name (length 5, with subtype) → Modifier
        val text = """
            types = {
                type[army] = {
                    modifiers = {
                        subtype[has_species] = {
                            <caret>some_modifier = {}
                        }
                    }
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Modifier)
    }

    @Test
    fun testResolveConfigType_modifier_inType_wrongDepth() {
        // types/type[*]/modifiers/mod_name/nested (length 5, no subtype at index 3) → null
        val text = """
            types = {
                type[army] = {
                    modifiers = {
                        some_modifier = {
                            <caret>nested = {}
                        }
                    }
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, null)
    }

    @Test
    fun testResolveConfigType_modifier_inType_subtypeContainerOnly() {
        // types/type[*]/modifiers/subtype[*] (length 4, subtype at index 3 but length != 5) → null
        val text = """
            types = {
                type[army] = {
                    modifiers = {
                        <caret>subtype[has_species] = {}
                    }
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, null)
    }

    @Test
    fun testResolveConfigType_row() {
        val text = """
            rows = {
                <caret>row[my_row] = {}
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Row)
    }

    @Test
    fun testResolveConfigType_defineNamespace() {
        val text = """
            defines = {
                <caret>Namespace = {
                    Variable = 0
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.DefineNamespace)
    }

    @Test
    fun testResolveConfigType_defineVariable() {
        val text = """
            defines = {
                Namespace = {
                    <caret>Variable = 0
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.DefineVariable)
    }

    @Test
    fun testResolveConfigType_enum() {
        val text = """
            enums = {
                <caret>enum[my_enum] = {}
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Enum)
    }

    @Test
    fun testResolveConfigType_enumValue() {
        // 值元素，位于 enum 块中
        val text = """
            enums = {
                enum[my_enum] = {
                    <caret>value1
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.EnumValue)
    }

    @Test
    fun testResolveConfigType_complexEnum() {
        val text = """
            enums = {
                <caret>complex_enum[my_ce] = {}
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ComplexEnum)
    }

    @Test
    fun testResolveConfigType_union() {
        val text = """
            unions = {
                <caret>union[loc_or_text] = { localisation scalar }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Union)
    }

    @Test
    fun testResolveConfigType_dynamicValueType() {
        val text = """
            values = {
                <caret>value[my_value] = {}
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.DynamicValueType)
    }

    @Test
    fun testResolveConfigType_dynamicValue() {
        // 值元素，位于 value 块中
        val text = """
            values = {
                value[my_value] = {
                    <caret>some_val
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.DynamicValue)
    }

    @Test
    fun testResolveConfigType_singleAlias() {
        val text = """
            <caret>single_alias[my_sa] = something
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.SingleAlias)
    }

    @Test
    fun testResolveConfigType_alias() {
        val text = """
            <caret>alias[other:my_alias] = something
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Alias)
    }

    @Test
    fun testResolveConfigType_alias_modifier() {
        val text = """
            <caret>alias[modifier:my_modifier] = something
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Modifier)
    }

    @Test
    fun testResolveConfigType_alias_trigger() {
        val text = """
            <caret>alias[trigger:my_trigger] = something
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Trigger)
    }

    @Test
    fun testResolveConfigType_alias_effect() {
        val text = """
            <caret>alias[effect:my_effect] = something
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Effect)
    }

    @Test
    fun testResolveConfigType_macro() {
        val text = """
            <caret>macro[my_dir] = something
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Macro)
    }

    @Test
    fun testResolveConfigType_link() {
        val text = """
            links = {
                <caret>my_link = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Link)
    }

    @Test
    fun testResolveConfigType_localisationLink() {
        val text = """
            localisation_links = {
                <caret>my_link = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.LocalisationLink)
    }

    @Test
    fun testResolveConfigType_localisationPromotion() {
        val text = """
            localisation_promotions = {
                <caret>my_promo = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.LocalisationPromotion)
    }

    @Test
    fun testResolveConfigType_localisationCommand() {
        val text = """
            localisation_commands = {
                <caret>my_cmd = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.LocalisationCommand)
    }

    @Test
    fun testResolveConfigType_modifierCategory() {
        val text = """
            modifier_categories = {
                <caret>my_cat = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ModifierCategory)
    }

    @Test
    fun testResolveConfigType_modifier_standalone() {
        val text = """
            modifiers = {
                <caret>my_modifier = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Modifier)
    }

    @Test
    fun testResolveConfigType_scope() {
        val text = """
            scopes = {
                <caret>my_scope = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Scope)
    }

    @Test
    fun testResolveConfigType_scopeGroup() {
        val text = """
            scope_groups = {
                <caret>my_group = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ScopeGroup)
    }

    @Test
    fun testResolveConfigType_databaseObjectType() {
        val text = """
            database_object_types = {
                <caret>my_type = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.DatabaseObjectType)
    }

    @Test
    fun testResolveConfigType_systemScope() {
        val text = """
            system_scopes = {
                <caret>my_scope = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.SystemScope)
    }

    @Test
    fun testResolveConfigType_locale() {
        val text = """
            locales = {
                <caret>my_locale = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.Locale)
    }

    @Test
    fun testResolveConfigType_extendedScriptedVariable() {
        val text = """
            scripted_variables = {
                <caret>my_var = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ExtendedScriptedVariable)
    }

    @Test
    fun testResolveConfigType_extendedDefinition() {
        val text = """
            definitions = {
                <caret>my_def = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ExtendedDefinition)
    }

    @Test
    fun testResolveConfigType_extendedGameRule() {
        val text = """
            game_rules = {
                <caret>my_rule = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ExtendedGameRule)
    }

    @Test
    fun testResolveConfigType_extendedOnAction() {
        val text = """
            on_actions = {
                <caret>my_action = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ExtendedOnAction)
    }

    @Test
    fun testResolveConfigType_extendedParameter() {
        val text = """
            parameters = {
                <caret>my_param = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ExtendedParameter)
    }

    @Test
    fun testResolveConfigType_extendedComplexEnumValue() {
        val text = """
            complex_enum_values = {
                my_type = {
                    <caret>my_value = something
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ExtendedComplexEnumValue)
    }

    @Test
    fun testResolveConfigType_extendedDynamicValue() {
        val text = """
            dynamic_values = {
                my_type = {
                    <caret>my_value = something
                }
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ExtendedDynamicValue)
    }

    @Test
    fun testResolveConfigType_extendedInlineScript() {
        val text = """
            inline_scripts = {
                <caret>my_script = something
            }
        """.trimIndent()
        assertResolveConfigType(text, CwtConfigTypes.ExtendedInlineScript)
    }

    @Test
    fun testResolveConfigType_noMatch() {
        // 不匹配任何已知模式
        val text = """
            unknown = {
                <caret>something = value
            }
        """.trimIndent()
        assertResolveConfigType(text, null)
    }

    @Test
    fun testResolveConfigType_topLevelProperty_noMatch() {
        // 顶层属性，不匹配任何已知容器模式（depth 1）
        val text = """
            <caret>unknown = value
        """.trimIndent()
        assertResolveConfigType(text, null)
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
    fun testResolveNameByConfigType_union() {
        assertEquals("loc_or_text", CwtConfigService.resolveNameByConfigType("union[loc_or_text]", CwtConfigTypes.Union))
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
    fun testResolveNameByConfigType_macro() {
        assertEquals("my_dir", CwtConfigService.resolveNameByConfigType("macro[my_dir]", CwtConfigTypes.Macro))
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
