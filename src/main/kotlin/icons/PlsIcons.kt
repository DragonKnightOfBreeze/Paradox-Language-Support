package icons

import com.intellij.icons.*
import com.intellij.ui.*
import javax.swing.*

object PlsIcons {
    @JvmField val GameDirectory = AllIcons.Modules.SourceRoot
    @JvmField val ModDirectory = AllIcons.Modules.SourceRoot
    @JvmField val ConfigGroupDirectory = AllIcons.Nodes.ConfigFolder
    @JvmField val Library = AllIcons.Nodes.PpLibFolder
    @JvmField val Image = loadIcon("/icons/image.svg")
    @JvmField val Presentation = loadIcon("/icons/presentation.svg")
    @JvmField val Steam = loadIcon("/icons/steam.svg")
    
    object FileTypes {
        @JvmField val Cwt = loadIcon("/icons/fileTypes/cwt.svg")
        @JvmField val ParadoxScript = loadIcon("/icons/fileTypes/paradoxScript.svg")
        @JvmField val ParadoxLocalisation = loadIcon("/icons/fileTypes/paradoxLocalisation.svg")
        @JvmField val ModeDescriptor = loadIcon("/icons/fileTypes/modDescriptor.svg")
        @JvmField val CwtConfig = loadIcon("/icons/fileTypes/cwtConfig.svg")
    }
    
    object Nodes {
        @JvmField val ScriptedVariable = loadIcon("/icons/nodes/scriptedVariable.svg")
        @JvmField val Definition = loadIcon("/icons/nodes/definition.svg")
        @JvmField val Localisation = loadIcon("/icons/nodes/localisation.svg")
        @JvmField val Property = loadIcon("/icons/nodes/property.svg")
        @JvmField val Value = loadIcon("/icons/nodes/value.svg")
        @JvmField val Parameter = loadIcon("/icons/nodes/parameter.svg")
        @JvmField val Type = loadIcon("/icons/nodes/type.svg")
        @JvmField val Variable = loadIcon("/icons/nodes/variable.svg")
        @JvmField val DynamicValueType = loadIcon("/icons/nodes/dynamicValueType.svg")
        @JvmField val DynamicValue = loadIcon("/icons/nodes/dynamicValue.svg")
        @JvmField val Enum = loadIcon("/icons/nodes/enum.svg")
        @JvmField val EnumValue = loadIcon("/icons/nodes/enumValue.svg")
        @JvmField val ComplexEnum = loadIcon("/icons/nodes/complexEnum.svg")
        @JvmField val ComplexEnumValue = loadIcon("/icons/nodes/complexEnumValue.svg")
        @JvmField val Scope = loadIcon("/icons/nodes/scope.svg")
        @JvmField val ScopeGroup = loadIcon("/icons/nodes/scopeGroup.svg")
        @JvmField val SystemScope = loadIcon("/icons/nodes/systemScope.svg")
        @JvmField val Link = loadIcon("/icons/nodes/link.svg")
        @JvmField val ValueField = loadIcon("/icons/nodes/valueField.svg")
        @JvmField val ModifierCategory = loadIcon("/icons/nodes/modifierCategory.svg")
        @JvmField val Modifier = loadIcon("/icons/nodes/modifier.svg")
        @JvmField val Trigger = loadIcon("/icons/nodes/trigger.svg")
        @JvmField val Effect = loadIcon("/icons/nodes/effect.svg")
        @JvmField val Tag = loadIcon("/icons/nodes/tag.svg")
        @JvmField val TemplateExpression = loadIcon("icons/nodes/templateExpression.svg")
        @JvmField val Alias = loadIcon("/icons/nodes/alias.svg")
        @JvmField val EventNamespace = loadIcon("/icons/nodes/eventNamespace.svg")
        @JvmField val EventId = loadIcon("/icons/nodes/eventId.svg")
        @JvmField val PathReference = AllIcons.FileTypes.Any_type
        @JvmField val Inline = AllIcons.FileTypes.AddAny
        @JvmField val InlineScript = loadIcon("/icons/nodes/inlineScript.svg")
        @JvmField val DatabaseObjectType = loadIcon("/icons/nodes/gameObjectType.svg")
        
        @JvmField val ScriptedVariableConfig = loadIcon("/icons/nodes/scriptedVariableConfig.svg")
        @JvmField val DefinitionConfig = loadIcon("/icons/nodes/definitionConfig.svg")
        @JvmField val ParameterConfig = loadIcon("/icons/nodes/parameterConfig.svg")
        @JvmField val InlineScriptConfig = loadIcon("/icons/nodes/inlineScriptConfig.svg")
        
