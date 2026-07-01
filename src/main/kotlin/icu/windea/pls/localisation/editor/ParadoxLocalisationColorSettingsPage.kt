package icu.windea.pls.localisation.editor

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.localisation.ParadoxLocalisationConstants
import icu.windea.pls.localisation.ParadoxLocalisationLanguage

class ParadoxLocalisationColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.operator"), ParadoxLocalisationHighlighterColors.OPERATOR),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.marker"), ParadoxLocalisationHighlighterColors.MARKER),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.comment"), ParadoxLocalisationHighlighterColors.COMMENT),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.keyword"), ParadoxLocalisationHighlighterColors.KEYWORD),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.locale"), ParadoxLocalisationHighlighterColors.LOCALE),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.number"), ParadoxLocalisationHighlighterColors.NUMBER),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.propertyKey"), ParadoxLocalisationHighlighterColors.PROPERTY_KEY),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.atSign"), ParadoxLocalisationHighlighterColors.AT_SIGN),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.scriptedVariableReference"), ParadoxLocalisationHighlighterColors.SCRIPTED_VARIABLE_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.parameter"), ParadoxLocalisationHighlighterColors.PARAMETER),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.argument"), ParadoxLocalisationHighlighterColors.ARGUMENT),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.text"), ParadoxLocalisationHighlighterColors.TEXT),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.color"), ParadoxLocalisationHighlighterColors.COLOR),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.icon"), ParadoxLocalisationHighlighterColors.ICON),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.command"), ParadoxLocalisationHighlighterColors.COMMAND),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.concept"), ParadoxLocalisationHighlighterColors.CONCEPT), // #008080
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.textIcon"), ParadoxLocalisationHighlighterColors.TEXT_ICON),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.textFormat"), ParadoxLocalisationHighlighterColors.TEXT_FORMAT),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.validEscape"), ParadoxLocalisationHighlighterColors.VALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.invalidEscape"), ParadoxLocalisationHighlighterColors.INVALID_ESCAPE),
        AttributesDescriptor(ChronicleBundle.message("localisation.displayName.badCharacter"), ParadoxLocalisationHighlighterColors.BAD_CHARACTER),

        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.operator"), ParadoxLocalisationHighlighterColors.SEMANTIC_OPERATOR),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.marker"), ParadoxLocalisationHighlighterColors.SEMANTIC_MARKER),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.keyword"), ParadoxLocalisationHighlighterColors.SEMANTIC_KEYWORD),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.string"), ParadoxLocalisationHighlighterColors.SEMANTIC_STRING),

        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.definitionReference"), ParadoxLocalisationHighlighterColors.DEFINITION_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.localisationReference"), ParadoxLocalisationHighlighterColors.LOCALISATION_REFERENCE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.dynamicValue"), ParadoxLocalisationHighlighterColors.DYNAMIC_VALUE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.variable"), ParadoxLocalisationHighlighterColors.VARIABLE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.systemCommandScope"), ParadoxLocalisationHighlighterColors.SYSTEM_COMMAND_SCOPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.commandScope"), ParadoxLocalisationHighlighterColors.COMMAND_SCOPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.commandScopePrefix"), ParadoxLocalisationHighlighterColors.COMMAND_SCOPE_PREFIX),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.commandField"), ParadoxLocalisationHighlighterColors.COMMAND_FIELD),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.commandFieldPrefix"), ParadoxLocalisationHighlighterColors.COMMAND_FIELD_PREFIX),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.databaseObjectType"), ParadoxLocalisationHighlighterColors.DATABASE_OBJECT_TYPE),
        AttributesDescriptor(ChronicleBundle.message("semantic.displayName.databaseObject"), ParadoxLocalisationHighlighterColors.DATABASE_OBJECT),

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

    override fun getDisplayName() = ChronicleBundle.message("localisation.settings.name")

    override fun getDemoText() = ParadoxLocalisationConstants.colorSettingsText
}
