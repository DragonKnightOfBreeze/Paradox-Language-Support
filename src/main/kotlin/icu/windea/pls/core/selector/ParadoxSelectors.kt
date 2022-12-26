package icu.windea.pls.core.selector

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.localisation.psi.*

class ParadoxDistinctSelector<T, K>(
	val keySelector: (T) -> K
) : ParadoxSelector<T> {
	val keys = mutableSetOf<K>()
	
	override fun selectAll(result: T): Boolean {
		val key = keySelector(result) ?: return false
		return keys.add(key)
	}
}

class ParadoxFilterSelector<T>(
	val predicate: (T) -> Boolean
): ParadoxSelector<T> {
	override fun selectAll(result: T): Boolean {
		return predicate(result)
	}
}

class ParadoxGameTypeSelector<T>(
	gameType: ParadoxGameType? = null,
	from: Any? = null
) : ParadoxSelector<T> {
	val gameType by lazy { gameType ?: selectGameType(from) }
	
	override fun select(result: T): Boolean {
		return gameType == selectGameType(result)
	}
	
	override fun selectAll(result: T): Boolean {
		return select(result)
	}
}

class ParadoxRootFileSelector<T>(
	rootFile: VirtualFile? = null,
	from: Any? = null
) : ParadoxSelector<T> {
	val rootFile by lazy { rootFile ?: selectRootFile(from) }
	
	override fun select(result: T): Boolean {
		return rootFile == selectRootFile(result)
	}
	
	override fun selectAll(result: T): Boolean {
		return select(result)
	}
}

class ParadoxPreferRootFileSelector<T>(
	rootFile: VirtualFile? = null,
	from: Any? = null
) : ParadoxSelector<T> {
	val rootFile by lazy { rootFile ?: selectRootFile(from) }
	
	override fun select(result: T): Boolean {
		return rootFile == selectRootFile(result)
	}
	
	override fun selectAll(result: T): Boolean {
		return true
	}
	
	override fun comparator(): Comparator<T> {
		return complexCompareBy({ it }, { null }, { rootFile == selectRootFile(it) })
	}
}

class ParadoxWithSearchScopeSelector<T : PsiElement>(
	val searchScope: ParadoxSearchScope,
	val context: PsiElement
) : ParadoxSelector<T> {
	private val root by lazy { findRoot(context) }
	
	override fun select(result: T): Boolean {
		return root == null || root == findRoot(result)
	}
	
	override fun selectAll(result: T): Boolean {
		return select(result)
	}
	
	private fun findRoot(context: PsiElement): PsiElement? {
		return searchScope.findRoot(context)
	}
	
	fun getGlobalSearchScope(): GlobalSearchScope? {
		return searchScope.getGlobalSearchScope(context)
	}
}

class ParadoxLocaleSelector(
	val locale: CwtLocalisationLocaleConfig
) : ParadoxSelector<ParadoxLocalisationProperty> {
	override fun select(result: ParadoxLocalisationProperty): Boolean {
		return locale == result.localeConfig
	}
	
	override fun selectAll(result: ParadoxLocalisationProperty): Boolean {
		return select(result)
	}
}

class ParadoxPreferLocaleSelector(
	val locale: CwtLocalisationLocaleConfig
) : ParadoxSelector<ParadoxLocalisationProperty> {
	override fun select(result: ParadoxLocalisationProperty): Boolean {
		return locale == result.localeConfig
	}
	
	override fun selectAll(result: ParadoxLocalisationProperty): Boolean {
		return true
	}
	
	override fun comparator(): Comparator<ParadoxLocalisationProperty> {
		return complexCompareBy({ it.localeConfig }, { it.id }, { locale == it }) //同时也按照localeId来进行排序
	}
}