        @JvmStatic fun Definition(type: String?) = when(type) {
            "event" -> EventId
            "event_namespace" -> EventNamespace
            else -> Definition
        }
        
        @JvmStatic fun DynamicValue(dynamicValueType: String?) = when(dynamicValueType) {
            "variable" -> Variable
            else -> DynamicValue
        }
    }
    
    object CwtNodes {
        @JvmField val Property = loadIcon("/icons/nodes/cwt/property.svg")
        @JvmField val Value = loadIcon("/icons/nodes/cwt/value.svg")
        @JvmField val Block = loadIcon("/icons/nodes/cwt/block.svg")
        @JvmField val Option = loadIcon("/icons/nodes/cwt/option.svg")
    }
    
    object ScriptNodes {
        @JvmField val ScriptedVariable = loadIcon("/icons/nodes/script/scriptedVariable.svg")
        @JvmField val Property = loadIcon("/icons/nodes/script/property.svg")
        @JvmField val Value = loadIcon("/icons/nodes/script/value.svg")
        @JvmField val Block = loadIcon("/icons/nodes/script/block.svg")
        @JvmField val ParameterCondition = loadIcon("/icons/nodes/script/parameterCondition.svg")
    }
    
    object LocalisationNodes {
        @JvmField val Locale = loadIcon("/icons/nodes/localisation/locale.svg")
        @JvmField val Property = loadIcon("/icons/nodes/localisation/property.svg")
        @JvmField val Icon = loadIcon("/icons/nodes/localisation/icon.svg")
        @JvmField val Command = loadIcon("/icons/nodes/localisation/command.svg")
        @JvmField val CommandScope = loadIcon("/icons/nodes/localisation/commandScope.svg")
        @JvmField val CommandField = loadIcon("/icons/nodes/localisation/commandField.svg")
        @JvmField val Concept = loadIcon("/icons/nodes/localisation/concept.svg")
    }
    
    object Actions {
        @JvmField val GameDirectory = AllIcons.Modules.SourceRoot
        @JvmField val ModDirectory = AllIcons.Modules.SourceRoot
        
        @JvmField val GameSettings = loadIcon("/icons/actions/gameSettings.svg")
        @JvmField val ModSettings = loadIcon("/icons/actions/modSettings.svg")
        
        @JvmField val SteamDirectory = loadIcon("/icons/actions/steamDirectory.svg")
        @JvmField val SteamGameDirectory = loadIcon("/icons/actions/steamGameDirectory.svg")
        @JvmField val SteamWorkshopDirectory = loadIcon("/icons/actions/steamWorkshopDirectory.svg")
        @JvmField val GameDataDirectory = loadIcon("/icons/actions/gameDataDirectory.svg")
        
        @JvmField val DuplicateDescriptor = AllIcons.Actions.Copy
        @JvmField val SwitchToPrevDescriptor = AllIcons.General.ArrowLeft
        @JvmField val SwitchToNextDescriptor = AllIcons.General.ArrowRight
        
        @JvmField val CreateReference = LocalisationNodes.Property
        @JvmField val CreateIcon = LocalisationNodes.Icon
        @JvmField val CreateCommand = LocalisationNodes.Command
        
        @JvmField val RefreshConfigGroups = loadIcon("/icons/actions/refreshConfigGroups.svg")
    }
    
    object Gutter {
        @JvmField val ScriptedVariable = loadIcon("/icons/gutter/scriptedVariable.svg")
        @JvmField val Definition = loadIcon("/icons/gutter/definition.svg")
        @JvmField val RelatedLocalisations = loadIcon("/icons/gutter/relatedLocalisations.svg")
        @JvmField val RelatedImages = loadIcon("/icons/gutter/relatedImages.svg")
        @JvmField val Localisation = loadIcon("/icons/gutter/localisation.svg")
        @JvmField val ComplexEnumValue = loadIcon("icons/gutter/complexEnumValue.svg")
    }
    
    object Hierarchy {
        @JvmField val Definition = AllIcons.Hierarchy.Subtypes
    }
    
    @JvmStatic fun loadIcon(path: String): Icon {
        return IconManager.getInstance().getIcon(path, PlsIcons.javaClass.classLoader)
    }
}
