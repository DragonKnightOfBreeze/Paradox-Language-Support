@file:Suppress("unused")

package icu.windea.pls.lang.resolve

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.io.fileSizeSafe
import icu.windea.pls.base.analysis.ChronicleAnalysisManager
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.base.settings.ChronicleImageSettings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.toUUID
import icu.windea.pls.core.util.ImageFrameInfo
import icu.windea.pls.core.util.io.toPathOrNull
import icu.windea.pls.images.ImageRenderService
import icu.windea.pls.images.ImageRenderSession
import icu.windea.pls.images.ImageService
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.util.ParadoxSpriteManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ChronicleConstants
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.policies.ImageRenderFailurePolicy
import icu.windea.pls.model.policies.ImageRenderPolicy
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

object ParadoxImageService {
    private val logger = logger<ParadoxImageService>()

    fun isImageFile(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        return isImageFile(vFile)
    }

    fun isImageFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (!ImageService.getInstance().isImageFileType(fileType)) return false
        val extension = file.extension?.lowercase() ?: return false
        if (extension !in ChronicleConstants.imageFileExtensions) return false
        return true
    }

    /**
     * 基于指定的定义（[definition]）解析图片。
     *
     * 输入的帧数信息（[frameInfo]）用于切分图片，通常来自特定类型的定义的特定属性的值。
     * 例如，`sprite` 的 `noOfFrames`。
     */
    fun resolveByDefinition(definition: ParadoxDefinitionElement, frameInfo: ImageFrameInfo? = null): ParadoxImageResolveResult? {
        val definitionInfo = definition.definitionInfo ?: return null
        val newFrameInfo = when {
            frameInfo == null -> null
            definitionInfo.type == ParadoxDefinitionTypes.sprite -> ParadoxSpriteManager.getFrameInfo(definition, frameInfo)
            else -> frameInfo
        }
        try {
            return doResolveByDefinition(definition, definitionInfo, newFrameInfo)
        } catch (e: Exception) {
            checkCancellation(e)
            logger.warn("Resolve image by definition failed. (definition name: ${definitionInfo.name.orAnonymous()})", e)
            return null
        }
    }

    /**
     * 基于指定的文件（[file]）解析图片。
     *
     * 输入的帧数信息（[frameInfo]）用于切分图片。
     */
    fun resolveByFile(file: PsiFile, frameInfo: ImageFrameInfo? = null): ParadoxImageResolveResult? {
        val vFile = file.virtualFile ?: return null
        return resolveByFile(vFile, frameInfo)
    }

    /**
     * 基于指定的文件（[file]）解析图片。
     *
     * 输入的帧数信息（[frameInfo]）用于切分图片。
     */
    fun resolveByFile(file: VirtualFile, frameInfo: ImageFrameInfo? = null): ParadoxImageResolveResult? {
        try {
            return doResolve(file, frameInfo)
        } catch (e: Exception) {
            checkCancellation(e)
            logger.warn("Resolve image by file failed. (file path: ${file.path})", e)
            return null
        }
    }

    /**
     * 基于指定的文件路径（[filePath]）解析图片。
     *
     * 输入的帧数信息（[frameInfo]）用于切分图片。
     */
    fun resolveByFilePath(filePath: String, project: Project, frameInfo: ImageFrameInfo? = null): ParadoxImageResolveResult? {
        try {
            val selector = ParadoxFilePathSearch.selector(project)
            val query = ParadoxFilePathSearch.searchImage(filePath, null, selector) // 3.0.1 optimize: limit file extensions
            val file = query.find() ?: return null
            return doResolve(file, frameInfo)
        } catch (e: Exception) {
            checkCancellation(e)
            logger.warn("Resolve image by file path failed. (file path: $filePath)", e)
            return null
        }
    }

    private fun doResolveByDefinition(
        definition: ParadoxDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        frameInfo: ImageFrameInfo?,
    ): ParadoxImageResolveResult? {
        // 兼容 `definition` 不是 `sprite` 的情况
        val resolved = runSmartReadAction {
            definitionInfo.primaryImages.firstNotNullOfOrNull {
                ParadoxLocationService.resolve(it.locationExpression, definition, definitionInfo, frameInfo, toFile = true)
            }
        }
        if (resolved !is ParadoxLocationResolveResult.Image.Static) return null
        val resolvedFile = resolved.element?.castOrNull<PsiFile>() ?: return null
        return doResolve(resolvedFile.virtualFile, resolved.frameInfo)
    }

    private fun doResolve(file: VirtualFile, frameInfo: ImageFrameInfo?): ParadoxImageResolveResult? {
        // accept various image file types (normal file types such as png, or extended file aka dds and tga)
        if (!ImageService.getInstance().isImageFileType(file.fileType)) return null

        val sourcePath = file.toNioPath()
        val finalFrameInfo = frameInfo?.takeIf { it.canApply() }
        val targetPath = if (finalFrameInfo == null) {
            sourcePath
        } else {
            doResolveImagePath(file, sourcePath, finalFrameInfo)
        }
        return ParadoxImageResolveResult(sourcePath, targetPath, finalFrameInfo)
    }

    private fun doResolveImagePath(file: VirtualFile, sourcePath: Path, frameInfo: ImageFrameInfo): Path {
        val imagesPath = ChronicleDataPathService.getInstance().imagesPath
        val imageAbsPath = sourcePath.absolutePathString().normalizePath()
        val imageRelPath = file.fileInfo?.let { it.gameType.id + "/" + it.path.path }?.normalizePath()
        // UUID：
        // - 基于源文件路径、大小、修改时间、帧数信息和目标格式
        // - 源文件变化后会得到不同的缓存路径
        val uuid = buildCacheUuid(sourcePath, imageAbsPath, frameInfo)
        val frameText = "@${frameInfo.frame}_${frameInfo.frames}"
        return if (imageRelPath != null) {
            // 路径：
            // - `~/.windea/chronicle/images/{relPathWithoutExtension}@{uuid}.png`
            // - `~/.windea/chronicle/images/{relPathWithoutExtension}@{frame}_{frames}@{uuid}.png`
            val relPathWithoutExtension = imageRelPath.substringBeforeLast('.')
            val finalPath = "${relPathWithoutExtension}${frameText}@${uuid}.png"
            imagesPath.resolve(finalPath).toAbsolutePath()
        } else {
            // 路径：
            // - `~/.windea/chronicle/images/_external/{fileNameWithoutExtension}@{uuid}.png`
            // - `~/.windea/chronicle/images/_external/{fileNameWithoutExtension}@{frame}_{frames}@{uuid}.png`
            val index = imageAbsPath.lastIndexOf('/')
            val fileName = if (index == -1) imageAbsPath else imageAbsPath.substring(index + 1)
            val fileNameWithoutExtension = fileName.substringBeforeLast('.')
            val finalPath = "_external/${fileNameWithoutExtension}${frameText}@${uuid}.png"
            imagesPath.resolve(finalPath).toAbsolutePath()
        }
    }

    private fun buildCacheUuid(sourcePath: Path, imageAbsPath: String, frameInfo: ImageFrameInfo): String {
        val size = runCatching { Files.size(sourcePath) }.getOrDefault(0L)
        val modificationStamp = runCatching { Files.getLastModifiedTime(sourcePath).toMillis() }.getOrDefault(0L)
        val key = "$imageAbsPath|$size|$modificationStamp|${frameInfo.frame}|${frameInfo.frames}|png"
        return key.toUUID().toString()
    }

    fun canResolve(iconUrl: String): Boolean {
        val iconFilePath = iconUrl.toPathOrNull() ?: return false
        if (!(iconFilePath.exists() && iconFilePath.fileSizeSafe() > 0)) return false
        return true
    }

    fun canResolve(image: ParadoxImageResolveResult): Boolean {
        val sourcePath = image.sourcePath
        if (!(sourcePath.exists() && sourcePath.fileSizeSafe() > 0)) return false
        return true
    }

    /**
     * 解析得到图片的最终地址。
     *
     * 说明：
     * - 如果不需要渲染，则直接返回源路径。
     * - 如果存在目标路径对应的本地缓存，则直接返回目标路径。
     * - 如果存在渲染会话，则在当前渲染会话中记录渲染请求，并返回目标路径。这适用于渲染快速文档等场景。
     * - 否则，按照渲染策略（[renderPolicy]，默认使用设置中的策略）同步渲染或异步请求渲染，并仅在同步渲染时返回目标路径。
     * - 同步渲染失败时，按照设置中的失败策略返回目标路径、`null` 或占位图路径。
     */
    fun resolveUrl(result: ParadoxImageResolveResult, project: Project, renderPolicy: ImageRenderPolicy? = null, imageSession: ImageRenderSession? = null): String? {
        if (!result.renderRequired) return result.sourcePath.absolutePathString()

        val request = result.toRenderRequest()
        val policy = renderPolicy ?: ChronicleImageSettings.getInstance().state.renderPolicy
        try {
            val renderService = ImageRenderService.getInstance()
            renderService.getCachedImagePath(request)?.let { return it.absolutePathString() }
            val recordedPath = if (imageSession != null) {
                renderService.getRecordedImagePath(request, imageSession)
            } else {
                renderService.getRecordedImagePath(request)
            }
            recordedPath?.let { return it.absolutePathString() }
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
}
