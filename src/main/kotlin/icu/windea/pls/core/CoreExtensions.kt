package icu.windea.pls.core

import com.intellij.openapi.fileTypes.ex.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import java.util.*

fun resolveRootInfo(rootFile: VirtualFile, canBeNotAvailable: Boolean = true): ParadoxRootInfo? {
	val rootInfo = rootFile.getUserData(PlsKeys.rootInfoKey)
	if(rootInfo != null && (canBeNotAvailable || rootInfo.isAvailable)) {
		ParadoxRootInfo.values.add(rootInfo)
		return rootInfo
	}
	ParadoxRootInfo.values.remove(rootInfo)
	val resolvedRootInfo = doResolveRootInfo(rootFile, canBeNotAvailable)
	runCatching {
		rootFile.putUserData(PlsKeys.rootInfoKey, resolvedRootInfo)
	}
	if(resolvedRootInfo != null) {
		ParadoxRootInfo.values.add(resolvedRootInfo)
	}
	return resolvedRootInfo
}

private fun doResolveRootInfo(rootFile: VirtualFile, canBeNotAvailable: Boolean): ParadoxRootInfo? {
	if(rootFile is StubVirtualFile || !rootFile.isValid) return null
	if(!rootFile.isDirectory) return null
	
	var rootType: ParadoxRootType? = null
	var descriptorFile: VirtualFile? = null
	var markerFile: VirtualFile? = null
	val rootName = rootFile.nameWithoutExtension //忽略扩展名
	when {
		rootName == ParadoxRootType.PdxLauncher.id -> {
			rootType = ParadoxRootType.PdxLauncher
			descriptorFile = rootFile.parent?.children?.find {
				!it.isDirectory && (canBeNotAvailable || it.isValid) && it.name.equals(launcherSettingsFileName, true)
			}
			markerFile = descriptorFile
		}
		rootName == ParadoxRootType.PdxOnlineAssets.id -> {
			rootType = ParadoxRootType.PdxOnlineAssets
			descriptorFile = rootFile.parent?.children?.find {
				!it.isDirectory && (canBeNotAvailable || it.isValid) && it.name.equals(launcherSettingsFileName, true)
			}
			markerFile = descriptorFile
		}
		rootName == ParadoxRootType.TweakerGuiAssets.id -> {
			rootType = ParadoxRootType.TweakerGuiAssets
			descriptorFile = rootFile.parent?.children?.find {
				!it.isDirectory && (canBeNotAvailable || it.isValid) && it.name.equals(launcherSettingsFileName, true)
			}
			markerFile = descriptorFile
		}
		else -> {
			for(rootChild in rootFile.children) {
				if(rootChild.isDirectory) continue
				if(!canBeNotAvailable && !rootChild.isValid) continue
				val rootChildName = rootChild.name
				when {
					rootChildName.equals(launcherSettingsFileName, true) -> {
						rootType = ParadoxRootType.Game
						descriptorFile = rootChild
						markerFile = rootChild
						break
					}
					rootChildName.equals(descriptorFileName, true) -> {
						rootType = ParadoxRootType.Mod
						descriptorFile = rootChild
						if(descriptorFile != null && markerFile != null) break
					}
					ParadoxGameType.resolve(rootChild) != null -> {
						markerFile = rootChild
						if(descriptorFile != null && markerFile != null) break
					}
				}
			}
		}
	}
	if(descriptorFile != null && rootType != null) {
		return ParadoxRootInfo(rootFile, descriptorFile, markerFile, rootType)
	}
	return null
}

fun resolveFileInfo(file: VirtualFile): ParadoxFileInfo? {
	val resolvedFileInfo = doResolveFileInfo(file)
	runCatching {
		file.putUserData(PlsKeys.fileInfoKey, resolvedFileInfo)
	}
	return resolvedFileInfo
}

fun doResolveFileInfo(file: VirtualFile): ParadoxFileInfo? {
	if(file is StubVirtualFile || !file.isValid) return null
	val fileName = file.name
	val subPaths = LinkedList<String>()
	subPaths.addFirst(fileName)
	var currentFile: VirtualFile? = file.parent
	while(currentFile != null) {
		val rootInfo = resolveRootInfo(currentFile, false)
		if(rootInfo != null) {
			val path = ParadoxPath.resolve(subPaths)
			val fileType = ParadoxFileType.resolve(file, rootInfo.gameType, path)
			val fileInfo = ParadoxFileInfo(fileName, path, fileType, rootInfo)
			file.putUserData(PlsKeys.fileInfoKey, fileInfo)
			return fileInfo
		}
		subPaths.addFirst(currentFile.name)
		currentFile = currentFile.parent
	}
	return null
}

fun reparseFilesInRoot(rootFile: VirtualFile) {
	//重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
	try {
		FileTypeManagerEx.getInstanceEx().makeFileTypesChange("Root of paradox files $rootFile changed.") { }
	} catch(e: Exception) {
		//ignore
	} finally {
		//要求重新索引
		FileBasedIndex.getInstance().requestReindex(rootFile)
		//要求重建缓存（CachedValue）
		ParadoxGameTypeModificationTracker.fromRoot(rootFile).increment()
	}
}

fun reparseScriptFiles() {
	try {
		FileTypeManagerEx.getInstanceEx().makeFileTypesChange("Ignored file name of paradox script files changed.") { }
	} catch(e: Exception) {
		//ignore
	} finally {
		//要求重新索引
		for(rootInfo in ParadoxRootInfo.values) {
			FileBasedIndex.getInstance().requestReindex(rootInfo.rootFile)
		}
	}
}