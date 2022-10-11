package icu.windea.pls.core.selector

import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.selector.ParadoxSelectorHandler.selectGameType
import icu.windea.pls.core.selector.ParadoxSelectorHandler.selectRootFile
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.core.model.*
import java.util.*

class ParadoxDistinctSelector<T, K>(
	private val selector: (T) -> K
): ParadoxSelector<T>{
	val keys = mutableSetOf<K>()
	
	override fun selectAll(result: T): Boolean {
		return keys.add(selector(result))
	}
}


class ParadoxGameTypeSelector<T>(
	gameType: ParadoxGameType? = null,
	from: Any? = null
) : ParadoxSelector<T> {
	private val gameType by lazy { gameType ?: selectGameType(from) }
	
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
	private val rootFile by lazy { rootFile ?: selectRootFile(from) }
	
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
	private val rootFile by lazy { rootFile ?: selectRootFile(from) }
	
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

class ParadoxLocaleSelector(
	private val locale: ParadoxLocaleConfig
): ParadoxSelector<ParadoxLocalisationProperty> {
	override fun select(result: ParadoxLocalisationProperty): Boolean {
		return locale == result.localeConfig
	}
	
	override fun selectAll(result: ParadoxLocalisationProperty): Boolean {
		return select(result)
	}
}

class ParadoxPreferLocaleSelector(
	private val locale: ParadoxLocaleConfig
): ParadoxSelector<ParadoxLocalisationProperty> {
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

//以下排序方法仅适用于ParadoxSelector，其他用途需要确认是否正确生效

/**
 * 通过[selector]得到需要的结果之后，
 * 首先按照[comparableSelector]的结果进行排序（如果结果是null，则保持原有的先后顺序），
 * 然后按照[pinPredicate]的结果置顶匹配的元素（如果存在多个匹配的元素，则保持原有的先后顺序）。
 *
 * 基于返回比较器的[SortedSet]的被认为包含一切元素。
 */
inline fun <T, R, C : Comparable<C>> complexCompareBy(
	crossinline selector: (T) -> R?,
	crossinline comparableSelector: (R) -> C? = { null },
	crossinline pinPredicate: (R) -> Boolean = { false }
): Comparator<T> {
	return Comparator { a, b ->
		val a1 = selector(a)
		val b1 = selector(b)
		when {
			a1 == b1 -> 0
			a1 == null -> 1
			b1 == null -> -1
			pinPredicate(b1) -> if(pinPredicate(a1)) 0 else 1
			pinPredicate(a1) -> -1
			else -> {
				val a2 = comparableSelector(a1) ?: return@Comparator 0
				val b2 = comparableSelector(b1) ?: return@Comparator 0
				a2.compareTo(b2)
			}
		}
	}
}

/**
 * 通过[selector]得到需要的结果之后，
 * 首先按照[comparableSelector]的结果进行降序（如果结果是null，则保持原有的先后顺序），
 * 然后按照[pinPredicate]的结果置顶匹配的元素（如果存在多个匹配的元素，则保持原有的先后顺序）。
 *
 * 基于返回比较器的[SortedSet]的被认为包含一切元素。
 */
inline fun <T, R, C : Comparable<C>> complexCompareByDescending(
	crossinline selector: (T) -> R?,
	crossinline comparableSelector: (R) -> C? = { null },
	crossinline pinPredicate: (R) -> Boolean = { false }
): Comparator<T> {
	return Comparator<T> { a, b ->
		val a1 = selector(a)
		val b1 = selector(b)
		when {
			a1 == b1 -> 1
			a1 == null -> 1
			b1 == null -> -1
			pinPredicate(b1) -> 1
			pinPredicate(a1) -> -1
			else -> {
				val a2 = comparableSelector(a1) ?: return@Comparator 1
				val b2 = comparableSelector(b1) ?: return@Comparator 1
				-a2.compareTo(b2)
			}
		}
	}
}