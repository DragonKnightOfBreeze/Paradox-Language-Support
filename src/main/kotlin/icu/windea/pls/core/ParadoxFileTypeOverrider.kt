package icu.windea.pls.core

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.fileTypes.impl.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*
import java.util.*

@Suppress("UnstableApiUsage")
class ParadoxFileTypeOverrider : FileTypeOverrider {
	//仅当从所在目录下找到exe文件或者descriptor.mod文件时
	//才有可能将所在目录（以及子目录）下的文件识别为Paradox本地化文件和脚本文件
	
	override fun getOverriddenFileType(file: VirtualFile): FileType? {
		val fileType = getFileType(file) ?: return null
		val fileName = file.name
		val subPaths = LinkedList<String>()
		subPaths.addFirst(fileName)
		var currentFile: VirtualFile? = file.parent
		while(currentFile != null) {
			//只有能够确定根目录类型的文件才会被解析
			val rootType = getRootType(currentFile)
			if(rootType != null) {
				val path = getPath(subPaths)
				val gameType = getGameType(currentFile)?:ParadoxGameType.defaultValue()
				val rootPath = currentFile.toNioPath()
				//只解析特定根目录下的文件
				return when {
					//脚本文件，根据正则指定需要排除的文件
					fileType == ParadoxFileType.ParadoxScript && !fileName.matches(ignoredScriptFileNameRegex) -> {
						runCatching {
							val fileInfo = ParadoxFileInfo(fileName, path,rootPath, fileType, rootType, gameType)
							file.putUserData(paradoxFileInfoKey,fileInfo)
						}
						ParadoxScriptFileType
					}
					//本地化文件
					fileType == ParadoxFileType.ParadoxLocalisation-> {
						runCatching {
							val fileInfo = ParadoxFileInfo(fileName, path, rootPath,fileType, rootType, gameType)
							file.putUserData(paradoxFileInfoKey, fileInfo)
						}
						ParadoxLocalisationFileType
					}
					//其他文件（dds）
					else -> {
						runCatching {
							val fileInfo = ParadoxFileInfo(fileName, path, rootPath,fileType, rootType, gameType)
							file.putUserData(paradoxFileInfoKey, fileInfo)
						}
						null
					}
				}
			}
			subPaths.addFirst(currentFile.name)
			currentFile = currentFile.parent
		}
		runCatching {
			file.putUserData(paradoxFileInfoKey, null)
		}
		return null
	}
	
	private fun getPath(subPaths: List<String>): ParadoxPath {
		return ParadoxPath(subPaths)
	}
	
	private fun getFileType(file: VirtualFile): ParadoxFileType? {
		if(file is StubVirtualFile || !file.isValid || file.isDirectory) return null
		val fileExtension = file.extension?.lowercase() ?: return null
		return when {
			fileExtension in scriptFileExtensions -> ParadoxFileType.ParadoxScript
			fileExtension in localisationFileExtensions -> ParadoxFileType.ParadoxLocalisation
			fileExtension == "dds" -> ParadoxFileType.Dds
			else -> null
		}
	}
	
	private fun getRootType(file: VirtualFile): ParadoxRootType? {
		if(file is StubVirtualFile || !file.isValid || !file.isDirectory) return null
		val fileName = file.name.lowercase()
		for(child in file.children) {
			val childName = child.name.lowercase()
			val childExpression = childName.substringAfterLast('.', "")
			when {
				//TODO 可能并不是这样命名，需要检查
				//childName in ParadoxGameType.exeFileNames -> return ParadoxRootType.Stdlib
				childExpression == "exe" -> return ParadoxRootType.Stdlib
				childName == descriptorFileName -> return ParadoxRootType.Mod
				fileName == ParadoxRootType.PdxLauncher.key -> return ParadoxRootType.PdxLauncher
				fileName == ParadoxRootType.PdxOnlineAssets.key -> return ParadoxRootType.PdxOnlineAssets
				fileName == ParadoxRootType.TweakerGuiAssets.key -> return ParadoxRootType.TweakerGuiAssets
			}
		}
		return null
	}
	
	private fun getGameType(file:VirtualFile): ParadoxGameType? {
		if(file is StubVirtualFile || !file.isValid || !file.isDirectory) return null
		for(child in file.children) {
			val childName = child.name
			if(childName.startsWith('.')){
				val gameType = ParadoxGameType.resolve(childName.drop(1))
				if(gameType != null) return gameType
			}
		}
		return null
	}
}

