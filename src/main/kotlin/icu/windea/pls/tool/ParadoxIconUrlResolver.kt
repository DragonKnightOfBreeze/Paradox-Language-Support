package icu.windea.pls.tool

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*
import org.slf4j.*
import java.lang.invoke.*
import java.nio.file.*

/**
 * 图标地址的解析器。
 */
@Suppress("unused")
object ParadoxIconUrlResolver {
	private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
	
	//TODO 基于定义中的icon属性进行解析
	
	/**
	 * 基于本地化文件中的本地化图标的名字进行解析。
	 */
	fun resolveByIconName(iconName: String, project: Project, defaultToUnknown: Boolean = true): String {
		if(iconName.isEmpty()) return getDefaultUrl(defaultToUnknown)
		try {
			//如果无法解析为png文件地址，则返回默认的地址
			val url = doResolveByIconName(iconName, project)
			if(url.isNullOrEmpty() || Files.notExists(url.toPath())) return getDefaultUrl(defaultToUnknown)
			return url
		} catch(e: Exception) {
			//如果出现异常，则返回默认图标
			logger.warn(e) { "Resolve paradox icon failed. (name: $iconName)" }
			return getDefaultUrl(defaultToUnknown)
		}
	}
	
	/**
	 * 基于gfx文件中的类型为spriteType的定义进行解析。
	 */
	fun resolveBySprite(sprite: ParadoxScriptProperty, defaultToUnknown: Boolean = true): String {
		val spriteName = sprite.definitionInfo?.name
		if(spriteName.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
		try {
			//如果无法解析为png文件地址，则返回默认的地址
			val url = doResolveBySprite(sprite)
			if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
			return url
		} catch(e: Exception) {
			//如果出现异常，那么返回默认图标
			logger.warn(e) { "Resolve paradox icon failed. (sprite name: $spriteName)" }
			return getDefaultUrl(defaultToUnknown)
		}
	}
	
	/**
	 * 直接基于dds的文件名进行解析。
	 */
	fun resolveByFile(file: VirtualFile, defaultToUnknown: Boolean = true): String {
		//NOTE 这些文件应当位于interface/icons目录（及其子目录）下，暂时不做限制
		try {
			//如果无法解析为png文件地址，则返回默认的地址
			val url = doResolveByFile(file)
			if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
			return url
		} catch(e: Exception) {
			//如果出现异常，那么返回默认图标
			logger.warn(e) { "Resolve paradox icon failed. (dds file path: ${file.path})" }
			return getDefaultUrl(defaultToUnknown)
		}
	}
	
	private fun doResolveByIconName(iconName: String, project: Project): String? {
		return doResolveBySprite("GFX_text_${iconName}", project)
			?: doResolveByFile("$iconName.dds", project)
	}
	
	private fun doResolveBySprite(spriteName: String, project: Project): String? {
		val sprite = findDefinitionByType(spriteName, "sprite|spriteType", project) ?: return null
		return doResolveBySprite(sprite)
	}
	
	private fun doResolveBySprite(sprite: ParadoxScriptProperty): String? {
		val ddsRelPath = sprite.findProperty("textureFile", true)?.value ?: return null
		val file = findFile(ddsRelPath, sprite.project) ?: return null
		return doResolveByFile(file)
	}
	
	private fun doResolveByFile(fileName: String, project: Project): String? {
		val files = FilenameIndex.getVirtualFilesByName(fileName, false, GlobalSearchScope.allScope(project))
		val file = files.firstOrNull() ?: return null //直接取第一个
		return doResolveByFile(file)
	}
	
	private fun doResolveByFile(file: VirtualFile): String? {
		val fileInfo = file.fileInfo ?: return null
		val rootPath = fileInfo.rootPath
		val ddsRelPath = fileInfo.path.path
		val ddsAbsPath = rootPath.resolve(ddsRelPath).normalize().toString()
		return DdsToPngConverter.convert(ddsAbsPath, ddsRelPath)
	}
	
	private fun getDefaultUrl(defaultToUnknown: Boolean): String {
		return if(defaultToUnknown) DdsToPngConverter.getUnknownPngPath() else ""
	}
}

