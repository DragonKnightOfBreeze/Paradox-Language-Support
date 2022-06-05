package icu.windea.pls

import com.intellij.openapi.util.*
import javax.swing.*

@Suppress("unused")
object PlsIcons {
	@JvmStatic val libraryIcon = loadIcon("/icons/library.svg")
	
	@JvmStatic val ddsFileIcon = loadIcon("/icons/ddsFile.svg")
	
	@JvmStatic val cwtFileIcon = loadIcon("/icons/cwtFile.svg")
	@JvmStatic val paradoxScriptFileIcon = loadIcon("/icons/paradoxScriptFile.svg")
	@JvmStatic val paradoxLocalisationFileIcon = loadIcon("/icons/paradoxLocalisationFile.svg")
	
	@JvmStatic val cwtPropertyIcon = loadIcon("/icons/cwtProperty.svg")
	@JvmStatic val cwtOptionIcon = loadIcon("/icons/cwtOption.svg")
	@JvmStatic val cwtValueIcon = loadIcon("/icons/cwtValue.svg")
	@JvmStatic val cwtBlockIcon = loadIcon("/icons/cwtBlock.svg")
	
	@JvmStatic val scriptPropertyIcon = loadIcon("/icons/scriptProperty.svg")
	@JvmStatic val scriptValueIcon = loadIcon("/icons/scriptValue.svg")
	@JvmStatic val scriptBlockIcon = loadIcon("/icons/scriptBlock.svg")
	@JvmStatic val scriptParameterConditionIcon = loadIcon("/icons/scriptParameterCondition.svg")
	@JvmStatic val scriptParameterIcon = loadIcon("/icons/scriptParameter.svg")
	
	@JvmStatic val localisationLocaleIcon = loadIcon("/icons/localisationLocale.svg")
	@JvmStatic val localisationPropertyIcon = loadIcon("/icons/localisationProperty.svg")
	@JvmStatic val localisationIconIcon = loadIcon("/icons/localisationIcon.svg")
	@JvmStatic val localisationCommandScopeIcon = loadIcon("/icons/localisationCommandScope.svg")
	@JvmStatic val localisationCommandFieldIcon = loadIcon("/icons/localisationCommandField.svg")
	
	@JvmStatic val definitionIcon = loadIcon("/icons/definition.svg")
	@JvmStatic val relatedLocalisationIcon = loadIcon("/icons/relatedLocalisation.svg")
	@JvmStatic val localisationIcon = loadIcon("/icons/localisation.svg")
	@JvmStatic val scriptedVariableIcon = loadIcon("/icons/scriptedVariable.svg")
	@JvmStatic val propertyIcon = loadIcon("/icons/property.svg")
	@JvmStatic val valueIcon = loadIcon("/icons/value.svg")
	@JvmStatic val parameterIcon = loadIcon("/icons/parameter.svg")
	@JvmStatic val variableIcon = loadIcon("/icons/variable.svg")
	@JvmStatic val valueValueIcon = loadIcon("/icons/valueValue.svg")
	@JvmStatic val enumValueIcon = loadIcon("/icons/enumValue.svg")
	@JvmStatic val scopeTypeIcon = loadIcon("/icons/scopeType.svg")
	@JvmStatic val scopeIcon = loadIcon("/icons/scope.svg")
	@JvmStatic val systemScopeIcon = loadIcon("/icons/systemScope.svg")
	@JvmStatic val modifierIcon = loadIcon("/icons/modifier.svg")
	@JvmStatic val aliasIcon = loadIcon("/icons/alias.svg")
	@JvmStatic val tagIcon = loadIcon("icons/tag.svg")
	
	@JvmStatic val definitionGutterIcon = loadIcon("/icons/gutter/definition.svg")
	@JvmStatic val relatedLocalisationGutterIcon = loadIcon("/icons/gutter/relatedLocalisation.svg")
	@JvmStatic val relatedPicturesGutterIcon = loadIcon("/icons/gutter/relatedPictures.svg")
	@JvmStatic val localisationGutterIcon = loadIcon("/icons/gutter/localisation.svg")
	
	private fun loadIcon(path: String): Icon {
		return IconLoader.getIcon(path, PlsIcons::class.java)
	}
}