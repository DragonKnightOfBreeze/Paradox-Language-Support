package icu.windea.pls.config.cwt

import com.intellij.openapi.application.*
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import java.lang.invoke.*

/**
 * 提供CWT规则。
 *
 * CWT规则来自目录`config/cwt`中的配置文件。使用内置且经过扩展和修改的CWT配置文件。
 */
@Service(Service.Level.PROJECT)
class CwtConfigProvider(
	private val project: Project
) {
	companion object {
		private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
		
		private const val cwtConfigPath = "/config/cwt"
		private val configFileExtensions = arrayOf("cwt", "csv")
	}
	
	val configGroups: CwtConfigGroups = initConfigGroups()
	
	//执行时间：读取3698ms 解析66ms
	@Synchronized
	private fun initConfigGroups(): CwtConfigGroups {
		val fileGroups = initConfigMaps()
		val configGroups = CwtConfigGroupsImpl(project, fileGroups)
		return configGroups
	}
	
	private fun initConfigMaps(): MutableMap<String, MutableMap<String, VirtualFile>> {
		logger.info("Resolve cwt config files.")
		val fileGroups: MutableMap<String, MutableMap<String, VirtualFile>> = mutableMapOf()
		val configUrl = cwtConfigPath.toClasspathUrl()
		//通过这种方式得到的virtualFile可以位于jar压缩包中，可以直接得到它的子节点
		val configDirectory = VfsUtil.findFileByURL(configUrl)
		if(configDirectory != null) {
			resolveFiles(fileGroups, configDirectory, configDirectory)
		}
		return fileGroups
	}
	
	private fun resolveFiles(fileGroups: MutableMap<String, MutableMap<String, VirtualFile>>, configDirectory: VirtualFile, configRootDirectory: VirtualFile) {
		for(configFile in configDirectory.children) {
			if(configFile.isDirectory) {
				//将目录的名字作为规则组的名字
				resolveFilesOfGroup(fileGroups, configFile, configRootDirectory)
			}
		}
	}
	
	private fun resolveFilesOfGroup(fileGroups: MutableMap<String, MutableMap<String, VirtualFile>>, groupDirectory: VirtualFile, configRootDirectory: VirtualFile) {
		val groupName = getGroupName(groupDirectory)
		logger.info("Resolve cwt config files of group '$groupName'.")
		val fileGroup = fileGroups.getOrPut(groupName) { mutableMapOf() }
		resolveFilesInGroup(fileGroup, groupDirectory, configRootDirectory)
	}
	
	private fun resolveFilesInGroup(fileGroup: MutableMap<String, VirtualFile>, configDirectory: VirtualFile, configRootDirectory: VirtualFile) {
		VfsUtilCore.visitChildrenRecursively(configDirectory, object : VirtualFileVisitor<Void>() {
			override fun visitFile(file: VirtualFile): Boolean {
				if(file.isDirectory) return true
				val extension = file.extension
				if(extension !in configFileExtensions) return true
				resolveFile(fileGroup, file, configRootDirectory)
				return true
			}
		})
	}
	
	private fun resolveFile(fileGroup: MutableMap<String, VirtualFile>, configFile: VirtualFile, configRootDirectory: VirtualFile) {
		val relativePath = configFile.relativePathTo(configRootDirectory)
		val file = configFile
		fileGroup.put(relativePath, file)
	}
	
	private fun getGroupName(groupDirectory: VirtualFile): String {
		return groupDirectory.name
	}
}
