package icu.windea.pls.tool

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import org.slf4j.*
import java.nio.file.*
import java.util.concurrent.*

/**
 * 图标地址的解析器。
 */
@Suppress("unused")
object ParadoxIconUrlResolver {
	private val logger = LoggerFactory.getLogger(ParadoxIconUrlResolver::class.java)
	
	private val nameUrlCache = ConcurrentHashMap<String, String>()
	private val spriteNameUrlCache = ConcurrentHashMap<String, String>()
	private val pathUrlCache = ConcurrentHashMap<String, String>()
	
	fun resolveByName(name: String, project: Project, defaultToUnknown: Boolean = true): String {
		if(name.isEmpty()) return getDefaultUrl(defaultToUnknown)
		return try {
			var url = nameUrlCache[name]
			//如果缓存中没有或者对应的本地png文件不存在，需要重新获取
			if(url.isNullOrEmpty() || Files.notExists(url.toPath())) {
				url = doResolveByName(name, project)
				nameUrlCache[name] = url.orEmpty()
			}
			//如果还是没有得到或者对应的本地png文件尚未生成完毕，则返回默认的url
			if(url.isNullOrEmpty() || Files.notExists(url.toPath())) getDefaultUrl(defaultToUnknown) else url
		} catch(e: Exception) {
			logger.warn("Resolve paradox icon failed.", e)
			//如果出现异常，那么返回默认图标
			getDefaultUrl(defaultToUnknown)
		}
	}
	
	fun resolveBySprite(sprite: ParadoxScriptProperty, defaultToUnknown: Boolean = true): String {
		val spriteName = sprite.definitionInfo?.name
		if(spriteName.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
		return try {
			var url = spriteNameUrlCache[spriteName]
			//如果缓存中没有或者对应的本地png文件不存在，需要重新获取
			if(url.isNullOrEmpty() || Files.notExists(url.toPath())) {
				url = doResolveBySprite(sprite)
				spriteNameUrlCache[spriteName] = url.orEmpty()
			}
			//如果还是没有得到或者对应的本地png文件尚未生成完毕，则返回默认的url
			if(url.isNullOrEmpty() || Files.notExists(url.toPath())) getDefaultUrl(defaultToUnknown) else url
		} catch(e: Exception) {
			logger.warn("Resolve paradox icon failed.", e)
			//如果出现异常，那么返回默认图标
			getDefaultUrl(defaultToUnknown)
		}
	}
	
	fun resolveByFile(file: VirtualFile, defaultToUnknown: Boolean = true): String {
		val path = file.fileInfo?.path?.toString()
		if(path.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
		return try {
			var url = pathUrlCache[path]
			//如果缓存中没有或者对应的本地png文件不存在，需要重新获取
			if(url.isNullOrEmpty() || Files.notExists(url.toPath())) {
				url = doResolveByFile(file)
				pathUrlCache[path] = url.orEmpty()
			}
			//如果还是没有得到或者对应的本地png文件尚未生成完毕，则返回默认的url
			if(url.isNullOrEmpty() || Files.notExists(url.toPath())) getDefaultUrl(defaultToUnknown) else url
		} catch(e: Exception) {
			logger.warn("Resolve paradox icon failed.", e)
			//如果出现异常，那么返回默认图标
			getDefaultUrl(defaultToUnknown)
		}
	}
	
	fun resolveByFile(file: PsiFile, defaultToUnknown: Boolean = true): String {
		val path = file.fileInfo?.path?.toString()
		if(path.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
		return try {
			var url = pathUrlCache[path]
			//如果缓存中没有或者对应的本地png文件不存在，需要重新获取
			if(url.isNullOrEmpty() || Files.notExists(url.toPath())) {
				url = doResolveByFile(file)
				pathUrlCache[path] = url.orEmpty()
			}
			//如果还是没有得到或者对应的本地png文件尚未生成完毕，则返回默认的url
			if(url.isNullOrEmpty() || Files.notExists(url.toPath())) getDefaultUrl(defaultToUnknown) else url
		} catch(e: Exception) {
			logger.warn("Resolve paradox icon failed.", e)
			//如果出现异常，那么返回默认图标
			getDefaultUrl(defaultToUnknown)
		}
	}
	
	//基于本地化文件中本地化图标的名字进行解析
	private fun doResolveByName(name: String, project: Project): String? {
		return doResolveBySprite("GFX_text_${name}", project) ?: doResolveByFile("$name.dds", project)
	}
	
	//基于gfx文件中的spriteType的定义进行解析
	private fun doResolveBySprite(spriteName: String, project: Project): String? {
		val sprite = findDefinitionByType(spriteName, "sprite", project)
			?: findDefinitionByType(spriteName, "spriteType", project) ?: return null
		return doResolveBySprite(sprite)
	}
	
	private fun doResolveBySprite(sprite: ParadoxScriptProperty): String? {
		val fileInfo = sprite.fileInfo ?: return null
		val rootPath = fileInfo.rootPath
		val ddsRelPath = sprite.findProperty("textureFile", true)?.value ?: return null
		val ddsAbsPath = rootPath.resolve(ddsRelPath).toString()
		return DdsToPngConverter.convert(ddsAbsPath, ddsRelPath)
	}
	
	//直接基于dds的文件名进行解析
	private fun doResolveByFile(fileName: String, project: Project): String? {
		val files = FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.allScope(project))
		val file = files.firstOrNull() ?: return null
		return doResolveByFile(file)
	}
	
	private fun doResolveByFile(file: VirtualFile): String? {
		val fileInfo = file.fileInfo ?: return null
		val rootPath = fileInfo.rootPath
		val ddsRelPath = fileInfo.path.path
		val ddsAbsPath = rootPath.resolve(ddsRelPath).toString()
		return DdsToPngConverter.convert(ddsAbsPath, ddsRelPath)
	}
	
	private fun doResolveByFile(file: PsiFile): String? {
		val fileInfo = file.fileInfo ?: return null
		val rootPath = fileInfo.rootPath
		val ddsRelPath = fileInfo.path.path
		val ddsAbsPath = rootPath.resolve(ddsRelPath).toString()
		return DdsToPngConverter.convert(ddsAbsPath, ddsRelPath)
	}
	
	private fun getDefaultUrl(defaultToUnknown: Boolean): String {
		return if(defaultToUnknown) DdsToPngConverter.getUnknownPngPath() else ""
	}
}

