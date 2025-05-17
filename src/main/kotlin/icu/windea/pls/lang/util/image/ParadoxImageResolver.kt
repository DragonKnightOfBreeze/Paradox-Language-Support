package icu.windea.pls.lang.util.image

import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.dds.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.*
import org.intellij.images.fileTypes.impl.*
import kotlin.io.path.*

object ParadoxImageResolver {
    //支持切分PNG图片
    //切分数量由sprite声明中的属性noOfFrames的值确定

    /**
     * 基于定义解析图片的路径。接受类型不为`sprite`的定义。返回用于渲染的图片的绝对路径。
     */
    fun resolveUrlByDefinition(definition: ParadoxScriptDefinitionElement, frameInfo: ImageFrameInfo? = null): String? {
        val definitionInfo = definition.definitionInfo ?: return null
        val newFrameInfo = when {
            frameInfo == null -> null
            definitionInfo.type == ParadoxDefinitionTypes.Sprite -> {
                val noOfFrames = definition.getData<ParadoxSpriteData>()?.noOfFrames
                if (noOfFrames != null) frameInfo.copy(frames = noOfFrames) else frameInfo
            }
            else -> frameInfo
        }
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveUrlByDefinition(definition, definitionInfo, newFrameInfo)
            if (url.isNullOrEmpty()) return null
            return url
        } catch (e: Exception) {
            //如果出现异常，那么返回默认图标
            if (e is ProcessCanceledException) throw e
            thisLogger().warn("Resolve dds url failed. (definition name: ${definitionInfo.name.orAnonymous()})", e)
            return null
        }
    }

    /**
     * 基于文件解析图片的路径。返回用于渲染的图片的绝对路径。
     */
    fun resolveUrlByFile(file: VirtualFile, frameInfo: ImageFrameInfo? = null): String? {
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveUrlByFile(file, frameInfo)
            if (url.isNullOrEmpty()) return null
            return url
        } catch (e: Exception) {
            //如果出现异常，那么返回默认图标
            if (e is ProcessCanceledException) throw e
            thisLogger().warn("Resolve dds url failed. (dds file path: ${file.path})", e)
            return null
        }
    }

    /**
     * 基于文件路径解析图片的路径。输入的文件路径需要相对于游戏或模组的根目录。返回用于渲染的图片的绝对路径。
     */
    fun resolveUrlByFilePath(filePath: String, project: Project, frameInfo: ImageFrameInfo? = null): String? {
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveUrlByFilePath(filePath, project, frameInfo)
            if (url.isNullOrEmpty()) return null
            return url
        } catch (e: Exception) {
            //如果出现异常，那么返回默认图标
            if (e is ProcessCanceledException) throw e
            thisLogger().warn("Resolve dds url failed. (dds file path: ${filePath})", e)
            return null
        }
    }

    private fun doResolveUrlByDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, frameInfo: ImageFrameInfo?): String? {
        //兼容definition不是sprite的情况
        val resolved = runReadAction {
            definitionInfo.primaryImages.firstNotNullOfOrNull {
                CwtLocationExpressionManager.resolve(it.locationExpression, definition, definitionInfo, frameInfo, toFile = true)
            }
        } ?: return null
        val resolvedFile = resolved.element?.castOrNull<PsiFile>() ?: return null
        return doResolveUrlByFile(resolvedFile.virtualFile, resolved.frameInfo)
    }

    private fun doResolveUrlByFile(file: VirtualFile, frameInfo: ImageFrameInfo?): String? {
        return when (file.fileType) {
            ImageFileType.INSTANCE -> {
                //accept normal image files (e.g., png file)
                file.toNioPath().absolutePathString()
            }
            DdsFileType -> {
                //convert dds file to png file and then return png file's actual url
                val fileInfo = file.fileInfo
                val ddsRelPath = fileInfo?.let { it.rootInfo.gameType.id + "/" + it.path.path }
                val ddsAbsPath = file.toNioPath().absolutePathString()
                ParadoxDdsImageResolver.resolveUrl(ddsAbsPath, ddsRelPath, frameInfo)
            }
            else -> null
        }
    }

    private fun doResolveUrlByFilePath(filePath: String, project: Project, frameInfo: ImageFrameInfo?): String? {
        val file = ParadoxFilePathSearch.search(filePath, null, selector(project).file()).find() ?: return null
        return doResolveUrlByFile(file, frameInfo)
    }

    fun getDefaultUrl(): String {
        return getUnknownPngUrl()
    }

    private fun getUnknownPngUrl(): String {
        return PlsConstants.Paths.unknownPngFile.path
    }
}
