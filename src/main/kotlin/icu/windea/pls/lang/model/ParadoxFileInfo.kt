package icu.windea.pls.lang.model

import icu.windea.pls.core.*
import java.util.*

class ParadoxFileInfo(
	val name: String,
	val path: ParadoxPath,
	val fileType: ParadoxFileType,
	val rootInfo: ParadoxRootInfo
) {
	/**
	 * 相对于入口目录（如，"game"）而非游戏或模组根目录的路径。
	 */
	val entryPath: ParadoxPath = doGetEntryPath()
	
	private fun doGetEntryPath(): ParadoxPath {
		val filePath = this.path.path
		filePath.removePrefixOrNull("game/")?.let { return ParadoxPath.resolve(it) }
		rootInfo.gameType.entries.forEach { entry ->
			filePath.removePrefixOrNull("$entry/")?.let { return ParadoxPath.resolve(it) }
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