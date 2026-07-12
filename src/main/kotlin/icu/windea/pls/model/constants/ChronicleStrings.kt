package icu.windea.pls.model.constants

@Suppress("unused")
object ChronicleStrings {
    const val optionPrefix = "(option)"
    const val optionFlagPrefix = "(option flag)"
    const val complexEnumValuePrefix = "(complex enum value)"
    const val dynamicValuePrefix = "(dynamic value)"
    const val modifierPrefix = "(modifier)"
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
    const val defineNamespacePrefix = "(define namespace)"
    const val defineVariablePrefix = "(define variable)"
    const val propertyPrefix = "(property)"
    const val stringPrefix = "(string)"
    const val sourcePropertyPrefix = "(source property)"
    const val sourceStringPrefix = "(source string)"
    const val sourcePrefix = "(source)"
    const val localePrefix = "(locale)"
    const val localisationPrefix = "(localisation)"
    const val localisationSyncedPrefix = "(synced localisation)"
    const val localisationPropertyPrefix = "(localisation property)"
    const val localisationColorPrefix = "(localisation color)"
    const val localisationIconPrefix = "(localisation icon)"
    const val inlineScriptPrefix = "(inline script)"
    const val definitionInjectionPrefix = "(definition injection)"
    const val definitionInjectionTargetPrefix = "(definition injection target)"
    const val shaderEffectPrefix = "(shader effect)"
    const val meshLocatorPrefix = "(mesh locator)"

    const val headerText = "<header>"
    const val rowText = "<row>"

    const val complexText = "<complex>"
    const val inlinedText = "<inlined>"
    const val parameterizedText = "<parameterized>"
    const val dynamicText = "<dynamic>"
    const val noParametersText = "<no parameters>"

    const val parameterMarker = '$'
    const val colorMarker = '§'
    const val iconMarker = '£'

    const val parameterStartMarker = "$"
    const val parameterEndMarker = "$"
    const val commandStartMarker = "["
    const val commandEndMarker = "]"
    const val colorStartMarker = "§"
    const val colorEndMarker = "§!"
    const val iconStartMarker = "£"
    const val iconEndMarker = "£"
    const val textFormatStartMarker = "#"
    const val textFormatEndMarker = "#!"
    const val textIconStartMarker = "@"
    const val textIconEndMarker = "!"

    const val commentFolder = "# ..."
    const val quotedFolder = "\"...\""
    const val blockFolder = "{...}"
    const val inlineMathFolder = "@[...]"
    val conditionalBlockFolder = { expression: String -> "[[$expression]...]" }
    const val commandFolder = "[...]"
    const val conceptCommandFolder = "['...']"
    const val conceptCommandWithTextFolder = "['...', ...]"
}
