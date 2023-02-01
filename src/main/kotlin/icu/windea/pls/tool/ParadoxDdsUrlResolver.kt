package icu.windea.pls.tool

import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.dds.*
import icu.windea.pls.script.psi.*
import java.lang.invoke.*
import kotlin.io.path.*

/**
 * DDS图片地址的解析器。
 */
@Suppress("unused")
object ParadoxDdsUrlResolver {
	private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
	
	/**
	 * 基于定义进行解析。定义类型可以不为sprite。返回对应的PNG图片的绝对路径。
	 * @param frame 帧数。用于切割图片，默认为0，表示不切割。如果为0，但对应的定义可以获取帧数信息，则使用那个帧数。
	 */
	fun resolveByDefinition(definition: ParadoxScriptDefinitionElement, frame: Int = 0, defaultToUnknown: Boolean = false): String {
		val definitionInfo = definition.definitionInfo ?: return getDefaultUrl(defaultToUnknown)
		try {
			//如果无法解析为png文件地址，则返回默认的地址
			val url = doResolveByDefinition(definition, frame, definitionInfo)
			if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
			return url
		} catch(e: Exception) {
			//如果出现异常，那么返回默认图标
			if(e !is ProcessCanceledException) {
				logger.warn("Resolve dds url failed. (definition name: ${definitionInfo.name})", e)
			}
			return getDefaultUrl(defaultToUnknown)
		}
	}
	
	/**
	 * 直接基于dds文件进行解析。返回对应的PNG图片的绝对路径。
	 * @param frame 帧数。用于切割图片，默认为0，表示不切割。
	 */
	fun resolveByFile(file: VirtualFile, frame: Int = 0, defaultToUnknown: Boolean = false): String {
		try {
			//如果无法解析为png文件地址，则返回默认的地址
			val url = doResolveByFile(file, frame)
			if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
			return url
		} catch(e: Exception) {
			//如果出现异常，那么返回默认图标
			if(e !is ProcessCanceledException) {
				logger.warn("Resolve dds url failed. (dds file path: ${file.path})", e)
			}
			return getDefaultUrl(defaultToUnknown)
		}
	}
	
	/**
	 * 直接基于dds文件的相对于游戏或模组目录的路径进行解析。返回对应的PNG图片的绝对路径。
	 */
	fun resolveByFilePath(filePath: String, project: Project, frame: Int = 0, defaultToUnknown: Boolean = false): String {
		try {
			//如果无法解析为png文件地址，则返回默认的地址
			val url = doResolveByFilePath(filePath, project, frame)
			if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
			return url
		} catch(e: Exception) {
			//如果出现异常，那么返回默认图标
			if(e !is ProcessCanceledException) {
				logger.warn("Resolve dds url failed. (dds file path: ${filePath})", e)
			}
			return getDefaultUrl(defaultToUnknown)
		}
	}
	
	private fun doResolveByDefinition(definition: ParadoxScriptDefinitionElement, frame: Int , definitionInfo: ParadoxDefinitionInfo): String? {
		//兼容definition不是sprite的情况
		val resolved = runReadAction {
			definitionInfo.primaryImageConfigs.firstNotNullOfOrNull {
				it.locationExpression.resolve(definition, definitionInfo, definitionInfo.project)
			}
		} ?: return null
		if(resolved.file == null) return null
		val frameToUse = if(frame == 0) resolved.frame else frame
		return doResolveByFile(resolved.file.virtualFile, frameToUse)
	}
	
	/**
	 * 得到sprite定义的对应DDS文件的filePath。基于名为"textureFile"的定义属性（忽略大小写）。
	 */
	fun getSpriteDdsFilePath(sprite: ParadoxScriptDefinitionElement): String? {
		return sprite.findProperty("textureFile", inline = true)?.findValue<ParadoxScriptString>()?.stringValue
	}
	
	//private fun doResolveByFile(fileName: String, project: Project, frame: Int): String? {
	//	val files = FilenameIndex.getVirtualFilesByName(fileName, false, GlobalSearchScope.allScope(project))
	//	val file = files.firstOrNull() ?: return null //直接取第一个
	//	return doResolveByFile(file, frame)
	//}
	
	private fun doResolveByFile(file: VirtualFile, frame: Int): String? {
		if(file.fileType != DdsFileType) return null
		//如果可以得到相对于游戏或模组根路径的文件路径，则使用绝对根路径+相对路径定位，否则直接使用绝对路径
		val fileInfo = file.fileInfo
		val rootPath = fileInfo?.rootPath
		val ddsRelPath = fileInfo?.path?.path
		val ddsAbsPath = if(rootPath != null && ddsRelPath != null) {
			rootPath.absolutePathString() + "/" + ddsRelPath
		} else {
			file.toNioPath().absolutePathString()
		}
		return DdsToPngConverter.convert(ddsAbsPath, ddsRelPath, frame)
	}
	
	private fun doResolveByFilePath(filePath: String, project: Project, frame: Int): String? {
		val file = ParadoxFilePathSearch.search(filePath, project).find() ?: return null
		return doResolveByFile(file, frame)
	}
	
	private fun getDefaultUrl(defaultToUnknown: Boolean): String {
		return if(defaultToUnknown) DdsToPngConverter.getUnknownPngPath() else ""
	}
	
	fun getPngFile(file: VirtualFile, frame: Int = 0): VirtualFile? {
		val absPngPath = doResolveByFile(file, frame) ?: return null
		return VfsUtil.findFile(absPngPath.toPath(), true)
	}
}
