package icu.windea.pls.core.selector

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.ext.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*

fun <S : ChainedParadoxSelector<T>, T, K> S.distinctBy(keySelector: (T) -> K) =
	apply { selectors += ParadoxDistinctSelector(keySelector) }

fun <S : ChainedParadoxSelector<T>, T> S.filterBy(predicate: (T) -> Boolean) =
	apply { selectors += ParadoxFilterSelector(predicate) }


fun <S : ChainedParadoxSelector<T>, T> S.gameType(gameType: ParadoxGameType?) =
	apply { if(gameType != null) selectors += ParadoxGameTypeSelector(gameType) }

/**
 * @param from [VirtualFile] | [PsiFile] | [PsiElement]
 */
fun <S : ChainedParadoxSelector<T>, T> S.gameTypeFrom(from: Any?) =
	apply { if(from != null) selectors += ParadoxGameTypeSelector(from = from) }

fun <S : ChainedParadoxSelector<T>, T> S.root(rootFile: VirtualFile?) =
	apply { if(rootFile != null) selectors += ParadoxRootFileSelector(rootFile) }

/**
 * @param from [VirtualFile] | [PsiFile] | [PsiElement]
 */
fun <S : ChainedParadoxSelector<T>, T> S.rootFrom(from: Any?) =
	apply { if(from != null) selectors += ParadoxRootFileSelector(from = from) }

@JvmOverloads
fun <S : ChainedParadoxSelector<T>, T> S.preferRoot(rootFile: VirtualFile?, condition: Boolean = true) =
	apply { if(rootFile != null && condition) selectors += ParadoxPreferRootFileSelector(rootFile) }

/**
 * @param from [VirtualFile] | [PsiFile] | [PsiElement]
 */
@JvmOverloads
fun <S : ChainedParadoxSelector<T>, T> S.preferRootFrom(from: Any?, condition: Boolean = true) =
	apply { if(from != null && condition) selectors += ParadoxPreferRootFileSelector(from = from) }

/**
 * 目前仅支持：[ParadoxComplexEnumValueSelector]
 * @see ParadoxSearchScope
 */
fun ParadoxComplexEnumValueSelector.withSearchScope(searchScope: String?, context: PsiElement) =
	apply { if(searchScope != null) selectors += ParadoxWithSearchScopeSelector(ParadoxSearchScope(searchScope), context) }

fun ParadoxScriptedVariableSelector.distinctByName() =
	distinctBy { it.name }

fun ParadoxDefinitionSelector.distinctByName() =
	distinctBy { ParadoxDefinitionHandler.getName(it) }

fun ParadoxLocalisationSelector.distinctByName() =
	distinctBy { it.name }

fun ParadoxComplexEnumValueSelector.distinctByName() =
	distinctBy { ParadoxComplexEnumValueHandler.getName(it) }

fun ParadoxValueSetValueSelector.distinctByValue() =
	distinctBy { ParadoxValueSetValueHandler.getName(it) }

fun ParadoxValueSetValueSelector.declarationOnly() = 
	filterBy { !ParadoxValueSetValueHandler.getRead(it) }

fun ParadoxLocalisationSelector.locale(locale: CwtLocalisationLocaleConfig?) =
	apply { if(locale != null) selectors += ParadoxLocaleSelector(locale) }

@JvmOverloads
fun ParadoxLocalisationSelector.preferLocale(locale: CwtLocalisationLocaleConfig?, condition: Boolean = true) =
	apply { if(locale != null && condition) selectors += ParadoxPreferLocaleSelector(locale) }
