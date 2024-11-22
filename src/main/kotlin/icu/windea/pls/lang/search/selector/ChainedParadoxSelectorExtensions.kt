package icu.windea.pls.lang.search.selector

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*

fun <S : ChainedParadoxSelector<T>, T> S.withGameType(gameType: ParadoxGameType?): S {
    if (gameType != null) selectors += ParadoxWithGameTypeSelector(gameType)
    return this
}

fun <S : ChainedParadoxSelector<T>, T> S.withSearchScope(searchScope: GlobalSearchScope?): S {
    if (searchScope != null) selectors += ParadoxWithSearchScopeSelector(searchScope)
    return this
}

fun <S : ChainedParadoxSelector<T>, T> S.withSearchScopeType(searchScopeType: String?): S {
    if (searchScopeType != null) selectors += ParadoxWithSearchScopeTypeSelector(searchScopeType, project, context)
    return this
}

/**
 * 首先尝试选用同一根目录下的，然后尝试选用同一文件下的。
 */
fun <S : ChainedParadoxSelector<T>, T> S.contextSensitive(condition: Boolean = true): S {
    if (condition) {
        if (rootFile != null) selectors += ParadoxPreferRootFileSelector(rootFile)
        if (file != null) selectors += ParadoxPreferFileSelector(file)
    }
    return this
}

fun <S : ChainedParadoxSelector<T>, T> S.filterBy(predicate: (T) -> Boolean): S {
    selectors += ParadoxFilterSelector(predicate)
    return this
}

fun <S : ChainedParadoxSelector<T>, T, K> S.distinctBy(keySelector: (T) -> K): S {
    selectors += ParadoxDistinctSelector(keySelector)
    return this
}

fun <S : ChainedParadoxSelector<T>, T : PsiElement> S.notSamePosition(element: PsiElement?): S {
    return filterBy { element == null || !element.isSamePosition(it) }
}

@JvmName("distinctByName_scriptedVariable")
fun <S : ChainedParadoxSelector<ParadoxScriptScriptedVariable>> S.distinctByName(): S {
    return distinctBy { it.name }
}

@JvmName("distinctByName_definition")
fun <S : ChainedParadoxSelector<ParadoxScriptDefinitionElement>> S.distinctByName(): S {
    return distinctBy { ParadoxDefinitionManager.getName(it) }
}

@JvmName("distinctByName_localisation")
fun <S : ChainedParadoxSelector<ParadoxLocalisationProperty>> S.distinctByName(): S {
    return distinctBy { it.name }
}

@JvmName("distinctByName_complexEnumValue")
fun <S : ChainedParadoxSelector<ParadoxComplexEnumValueIndexInfo>> S.distinctByName(): S {
    return distinctBy { it.name }
}

@JvmName("distinctByName_dynamicValue")
fun <S : ChainedParadoxSelector<ParadoxDynamicValueIndexInfo>> S.distinctByName(): S {
    return distinctBy { it.name }
}

@JvmName("distinctByName_parameter")
fun <S : ChainedParadoxSelector<ParadoxParameterIndexInfo>> S.distinctByName(): S {
    return distinctBy { it.name }
}

@JvmName("distinctByName_localisationParameter")
fun <S : ChainedParadoxSelector<ParadoxLocalisationParameterIndexInfo>> S.distinctByName(): S {
    return distinctBy { it.name }
}

fun <S : ChainedParadoxSelector<VirtualFile>> S.distinctByFilePath(): S {
    return distinctBy { it.fileInfo?.path }
}

fun <S : ChainedParadoxSelector<ParadoxLocalisationProperty>> S.locale(locale: CwtLocalisationLocaleConfig?): S {
    if (locale != null) selectors += ParadoxLocaleSelector(locale)
    return this
}

fun <S : ChainedParadoxSelector<ParadoxLocalisationProperty>> S.preferLocale(locale: CwtLocalisationLocaleConfig?, condition: Boolean = true): S {
    if (locale != null && condition) selectors += ParadoxPreferLocaleSelector(locale)
    return this
}

fun <S : ChainedParadoxSelector<ParadoxLocalisationProperty>> S.withConstraint(constraint: ParadoxLocalisationConstraint): S {
    selectors += ParadoxWithConstraintSelector(constraint)
    return this
}

fun <S : ChainedParadoxSelector<ParadoxLocalisationProperty>> S.getConstraint(): ParadoxLocalisationConstraint {
    return selectors.findIsInstance<ParadoxWithConstraintSelector>()?.constraint ?: ParadoxLocalisationConstraint.Default
}

fun <S : ChainedParadoxSelector<VirtualFile>> S.withFileExtensions(fileExtensions: Set<String>): S {
    if (fileExtensions.isEmpty()) return this
    return filterBy { it.extension?.let { e -> ".$e" }.orEmpty() in fileExtensions }
}
