@file:Suppress("unused")

package icu.windea.pls.lang.util

import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.io.fileSizeSafe
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.images.*
import icu.windea.pls.images.dds.*
import icu.windea.pls.images.tga.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import org.intellij.images.fileTypes.impl.*
import java.nio.file.*
import javax.imageio.*
import kotlin.contracts.*
import kotlin.io.path.*
import kotlin.io.path.exists

object ParadoxImageManager {
    object Keys : KeyRegistry() {
        val sliceInfos by createKey<MutableSet<String>>(Keys)
    }

    private val logger = logger<ParadoxImageManager>()

    fun isImageFile(file: PsiFile): Boolean {
        val vFile = file.virtualFile ?: return false
        return isImageFile(vFile)
    }

    fun isImageFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType !is ImageFileType && fileType !is DdsFileType && fileType !is TgaFileType) return false
        val extension = file.extension?.lowercase() ?: return false
        if (extension !in PlsConstants.imageFileExtensions) return false
        return true
    }

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
            definitionInfo.type == ParadoxDefinitionTypes.Sprite -> ParadoxSpriteManager.getFrameInfo(definition, frameInfo)
            else -> frameInfo
        }
        try {
            val url = doResolveUrlByDefinition(definition, definitionInfo, newFrameInfo)
            if (url.isNullOrEmpty()) return null
            return url
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.warn("Resolve url for dds image failed. (definition name: ${definitionInfo.name.or.anonymous()})", e)
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
        return doResolveUrlWithFrameInfo(resolvedFile.virtualFile, resolvedFile.project, resolved.frameInfo)
    }

    private fun doResolveUrlByFile(file: VirtualFile, project: Project, frameInfo: ImageFrameInfo?): String? {
        return doResolveUrlWithFrameInfo(file, project, frameInfo)
    }

    private fun doResolveUrlByFilePath(filePath: String, project: Project, frameInfo: ImageFrameInfo?): String? {
        val file = ParadoxFilePathSearch.search(filePath, null, selector(project).file()).find() ?: return null
        return doResolveUrlWithFrameInfo(file, project, frameInfo)
    }

    private fun doResolveUrlWithFrameInfo(file: VirtualFile, project: Project, frameInfo: ImageFrameInfo?): String? {
        //accept various image file types (normal file types such as png, or extended file aka dds and tga)
        if (!ImageManager.isImageFileType(file.fileType)) return null

        val filePath = file.toNioPath()
        if (frameInfo == null || !frameInfo.canApply()) return filePath.absolutePathString()

        val imageAbsPath = filePath.absolutePathString().normalizePath()
        val imageRelPath = file.fileInfo?.let { it.rootInfo.gameType.id + "/" + it.path.path }?.normalizePath()
        val imagePath = doResolveSlicedImagePath(imageAbsPath, imageRelPath, frameInfo)
        val created = doCreateSlicedImageFile(file, filePath, imagePath, frameInfo)
        if (!created) return filePath.absolutePathString()
        return imagePath.absolutePathString()
    }

    private fun doResolveSlicedImagePath(imageAbsPath: String, imageRelPath: String?, frameInfo: ImageFrameInfo): Path {
        val imagesPath = PlsPathConstants.images
        imagesPath.createDirectories()
        if (imageRelPath != null) {
            //路径：~/.pls/images/${relPathWithoutExtension}@${frame}_${frames}@${uuid}.png
            //UUID：基于游戏或模组目录的绝对路径
            val relPathWithoutExtension = imageRelPath.substringBeforeLast('.')
            val uuid = imageAbsPath.removeSuffix(imageRelPath).trim('/').toUUID().toString()
            val frameText = "@${frameInfo.frame}_${frameInfo.frames}"
            val finalPath = "${relPathWithoutExtension}${frameText}@${uuid}.png"
            return imagesPath.resolve(finalPath).toAbsolutePath()
        } else {
            //路径：~/.pls/images/_external/${fileNameWithoutExtension}@${frame}_${frames}@${uuid}.png
            //UUID：基于DDS文件所在目录
            val index = imageAbsPath.lastIndexOf('/')
            val parent = if (index == -1) "" else imageAbsPath.substring(0, index)
            val fileName = if (index == -1) imageAbsPath else imageAbsPath.substring(index + 1)
            val fileNameWithoutExtension = fileName.substringBeforeLast('.')
            val uuid = if (parent.isEmpty()) "" else parent.toUUID().toString()
            val frameText = "@${frameInfo.frame}_${frameInfo.frames}"
            val finalPath = "_external/${fileNameWithoutExtension}${frameText}@${uuid}.png"
            return imagesPath.resolve(finalPath).toAbsolutePath()
        }
    }

    private fun doCreateSlicedImageFile(file: VirtualFile, filePath: Path, imagePath: Path, frameInfo: ImageFrameInfo): Boolean {
        if (imagePath.exists()) {
            val sliceInfo = "${frameInfo.frame}_${frameInfo.frames}"
            val slicedInfos = file.getOrPutUserData(Keys.sliceInfos) { mutableSetOf() }
            if (!slicedInfos.add(sliceInfo)) return true
            imagePath.deleteIfExists() //IDE newly opened or outdated, delete it
        }

        imagePath.create()
        val image = ImageIO.read(filePath.toFile()) ?: return false
        val slicedImage = ImageManager.sliceImage(image, frameInfo) ?: return false
        ImageIO.write(slicedImage, "png", imagePath.toFile())
        return true
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
