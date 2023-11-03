package icu.windea.pls.core.search.selector

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.psi.*

//region nopSelector
fun <T> nopSelector(project: Project) = ChainedParadoxSelector<T>(project)
//endregion

//region scriptedVariableSelector
fun scriptedVariableSelector(project: Project, context: Any? = null) = ChainedParadoxSelector<ParadoxScriptScriptedVariable>(project, context)

@JvmName("distinctByName_scriptedVariableSelector")
fun ChainedParadoxSelector<ParadoxScriptScriptedVariable>.distinctByName() =
    distinctBy { it.name }
//endregion

//region definitionSelector
fun definitionSelector(project: Project, context: Any? = null) = ChainedParadoxSelector<ParadoxScriptDefinitionElement>(project, context)

@JvmName("distinctByName_definitionSelector")
fun ChainedParadoxSelector<ParadoxScriptDefinitionElement>.distinctByName() =
    distinctBy { ParadoxDefinitionHandler.getName(it) }
//endregion

//region localisationSelector
fun localisationSelector(project: Project, context: Any? = null) = ChainedParadoxSelector<ParadoxLocalisationProperty>(project, context)

fun ChainedParadoxSelector<ParadoxLocalisationProperty>.locale(locale: CwtLocalisationLocaleConfig?) =
    apply { if(locale != null) selectors += ParadoxLocaleSelector(locale) }

fun ChainedParadoxSelector<ParadoxLocalisationProperty>.preferLocale(locale: CwtLocalisationLocaleConfig?, condition: Boolean = true) =
    apply { if(locale != null && condition) selectors += ParadoxPreferLocaleSelector(locale) }

fun ChainedParadoxSelector<ParadoxLocalisationProperty>.distinctByName() =
    distinctBy { it.name }

class WithConstraintSelector(val constraint: ParadoxLocalisationConstraint) : ParadoxSelector<ParadoxLocalisationProperty>

fun ChainedParadoxSelector<ParadoxLocalisationProperty>.withConstraint(constraint: ParadoxLocalisationConstraint) =
    apply { selectors += WithConstraintSelector(constraint) }

fun ChainedParadoxSelector<ParadoxLocalisationProperty>.getConstraint(): ParadoxLocalisationConstraint =
    selectors.findIsInstance<WithConstraintSelector>()?.constraint ?: ParadoxLocalisationConstraint.Default
//endregion

//region fileSelector
fun fileSelector(project: Project, context: Any? = null) = ChainedParadoxSelector<VirtualFile>(project, context)

fun ChainedParadoxSelector<VirtualFile>.withFileExtensions(fileExtensions: Set<String>) =
    if(fileExtensions.isNotEmpty()) filterBy { it.extension?.let { e -> ".$e" }.orEmpty() in fileExtensions } else this

fun ChainedParadoxSelector<VirtualFile>.distinctByFilePath() =
    distinctBy { it.fileInfo?.path }
//endregion

//region complexEnumValueSelector
fun complexEnumValueSelector(project: Project, context: Any? = null) = ChainedParadoxSelector<ParadoxComplexEnumValueInfo>(project, context)

@JvmName("distinctByName_complexEnumValueSelector")
fun ChainedParadoxSelector<ParadoxComplexEnumValueInfo>.distinctByName() =
    distinctBy { it.name }
//endregion

//region valueSetValueSelector
fun valueSetValueSelector(project: Project, context: Any? = null) = ChainedParadoxSelector<ParadoxValueSetValueInfo>(project, context)

@JvmName("distinctByName_valueSetValueSelector")
fun ChainedParadoxSelector<ParadoxValueSetValueInfo>.distinctByName() =
    distinctBy { it.name }
//endregion

//region inlineScriptSelector
fun inlineScriptSelector(project: Project, context: Any? = null) = ChainedParadoxSelector<ParadoxInlineScriptUsageInfo>(project, context)
//endregion

//region valueSetValueSelector
fun parameterSelector(project: Project, context: Any? = null) = ChainedParadoxSelector<ParadoxParameterInfo>(project, context)

@JvmName("distinctByName_parameterSelector")
fun ChainedParadoxSelector<ParadoxParameterInfo>.distinctByName() =
    distinctBy { it.name }
//endregion

//region localisationParameterSelector
fun localisationParameterSelector(project: Project, context: Any? = null) = ChainedParadoxSelector<ParadoxLocalisationParameterInfo>(project, context)

@JvmName("distinctByName_localisationParameterSelector")
fun ChainedParadoxSelector<ParadoxLocalisationParameterInfo>.distinctByName() =
    distinctBy { it.name }
//endregion