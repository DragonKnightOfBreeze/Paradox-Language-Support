package icu.windea.pls.core.model

import java.util.*

class ParadoxFileInfo(
	val name: String,
	val path: ParadoxPath,
	val fileType: ParadoxFileType,
	val rootInfo: ParadoxRootInfo
) {
	val rootFile get() = rootInfo.rootFile
	val descriptorFile get() = rootInfo.descriptorFile
	val descriptorInfo get() = rootInfo.descriptorInfo
	val rootType get() = rootInfo.rootType
	val rootPath get() = rootInfo.rootPath
	
	val isValid get() = rootInfo.isValid 
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxFileInfo && path == other.path && rootInfo == other.rootInfo
	}
	
	override fun hashCode(): Int {
		return Objects.hash(path, rootInfo)
	}
}