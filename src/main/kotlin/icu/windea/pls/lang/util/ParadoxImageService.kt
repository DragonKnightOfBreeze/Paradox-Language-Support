@file:Suppress("unused")

package icu.windea.pls.lang.util

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import icu.windea.pls.base.io.ChronicleDataPathService
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.checkCancellation
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.toUUID
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.images.ImageService
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.resolve.ParadoxImageLocationExpressionResolveResult
import icu.windea.pls.lang.resolve.ParadoxLocationExpressionService
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * 语义层的图片解析服务。
 *
 * 只负责把定义、文件或文件路径解析为 [ParadoxImageResolveResult]，不会直接渲染图片。
 */
object ParadoxImageService {
    private val logger = logger<ParadoxImageService>()

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

    fun resolveByFile(file: VirtualFile, frameInfo: ImageFrameInfo? = null): ParadoxImageResolveResult? {
        try {
            return doResolve(file, frameInfo)
        } catch (e: Exception) {
            checkCancellation(e)
            logger.warn("Resolve image by file failed. (file path: ${file.path})", e)
            return null
        }
    }

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
                ParadoxLocationExpressionService.resolve(it.locationExpression, definition, definitionInfo, frameInfo, toFile = true)
            }
        }
        if (resolved !is ParadoxImageLocationExpressionResolveResult.Static) return null
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
}
