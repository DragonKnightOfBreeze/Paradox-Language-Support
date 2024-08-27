package icu.windea.pls.lang

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.encoding.*
import icu.windea.pls.model.*

/**
 * 基于文件的扩展名以及相对于入口目录的路径，判断创建新的脚本文件或本地化文件时，是否需要添加BOM。
 */
class ParadoxUtf8BomOptionProvider : Utf8BomOptionProvider {
	override fun shouldAddBOMForNewUtf8File(file: VirtualFile): Boolean {
		val fileInfo = file.fileInfo ?: return false
		return when(fileInfo.fileType) {
			ParadoxFileType.ParadoxScript -> fileInfo.path.parent.startsWith("common/name_lists")
			ParadoxFileType.ParadoxLocalisation -> true
			else -> false
		}
	}
}
