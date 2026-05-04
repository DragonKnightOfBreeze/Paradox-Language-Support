package icu.windea.pls

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import com.intellij.platform.images.icons.PlatformImagesIcons
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import javax.swing.Icon

@Suppress("unused")
object PlsIcons {
    private fun getIcon(path: String): Icon = IconLoader.getIcon(path, PlsIcons.javaClass.classLoader)

    object General {
        @JvmField val GameDirectory = AllIcons.Nodes.Module
        @JvmField val ModDirectory = AllIcons.Modules.SourceRoot
        @JvmField val EntryDirectory = AllIcons.Modules.SourceRoot
        @JvmField val ConfigGroupDirectory = AllIcons.Modules.EditFolder
        @JvmField val Library = AllIcons.Nodes.PpLibFolder
        @JvmField val Presentation = PlatformImagesIcons.ThumbnailToolWindow

        @JvmField val Steam = getIcon("/icons/steam.svg")
    }

    object FileTypes {
        @JvmField val Cwt = getIcon("/icons/fileTypes/cwt.svg")
        @JvmField val ParadoxScript = getIcon("/icons/fileTypes/paradoxScript.svg")
        @JvmField val ParadoxLocalisation = getIcon("/icons/fileTypes/paradoxLocalisation.svg")
        @JvmField val ParadoxCsv = AllIcons.FileTypes.Csv

        @JvmField val CwtConfig = getIcon("/icons/fileTypes/cwtConfig.svg") // from general/gear.svg, fill #9AA7B0
        @JvmField val ModDescriptor = getIcon("/icons/fileTypes/modDescriptor.svg") // from general/gear.svg, fill #5D8DC3
    }

    object Nodes {
        @JvmField val Option = getIcon("/icons/nodes/option.svg")
        @JvmField val ScriptedVariable = getIcon("/icons/nodes/scriptedVariable.svg")
        @JvmField val Property = getIcon("/icons/nodes/property.svg")
        @JvmField val Value = getIcon("/icons/nodes/value.svg")
        @JvmField val Block = getIcon("/icons/nodes/block.svg")
        @JvmField val Parameter = getIcon("/icons/nodes/parameter.svg")
        @JvmField val ParameterCondition = getIcon("/icons/nodes/parameterCondition.svg")
        @JvmField val LocalisationLocale = getIcon("/icons/nodes/localisationLocale.svg")
        @JvmField val LocalisationProperty = getIcon("/icons/nodes/localisationProperty.svg")
        @JvmField val LocalisationIcon = getIcon("/icons/nodes/localisationIcon.svg")
        @JvmField val LocalisationCommand = getIcon("/icons/nodes/localisationCommand.svg")
        @JvmField val LocalisationConceptCommand = getIcon("/icons/nodes/localisationConceptCommand.svg")
        @JvmField val LocalisationTextFormat = getIcon("/icons/nodes/localisationTextFormat.svg")
        @JvmField val LocalisationTextIcon = getIcon("/icons/nodes/localisationTextIcon.svg")
        @JvmField val Row = AllIcons.Nodes.DataTables
        @JvmField val Column = AllIcons.Nodes.DataColumn

