package icons

import com.intellij.icons.*
import com.intellij.ui.*
import javax.swing.*

object PlsIcons {
	@JvmField val GameDirectory = AllIcons.Modules.SourceRoot
	@JvmField val ModDirectory = AllIcons.Modules.SourceRoot
	@JvmField val Library = AllIcons.Nodes.PpLibFolder
	@JvmField val Steam = loadIcon("/icons/steam.svg")
	
	@JvmField val DdsFile = loadIcon("/icons/ddsFile.svg")
	@JvmField val CwtFile = loadIcon("/icons/cwtFile.svg")
	@JvmField val ParadoxScriptFile = loadIcon("/icons/paradoxScriptFile.svg")
	@JvmField val ParadoxLocalisationFile = loadIcon("/icons/paradoxLocalisationFile.svg")
	@JvmField val DescriptorFile = loadIcon("icons/descriptorFile.svg")
	
	@JvmField val CwtProperty = loadIcon("/icons/cwtProperty.svg")
	@JvmField val CwtValue = loadIcon("/icons/cwtValue.svg")
	@JvmField val CwtBlock = loadIcon("/icons/cwtBlock.svg")
	@JvmField val CwtOption = loadIcon("/icons/cwtOption.svg")
	
	@JvmField val ScriptProperty = loadIcon("/icons/scriptProperty.svg")
	@JvmField val ScriptValue = loadIcon("/icons/scriptValue.svg")
	@JvmField val ScriptBlock = loadIcon("/icons/scriptBlock.svg")
	@JvmField val ScriptParameterCondition = loadIcon("/icons/scriptParameterCondition.svg")
	
	@JvmField val LocalisationLocale = loadIcon("/icons/localisationLocale.svg")
	@JvmField val LocalisationProperty = loadIcon("/icons/localisationProperty.svg")
	@JvmField val LocalisationIcon = loadIcon("/icons/localisationIcon.svg")
	@JvmField val LocalisationCommand = loadIcon("/icons/localisationCommand.svg")
	@JvmField val LocalisationCommandScope = loadIcon("/icons/localisationCommandScope.svg")
	@JvmField val LocalisationCommandField = loadIcon("/icons/localisationCommandField.svg")
	@JvmField val LocalisationConceptName = loadIcon("/icons/localisationConceptName.svg")
	
	@JvmField val Definition = loadIcon("/icons/definition.svg")
	@JvmField val Localisation = loadIcon("/icons/localisation.svg")
	@JvmField val ScriptedVariable = loadIcon("/icons/scriptedVariable.svg")
	@JvmField val Property = loadIcon("/icons/property.svg")
	@JvmField val Value = loadIcon("/icons/value.svg")
	@JvmField val Parameter = loadIcon("/icons/parameter.svg")
	@JvmField val PredefinedParameter = loadIcon("/icons/predefinedParameter.svg") //localisation_predefined_parameter
	@JvmField val Variable = loadIcon("/icons/variable.svg") //value[variable]
	@JvmField val ValueSetValue = loadIcon("/icons/valueSetValue.svg")
	@JvmField val PredefinedValueSetValue = loadIcon("/icons/hardCodedValueSetValue.svg")
	@JvmField val EnumValue = loadIcon("/icons/enumValue.svg")
	@JvmField val ComplexEnumValue = loadIcon("/icons/complexEnumValue.svg")
	@JvmField val Scope = loadIcon("/icons/scope.svg")
	@JvmField val ScopeGroup = loadIcon("/icons/scopeGroup.svg")
	@JvmField val SystemScope = loadIcon("/icons/systemScope.svg")
	@JvmField val Link = loadIcon("/icons/link.svg")
	@JvmField val ScopeLinkPrefix = loadIcon("/icons/scopeLinkPrefix.svg")
	@JvmField val ValueLinkPrefix = loadIcon("/icons/valueLinkPrefix.svg")
	@JvmField val ValueLinkValue = loadIcon("/icons/valueLinkValue.svg")
	@JvmField val ModifierCategory = loadIcon("/icons/modifierCategory.svg")
	@JvmField val Modifier = loadIcon("/icons/modifier.svg")
	@JvmField val Trigger = loadIcon("/icons/trigger.svg")
	@JvmField val Effect = loadIcon("/icons/effect.svg")
	@JvmField val Tag = loadIcon("/icons/tag.svg")
	@JvmField val TemplateExpression = loadIcon("icons/templateExpression.svg")
	@JvmField val Alias = loadIcon("/icons/alias.svg")
	@JvmField val PathReference = AllIcons.FileTypes.Any_type 
	@JvmField val InlineScript = AllIcons.FileTypes.AddAny
	
	@JvmField val OnAction = loadIcon("/icons/onAction.svg") 
	@JvmField val EventNamespace = loadIcon("/icons/eventNamespace.svg")
	@JvmField val EventId = loadIcon("/icons/eventId.svg")
	@JvmField val Type = loadIcon("/icons/type.svg")
	@JvmField val Image = loadIcon("/icons/image.svg")
	@JvmField val Presentation = loadIcon("/icons/presentation.svg")
	
	@JvmStatic fun Definition(type: String) = when(type) {
		"event" -> EventId
		"event_namespace" -> EventNamespace
		else -> Definition
	}
	
	@JvmStatic fun ValueSetValue(valueSetName: String) = when(valueSetName) {
		"variable" -> Variable
		else -> ValueSetValue
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
		
		@JvmField val CreateReference = LocalisationProperty
		@JvmField val CreateIcon = LocalisationIcon
		@JvmField val CreateCommand = LocalisationCommand
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
