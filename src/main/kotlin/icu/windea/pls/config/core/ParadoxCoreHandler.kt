package icu.windea.pls.config.core

import com.intellij.openapi.fileTypes.ex.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import java.util.*

object ParadoxCoreHandler {
	fun shouldIndexFile(virtualFile: VirtualFile): Boolean {
		try {
			//仅索引有根目录的文件
			val fileInfo = virtualFile.fileInfo ?: return false
			val rootType = fileInfo.rootType
			val path = fileInfo.path.path
			//仅索引游戏或模组根目录下的文件
			if(rootType != ParadoxRootType.Game && rootType != ParadoxRootType.Mod) return false
			//不索引内联脚本文件
			if(path.matchesPath("common/inline_scripts")) return false
			return true
		} catch(e: Exception) {
			return false
		}
	}
	
	fun getFileInfo(virtualFile: VirtualFile): ParadoxFileInfo? {
		return virtualFile.getCopyableUserData(PlsKeys.fileInfoKey)
	}
	
	fun getFileInfo(file: PsiFile): ParadoxFileInfo? {
		return file.originalFile.virtualFile?.let { getFileInfo(it) }
	}
	
	fun getFileInfo(element: PsiElement): ParadoxFileInfo? {
		return element.containingFile?.let { getFileInfo(it) }
	}
	
	@JvmStatic
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
				descriptorFile = rootFile.parent?.findChild(PlsConstants.launcherSettingsFileName)
					?.takeIf { !it.isDirectory && (canBeNotAvailable || it.isValid) }
				markerFile = descriptorFile
			}
			rootName == ParadoxRootType.PdxOnlineAssets.id -> {
				rootType = ParadoxRootType.PdxOnlineAssets
				descriptorFile = rootFile.parent?.findChild(PlsConstants.launcherSettingsFileName)
					?.takeIf { !it.isDirectory && (canBeNotAvailable || it.isValid) }
				markerFile = descriptorFile
			}
			rootName == ParadoxRootType.TweakerGuiAssets.id -> {
				rootType = ParadoxRootType.TweakerGuiAssets
				descriptorFile = rootFile.parent?.findChild(PlsConstants.launcherSettingsFileName)
					?.takeIf { !it.isDirectory && (canBeNotAvailable || it.isValid) }
				markerFile = descriptorFile
			}
			rootName == "game" -> {
				return doResolveRootInfo(rootFile.parent ?: return null, canBeNotAvailable)
			}
			else -> {
				for(rootChild in rootFile.children) {
					if(rootChild.isDirectory) continue
					if(!canBeNotAvailable && !rootChild.isValid) continue
					val rootChildName = rootChild.name
					when {
						rootChildName.equals(PlsConstants.launcherSettingsFileName, true) -> {
							rootType = ParadoxRootType.Game
							descriptorFile = rootChild
							markerFile = rootChild
							break
						}
						rootChildName.equals(PlsConstants.descriptorFileName, true) -> {
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
	
	@JvmStatic
	fun resolveFileInfo(file: VirtualFile): ParadoxFileInfo? {
		val resolvedFileInfo = doResolveFileInfo(file)
		runCatching {
			file.putCopyableUserData(PlsKeys.fileInfoKey, resolvedFileInfo)
		}
		return resolvedFileInfo
	}
	
	@JvmStatic
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
				return fileInfo
			}
			subPaths.addFirst(currentFile.name)
			currentFile = currentFile.parent
		}
		return null
	}
	
	@JvmStatic
	fun reparseFilesInRoot(rootFile: VirtualFile) {
		//重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
		try {
			FileTypeManagerEx.getInstanceEx().makeFileTypesChange("Root of paradox files $rootFile changed.") { }
		} catch(e: Exception) {
			//ignore
		} finally {
			//要求重新索引
			FileBasedIndex.getInstance().requestReindex(rootFile)
		}
	}
	
	@JvmStatic
	fun reparseFilesByFileNames(fileNames: Set<String>) {
		//重新解析指定的根目录中的所有文件，包括非脚本非本地化文件
		val files = mutableListOf<VirtualFile>()
		try {
			val project = getTheOnlyOpenOrDefaultProject()
			FilenameIndex.processFilesByNames(fileNames, true, GlobalSearchScope.allScope(project), null) { file ->
				files.add(file)
				true
			}
			FileContentUtil.reparseFiles(project, files, true)
		} catch(e: Exception) {
			//ignore
		} finally {
			//要求重新索引
			for(file in files) {
				FileBasedIndex.getInstance().requestReindex(file)
			}
		}
	}
}