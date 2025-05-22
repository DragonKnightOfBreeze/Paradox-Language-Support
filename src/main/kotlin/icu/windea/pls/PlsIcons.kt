package icu.windea.pls

import com.intellij.icons.*
import com.intellij.ui.*
import javax.swing.*

object PlsIcons {
    private val colorWhite = ColorUtil.fromHex("#9AA7B0")
    private val colorBlue = ColorUtil.fromHex("#5D8DC3")

    private fun IconManager.getIcon(path: String): Icon = getIcon(path, PlsIcons.javaClass.classLoader)

    object General {
        @JvmField
        val GameDirectory = AllIcons.Modules.SourceRoot
        @JvmField
        val ModDirectory = AllIcons.Modules.SourceRoot
        @JvmField
        val ConfigGroupDirectory = AllIcons.Modules.ResourcesRoot
        @JvmField
        val Library = AllIcons.Nodes.PpLibFolder
        @JvmField
        val Image = IconManager.getInstance().getIcon("/icons/general/image.svg") // from ThumbnailToolWindow.svg
        @JvmField
        val Presentation = IconManager.getInstance().getIcon("/icons/general/presentation.svg") // from ThumbnailToolWindow.svg, fill #5D8DC3
        @JvmField
        val Steam = IconManager.getInstance().getIcon("/icons/general/steam.svg")
    }

    object FileTypes {
        @JvmField
        val Cwt = IconManager.getInstance().getIcon("/icons/fileTypes/cwt.svg")
        @JvmField
        val ParadoxScript = IconManager.getInstance().getIcon("/icons/fileTypes/paradoxScript.svg")
        @JvmField
        val ParadoxLocalisation = IconManager.getInstance().getIcon("/icons/fileTypes/paradoxLocalisation.svg")
        @JvmField
        val CwtConfig = IconManager.getInstance().getIcon("/icons/fileTypes/cwtConfig.svg") // from general/gear.svg, fill #9AA7B0
        @JvmField
        val ModeDescriptor = IconManager.getInstance().getIcon("/icons/fileTypes/modDescriptor.svg") // from general/gear.svg, fill #5D8DC3
    }

    object Nodes {
        @JvmField
        val CwtProperty = IconManager.getInstance().getIcon("/icons/nodes/cwt_property.svg")
        @JvmField
        val CwtValue = IconManager.getInstance().getIcon("/icons/nodes/cwt_value.svg")
        @JvmField
        val CwtBlock = IconManager.getInstance().getIcon("/icons/nodes/cwt_block.svg")
        @JvmField
        val CwtOption = IconManager.getInstance().getIcon("/icons/nodes/cwt_option.svg")

        @JvmField
        val ScriptedVariableConfig = IconManager.getInstance().getIcon("/icons/nodes/config_scriptedVariable.svg") // fill #9AA7B0
        @JvmField
        val DefinitionConfig = IconManager.getInstance().getIcon("/icons/nodes/config_definition.svg") // fill #9AA7B0
        @JvmField
        val InlineScriptConfig = IconManager.getInstance().getIcon("/icons/nodes/config_inlineScript.svg") // fill #9AA7B0
        @JvmField
        val ParameterConfig = IconManager.getInstance().getIcon("/icons/nodes/config_parameter.svg") // fill #9AA7B0
        @JvmField
        val DynamicValueConfig = IconManager.getInstance().getIcon("/icons/nodes/config_dynamicValue.svg") // fill #9AA7B0
        @JvmField
        val EnumValueConfig = IconManager.getInstance().getIcon("/icons/nodes/config_enumValue.svg") // fill #9AA7B0

        @JvmField
        val ScriptedVariable = IconManager.getInstance().getIcon("/icons/nodes/scriptedVariable.svg")
        @JvmField
        val Property = IconManager.getInstance().getIcon("/icons/nodes/property.svg")
        @JvmField
        val Value = IconManager.getInstance().getIcon("/icons/nodes/value.svg")
        @JvmField
        val Block = IconManager.getInstance().getIcon("/icons/nodes/block.svg")
        @JvmField
        val Parameter = IconManager.getInstance().getIcon("/icons/nodes/parameter.svg")
        @JvmField
        val ParameterCondition = IconManager.getInstance().getIcon("/icons/nodes/parameterCondition.svg")

        @JvmField
        val LocalisationLocale = IconManager.getInstance().getIcon("/icons/nodes/localisationLocale.svg")
        @JvmField
        val LocalisationProperty = IconManager.getInstance().getIcon("/icons/nodes/localisationProperty.svg")
        @JvmField
        val LocalisationIcon = IconManager.getInstance().getIcon("/icons/nodes/localisationIcon.svg")
        @JvmField
        val LocalisationCommand = IconManager.getInstance().getIcon("/icons/nodes/localisationCommand.svg")
        @JvmField
        val LocalisationCommandScope = IconManager.getInstance().getIcon("/icons/nodes/localisationCommandScope.svg")
        @JvmField
        val LocalisationCommandField = IconManager.getInstance().getIcon("/icons/nodes/localisationCommandField.svg")
        @JvmField
        val LocalisationConcept = IconManager.getInstance().getIcon("/icons/nodes/localisationConcept.svg")
        @JvmStatic
        val LocalisationTextFormat = IconManager.getInstance().getIcon("/icons/nodes/localisationTextFormat.svg")
        @JvmStatic
        val LocalisationTextIcon = IconManager.getInstance().getIcon("/icons/nodes/localisationTextIcon.svg") //same as LocalisationIcon

