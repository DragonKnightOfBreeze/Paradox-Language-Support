package icu.windea.pls.lang.search.selector

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.isSamePosition
import icu.windea.pls.core.letIf
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.model.index.ParadoxParameterIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

fun <S : ParadoxSearchSelector<T>, T> S.withGameType(gameType: ParadoxGameType?): S {
    if (gameType != null) selectors += ParadoxWithGameTypeSelector(gameType)
    return this
}

fun <S : ParadoxSearchSelector<T>, T> S.withSearchScope(searchScope: GlobalSearchScope?): S {
    if (searchScope != null) selectors += ParadoxWithSearchScopeSelector(searchScope)
    return this
}

fun <S : ParadoxSearchSelector<T>, T> S.withSearchScopeType(searchScopeType: String?): S {
    if (searchScopeType != null) selectors += ParadoxWithSearchScopeTypeSelector(searchScopeType, project, context)
    return this
}

/**
 * 首先尝试选用同一根目录下的，然后尝试选用同一文件下的。
 */
fun <S : ParadoxSearchSelector<T>, T> S.contextSensitive(condition: Boolean = true): S {
    if (condition) {
        if (rootFile != null) selectors += ParadoxPreferRootFileSelector(rootFile)
        if (file != null) selectors += ParadoxPreferFileSelector(file)
    }
    return this
}

fun <S : ParadoxSearchSelector<T>, T> S.filterBy(predicate: (T) -> Boolean): S {
    selectors += ParadoxFilterSelector(predicate)
    return this
}

fun <S : ParadoxSearchSelector<T>, T, K> S.distinctBy(keySelector: (T) -> K): S {
    selectors += ParadoxDistinctSelector(keySelector)
    return this
}

fun <S : ParadoxSearchSelector<T>, T : PsiElement> S.notSamePosition(element: PsiElement?): S {
    return filterBy { element == null || !element.isSamePosition(it) }
}

@JvmName("distinctByScriptedVariableName")
fun <S : ParadoxSearchSelector<ParadoxScriptScriptedVariable>> S.distinctByName(): S {
    return distinctBy { it.name }
}

@JvmName("distinctByDefinitionName")
fun <S : ParadoxSearchSelector<ParadoxDefinitionIndexInfo>> S.distinctByDefinitionName(): S {
    return distinctBy { it.name }
}

@JvmName("distinctByDefineExpression")
fun <S : ParadoxSearchSelector<ParadoxScriptProperty>> S.distinctByDefineExpression(): S {
    return distinctBy { ParadoxDefineManager.getExpression(it) }
}

@Suppress("unused")
@JvmName("distinctByLocalisationName")
fun <S : ParadoxSearchSelector<ParadoxLocalisationProperty>> S.distinctByName(): S {
    return distinctBy { it.name }
}

@JvmName("distinctByIndexInfoName")
fun <S : ParadoxSearchSelector<T>, T : ParadoxIndexInfo> S.distinctByName(): S {
    return distinctBy {
        when (it) {
            is ParadoxComplexEnumValueIndexInfo -> it.name.letIf(it.caseInsensitive) { n -> n.lowercase() } // #261
            is ParadoxDynamicValueIndexInfo -> it.name
            is ParadoxParameterIndexInfo -> it.name
            is ParadoxLocalisationParameterIndexInfo -> it.name
            else -> null
        }
    }
}

@JvmName("distinctByFilePath")
fun <S : ParadoxSearchSelector<VirtualFile>> S.distinctByFilePath(): S {
    return distinctBy { it.fileInfo?.path }
}

fun <S : ParadoxSearchSelector<T>, T> S.withConstraint(constraint: ParadoxIndexConstraint<T>?): S {
    if (constraint != null) selectors += ParadoxWithConstraintSelector(constraint)
    return this
}

fun <S : ParadoxSearchSelector<T>, T> S.getConstraint(): ParadoxIndexConstraint<T>? {
    return selectors.findIsInstance<ParadoxWithConstraintSelector<T>>()?.constraint
}

fun <S : ParadoxSearchSelector<ParadoxLocalisationProperty>> S.locale(locale: CwtLocaleConfig?): S {
    if (locale != null) selectors += ParadoxLocaleSelector(locale)
    return this
}

fun <S : ParadoxSearchSelector<ParadoxLocalisationProperty>> S.preferLocale(locale: CwtLocaleConfig?, condition: Boolean = true): S {
    if (locale != null && condition) selectors += ParadoxPreferLocaleSelector(locale)
    return this
}

fun <S : ParadoxSearchSelector<VirtualFile>> S.withFileExtensions(fileExtensions: Set<String>): S {
    if (fileExtensions.isEmpty()) return this
    return filterBy { it.extension?.let { e -> ".$e" }.orEmpty() in fileExtensions }
}
