package icu.windea.pls.core

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*

fun getFileType(file: VirtualFile): ParadoxFileType? {
	if(file is StubVirtualFile || !file.isValid) return null
	if(file.isDirectory) return ParadoxFileType.Directory
	val fileName = file.name
	val fileExtension = file.extension?.lowercase() ?: return ParadoxFileType.Other
	return when {
		fileName == descriptorFileName -> ParadoxFileType.ParadoxScript
		fileExtension in scriptFileExtensions -> ParadoxFileType.ParadoxScript
		fileExtension in localisationFileExtensions -> ParadoxFileType.ParadoxLocalisation
		fileExtension in ddsFileExtensions -> ParadoxFileType.Dds
		else -> ParadoxFileType.Other
	}
}

fun setFileInfoAndGetFileType(
	file: VirtualFile,
	root: VirtualFile,
	project: Project?,
	subPaths: List<String>,
	fileName: String,
	fileType: ParadoxFileType,
	getFileType: Boolean = true
): FileType? {
	//只有能够确定根目录类型的文件才会被解析
	var rootType: ParadoxRootType? = null
	var gameType: ParadoxGameType? = null
	var descriptor: VirtualFile? = null
	if(root !is StubVirtualFile && root.isValid && root.isDirectory) {
		val rootName = root.nameWithoutExtension //忽略扩展名
		when {
			rootName == ParadoxRootType.PdxLauncher.id -> {
				rootType = ParadoxRootType.PdxLauncher
				descriptor = root.parent?.findChild(launcherSettingsFileName)
			}
			rootName == ParadoxRootType.PdxOnlineAssets.id -> {
				rootType = ParadoxRootType.PdxOnlineAssets
				descriptor = root.parent?.findChild(launcherSettingsFileName)
			}
			rootName == ParadoxRootType.TweakerGuiAssets.id -> {
				rootType = ParadoxRootType.TweakerGuiAssets
				descriptor = root.parent?.findChild(launcherSettingsFileName)
			}
			else -> {
				for(rootChild in root.children) {
					val rootChildName = rootChild.name
					when {
						rootChildName == launcherSettingsFileName -> {
							rootType = ParadoxRootType.Game
							descriptor = rootChild
							break
						}
						rootChildName == descriptorFileName -> {
							rootType = ParadoxRootType.Mod
							descriptor = rootChild
							break
						}
					}
				}
			}
		}
	}
	//从launcher-settings.json获取必要信息
	if(descriptor != null && descriptor.name == launcherSettingsFileName) {
		//基于其中的gameId的属性的值，得到游戏类型
		val gameId = jsonMapper.readTree(descriptor.inputStream).get("gameId").textValue()
		if(gameId != null) {
			gameType = ParadoxGameType.resolve(gameId)
		}
	}
	//处理得到游戏类型
	if(gameType == null) {
		for(rootChild in root.children) {
			val rootChildName = rootChild.name
			if(rootChildName.startsWith('.')) {
				gameType = ParadoxGameType.resolve(rootChildName.drop(1))
			}
		}
		if(gameType == null) {
			gameType = getSettings().defaultGameType
		}
	}
	if(rootType != null) {
		val path = ParadoxPath.resolve(subPaths)
		
		//缓存文件信息
		runCatching {
			val fileInfo = ParadoxFileInfo(fileName, path, root, descriptor, fileType, rootType, gameType)
			file.putUserData(paradoxFileInfoKey, fileInfo)
		}
		
		if(!getFileType) return null
		
		//如果存在对应的folders配置，则path要与之匹配或者为空，才解析为脚本或本地化文件
		val shouldOverride = when {
			path.isEmpty() -> true
			project != null -> isInFolders(project, gameType, path)
			else -> ProjectManager.getInstance().openProjects.any { isInFolders(it, gameType, path) } //任意项目需要重载即可
		}
		
		//只解析特定根目录下的文件
		return when {
			//脚本文件
			shouldOverride && fileType == ParadoxFileType.ParadoxScript && !isIgnored(fileName) -> ParadoxScriptFileType
			//本地化文件
			shouldOverride && fileType == ParadoxFileType.ParadoxLocalisation -> ParadoxLocalisationFileType
			//其他文件（如dds）
			else -> MockLanguageFileType.INSTANCE //这里不能直接返回null，否则缓存的文件信息会被清除
		}
	}
	return null
}

private fun isInFolders(project: Project, gameType: ParadoxGameType, path: ParadoxPath): Boolean {
	val folders = getCwtConfig(project).get(gameType)?.folders
	return folders.isNullOrEmpty() || folders.any { it.matchesPath(path.parent) }
}

private fun isIgnored(fileName: String): Boolean {
	return getSettings().finalScriptIgnoredFileNames.contains(fileName)
}