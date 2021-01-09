package com.windea.plugin.idea.paradox

import com.intellij.openapi.fileTypes.impl.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import com.windea.plugin.idea.paradox.localisation.*
import com.windea.plugin.idea.paradox.script.*

@Suppress("UnstableApiUsage")
class ParadoxFileTypeOverrider : FileTypeOverrider {
	//仅当从所在目录下找到exe文件或者descriptor.mod文件时
	//才有可能将所在目录（以及子目录）下的文件识别为Paradox本地化文件和脚本文件
	
	override fun getOverriddenFileType(file: VirtualFile): com.intellij.openapi.fileTypes.FileType? {
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
					fileType == ParadoxFileType.Script && isValidScriptPath(path, gameType) -> {
						runCatching {
							val fileInfo = ParadoxFileInfo(fileName, path, fileType, rootType, gameType)
							file.putUserData(paradoxFileInfoKey,fileInfo)
						}
						ParadoxScriptFileType
					}
					fileType == ParadoxFileType.Localisation && isValidLocalisationPath(path) -> {
						runCatching {
							val fileInfo = ParadoxFileInfo(fileName, path, fileType, rootType, gameType)
							file.putUserData(paradoxFileInfoKey, fileInfo)
						}
						ParadoxLocalisationFileType
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
	
	private fun isValidScriptPath(path: ParadoxPath, gameType: ParadoxGameType): Boolean {
		val ruleGroup = ruleGroups[gameType.key] ?: return false
		val locations = ruleGroup.locations
		val fastLocation = locations[path.parent]
		//首先尝试直接通过path作为key来匹配
		if(fastLocation != null) {
			val fastResult = fastLocation.matches(path, false)
			if(fastResult) return true
		}
		//然后遍历locations进行匹配
		val location = locations.values.find { it.matches(path) }
		return location != null
	}
	
	private fun isValidLocalisationPath(path: ParadoxPath): Boolean {
		val root = path.root
		return root == "localisation" || root == "localisation_synced"
	}
	
	private fun getPath(subPaths: List<String>): ParadoxPath {
		return ParadoxPath(subPaths)
	}
	
	private fun getFileType(file: VirtualFile): ParadoxFileType? {
		if(file is StubVirtualFile || !file.isDirectory) {
			val fileName = file.name.toLowerCase()
			val fileExtension = fileName.substringAfterLast('.')
			return when {
				fileExtension in scriptFileExtensions -> ParadoxFileType.Script
				fileExtension in localisationFileExtensions -> ParadoxFileType.Localisation
				else -> null
			}
		}
		return null
	}
	
	private fun getRootType(file: VirtualFile): ParadoxRootType? {
		if(file is StubVirtualFile || !file.isDirectory) return null
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

