package icons

import com.intellij.ui.*
import javax.swing.*

object PlsIcons {
	@JvmField val Library = loadIcon("/icons/library.svg")
	
	@JvmField val DdsFile = loadIcon("/icons/ddsFile.svg")
	
	@JvmField val DescriptorFile = loadIcon("icons/descriptorFile.svg")
	@JvmField val CwtFile = loadIcon("/icons/cwtFile.svg")
	@JvmField val ParadoxScriptFile = loadIcon("/icons/paradoxScriptFile.svg")
	@JvmField val ParadoxLocalisationFile = loadIcon("/icons/paradoxLocalisationFile.svg")
	
	@JvmField val CwtProperty = loadIcon("/icons/cwtProperty.svg")
	@JvmField val CwtOption = loadIcon("/icons/cwtOption.svg")
	@JvmField val CwtValue = loadIcon("/icons/cwtValue.svg")
	@JvmField val CwtBlock = loadIcon("/icons/cwtBlock.svg")
	
	@JvmField val ScriptProperty = loadIcon("/icons/scriptProperty.svg")
	@JvmField val ScriptValue = loadIcon("/icons/scriptValue.svg")
	@JvmField val ScriptBlock = loadIcon("/icons/scriptBlock.svg")
	@JvmField val ScriptParameterCondition = loadIcon("/icons/scriptParameterCondition.svg")
	@JvmField val ScriptParameter = loadIcon("/icons/scriptParameter.svg")
	
	@JvmField val LocalisationLocale = loadIcon("/icons/localisationLocale.svg")
	@JvmField val LocalisationProperty = loadIcon("/icons/localisationProperty.svg")
	@JvmField val LocalisationIcon = loadIcon("/icons/localisationIcon.svg")
	@JvmField val LocalisationCommandScope = loadIcon("/icons/localisationCommandScope.svg")
	@JvmField val LocalisationCommandField = loadIcon("/icons/localisationCommandField.svg")
	
	@JvmField val Definition = loadIcon("/icons/definition.svg")
	@JvmField val RelatedLocalisation = loadIcon("/icons/relatedLocalisation.svg")
	@JvmField val Localisation = loadIcon("/icons/localisation.svg")
	@JvmField val ScriptedVariable = loadIcon("/icons/scriptedVariable.svg")
	@JvmField val Property = loadIcon("/icons/property.svg")
	@JvmField val Value = loadIcon("/icons/value.svg")
	@JvmField val Parameter = loadIcon("/icons/parameter.svg")
	@JvmField val Variable = loadIcon("/icons/variable.svg")
	@JvmField val ValueSetValue = loadIcon("/icons/valueSetValue.svg")
	@JvmField val HardCodedValueSetValue = loadIcon("/icons/hardCodedValueSetValue.svg")
	@JvmField val EnumValue = loadIcon("/icons/enumValue.svg")
	@JvmField val ComplexEnumValue = loadIcon("/icons/complexEnumValue.svg")
	@JvmField val ScopeType = loadIcon("/icons/scopeType.svg")
	@JvmField val SystemScope = loadIcon("/icons/systemScope.svg")
	@JvmField val Scope = loadIcon("/icons/scope.svg")
	@JvmField val ScopeFieldPrefix = loadIcon("/icons/scopeFieldPrefix.svg")
	@JvmField val ValueOfValueField = loadIcon("/icons/valueOfValueField.svg")
	@JvmField val ValueFieldPrefix = loadIcon("/icons/valueFieldPrefix.svg")
	@JvmField val Modifier = loadIcon("/icons/modifier.svg")
	@JvmField val Alias = loadIcon("/icons/alias.svg")
	@JvmField val Tag = loadIcon("icons/tag.svg")
	
	object Actions {
		@JvmField val SteamDirectory = loadIcon("/icons/actions/steamDirectory.svg")
		@JvmField val SteamGameDirectory = loadIcon("/icons/actions/steamGameDirectory.svg")
		@JvmField val SteamWorkshopDirectory = loadIcon("/icons/actions/steamWorkshopDirectory.svg")
		@JvmField val GameModDirectory = loadIcon("/icons/actions/gameModDirectory.svg")
	}
	
	object Gutter {
		@JvmField val Definition = loadIcon("/icons/gutter/definition.svg")
		@JvmField val RelatedLocalisation = loadIcon("/icons/gutter/relatedLocalisation.svg")
		@JvmField val RelatedImages = loadIcon("/icons/gutter/relatedImages.svg")
		@JvmField val Localisation = loadIcon("/icons/gutter/localisation.svg")
		@JvmField val ComplexEnumValue = loadIcon("icons/gutter/complexEnumValue.svg")
	}
	
	@JvmStatic fun loadIcon(path: String): Icon {
		return IconManager.getInstance().getIcon(path, PlsIcons::class.java)
	}
}