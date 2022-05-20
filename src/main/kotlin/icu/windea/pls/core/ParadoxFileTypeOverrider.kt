package icu.windea.pls.core

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.fileTypes.impl.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import icu.windea.pls.*
import java.util.*

/**
 * 文件类型重载器。
 *
 * 基于文件后缀名以及相对于游戏或模组根目录的路径，将符合的文件重载为Paradox脚本文件或Paradox本地化文件。
 */
@Suppress("UnstableApiUsage")
class ParadoxFileTypeOverrider : FileTypeOverrider {
	//仅当从所在目录下找到exe文件或者descriptor.mod文件时
	//才有可能将所在目录（以及子目录）下的文件识别为Paradox本地化文件和脚本文件
	
	override fun getOverriddenFileType(file: VirtualFile): FileType? {
		if(file is StubVirtualFile || !file.isValid) return null
		val fileName = file.name
		val subPaths = LinkedList<String>()
		subPaths.addFirst(fileName)
		var currentFile: VirtualFile? = file.parent
		while(currentFile != null) {
			val targetFileType = setFileInfoAndGetFileType(file, currentFile, null, subPaths, fileName)
			if(targetFileType != null){
				return if(targetFileType != MockLanguageFileType.INSTANCE) targetFileType else null
			}
			subPaths.addFirst(currentFile.name)
			currentFile = currentFile.parent
		}
		runCatching {
			file.putUserData(paradoxFileInfoKey, null)
		}
		return null
	}
}

