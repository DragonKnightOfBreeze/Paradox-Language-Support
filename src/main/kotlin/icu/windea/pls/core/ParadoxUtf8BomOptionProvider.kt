package icu.windea.pls.core

import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.encoding.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*

/**
 * UTF8 BOM选项提供器。
 *
 * 基于文件后缀名以及相对于游戏或模组根目录的路径，判断创建新的Paradox脚本文件或Paradox本地化文件时，是否需要添加BOM。
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