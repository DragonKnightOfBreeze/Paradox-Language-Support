package icu.windea.pls.localisation.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.model.constants.PlsPreviewTexts
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxLocalisationColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(PlsBundle.message("localisation.displayName.operator"), ParadoxLocalisationAttributesKeys.OPERATOR),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.marker"), ParadoxLocalisationAttributesKeys.MARKER),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.comment"), ParadoxLocalisationAttributesKeys.COMMENT),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.keyword"), ParadoxLocalisationAttributesKeys.KEYWORD),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.locale"), ParadoxLocalisationAttributesKeys.LOCALE),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.number"), ParadoxLocalisationAttributesKeys.NUMBER),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.propertyKey"), ParadoxLocalisationAttributesKeys.PROPERTY_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.atSign"), ParadoxLocalisationAttributesKeys.AT_SIGN),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.scriptedVariableReference"), ParadoxLocalisationAttributesKeys.SCRIPTED_VARIABLE_REFERENCE),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.parameter"), ParadoxLocalisationAttributesKeys.PARAMETER),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.argument"), ParadoxLocalisationAttributesKeys.ARGUMENT),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.text"), ParadoxLocalisationAttributesKeys.TEXT),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.color"), ParadoxLocalisationAttributesKeys.COLOR),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.icon"), ParadoxLocalisationAttributesKeys.ICON),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.command"), ParadoxLocalisationAttributesKeys.COMMAND),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.concept"), ParadoxLocalisationAttributesKeys.CONCEPT), // #008080
        AttributesDescriptor(PlsBundle.message("localisation.displayName.textIcon"), ParadoxLocalisationAttributesKeys.TEXT_ICON),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.textFormat"), ParadoxLocalisationAttributesKeys.TEXT_FORMAT),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.validEscape"), ParadoxLocalisationAttributesKeys.VALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.invalidEscape"), ParadoxLocalisationAttributesKeys.INVALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.badCharacter"), ParadoxLocalisationAttributesKeys.BAD_CHARACTER),

        AttributesDescriptor(PlsBundle.message("semantic.displayName.operator"), ParadoxLocalisationAttributesKeys.SEMANTIC_OPERATOR),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.marker"), ParadoxLocalisationAttributesKeys.SEMANTIC_MARKER),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.keyword"), ParadoxLocalisationAttributesKeys.SEMANTIC_KEYWORD),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.string"), ParadoxLocalisationAttributesKeys.SEMANTIC_STRING),

        AttributesDescriptor(PlsBundle.message("semantic.displayName.definitionReference"), ParadoxLocalisationAttributesKeys.DEFINITION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.localisationReference"), ParadoxLocalisationAttributesKeys.LOCALISATION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.dynamicValue"), ParadoxLocalisationAttributesKeys.DYNAMIC_VALUE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.variable"), ParadoxLocalisationAttributesKeys.VARIABLE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.systemCommandScope"), ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandScope"), ParadoxLocalisationAttributesKeys.COMMAND_SCOPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandScopePrefix"), ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_PREFIX),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandField"), ParadoxLocalisationAttributesKeys.COMMAND_FIELD),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandFieldPrefix"), ParadoxLocalisationAttributesKeys.COMMAND_FIELD_PREFIX),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.databaseObjectType"), ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_TYPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.databaseObject"), ParadoxLocalisationAttributesKeys.DATABASE_OBJECT),

    )

    private val _tagToDescriptorMap = mapOf(
        "OPERATOR" to ParadoxScriptAttributesKeys.SEMANTIC_OPERATOR,
        "MARKER" to ParadoxScriptAttributesKeys.SEMANTIC_MARKER,
        "KEYWORD" to ParadoxScriptAttributesKeys.SEMANTIC_KEYWORD,
        "STRING" to ParadoxScriptAttributesKeys.SEMANTIC_STRING,

        "DEFINITION_REFERENCE" to ParadoxLocalisationAttributesKeys.DEFINITION_REFERENCE,
        "LOCALISATION_REFERENCE" to ParadoxLocalisationAttributesKeys.LOCALISATION_REFERENCE,
        "DYNAMIC_VALUE" to ParadoxLocalisationAttributesKeys.DYNAMIC_VALUE,
        "VARIABLE" to ParadoxLocalisationAttributesKeys.VARIABLE,
        "SYSTEM_COMMAND_SCOPE" to ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE,
        "COMMAND_SCOPE" to ParadoxLocalisationAttributesKeys.COMMAND_SCOPE,
        "COMMAND_SCOPE_PREFIX" to ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_PREFIX,
        "COMMAND_FIELD" to ParadoxLocalisationAttributesKeys.COMMAND_FIELD,
        "COMMAND_FIELD_PREFIX" to ParadoxLocalisationAttributesKeys.COMMAND_FIELD_PREFIX,
        "DATABASE_OBJECT_TYPE" to ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_TYPE,
        "DATABASE_OBJECT" to ParadoxLocalisationAttributesKeys.DATABASE_OBJECT,
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxLocalisationLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap

    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("localisation.settings.name")

    override fun getDemoText() = PlsPreviewTexts.localisationColorSettings
}
