package icu.windea.pls.config.core.config

import java.util.*

class ParadoxFileInfo(
	val name: String,
	val path: ParadoxPath,
	val fileType: ParadoxFileType,
	val rootInfo: ParadoxRootInfo
) {
	val rootFile get() = rootInfo.rootFile
	val rootType get() = rootInfo.rootType
	val rootPath get() = rootInfo.rootPath
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxFileInfo && path == other.path && rootInfo == other.rootInfo
	}
	
	override fun hashCode(): Int {
		return Objects.hash(path, rootInfo)
	}
}