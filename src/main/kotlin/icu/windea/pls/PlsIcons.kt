package icu.windea.pls

import com.intellij.openapi.util.*
import javax.swing.*

object PlsIcons {
	@JvmStatic val Library = loadIcon("/icons/library.svg")
	
	@JvmStatic val DdsFile = loadIcon("/icons/ddsFile.svg")
	
	@JvmStatic val CwtFile = loadIcon("/icons/cwtFile.svg")
	@JvmStatic val ParadoxScriptFile = loadIcon("/icons/paradoxScriptFile.svg")
	@JvmStatic val ParadoxLocalisationFile = loadIcon("/icons/paradoxLocalisationFile.svg")
	
	@JvmStatic val CwtProperty = loadIcon("/icons/cwtProperty.svg")
	@JvmStatic val CwtOption = loadIcon("/icons/cwtOption.svg")
	@JvmStatic val CwtValue = loadIcon("/icons/cwtValue.svg")
	@JvmStatic val CwtBlock = loadIcon("/icons/cwtBlock.svg")
	
	@JvmStatic val ScriptProperty = loadIcon("/icons/scriptProperty.svg")
	@JvmStatic val ScriptValue = loadIcon("/icons/scriptValue.svg")
	@JvmStatic val ScriptBlock = loadIcon("/icons/scriptBlock.svg")
	@JvmStatic val ScriptParameterCondition = loadIcon("/icons/scriptParameterCondition.svg")
	@JvmStatic val ScriptParameter = loadIcon("/icons/scriptParameter.svg")
	
	@JvmStatic val LocalisationLocale = loadIcon("/icons/localisationLocale.svg")
	@JvmStatic val LocalisationProperty = loadIcon("/icons/localisationProperty.svg")
	@JvmStatic val LocalisationIcon = loadIcon("/icons/localisationIcon.svg")
	@JvmStatic val LocalisationCommandScope = loadIcon("/icons/localisationCommandScope.svg")
	@JvmStatic val LocalisationCommandField = loadIcon("/icons/localisationCommandField.svg")
	
	@JvmStatic val definitionIcon = loadIcon("/icons/definition.svg")
	@JvmStatic val RelatedLocalisation = loadIcon("/icons/relatedLocalisation.svg")
	@JvmStatic val Localisation = loadIcon("/icons/localisation.svg")
	@JvmStatic val ScriptedVariable = loadIcon("/icons/scriptedVariable.svg")
	@JvmStatic val Property = loadIcon("/icons/property.svg")
	@JvmStatic val Value = loadIcon("/icons/value.svg")
	@JvmStatic val Parameter = loadIcon("/icons/parameter.svg")
	@JvmStatic val Variable = loadIcon("/icons/variable.svg")
	@JvmStatic val ValueInValueSet = loadIcon("/icons/valueValue.svg")
	@JvmStatic val EnumValue = loadIcon("/icons/enumValue.svg")
	@JvmStatic val ScopeType = loadIcon("/icons/scopeType.svg")
	@JvmStatic val Scope = loadIcon("/icons/scope.svg")
	@JvmStatic val SystemScope = loadIcon("/icons/systemScope.svg")
	@JvmStatic val Modifier = loadIcon("/icons/modifier.svg")
	@JvmStatic val Alias = loadIcon("/icons/alias.svg")
	@JvmStatic val Tag = loadIcon("icons/tag.svg")
	
	object Actions {
		@JvmStatic val SteamDirectory = loadIcon("/icons/actions/steamDirectory.svg")
		@JvmStatic val SteamGameDirectory = loadIcon("/icons/actions/steamGameDirectory.svg")
		@JvmStatic val SteamWorkshopDirectory = loadIcon("/icons/actions/steamWorkshopDirectory.svg")
		@JvmStatic val GameModDirectory = loadIcon("/icons/actions/gameModDirectory.svg")
	}
	
	object Gutter {
		@JvmStatic val Definition = loadIcon("/icons/gutter/definition.svg")
		@JvmStatic val RelatedLocalisation = loadIcon("/icons/gutter/relatedLocalisation.svg")
		@JvmStatic val RelatedImages = loadIcon("/icons/gutter/relatedImages.svg")
		@JvmStatic val Localisation = loadIcon("/icons/gutter/localisation.svg")
	}
	
	private fun loadIcon(path: String): Icon {
		return IconLoader.getIcon(path, PlsIcons::class.java)
	}
}