        @JvmField val Type = getIcon("/icons/nodes/semantic/type.svg")
        @JvmField val DefinitionGroup = getIcon("icons/nodes/semantic/definitionGroup.svg")
        @JvmField val Definition = getIcon("/icons/nodes/semantic/definition.svg")
        @JvmField val LocalisationGroup = getIcon("/icons/nodes/semantic/localisationGroup.svg")
        @JvmField val Localisation = getIcon("/icons/nodes/semantic/localisation.svg")
        @JvmField val DefineNamespace = getIcon("/icons/nodes/semantic/defineNamespace.svg")
        @JvmField val DefineVariable = getIcon("/icons/nodes/semantic/defineVariable.svg")
        @JvmField val EnumValue = getIcon("/icons/nodes/semantic/enumValue.svg")
        @JvmField val DynamicValue = getIcon("/icons/nodes/semantic/dynamicValue.svg")
        @JvmField val Variable = getIcon("/icons/nodes/semantic/variable.svg")
        @JvmField val Modifier = getIcon("/icons/nodes/semantic/modifier.svg")
        @JvmField val Trigger = getIcon("/icons/nodes/semantic/trigger.svg")
        @JvmField val Effect = getIcon("/icons/nodes/semantic/effect.svg")
        @JvmField val SystemScope = getIcon("/icons/nodes/semantic/systemScope.svg")
        @JvmField val StaticScope = getIcon("/icons/nodes/semantic/staticScope.svg")
        @JvmField val DynamicScope = getIcon("/icons/nodes/semantic/dynamicScope.svg")
        @JvmField val StaticValueField = getIcon("/icons/nodes/semantic/staticValueField.svg")
        @JvmField val DynamicValueField = getIcon("/icons/nodes/semantic/dynamicValueField.svg")
        @JvmField val SystemCommandScope = getIcon("/icons/nodes/semantic/systemCommandScope.svg")
        @JvmField val StaticCommandScope = getIcon("/icons/nodes/semantic/staticCommandScope.svg")
        @JvmField val DynamicCommandScope = getIcon("/icons/nodes/semantic/dynamicCommandScope.svg")
        @JvmField val StaticCommandField = getIcon("/icons/nodes/semantic/staticCommandField.svg")
        @JvmField val DynamicCommandField = getIcon("/icons/nodes/semantic/dynamicCommandField.svg")
        @JvmField val DatabaseObjectType = getIcon("/icons/nodes/semantic/databaseObjectType.svg")
        @JvmField val Tag = getIcon("/icons/nodes/semantic/tag.svg")
        @JvmField val Directive = getIcon("/icons/nodes/semantic/directive.svg")
        @JvmField val PathReference = AllIcons.FileTypes.Any_type

        @JvmField val EventNamespace = getIcon("/icons/nodes/semantic/eventNamespace.svg")
        @JvmField val Event = getIcon("/icons/nodes/semantic/event.svg")

        @JvmStatic
        fun Definition(type: String?) = when (type) {
            ParadoxDefinitionTypes.eventNamespace -> EventNamespace
            ParadoxDefinitionTypes.event -> Event
            else -> Definition
        }

        @JvmStatic
        fun ComplexEnumValue(type: String?) = EnumValue

        @JvmStatic
        fun DynamicValue(dynamicValueType: String?) = when (dynamicValueType) {
            "variable" -> Variable
            else -> DynamicValue
        }

        @JvmStatic
        fun PathReference(pathExpression: CwtDataExpression?) = when (pathExpression) {
            ParadoxInlineScriptManager.inlineScriptPathExpression -> Directive
            else -> PathReference
        }
    }

    object Configs {
        @JvmField val Type = getIcon("/icons/configs/type.svg")
        @JvmField val Row = getIcon("/icons/configs/row.svg")
        @JvmField val DefineNamespace = getIcon("/icons/configs/defineNamespace.svg")
        @JvmField val DefineVariable = getIcon("/icons/configs/defineVariable.svg")
        @JvmField val Enum = getIcon("/icons/configs/enum.svg")
        @JvmField val EnumValue = getIcon("/icons/configs/enumValue.svg")
        @JvmField val ComplexEnum = getIcon("/icons/configs/complexEnum.svg")
        @JvmField val DynamicValueType = getIcon("/icons/configs/dynamicValueType.svg")
        @JvmField val DynamicValue = getIcon("/icons/configs/dynamicValue.svg")
        @JvmField val Alias = getIcon("/icons/configs/alias.svg")
        @JvmField val Directive = getIcon("/icons/configs/directive.svg")
        @JvmField val Link = getIcon("/icons/configs/link.svg")
        @JvmField val LocalisationPromotion = getIcon("/icons/configs/localisationPromotion.svg")
        @JvmField val LocalisationCommand = getIcon("/icons/configs/localisationCommand.svg")
        @JvmField val ModifierCategory = getIcon("/icons/configs/modifierCategory.svg")
        @JvmField val Modifier = getIcon("/icons/configs/modifier.svg")
        @JvmField val Trigger = getIcon("/icons/configs/trigger.svg")
        @JvmField val Effect = getIcon("/icons/configs/effect.svg")
        @JvmField val Scope = getIcon("/icons/configs/scope.svg")
        @JvmField val ScopeGroup = getIcon("/icons/configs/scopeGroup.svg")
        @JvmField val DatabaseObjectType = getIcon("/icons/configs/databaseObjectType.svg")
        @JvmField val SystemScope = getIcon("/icons/configs/systemScope.svg")
        @JvmField val Locale = getIcon("/icons/configs/locale.svg")

