package icu.windea.pls.lang.util.image

import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.io.fileSizeSafe
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.images.dds.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import org.intellij.images.fileTypes.impl.*
import java.lang.invoke.*
import kotlin.contracts.*
import kotlin.io.path.*

object ParadoxImageResolver {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    /**
     * 基于定义解析图片的路径，返回用于渲染的图片的绝对路径。
     *
     * @param definition 输入的定义。
     * @param frameInfo 输入的帧数信息，用于切分图片，一般来自特定类型的定义的特定属性的值（例如`sprite`的`noOfFrames`）。
     */
    fun resolveUrlByDefinition(definition: ParadoxScriptDefinitionElement, frameInfo: ImageFrameInfo? = null): String? {
        val definitionInfo = definition.definitionInfo ?: return null
        val newFrameInfo = when {
            frameInfo == null -> null
            definitionInfo.type == ParadoxDefinitionTypes.Sprite -> {
                val noOfFrames = definition.getData<ParadoxSpriteData>()?.noOfFrames
                if (noOfFrames != null) ImageFrameInfo.of(frameInfo.frame, noOfFrames) else frameInfo
            }
            else -> frameInfo
        }
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveUrlByDefinition(definition, definitionInfo, newFrameInfo)
            if (url.isNullOrEmpty()) return null
            return url
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.warn("Resolve url for dds image failed. (definition name: ${definitionInfo.name.orAnonymous()})", e)
            return null
        }
    }

    /**
     * 基于文件解析图片的路径，返回用于渲染的图片的绝对路径。
     *
     * @param file 输入的文件。
     * @param frameInfo 输入的帧数信息，用于切分图片，
     */
    fun resolveUrlByFile(file: VirtualFile, project: Project, frameInfo: ImageFrameInfo? = null): String? {
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveUrlByFile(file, project, frameInfo)
            if (url.isNullOrEmpty()) return null
            return url
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.warn("Resolve url for dds image failed. (dds file path: ${file.path})", e)
            return null
        }
    }

    /**
     * 基于文件路径解析图片的路径，返回用于渲染的图片的绝对路径。
     *
     * @param filePath 输入的文件路径，相对于游戏或模组的根目录。
     * @param project 输入的项目。
     * @param frameInfo 输入的帧数信息，用于切分图片，
     */
    fun resolveUrlByFilePath(filePath: String, project: Project, frameInfo: ImageFrameInfo? = null): String? {
        try {
            //如果无法解析为png文件地址，则返回默认的地址
            val url = doResolveUrlByFilePath(filePath, project, frameInfo)
            if (url.isNullOrEmpty()) return null
            return url
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.warn("Resolve url for dds image failed. (dds file path: ${filePath})", e)
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
        return doResolveUrlByFile(resolvedFile.virtualFile, resolvedFile.project, resolved.frameInfo)
    }

    private fun doResolveUrlByFile(file: VirtualFile, project: Project, frameInfo: ImageFrameInfo?): String? {
        return when (file.fileType) {
            is ImageFileType -> {
                //accept normal image files (e.g., png file)
                file.toNioPath().absolutePathString()
            }
            is DdsFileType -> {
                //convert dds file to png file and then return png file's actual url
                val fileInfo = file.fileInfo
                val ddsRelPath = fileInfo?.let { it.rootInfo.gameType.id + "/" + it.path.path }
                val ddsAbsPath = file.toNioPath().absolutePathString()
                ParadoxDdsImageResolver.resolveUrl(project, ddsAbsPath, ddsRelPath, frameInfo)
            }
            else -> null
        }
    }

    private fun doResolveUrlByFilePath(filePath: String, project: Project, frameInfo: ImageFrameInfo?): String? {
        val file = ParadoxFilePathSearch.search(filePath, null, selector(project).file()).find() ?: return null
        return doResolveUrlByFile(file, project, frameInfo)
    }

    @OptIn(ExperimentalContracts::class)
    fun canResolve(iconUrl: String?): Boolean {
        contract {
            returns() implies (iconUrl != null)
        }
        if (iconUrl == null) return false
        val iconFilePath = iconUrl.toPathOrNull()
        if (iconFilePath == null) return false
        if (!(iconFilePath.exists() && iconFilePath.fileSizeSafe() > 0L)) return false
        return true
    }
}
