package icu.windea.pls.core.selector

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

private val NopParadoxSelector = ChainedParadoxSelector<Nothing>()

@Suppress("UNCHECKED_CAST")
fun <T> nopSelector() = NopParadoxSelector as ChainedParadoxSelector<T>

fun <T> selector() = ChainedParadoxSelector<T>()

fun fileSelector() = ChainedParadoxSelector<VirtualFile>()

fun scriptedVariableSelector() = ChainedParadoxSelector<ParadoxScriptVariable>()

fun definitionSelector() = ChainedParadoxSelector<ParadoxDefinitionProperty>()

fun localisationSelector() = ChainedParadoxSelector<ParadoxLocalisationProperty>()

fun complexEnumSelector() = ChainedParadoxSelector<ParadoxExpressionAwareElement>()

fun valueSetValueSelector() = ChainedParadoxSelector<ParadoxScriptString>()


fun <T, K> ChainedParadoxSelector<T>.distinctBy(selector: (T) -> K) = apply { selectors += ParadoxDistinctSelector(selector) }


fun <T> ChainedParadoxSelector<T>.gameType(gameType: ParadoxGameType?) = apply { if(gameType != null) selectors += ParadoxGameTypeSelector(gameType) }

/**
 * @param from [VirtualFile] | [PsiFile] | [PsiElement]
 */
fun <T> ChainedParadoxSelector<T>.gameTypeFrom(from: Any?) = apply { if(from != null) selectors += ParadoxGameTypeSelector(from = from) }

fun <T> ChainedParadoxSelector<T>.root(rootFile: VirtualFile?) = apply { if(rootFile != null) selectors += ParadoxRootFileSelector(rootFile) }

/**
 * @param from [VirtualFile] | [PsiFile] | [PsiElement]
 */
fun <T> ChainedParadoxSelector<T>.rootFrom(from: Any?) = apply { if(from != null) selectors += ParadoxRootFileSelector(from = from) }

fun <T> ChainedParadoxSelector<T>.preferRoot(rootFile: VirtualFile?) = apply { if(rootFile != null) selectors += ParadoxPreferRootFileSelector(rootFile) }

/**
 * @param from [VirtualFile] | [PsiFile] | [PsiElement]
 */
fun <T> ChainedParadoxSelector<T>.preferRootFrom(from: Any?) = apply { if(from != null) selectors += ParadoxPreferRootFileSelector(from = from) }

fun ChainedParadoxSelector<ParadoxLocalisationProperty>.locale(locale: ParadoxLocaleConfig?) = apply { if(locale != null) selectors += ParadoxLocaleSelector(locale) }

fun ChainedParadoxSelector<ParadoxLocalisationProperty>.preferLocale(locale: ParadoxLocaleConfig?) = apply { if(locale != null) selectors += ParadoxPreferLocaleSelector(locale) }