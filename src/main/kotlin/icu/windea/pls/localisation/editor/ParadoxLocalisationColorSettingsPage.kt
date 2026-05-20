package icu.windea.pls.localisation.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.localisation.ParadoxLocalisationConstants
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

class ParadoxLocalisationColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(PlsBundle.message("localisation.displayName.operator"), ParadoxLocalisationHighlighterColors.OPERATOR),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.marker"), ParadoxLocalisationHighlighterColors.MARKER),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.comment"), ParadoxLocalisationHighlighterColors.COMMENT),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.keyword"), ParadoxLocalisationHighlighterColors.KEYWORD),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.locale"), ParadoxLocalisationHighlighterColors.LOCALE),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.number"), ParadoxLocalisationHighlighterColors.NUMBER),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.propertyKey"), ParadoxLocalisationHighlighterColors.PROPERTY_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.atSign"), ParadoxLocalisationHighlighterColors.AT_SIGN),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.scriptedVariableReference"), ParadoxLocalisationHighlighterColors.SCRIPTED_VARIABLE_REFERENCE),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.parameter"), ParadoxLocalisationHighlighterColors.PARAMETER),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.argument"), ParadoxLocalisationHighlighterColors.ARGUMENT),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.text"), ParadoxLocalisationHighlighterColors.TEXT),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.color"), ParadoxLocalisationHighlighterColors.COLOR),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.icon"), ParadoxLocalisationHighlighterColors.ICON),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.command"), ParadoxLocalisationHighlighterColors.COMMAND),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.concept"), ParadoxLocalisationHighlighterColors.CONCEPT), // #008080
        AttributesDescriptor(PlsBundle.message("localisation.displayName.textIcon"), ParadoxLocalisationHighlighterColors.TEXT_ICON),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.textFormat"), ParadoxLocalisationHighlighterColors.TEXT_FORMAT),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.validEscape"), ParadoxLocalisationHighlighterColors.VALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.invalidEscape"), ParadoxLocalisationHighlighterColors.INVALID_ESCAPE),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.badCharacter"), ParadoxLocalisationHighlighterColors.BAD_CHARACTER),

        AttributesDescriptor(PlsBundle.message("semantic.displayName.operator"), ParadoxLocalisationHighlighterColors.SEMANTIC_OPERATOR),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.marker"), ParadoxLocalisationHighlighterColors.SEMANTIC_MARKER),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.keyword"), ParadoxLocalisationHighlighterColors.SEMANTIC_KEYWORD),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.string"), ParadoxLocalisationHighlighterColors.SEMANTIC_STRING),

        AttributesDescriptor(PlsBundle.message("semantic.displayName.definitionReference"), ParadoxLocalisationHighlighterColors.DEFINITION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.localisationReference"), ParadoxLocalisationHighlighterColors.LOCALISATION_REFERENCE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.dynamicValue"), ParadoxLocalisationHighlighterColors.DYNAMIC_VALUE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.variable"), ParadoxLocalisationHighlighterColors.VARIABLE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.systemCommandScope"), ParadoxLocalisationHighlighterColors.SYSTEM_COMMAND_SCOPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandScope"), ParadoxLocalisationHighlighterColors.COMMAND_SCOPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandScopePrefix"), ParadoxLocalisationHighlighterColors.COMMAND_SCOPE_PREFIX),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandField"), ParadoxLocalisationHighlighterColors.COMMAND_FIELD),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.commandFieldPrefix"), ParadoxLocalisationHighlighterColors.COMMAND_FIELD_PREFIX),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.databaseObjectType"), ParadoxLocalisationHighlighterColors.DATABASE_OBJECT_TYPE),
        AttributesDescriptor(PlsBundle.message("semantic.displayName.databaseObject"), ParadoxLocalisationHighlighterColors.DATABASE_OBJECT),

    )

    private val _tagToDescriptorMap = mapOf(
        "OPERATOR" to ParadoxLocalisationHighlighterColors.SEMANTIC_OPERATOR,
        "MARKER" to ParadoxLocalisationHighlighterColors.SEMANTIC_MARKER,
        "KEYWORD" to ParadoxLocalisationHighlighterColors.SEMANTIC_KEYWORD,
        "STRING" to ParadoxLocalisationHighlighterColors.SEMANTIC_STRING,

        "DEFINITION_REFERENCE" to ParadoxLocalisationHighlighterColors.DEFINITION_REFERENCE,
        "LOCALISATION_REFERENCE" to ParadoxLocalisationHighlighterColors.LOCALISATION_REFERENCE,
        "DYNAMIC_VALUE" to ParadoxLocalisationHighlighterColors.DYNAMIC_VALUE,
        "VARIABLE" to ParadoxLocalisationHighlighterColors.VARIABLE,
        "SYSTEM_COMMAND_SCOPE" to ParadoxLocalisationHighlighterColors.SYSTEM_COMMAND_SCOPE,
        "COMMAND_SCOPE" to ParadoxLocalisationHighlighterColors.COMMAND_SCOPE,
        "COMMAND_SCOPE_PREFIX" to ParadoxLocalisationHighlighterColors.COMMAND_SCOPE_PREFIX,
        "COMMAND_FIELD" to ParadoxLocalisationHighlighterColors.COMMAND_FIELD,
        "COMMAND_FIELD_PREFIX" to ParadoxLocalisationHighlighterColors.COMMAND_FIELD_PREFIX,
        "DATABASE_OBJECT_TYPE" to ParadoxLocalisationHighlighterColors.DATABASE_OBJECT_TYPE,
        "DATABASE_OBJECT" to ParadoxLocalisationHighlighterColors.DATABASE_OBJECT,
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxLocalisationLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap

    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("localisation.settings.name")

    override fun getDemoText() = ParadoxLocalisationConstants.colorSettingsText
}
