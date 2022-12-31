package icu.windea.pls.core.selector

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*
import java.util.*

tailrec fun selectGameType(from: Any?): ParadoxGameType? {
	return when {
		from == null -> null
		from is VirtualFile -> from.fileInfo?.rootInfo?.gameType
		from is PsiFile -> from.fileInfo?.rootInfo?.gameType
			?: ParadoxMagicCommentHandler.resolveFilePathComment(from)?.first
		from is ParadoxScriptScriptedVariable -> runCatching { from.stub }.getOrNull()?.gameType
			?: selectGameType(from.parent)
		from is ParadoxScriptDefinitionElement -> runCatching { from.getStub() }.getOrNull()?.gameType
			?: from.definitionInfo?.gameType
			?: ParadoxMagicCommentHandler.resolveDefinitionTypeComment(from)?.first //这个如果合法的话会被上一个选择逻辑覆盖
			?: selectGameType(from.parent)
		from is ParadoxScriptStringExpressionElement -> runCatching { from.stub?.gameType }.getOrNull()
			?: selectGameType(from.parent)
		from is PsiElement -> selectGameType(from.parent)
		else -> null
	}
}

tailrec fun selectRootFile(from: Any?): VirtualFile? {
	return when {
		from == null -> null
		from is VirtualFile -> from.fileInfo?.rootFile
		from is PsiFile -> from.fileInfo?.rootFile
		from is PsiElement -> selectRootFile(from.parent)
		else -> null
	}
}

//以下排序方法仅适用于ParadoxSelector，其他用途需要确认是否正确生效

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
): java.util.Comparator<T> {
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
): java.util.Comparator<T> {
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
