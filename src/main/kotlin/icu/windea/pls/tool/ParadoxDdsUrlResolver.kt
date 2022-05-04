package icu.windea.pls.tool

import com.intellij.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.openapi.vfs.newvfs.events.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.dds.*
import icu.windea.pls.script.psi.*
import org.slf4j.*
import java.lang.invoke.*
import kotlin.io.path.*

/**
 * DDS图片地址的解析器。
 */
@Suppress("unused")
object ParadoxDdsUrlResolver {
	private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
	
	/**
	 * 基于gfx文件中的类型为sprite的定义进行解析。
	 */
	fun resolveBySprite(sprite: ParadoxDefinitionProperty, defaultToUnknown: Boolean = false): String {
		val spriteName = sprite.definitionInfo?.name
		if(spriteName.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
		try {
			//如果无法解析为png文件地址，则返回默认的地址
			val url = doResolveBySprite(sprite)
			if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
			return url
		} catch(e: Exception) {
			//如果出现异常，那么返回默认图标
			logger.warn(e) { "Resolve dds url failed. (sprite name: $spriteName)" }
			return getDefaultUrl(defaultToUnknown)
		}
	}
	
	/**
	 * 直接基于dds文件的文件名进行解析。
	 */
	fun resolveByFile(file: VirtualFile, defaultToUnknown: Boolean = false): String {
		try {
			//如果无法解析为png文件地址，则返回默认的地址
			val url = doResolveByFile(file)
			if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
			return url
		} catch(e: Exception) {
			//如果出现异常，那么返回默认图标
			logger.warn(e) { "Resolve dds url failed. (dds file path: ${file.path})" }
			return getDefaultUrl(defaultToUnknown)
		}
	}
	
	/**
	 * 直接基于dds文件的相对于游戏或模组目录的路径进行解析。
	 */
	fun resolveByFilePath(filePath: String, project: Project, defaultToUnknown: Boolean = false): String {
		try {
			//如果无法解析为png文件地址，则返回默认的地址
			val url = doResolveByFilePath(filePath, project)
			if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
			return url
		} catch(e: Exception) {
			//如果出现异常，那么返回默认图标
			logger.warn(e) { "Resolve dds url failed. (dds file path: ${filePath})" }
			return getDefaultUrl(defaultToUnknown)
		}
	}
	
	private fun doResolveBySprite(spriteName: String, project: Project): String? {
		val sprite = findDefinitionByType(spriteName, "sprite|spriteType", project) ?: return null
		return doResolveBySprite(sprite)
	}
	
	private fun doResolveBySprite(sprite: ParadoxDefinitionProperty): String? {
		val ddsRelPath = getSpriteDdsFilePath(sprite) ?: return null
		val file = findFileByFilePath(ddsRelPath, sprite.project) ?: return null
		return doResolveByFile(file)
	}
	
	private fun doResolveByFile(fileName: String, project: Project): String? {
		val files = FilenameIndex.getVirtualFilesByName(fileName, false, GlobalSearchScope.allScope(project))
		val file = files.firstOrNull() ?: return null //直接取第一个
		return doResolveByFile(file)
	}
	
	private fun doResolveByFile(file: VirtualFile): String? {
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
		return DdsToPngConverter.convert(ddsAbsPath, ddsRelPath)
	}
	
	private fun doResolveByFilePath(filePath: String, project: Project): String? {
		val file = findFileByFilePath(filePath, project) ?: return null
		return doResolveByFile(file)
	}
	
	private fun getDefaultUrl(defaultToUnknown: Boolean): String {
		return if(defaultToUnknown) DdsToPngConverter.getUnknownPngPath() else ""
	}
	
	fun getPngFile(file: VirtualFile): VirtualFile?{
		val absPngPath = doResolveByFile(file) ?: return null
		return VfsUtil.findFile(absPngPath.toPath(), true)
	}
}