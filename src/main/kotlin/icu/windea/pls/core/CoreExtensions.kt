package icu.windea.pls.core

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.impl.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*

fun setFileInfoAndGetFileType(
	file: VirtualFile,
	root: VirtualFile,
	project: Project?,
	subPaths: List<String>,
	fileName: String
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
		val fileType = getFileType(file, project, gameType, path)
		
		//缓存文件信息
		runCatching {
			val fileInfo = ParadoxFileInfo(fileName, path, root, descriptor, fileType, rootType, gameType)
			file.putUserData(paradoxFileInfoKey, fileInfo)
		}
		
		//只解析特定根目录下的文件
		return when {
			//模组描述符文件
			fileType == ParadoxFileType.ParadoxScript -> ParadoxScriptFileType
			//本地化文件
			fileType == ParadoxFileType.ParadoxLocalisation -> ParadoxLocalisationFileType
			//目录或者其他文件（如dds）
			else -> MockLanguageFileType.INSTANCE //这里不能直接返回null，否则缓存的文件信息会被清除
		}
	}
	return null
}

private fun getFileType(file: VirtualFile, project: Project?, gameType: ParadoxGameType, path: ParadoxPath): ParadoxFileType {
	if(file.isDirectory) return ParadoxFileType.Directory
	val fileName = file.name
	val fileExtension = file.extension?.lowercase() ?: return ParadoxFileType.Other
	return when {
		fileName == descriptorFileName -> ParadoxFileType.ParadoxScript
		fileExtension in scriptFileExtensions && !isIgnored(fileName) && isInFolders(project, gameType, path) -> ParadoxFileType.ParadoxScript
		fileExtension in localisationFileExtensions -> ParadoxFileType.ParadoxLocalisation
		fileExtension in ddsFileExtensions -> ParadoxFileType.Dds
		else -> ParadoxFileType.Other
	}
}

private fun isInFolders(project: Project?, gameType: ParadoxGameType, path: ParadoxPath): Boolean {
	//如果存在对应的folders配置，则path要与之匹配，才解析为脚本或本地化文件
	return when {
		project != null -> doIsInFolders(project, gameType, path)
		else -> ProjectManager.getInstance().openProjects.any { doIsInFolders(it, gameType, path) } //任意项目需要重载即可
	}
}

private fun doIsInFolders(project: Project, gameType: ParadoxGameType, path: ParadoxPath): Boolean {
	//排除除了模组描述符文件以外的在顶级目录的文件
	if(path.parent.isEmpty()) return false
	val folders = getCwtConfig(project).get(gameType)?.folders
	return folders.isNullOrEmpty() || folders.any { it.matchesPath(path.parent) }
}

private fun isIgnored(fileName: String): Boolean {
	return getSettings().finalScriptIgnoredFileNames.contains(fileName)
}