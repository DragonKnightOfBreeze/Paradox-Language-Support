package icu.windea.pls.model.constants

import icu.windea.pls.core.loadText

object PlsStrings {
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
    const val directivePrefix = "(directive)"
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
    const val relatedScriptedVariablePrefix = "(related scripted variable)"
    const val relatedDefinitionPrefix = "(related definition)"
    const val relatedLocalisationPrefix = "(related localisation)"
    const val relatedImagePrefix = "(related image)"
    const val generatedModifierPrefix = "(generated modifier)"
    const val eventTypePrefix = "(event type)"
    const val definitionPropertyPrefix = "(definition property)"
    const val definitionValuePrefix = "(definition value)"
    const val propertyPrefix = "(property)"
    const val stringPrefix = "(string)"
    const val localePrefix = "(locale)"
    const val localisationPrefix = "(localisation)"
    const val localisationSyncedPrefix = "(localisation_synced)"
    const val localisationPropertyPrefix = "(localisation property)"
    const val localisationColorPrefix = "(localisation color)"
    const val localisationIconPrefix = "(localisation icon)"
    const val inlineScriptPrefix = "(inline script)"
    const val definitionInjectionPrefix = "(definition injection)"
    const val definitionInjectionTargetPrefix = "(definition injection target)"

    const val headerMarker = "<header>"
    const val rowMarker = "<row>"
    // const val loadingMarker = "<loading...>"

    const val commentFolder = "# ..."
    const val quotedFolder = "\"...\""
    const val blockFolder = "{...}"
    val parameterConditionFolder = { expression: String -> "[[$expression]...]" }
    const val inlineMathFolder = "@[...]"
    const val commandFolder = "[...]"
    const val conceptCommandFolder = "['...']"
    const val conceptCommandWithTextFolder = "['...', ...]"

    const val suppressInspectionsTagName = "noinspection"

    val cwtColorSettingsSample by lazy { loadText("/samples/Cwt.colorSettings.txt") }
    val cwtCodeStyleSettingsSample by lazy { loadText("/samples/Cwt.codeStyleSettings.txt") }

    val paradoxLocalisationColorSettingsSample by lazy { loadText("/samples/ParadoxLocalisation.colorSettings.txt") }
    val paradoxLocalisationCodeStyleSettingsSample by lazy { loadText("/samples/ParadoxLocalisation.codeStyleSettings.txt") }

    val paradoxScriptColorSettingsSample by lazy { loadText("/samples/ParadoxScript.colorSettings.txt") }
    val paradoxScriptCodeStyleSettingsSample by lazy { loadText("/samples/ParadoxScript.codeStyleSettings.txt") }

    val paradoxCsvColorSettingsSample by lazy { loadText("/samples/ParadoxCsv.colorSettings.txt") }
    val paradoxCsvCodeStyleSettingsSample by lazy { loadText("/samples/ParadoxCsv.codeStyleSettings.txt") }
}