        @JvmField val ExtendedScriptedVariable = getIcon("/icons/configs/extended/extendedScriptedVariable.svg")
        @JvmField val ExtendedDefinition = getIcon("/icons/configs/extended/extendedDefinition.svg")
        @JvmField val ExtendedGameRule = ExtendedDefinition
        @JvmField val ExtendedOnAction = ExtendedDefinition
        @JvmField val ExtendedParameter = getIcon("/icons/configs/extended/extendedParameter.svg")
        @JvmField val ExtendedComplexEnumValue = getIcon("/icons/configs/extended/extendedEnumValue.svg")
        @JvmField val ExtendedDynamicValue = getIcon("/icons/configs/extended/extendedDynamicValue.svg")
        @JvmField val ExtendedInlineScript = getIcon("/icons/configs/extended/extendedInlineScript.svg")
    }

    object Gutter {
        @JvmField val ScriptedVariable = getIcon("/icons/gutter/scriptedVariable.svg")
        @JvmField val Definition = getIcon("/icons/gutter/definition.svg")
        @JvmField val Localisation = getIcon("/icons/gutter/localisation.svg")
        @JvmField val RelatedScriptedVariables = getIcon("/icons/gutter/relatedScriptedVariables.svg")
        @JvmField val RelatedDefinitions = getIcon("/icons/gutter/relatedDefinitions.svg")
        @JvmField val RelatedLocalisations = getIcon("/icons/gutter/relatedLocalisations.svg")
        @JvmField val RelatedDefinitionInjections = getIcon("/icons/gutter/relatedDefinitionInjections.svg")
        @JvmField val RelatedImages = getIcon("/icons/gutter/relatedImages.svg")
        @JvmField val DefineVariable = getIcon("/icons/gutter/defineVariable.svg")
        @JvmField val InlineScripts = getIcon("/icons/gutter/inlineScripts.svg")
        @JvmField val DefinitionInjections = getIcon("/icons/gutter/definitionInjections.svg")
        @JvmField val DefinitionInjectionTargets = getIcon("/icons/gutter/definitionInjectionTargets.svg")
    }

    object Actions {
        @JvmField val GameSettings = getIcon("/icons/actions/gameSettings.svg") // from general/gear.svg, fill #5D8DC3
        @JvmField val ModSettings = getIcon("/icons/actions/modSettings.svg") // from general/gear.svg, fill #5D8DC3
        @JvmField val RefreshConfigGroups = getIcon("/icons/actions/refreshConfigGroups.svg") // from icons/refresh.svg, fill #5D8DC3
        @JvmField val SyncConfigGroupsFromRemote = getIcon("/icons/actions/syncConfigGroupsFromRemote.svg") // from icons/clone.svg, fill #5D8DC3
    }

    object EditorActions {
        @JvmField val AddColumnLeft = getIcon("/icons/editorActions/addColumnLeft.svg") // from Markdown plugin
        @JvmField val AddColumnRight = getIcon("/icons/editorActions/addColumnRight.svg") // from Markdown plugin
        @JvmField val AddRowAbove = getIcon("/icons/editorActions/addRowAbove.svg") // from Markdown plugin
        @JvmField val AddRowBelow = getIcon("/icons/editorActions/addRowBelow.svg") // from Markdown plugin
    }
}
