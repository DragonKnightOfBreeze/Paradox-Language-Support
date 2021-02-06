package com.windea.plugin.idea.paradox.core

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.fileTypes.impl.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.*
import com.windea.plugin.idea.paradox.script.*

@Suppress("UnstableApiUsage")
class ParadoxFileTypeOverrider : FileTypeOverrider {
	//仅当从所在目录下找到exe文件或者descriptor.mod文件时
	//才有可能将所在目录（以及子目录）下的文件识别为Paradox本地化文件和脚本文件
	
	override fun getOverriddenFileType(file: VirtualFile): FileType? {
		val fileType = getFileType(file) ?: return null
		val fileName = file.name
		val subPaths = mutableListOf(fileName)
		var currentFile: VirtualFile? = file.parent
		while(currentFile != null) {
			//只有能够确定根目录类型的文件才会被解析
			val rootType = getRootType(currentFile)
			if(rootType != null) {
				val path = getPath(subPaths)
				val gameType = getGameType()
				//只解析特定根目录下的文件
				return when {
					//脚本文件，根据正则指定需要排除的文件
					fileType == ParadoxFileType.Script && !fileName.matches(ignoredScriptFileNameRegex) -> {
						runCatching {
							val fileInfo = ParadoxFileInfo(fileName, path, fileType, rootType, gameType)
							file.putUserData(paradoxFileInfoKey,fileInfo)
						}
						ParadoxScriptFileType
					}
					//本地化文件
					fileType == ParadoxFileType.Localisation-> {
						runCatching {
							val fileInfo = ParadoxFileInfo(fileName, path, fileType, rootType, gameType)
							file.putUserData(paradoxFileInfoKey, fileInfo)
						}
						ParadoxLocalisationFileType
					}
					//脚本规则文件，相比脚本文件应当仅提供基础的语言功能支持
					fileType == ParadoxFileType.ScriptRule -> {
						ParadoxScriptFileType
					}
					else -> null
				}
			}
			subPaths.add(0, currentFile.name)
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
		if(file is StubVirtualFile || !file.isValid || !file.isDirectory) {
			val fileName = file.name.toLowerCase()
			val fileExtension = fileName.substringAfterLast('.')
			return when {
				fileExtension in scriptFileExtensions -> ParadoxFileType.Script
				fileExtension in localisationFileExtensions -> ParadoxFileType.Localisation
				fileExtension in scriptRuleFileExtensions -> ParadoxFileType.ScriptRule
				else -> null
			}
		}
		return null
	}
	
	private fun getRootType(file: VirtualFile): ParadoxRootType? {
		if(file is StubVirtualFile || !file.isValid || !file.isDirectory) return null
		val fileName = file.name
		for(child in file.children) {
			val childName = child.name
			when {
				exeFileNames.any { exeFileName -> childName.equals(exeFileName, true) } -> return ParadoxRootType.Stdlib
				childName.equals(descriptorFileName, true) -> return ParadoxRootType.Mod
				fileName == ParadoxRootType.PdxLauncher.key -> return ParadoxRootType.PdxLauncher
				fileName == ParadoxRootType.PdxOnlineAssets.key -> return ParadoxRootType.PdxOnlineAssets
				fileName == ParadoxRootType.TweakerGuiAssets.key -> return ParadoxRootType.TweakerGuiAssets
			}
		}
		return null
	}
	
	private fun getGameType(): ParadoxGameType {
		return ParadoxGameType.Stellaris //TODO
	}
}

