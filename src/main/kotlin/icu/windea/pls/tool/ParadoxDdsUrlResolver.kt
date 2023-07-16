package icu.windea.pls.tool

import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.dds.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import org.intellij.images.fileTypes.impl.*
import java.lang.invoke.*
import kotlin.io.path.*

/**
 * 图片地址的解析器。用于从定义、图片文件、图片路径解析得到用于渲染的图片路径。输入的图片可以是PNG图片和PNG图片。
 */
object ParadoxDdsUrlResolver {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    /**
     * 基于定义进行解析。接受类型不为`sprite`的定义。返回用于渲染的图片的绝对路径。
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
            if(e is ProcessCanceledException) throw e
            logger.warn("Resolve dds url failed. (definition name: ${definitionInfo.name.orAnonymous()})", e)
            return getDefaultUrl(defaultToUnknown)
        }
    }
    
    /**
     * 基于文件进行解析。返回用于渲染的图片的绝对路径。
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
            if(e is ProcessCanceledException) throw e
            logger.warn("Resolve dds url failed. (dds file path: ${file.path})", e)
            return getDefaultUrl(defaultToUnknown)
        }
    }
    
    /**
     * 基于文件路径进行解析。输入的文件路径需要相对于游戏或模组的根目录。返回用于渲染的图片的绝对路径。
     */
    fun resolveByFilePath(filePath: String, project: Project, frame: Int = 0, defaultToUnknown: Boolean = false): String {
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveByFilePath(filePath, project, frame)
            if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
            return url
        } catch(e: Exception) {
            //如果出现异常，那么返回默认图标
            if(e is ProcessCanceledException) throw e
            logger.warn("Resolve dds url failed. (dds file path: ${filePath})", e)
            return getDefaultUrl(defaultToUnknown)
        }
    }
    
    private fun doResolveByDefinition(definition: ParadoxScriptDefinitionElement, frame: Int, definitionInfo: ParadoxDefinitionInfo): String? {
        //兼容definition不是sprite的情况
        val resolved = runReadAction {
            definitionInfo.primaryImages.firstNotNullOfOrNull {
                it.locationExpression.resolve(definition, definitionInfo, definitionInfo.project)
            }
        } ?: return null
        if(resolved.file == null) return null
        val frameToUse = if(frame == 0) resolved.frame else frame
        return doResolveByFile(resolved.file.virtualFile, frameToUse)
    }
    
    private fun doResolveByFile(file: VirtualFile, frame: Int): String? {
        val fileType = file.fileType
        if(fileType == ImageFileType.INSTANCE && file.extension?.lowercase().let { it == "png" }) {
            //accept png file
            return file.toNioPath().absolutePathString()
        }
        if(fileType != DdsFileType) return null
        
        //如果可以得到相对于游戏或模组根路径的文件路径，则使用绝对根路径+相对路径定位，否则直接使用绝对路径
        val fileInfo = file.fileInfo
        val rootPath = fileInfo?.rootInfo?.gameRootPath
        val ddsRelPath = fileInfo?.path?.path
        val ddsAbsPath = if(rootPath != null && ddsRelPath != null) {
            rootPath.absolutePathString() + "/" + ddsRelPath
        } else {
            file.toNioPath().absolutePathString()
        }
        return DdsConverter.convertUrl(ddsAbsPath, ddsRelPath, frame)
    }
    
    private fun doResolveByFilePath(filePath: String, project: Project, frame: Int): String? {
        val file = ParadoxFilePathSearch.search(filePath, null, nopSelector(project)).find() ?: return null
        return doResolveByFile(file, frame)
    }
    
    private fun getDefaultUrl(defaultToUnknown: Boolean): String {
        return if(defaultToUnknown) DdsConverter.getUnknownPngUrl() else ""
    }
    
    fun getPngFile(ddsFile: VirtualFile, frame: Int = 0): VirtualFile? {
        if(ddsFile.fileType != DdsFileType) return null // input file must be a dds file 
        val absPngPath = doResolveByFile(ddsFile, frame) ?: return null
        return VfsUtil.findFile(absPngPath.toPath(), true)
    }
}