        @JvmField
        val Definition = IconManager.getInstance().getIcon("/icons/nodes/definition.svg")
        @JvmField
        val Localisation = IconManager.getInstance().getIcon("/icons/nodes/localisation.svg")
        @JvmField
        val Type = IconManager.getInstance().getIcon("/icons/nodes/type.svg")
        @JvmField
        val Variable = IconManager.getInstance().getIcon("/icons/nodes/variable.svg")
        @JvmField
        val DynamicValueType = IconManager.getInstance().getIcon("/icons/nodes/dynamicValueType.svg")
        @JvmField
        val DynamicValue = IconManager.getInstance().getIcon("/icons/nodes/dynamicValue.svg")
        @JvmField
        val Enum = IconManager.getInstance().getIcon("/icons/nodes/enum.svg")
        @JvmField
        val EnumValue = IconManager.getInstance().getIcon("/icons/nodes/enumValue.svg")
        @JvmField
        val Scope = IconManager.getInstance().getIcon("/icons/nodes/scope.svg")
        @JvmField
        val ScopeGroup = IconManager.getInstance().getIcon("/icons/nodes/scopeGroup.svg")
        @JvmField
        val SystemScope = IconManager.getInstance().getIcon("/icons/nodes/systemScope.svg")
        @JvmField
        val Link = IconManager.getInstance().getIcon("/icons/nodes/link.svg")
        @JvmField
        val ValueField = IconManager.getInstance().getIcon("/icons/nodes/valueField.svg")
        @JvmField
        val ModifierCategory = IconManager.getInstance().getIcon("/icons/nodes/modifierCategory.svg")
        @JvmField
        val Modifier = IconManager.getInstance().getIcon("/icons/nodes/modifier.svg")
        @JvmField
        val Trigger = IconManager.getInstance().getIcon("/icons/nodes/trigger.svg")
        @JvmField
        val Effect = IconManager.getInstance().getIcon("/icons/nodes/effect.svg")
        @JvmField
        val Tag = IconManager.getInstance().getIcon("/icons/nodes/tag.svg")
        @JvmField
        val TemplateExpression = IconManager.getInstance().getIcon("icons/nodes/templateExpression.svg")
        @JvmField
        val Alias = IconManager.getInstance().getIcon("/icons/nodes/alias.svg")
        @JvmField
        val EventNamespace = IconManager.getInstance().getIcon("/icons/nodes/eventNamespace.svg")
        @JvmField
        val EventId = IconManager.getInstance().getIcon("/icons/nodes/eventId.svg")
        @JvmField
        val PathReference = AllIcons.FileTypes.Any_type
        @JvmField
        val InlineScript = IconManager.getInstance().getIcon("/icons/nodes/inlineScript.svg")
        @JvmField
        val DatabaseObjectType = IconManager.getInstance().getIcon("/icons/nodes/databaseObjectType.svg")
        @JvmField
        val DefineNamespace = IconManager.getInstance().getIcon("/icons/nodes/defineNamespace.svg")
        @JvmField
        val DefineVariable = IconManager.getInstance().getIcon("/icons/nodes/defineVariable.svg")

        @JvmField
        val DefinitionGroup = IconManager.getInstance().getIcon("/icons/nodes/definitionGroup.svg")

        @JvmStatic
        fun Definition(type: String?) = when (type) {
            "event" -> EventId
            "event_namespace" -> EventNamespace
            else -> Definition
        }

        @JvmStatic
        fun DynamicValue(dynamicValueType: String?) = when (dynamicValueType) {
            "variable" -> Variable
            else -> DynamicValue
        }
    }

    object Gutter {
        @JvmField
        val ScriptedVariable = IconManager.getInstance().getIcon("/icons/gutter/scriptedVariable.svg")
        @JvmField
        val Definition = IconManager.getInstance().getIcon("/icons/gutter/definition.svg")
        @JvmField
        val Localisation = IconManager.getInstance().getIcon("/icons/gutter/localisation.svg")
        @JvmField
        val RelatedDefinitions = IconManager.getInstance().getIcon("/icons/gutter/relatedDefinitions.svg")
        @JvmField
        val RelatedLocalisations = IconManager.getInstance().getIcon("/icons/gutter/relatedLocalisations.svg")
        @JvmField
        val RelatedImages = IconManager.getInstance().getIcon("/icons/gutter/relatedImages.svg")
    }

    object Actions {
        @JvmField
        val GameSettings = IconManager.getInstance().getIcon("/icons/actions/gameSettings.svg") // from general/gear.svg, fill #5D8DC3
        @JvmField
        val ModSettings = IconManager.getInstance().getIcon("/icons/actions/modSettings.svg") // from general/gear.svg, fill #5D8DC3
        @JvmField
        val RefreshConfigGroups = IconManager.getInstance().getIcon("/icons/actions/refreshConfigGroups.svg") // from icons/refresh.svg, fill #5D8DC3
    }
}
