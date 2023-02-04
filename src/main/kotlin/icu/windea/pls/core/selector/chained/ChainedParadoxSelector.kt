package icu.windea.pls.core.selector.chained

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.lang.model.*

open class ChainedParadoxSelector<T>(
	private val baseComparator: Comparator<T>? = null
) : ParadoxSelector<T> {
	val selectors = mutableListOf<ParadoxSelector<T>>()
	
	var defaultValue: T? = null
	var defaultValuePriority = 0
	
	override fun select(result: T): Boolean {
		if(selectors.isEmpty()) return super.select(result)
		var finalSelectResult = true
		var finalSelectDefaultResult = true
		var finalDefaultValuePriority = 0
		for(selector in selectors) {
			val selectResult = selector.select(result)
			finalSelectResult = finalSelectResult && selectResult
			if(selectResult) finalDefaultValuePriority++
			finalSelectDefaultResult = finalSelectDefaultResult && (selectResult || selector.selectAll(result))
		}
		if(finalSelectDefaultResult && defaultValuePriority < finalDefaultValuePriority){
			defaultValue = result
			defaultValuePriority = finalDefaultValuePriority
		}
		return finalSelectResult
	}
	
	override fun selectAll(result: T): Boolean {
		if(selectors.isEmpty()) return super.selectAll(result)
		var finalSelectAllResult = true
		for(selector in selectors) {
			val selectAllResult = selector.selectAll(result)
			finalSelectAllResult = finalSelectAllResult && selectAllResult
		}
		return finalSelectAllResult
	}
	
	override fun comparator(): Comparator<T>? {
		if(selectors.isEmpty()) return super.comparator()
		var comparator: Comparator<T>? = baseComparator
		for(paradoxSelector in selectors) {
			val nextComparator = paradoxSelector.comparator() ?: continue
			if(comparator == null) {
				comparator = nextComparator
			} else {
				comparator = comparator.thenComparing(nextComparator)
			}
		}
		//最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等
		return comparator?.thenComparing { a, b ->
			if(a == b) 0 else 1
		}
	}
	
	fun getGlobalSearchScope(): GlobalSearchScope? {
		return selectors.findIsInstance<ParadoxWithSearchScopeSelector<*>>()?.getGlobalSearchScope()
	}
}

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

fun <S: ChainedParadoxSelector<T>, T: PsiElement> S.notSamePosition(element: PsiElement?) = 
	filterBy { element == null || !element.isSamePosition(it) }


val ChainedParadoxSelector<*>.gameType: ParadoxGameType?
	get() = selectors.findIsInstance<ParadoxGameTypeSelector<*>>()?.gameType