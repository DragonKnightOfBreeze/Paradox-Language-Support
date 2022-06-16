package icu.windea.pls.util

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*

/**
 * 用于指定如何选择需要查找的定义、本地化、文件等，尤其时当存在覆盖与重载的情况时。
 */
sealed interface ParadoxSelector<T> {
	val defaultValue: T? get() = null
	
	fun select(result: T): Boolean = true
	
	fun selectAll(result: T): Boolean = true
	
	fun comparator(): Comparator<T>? = null
}


sealed interface ParadoxFileSelector : ParadoxSelector<VirtualFile>

object ParadoxDefaultFileSelector : ParadoxFileSelector

class ParadoxSameRootFileSelector(private val rootPath: String) : ParadoxFileSelector {
	override var defaultValue: VirtualFile? = null
	
	override fun select(result: VirtualFile): Boolean {
		if(defaultValue == null) defaultValue = result
		return rootPath.matchesPath(result.path)
	}
	
	override fun selectAll(result: VirtualFile): Boolean {
		return true
	}
	
	override fun comparator(): Comparator<VirtualFile> {
		return compareBy<VirtualFile> { it.fileInfo?.path?.path }.thenComparing { it -> rootPath.matchesPath(it.path) }
	}
}

object ParadoxFileSelectors {
	/**
	 * 按照相对于游戏或模组根路径的路径进行排序。
	 */
	fun default(): ParadoxFileSelector {
		return ParadoxDefaultFileSelector
	}
	
	/**
	 * 优先查找游戏或模组根目录与[context]相同的文件，按照相对于游戏或模组根路径的路径进行排序，并将相同游戏或模组根目录的排序在前。
	 */
	fun preferSameRoot(context: PsiElement): ParadoxFileSelector {
		val rootPath = context.fileInfo?.root?.path ?: return default()
		return ParadoxSameRootFileSelector(rootPath)
	}
}