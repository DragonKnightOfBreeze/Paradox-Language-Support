package icu.windea.pls.lang.model

import icu.windea.pls.core.*
import java.util.*

class ParadoxFileInfo(
	val name: String,
	val path: ParadoxPath,
	val fileType: ParadoxFileType,
	val rootInfo: ParadoxRootInfo
) {
	//path - 用于显示在快速文档中，相对于游戏或模组根目录的路径
	//entryPath - 用于匹配CWT规则文件中指定的路径（后者一般以"game/"开始，插件会忽略掉此前缀）
	
	/**
	 * 相对于入口目录（如，"game"）而非游戏或模组根目录的路径。
	 */
	val entryPath: ParadoxPath = doGetEntryPath()
	
	private fun doGetEntryPath(): ParadoxPath {
		val filePath = this.path.path
		rootInfo.gameEntry?.let { entry ->
			filePath.removePrefixOrNull(entry)?.let { return ParadoxPath.resolve(it) }
		}
		rootInfo.gameType.entries.forEach { entry ->
			filePath.removePrefixOrNull(entry)?.let { return ParadoxPath.resolve(it) }
		}
		return path
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxFileInfo && path == other.path && rootInfo == other.rootInfo
	}
	
	override fun hashCode(): Int {
		return Objects.hash(path, rootInfo)
	}
}