@file:Suppress("unused")

package icu.windea.pls.lang.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.io.fileSizeSafe
import icu.windea.pls.base.analysis.ChronicleAnalysisManager
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.images.ImageRenderService
import icu.windea.pls.images.dds.DdsFileType
import icu.windea.pls.images.tga.TgaFileType
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.resolve.ParadoxImageResolveResult
import icu.windea.pls.lang.resolve.ParadoxImageService
import icu.windea.pls.model.constants.ChronicleConstants
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import icu.windea.pls.model.policies.ImageRenderPolicy
import org.intellij.images.fileTypes.impl.ImageFileType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

object ParadoxImageManager {
    private val logger = logger<ParadoxImageManager>()

    fun isImageFile(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        return isImageFile(vFile)
    }

    fun isImageFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType !is ImageFileType && fileType !is DdsFileType && fileType !is TgaFileType) return false
        val extension = file.extension?.lowercase() ?: return false
        if (extension !in ChronicleConstants.imageFileExtensions) return false
        return true
    }

    fun resolveImageByDefinition(definition: ParadoxDefinitionElement, frameInfo: ImageFrameInfo? = null): ParadoxImageResolveResult? {
        return ParadoxImageService.resolveByDefinition(definition, frameInfo)
    }

    fun resolveImageByFile(file: PsiFile, frameInfo: ImageFrameInfo? = null): ParadoxImageResolveResult? {
        val vFile = file.virtualFile ?: return null
        return ParadoxImageService.resolveByFile(vFile, frameInfo)
    }

    fun resolveImageByFile(file: VirtualFile, frameInfo: ImageFrameInfo? = null): ParadoxImageResolveResult? {
        return ParadoxImageService.resolveByFile(file, frameInfo)
    }

    fun resolveImageByFilePath(filePath: String, project: Project, frameInfo: ImageFrameInfo? = null): ParadoxImageResolveResult? {
        return ParadoxImageService.resolveByFilePath(filePath, project, frameInfo)
    }

    /**
     * 基于定义解析图片的路径，返回用于渲染的图片的绝对路径。
     *
     * @param definition 输入的定义。
     * @param frameInfo 输入的帧数信息，用于切分图片，一般来自特定类型的定义的特定属性的值（例如`sprite`的`noOfFrames`）。
     * @param renderPolicy 没有渲染会话且缓存未命中时使用的渲染策略。
     */
    fun resolveUrlByDefinition(
        definition: ParadoxDefinitionElement,
        frameInfo: ImageFrameInfo? = null,
        renderPolicy: ImageRenderPolicy = ImageRenderPolicy.Sync,
    ): String? {
        return resolveUrl(resolveImageByDefinition(definition, frameInfo), definition.project, renderPolicy)
    }

    /**
     * 基于文件解析图片的路径，返回用于渲染的图片的绝对路径。
     *
     * @param file 输入的文件。
     * @param frameInfo 输入的帧数信息，用于切分图片。
     * @param renderPolicy 没有渲染会话且缓存未命中时使用的渲染策略。
     */
    fun resolveUrlByFile(
        file: PsiFile,
        frameInfo: ImageFrameInfo? = null,
        renderPolicy: ImageRenderPolicy = ImageRenderPolicy.Sync,
    ): String? {
        val vFile = file.virtualFile ?: return null
        val project = file.project
        return resolveUrl(resolveImageByFile(vFile, frameInfo), project, renderPolicy)
    }

    /**
     * 基于文件解析图片的路径，返回用于渲染的图片的绝对路径。
     *
     * @param file 输入的文件。
     * @param project 输入的项目。
     * @param frameInfo 输入的帧数信息，用于切分图片。
     * @param renderPolicy 没有渲染会话且缓存未命中时使用的渲染策略。
     */
    fun resolveUrlByFile(
        file: VirtualFile,
        project: Project,
        frameInfo: ImageFrameInfo? = null,
        renderPolicy: ImageRenderPolicy = ImageRenderPolicy.Sync,
    ): String? {
        return resolveUrl(resolveImageByFile(file, frameInfo), project, renderPolicy)
    }

    /**
     * 基于文件路径解析图片的路径，返回用于渲染的图片的绝对路径。
     *
     * @param filePath 输入的文件路径，相对于游戏或模组的根目录。
     * @param project 输入的项目。
     * @param frameInfo 输入的帧数信息，用于切分图片。
     * @param renderPolicy 没有渲染会话且缓存未命中时使用的渲染策略。
     */
    fun resolveUrlByFilePath(
        filePath: String,
        project: Project,
        frameInfo: ImageFrameInfo? = null,
        renderPolicy: ImageRenderPolicy = ImageRenderPolicy.Sync,
    ): String? {
        return resolveUrl(resolveImageByFilePath(filePath, project, frameInfo), project, renderPolicy)
    }

    /**
     * 解析图片的最终 URL。
     *
     * 说明：
     * - 如果不需要渲染，则直接返回源路径。
     * - 如果存在目标路径对应的本地缓存，则直接返回目标路径。
     * - 如果存在渲染会话，则在当前渲染会话中记录渲染请求，并返回目标路径。这适用于渲染快速文档等场景。
     * - 否则，按照渲染策略（[renderPolicy]，默认使用设置中的策略）同步渲染或异步请求渲染，并仅在同步渲染时返回目标路径。
     * - 同步渲染失败时，按照设置中的失败策略返回目标路径、`null` 或占位图路径。
     */
    fun resolveUrl(
        result: ParadoxImageResolveResult?,
        project: Project,
        renderPolicy: ImageRenderPolicy? = null,
    ): String? {
        if (result == null) return null
        if (!result.renderRequired) return result.sourcePath.absolutePathString()

        val request = result.toRenderRequest()
        val policy = renderPolicy ?: ChronicleImageSettings.getInstance().state.renderPolicy
        try {
            val renderService = ImageRenderService.getInstance()
            renderService.getCachedImagePath(request)?.let { return it.absolutePathString() }
            renderService.getRecordedImagePath(request)?.let { return it.absolutePathString() }
            return when (policy) {
                ImageRenderPolicy.Sync -> renderService.requestImageRender(request, project)?.absolutePathString()
                ImageRenderPolicy.Async -> {
                    renderService.requestImageRenderAsync(request, project).whenComplete { path, error ->
                        if (path != null && error == null) {
                            ApplicationManager.getApplication()?.invokeLater {
                                ChronicleAnalysisManager.refreshInlayHints()
                            }
                        }
                    }
                    null
                }
            }
        } catch (e: Exception) {
            checkCancellation(e)
            logger.warn("Render image failed. (source path: ${result.sourcePath})", e)
            return when (ChronicleImageSettings.getInstance().state.renderFailurePolicy) {
                ImageRenderFailurePolicy.AcceptBroken -> request.targetPath.absolutePathString()
                ImageRenderFailurePolicy.RemoveImage -> null
                ImageRenderFailurePolicy.Placeholder -> {
                    runCatching { ImageRenderService.getInstance().getPlaceholderImagePath().absolutePathString() }.getOrNull()
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun canResolve(image: ParadoxImageResolveResult?): Boolean {
        contract {
            returns() implies (image != null)
        }
        if (image == null) return false
        val sourcePath = image.sourcePath
        if (!(sourcePath.exists() && sourcePath.fileSizeSafe() > 0)) return false
        return true
    }

    @OptIn(ExperimentalContracts::class)
    fun canResolve(iconUrl: String?): Boolean {
        contract {
            returns() implies (iconUrl != null)
        }
        if (iconUrl == null) return false
        val iconFilePath = iconUrl.toPathOrNull() ?: return false
        if (!(iconFilePath.exists() && iconFilePath.fileSizeSafe() > 0)) return false
        return true
    }
}
