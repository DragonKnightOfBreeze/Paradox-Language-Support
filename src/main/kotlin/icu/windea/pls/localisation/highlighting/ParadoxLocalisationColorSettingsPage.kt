package icu.windea.pls.localisation.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.localisation.ParadoxLocalisationConstants
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

class ParadoxLocalisationColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.operator"), ParadoxLocalisationHighlighterColors.OPERATOR),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.marker"), ParadoxLocalisationHighlighterColors.MARKER),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.comment"), ParadoxLocalisationHighlighterColors.COMMENT),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.keyword"), ParadoxLocalisationHighlighterColors.KEYWORD),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.locale"), ParadoxLocalisationHighlighterColors.LOCALE),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.number"), ParadoxLocalisationHighlighterColors.NUMBER),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.propertyKey"), ParadoxLocalisationHighlighterColors.PROPERTY_KEY),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.atSign"), ParadoxLocalisationHighlighterColors.AT_SIGN),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.scriptedVariableReference"), ParadoxLocalisationHighlighterColors.SCRIPTED_VARIABLE_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.parameter"), ParadoxLocalisationHighlighterColors.PARAMETER),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.argument"), ParadoxLocalisationHighlighterColors.ARGUMENT),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.text"), ParadoxLocalisationHighlighterColors.TEXT),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.color"), ParadoxLocalisationHighlighterColors.COLOR),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.icon"), ParadoxLocalisationHighlighterColors.ICON),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.command"), ParadoxLocalisationHighlighterColors.COMMAND),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.concept"), ParadoxLocalisationHighlighterColors.CONCEPT), // #008080
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.textIcon"), ParadoxLocalisationHighlighterColors.TEXT_ICON),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.textFormat"), ParadoxLocalisationHighlighterColors.TEXT_FORMAT),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.tag"), ParadoxLocalisationHighlighterColors.TAG),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.contextTag"), ParadoxLocalisationHighlighterColors.CONTEXT_TAG),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.validEscape"), ParadoxLocalisationHighlighterColors.VALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.invalidEscape"), ParadoxLocalisationHighlighterColors.INVALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("localisation.color.option.badCharacter"), ParadoxLocalisationHighlighterColors.BAD_CHARACTER),

        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.operator"), ParadoxLocalisationHighlighterColors.SEMANTIC_OPERATOR),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.marker"), ParadoxLocalisationHighlighterColors.SEMANTIC_MARKER),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.keyword"), ParadoxLocalisationHighlighterColors.SEMANTIC_KEYWORD),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.string"), ParadoxLocalisationHighlighterColors.SEMANTIC_STRING),

        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.definitionReference"), ParadoxLocalisationHighlighterColors.DEFINITION_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.localisationReference"), ParadoxLocalisationHighlighterColors.LOCALISATION_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.dynamicValue"), ParadoxLocalisationHighlighterColors.DYNAMIC_VALUE),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.variable"), ParadoxLocalisationHighlighterColors.VARIABLE),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.systemCommandScope"), ParadoxLocalisationHighlighterColors.SYSTEM_COMMAND_SCOPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.commandScope"), ParadoxLocalisationHighlighterColors.COMMAND_SCOPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.commandScopePrefix"), ParadoxLocalisationHighlighterColors.COMMAND_SCOPE_PREFIX),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.commandField"), ParadoxLocalisationHighlighterColors.COMMAND_FIELD),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.commandFieldPrefix"), ParadoxLocalisationHighlighterColors.COMMAND_FIELD_PREFIX),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.databaseObjectType"), ParadoxLocalisationHighlighterColors.DATABASE_OBJECT_TYPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.color.option.databaseObject"), ParadoxLocalisationHighlighterColors.DATABASE_OBJECT),
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

    override fun getIcon() = ChronicleIcons.FileTypes.ParadoxLocalisation

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = ChronicleBundle.message("localisation.color.settings.displayName")

    override fun getDemoText() = ParadoxLocalisationConstants.colorSettingsText
}
