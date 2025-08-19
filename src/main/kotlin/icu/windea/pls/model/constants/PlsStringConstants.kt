package icu.windea.pls.model.constants

import icu.windea.pls.core.*

object PlsStringConstants {
    const val anonymous = "(anonymous)"
    const val unknown = "(unknown)"
    const val unresolved = "(unresolved)"

    const val typePrefix = "(type)"
    const val subtypePrefix = "(subtype)"
    const val rowPrefix = "(row)"
    const val gameRulePrefix = "(game rule)"
    const val onActionPrefix = "(on action)"
    const val enumPrefix = "(enum)"
    const val enumValuePrefix = "(enum value)"
    const val complexEnumPrefix = "(complex enum)"
    const val complexEnumValuePrefix = "(complex enum value)"
    const val dynamicValueTypePrefix = "(dynamic value type)"
    const val dynamicValuePrefix = "(dynamic value)"
    const val inlinePrefix = "(inline)"
    const val singleAliasPrefix = "(single alias)"
    const val aliasPrefix = "(alias)"
    const val linkPrefix = "(link)"
    const val localisationLinkPrefix = "(localisation link)"
    const val localisationPromotionPrefix = "(localisation promotion)"
    const val localisationCommandPrefix = "(localisation command)"
    const val modifierCategoryPrefix = "(modifier category)"
    const val modifierPrefix = "(modifier)"
    const val triggerPrefix = "(trigger)"
    const val effectPrefix = "(effect)"
    const val scopePrefix = "(scope)"
    const val scopeGroupPrefix = "(scope group)"
    const val databaseObjectTypePrefix = "(database object type)"
    const val systemScopePrefix = "(system scope)"
    const val scriptedVariablePrefix = "(scripted variable)"
    const val parameterPrefix = "(parameter)"
    const val definitionPrefix = "(definition)"
    const val relatedDefinitionPrefix = "(related definition)"
    const val relatedLocalisationPrefix = "(related localisation)"
    const val relatedImagePrefix = "(related image)"
    const val generatedModifierPrefix = "(generated modifier)"
    const val eventTypePrefix = "(event type)"
    const val definitionPropertyPrefix = "(definition property)"
    const val definitionValuePrefix = "(definition value)"
    const val propertyPrefix = "(property)"
    const val localePrefix = "(locale)"
    const val localisationPrefix = "(localisation)"
    const val localisationSyncedPrefix = "(localisation_synced)"
    const val localisationPropertyPrefix = "(localisation property)"
    const val localisationColorPrefix = "(localisation color)"
    const val localisationIconPrefix = "(localisation icon)"
    const val inlineScriptPrefix = "(inline script)"

    const val headerMarker = "<header>"
    const val rowMarker = "<row>"
    const val loadingMarker = "<loading...>"

    const val commentFolder = "# ..."
    const val blockFolder = "{...}"
    val parameterConditionFolder = { expression: String -> "[[$expression]...]" }
    const val inlineMathFolder = "@[...]"
    const val commandFolder = "[...]"
    const val conceptCommandFolder = "['...']"
    const val conceptCommandWithTextFolder = "['...', ...]"

    const val suppressInspectionsTagName = "noinspection"

    val cwtColorSettingsSample by lazy { "/samples/Cwt.colorSettings.txt".toClasspathUrl().readText() }
    val cwtCodeStyleSettingsSample by lazy { "/samples/Cwt.codeStyleSettings.txt".toClasspathUrl().readText() }

    val paradoxLocalisationColorSettingsSample by lazy { "/samples/ParadoxLocalisation.colorSettings.txt".toClasspathUrl().readText() }
    val paradoxLocalisationCodeStyleSettingsSample by lazy { "/samples/ParadoxLocalisation.codeStyleSettings.txt".toClasspathUrl().readText() }

    val paradoxScriptColorSettingsSample by lazy { "/samples/ParadoxScript.colorSettings.txt".toClasspathUrl().readText() }
    val paradoxScriptCodeStyleSettingsSample by lazy { "/samples/ParadoxScript.codeStyleSettings.txt".toClasspathUrl().readText() }

    val paradoxCsvColorSettingsSample by lazy { "/samples/ParadoxCsv.colorSettings.txt".toClasspathUrl().readText() }
    val paradoxCsvCodeStyleSettingsSample by lazy { "/samples/ParadoxCsv.codeStyleSettings.txt".toClasspathUrl().readText() }
}
