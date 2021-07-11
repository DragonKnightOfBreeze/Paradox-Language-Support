package icu.windea.pls

import com.intellij.openapi.util.*

val libraryIcon = IconLoader.getIcon("/icons/library.svg",locationClass)

val cwtFileIcon = IconLoader.getIcon("/icons/cwtFile.svg", locationClass)
val paradoxScriptFileIcon = IconLoader.getIcon("/icons/paradoxScriptFile.svg",locationClass)
val paradoxLocalisationFileIcon = IconLoader.getIcon("/icons/paradoxLocalisationFile.svg",locationClass)

val cwtPropertyIcon = IconLoader.getIcon("/icons/cwtProperty.svg", locationClass)
val cwtOptionIcon = IconLoader.getIcon("/icons/cwtOption.svg", locationClass)
val cwtValueIcon = IconLoader.getIcon("/icons/cwtValue.svg", locationClass)

val scriptVariableIcon = IconLoader.getIcon("/icons/scriptVariable.svg",locationClass)
val scriptPropertyIcon = IconLoader.getIcon("/icons/paradoxScriptProperty.svg",locationClass)
val scriptValueIcon = IconLoader.getIcon("/icons/paradoxScriptValue.svg",locationClass)

val localisationLocaleIcon = IconLoader.getIcon("/icons/localisationLocale.svg",locationClass)
val localisationPropertyIcon = IconLoader.getIcon("/icons/localisationProperty.svg",locationClass)
val localisationIconIcon = IconLoader.getIcon("/icons/localisationIcon.svg",locationClass)
val localisationSequentialNumberIcon = IconLoader.getIcon("/icons/localisationSequentialNumber.svg",locationClass)
val localisationCommandScopeIcon = IconLoader.getIcon("/icons/localisationCommandScope.svg",locationClass)
val localisationCommandFieldIcon = IconLoader.getIcon("/icons/localisationCommandField.svg",locationClass)

val definitionIcon = IconLoader.getIcon("/icons/definition.svg",locationClass)
val definitionLocalisationIcon = IconLoader.getIcon("/icons/definitionLocalisation.svg",locationClass)
val localisationIcon = IconLoader.getIcon("/icons/localisation.svg",locationClass)
val propertyIcon = IconLoader.getIcon("/icons/property.svg", locationClass)
val valueIcon = IconLoader.getIcon("/icons/value.svg",locationClass)
val enumIcon = IconLoader.getIcon("/icons/enum.svg",locationClass)
val modifierIcon = IconLoader.getIcon("/icons/modifier.svg", locationClass)
val aliasIcon = IconLoader.getIcon("/icons/alias.svg",locationClass) //目前用不到，因为名字是表达式

val definitionGutterIcon = definitionIcon.resize(12)
val definitionLocalisationGutterIcon = definitionLocalisationIcon.resize(12)
val localisationGutterIcon = localisationIcon.resize(12)