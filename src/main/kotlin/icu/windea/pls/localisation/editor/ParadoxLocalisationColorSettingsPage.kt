package icu.windea.pls.localisation.editor

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*

class ParadoxLocalisationColorSettingsPage : ColorSettingsPage {
    private val _attributesDescriptors = arrayOf(
        AttributesDescriptor(PlsBundle.message("localisation.displayName.operator"), ParadoxLocalisationAttributesKeys.OPERATOR_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.marker"), ParadoxLocalisationAttributesKeys.MARKER_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.comment"), ParadoxLocalisationAttributesKeys.COMMENT_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.number"), ParadoxLocalisationAttributesKeys.NUMBER_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.locale"), ParadoxLocalisationAttributesKeys.LOCALE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.propertyKey"), ParadoxLocalisationAttributesKeys.PROPERTY_KEY_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.color"), ParadoxLocalisationAttributesKeys.COLOR_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.propertyReference"), ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.propertyReferenceArgument"), ParadoxLocalisationAttributesKeys.PROPERTY_REFERENCE_ARGUMENT_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.scriptedVariable"), ParadoxLocalisationAttributesKeys.SCRIPTED_VARIABLE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.command"), ParadoxLocalisationAttributesKeys.COMMAND_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.commandArgument"), ParadoxLocalisationAttributesKeys.COMMAND_ARGUMENT_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.icon"), ParadoxLocalisationAttributesKeys.ICON_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.concept"), ParadoxLocalisationAttributesKeys.CONCEPT_KEY), //#008080
        AttributesDescriptor(PlsBundle.message("localisation.displayName.string"), ParadoxLocalisationAttributesKeys.STRING_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.validEscape"), ParadoxLocalisationAttributesKeys.VALID_ESCAPE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.invalidEscape"), ParadoxLocalisationAttributesKeys.INVALID_ESCAPE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.badCharacter"), ParadoxLocalisationAttributesKeys.BAD_CHARACTER_KEY),

        //unused
        //AttributesDescriptor(PlsBundle.message("localisation.displayName.localisation"), ParadoxLocalisationAttributesKeys.LOCALISATION_KEY),
        //AttributesDescriptor(PlsBundle.message("localisation.displayName.syncedLocalisation"), ParadoxLocalisationAttributesKeys.SYNCED_LOCALISATION_KEY),

        AttributesDescriptor(PlsBundle.message("localisation.displayName.systemCommandScope"), ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.commandScope"), ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.commandScopeLinkPrefix"), ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_LINK_PREFIX_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.commandScopeLinkValue"), ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_LINK_VALUE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.commandField"), ParadoxLocalisationAttributesKeys.COMMAND_FIELD_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.dynamicValue"), ParadoxLocalisationAttributesKeys.DYNAMIC_VALUE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.variable"), ParadoxLocalisationAttributesKeys.VARIABLE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.scriptedLoc"), ParadoxLocalisationAttributesKeys.SCRIPTED_LOC_KEY),

        AttributesDescriptor(PlsBundle.message("localisation.displayName.databaseObjectType"), ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_TYPE_KEY),
        AttributesDescriptor(PlsBundle.message("localisation.displayName.databaseObject"), ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_KEY),
    )

    private val _tagToDescriptorMap = mapOf(
        "MARKER" to ParadoxLocalisationAttributesKeys.MARKER_KEY,
        "OPERATOR" to ParadoxLocalisationAttributesKeys.OPERATOR_KEY,
        "SYSTEM_COMMAND_SCOPE" to ParadoxLocalisationAttributesKeys.SYSTEM_COMMAND_SCOPE_KEY,
        "COMMAND_SCOPE" to ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_KEY,
        "COMMAND_SCOPE_LINK_PREFIX" to ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_LINK_PREFIX_KEY,
        "COMMAND_SCOPE_LINK_VALUE" to ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_LINK_VALUE_KEY,
        "COMMAND_FIELD" to ParadoxLocalisationAttributesKeys.COMMAND_FIELD_KEY,
        "DYNAMIC_VALUE" to ParadoxLocalisationAttributesKeys.DYNAMIC_VALUE_KEY,
        "VARIABLE" to ParadoxLocalisationAttributesKeys.VARIABLE_KEY,
        "SCRIPTED_LOC" to ParadoxLocalisationAttributesKeys.SCRIPTED_LOC_KEY,
        "DATABASE_OBJECT_TYPE" to ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_TYPE_KEY,
        "DATABASE_OBJECT" to ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_KEY,
    )

    override fun getHighlighter() = SyntaxHighlighterFactory.getSyntaxHighlighter(ParadoxLocalisationLanguage, null, null)

    override fun getAdditionalHighlightingTagToDescriptorMap() = _tagToDescriptorMap

    override fun getIcon() = PlsIcons.FileTypes.ParadoxLocalisation

    override fun getAttributeDescriptors() = _attributesDescriptors

    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName() = PlsBundle.message("language.name.localisation")

    override fun getDemoText() = PlsConstants.Samples.paradoxLocalisationColorSettings
}
