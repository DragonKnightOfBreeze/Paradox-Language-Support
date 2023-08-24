package icu.windea.pls.tool

import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.dds.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import org.intellij.images.fileTypes.impl.*
import java.lang.invoke.*
import kotlin.io.path.*

object ParadoxImageResolver {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    //NOTE 切分数量由sprite声明中的属性noOfFrames的值确定
    //TODO 1.0.7+ 支持切分PNG图片
    
    /**
     * 基于定义解析图片的路径。接受类型不为`sprite`的定义。返回用于渲染的图片的绝对路径。
     * @param frame 帧数。用于切割图片，默认为0，表示不切割。如果为0，但对应的定义可以获取帧数信息，则使用那个帧数。
     */
    fun resolveUrlByDefinition(definition: ParadoxScriptDefinitionElement, frame: Int = 0, defaultToUnknown: Boolean = false): String {
        val definitionInfo = definition.definitionInfo ?: return getDefaultUrl(defaultToUnknown)
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveUrlByDefinition(definition, frame, definitionInfo)
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
     * 基于文件解析图片的路径。返回用于渲染的图片的绝对路径。
     * @param frame 帧数。用于切割图片，默认为0，表示不切割。
     */
    fun resolveUrlByFile(file: VirtualFile, frame: Int = 0, defaultToUnknown: Boolean = false): String {
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveUrlByFile(file, frame)
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
     * 基于文件路径解析图片的路径。输入的文件路径需要相对于游戏或模组的根目录。返回用于渲染的图片的绝对路径。
     */
    fun resolveUrlByFilePath(filePath: String, project: Project, frame: Int = 0, defaultToUnknown: Boolean = false): String {
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveUrlByFilePath(filePath, project, frame)
            if(url.isNullOrEmpty()) return getDefaultUrl(defaultToUnknown)
            return url
        } catch(e: Exception) {
            //如果出现异常，那么返回默认图标
            if(e is ProcessCanceledException) throw e
            logger.warn("Resolve dds url failed. (dds file path: ${filePath})", e)
            return getDefaultUrl(defaultToUnknown)
        }
    }
    
    private fun doResolveUrlByDefinition(definition: ParadoxScriptDefinitionElement, frame: Int, definitionInfo: ParadoxDefinitionInfo): String? {
        //兼容definition不是sprite的情况
        val resolved = runReadAction {
            definitionInfo.primaryImages.firstNotNullOfOrNull {
                it.locationExpression.resolve(definition, definitionInfo, definitionInfo.project)
            }
        } ?: return null
        if(resolved.file == null) return null
        val frameToUse = if(frame == 0) resolved.frame else frame
        return doResolveUrlByFile(resolved.file.virtualFile, frameToUse)
    }
    
    private fun doResolveUrlByFile(file: VirtualFile, frame: Int): String? {
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
        return ParadoxDdsResolver.resolveUrl(ddsAbsPath, ddsRelPath, frame)
    }
    
    private fun doResolveUrlByFilePath(filePath: String, project: Project, frame: Int): String? {
        val file = ParadoxFilePathSearch.search(filePath, null, nopSelector(project)).find() ?: return null
        return doResolveUrlByFile(file, frame)
    }
    
    private fun getDefaultUrl(defaultToUnknown: Boolean): String {
        return if(defaultToUnknown) ParadoxDdsResolver.getUnknownPngUrl() else ""
    }
    
    fun getPngFile(ddsFile: VirtualFile, frame: Int = 0): VirtualFile? {
        if(ddsFile.fileType != DdsFileType) return null // input file must be a dds file 
        val absPngPath = doResolveUrlByFile(ddsFile, frame) ?: return null
        return VfsUtil.findFile(absPngPath.toPath(), true)
    }
}
