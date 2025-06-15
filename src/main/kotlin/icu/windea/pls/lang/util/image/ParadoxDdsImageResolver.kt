package icu.windea.pls.lang.util.image

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.*
import com.jetbrains.rd.util.ConcurrentHashMap
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.dds.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import kotlinx.coroutines.*
import java.lang.invoke.*
import java.nio.file.*
import javax.imageio.*
import kotlin.io.path.*
import kotlin.io.use

object ParadoxDdsImageResolver {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    private val cache: MutableMap<String, MutableMap<String, Path>> = ConcurrentHashMap() //absPath - frameInfo & relPath - pngPath

    /**
     * 基于路径信息解析DDS图片的路径，返回用于渲染的PNG图片的绝对路径。如果发生异常，则返回null。
     *
     * 如有必要，会在后台异步转换DDS图片为PNG图片，不等待转换完成。
     *
     * @param absPath DDS文件的绝对路径。
     * @param relPath DDS文件相对于游戏或模组目录的路径（如果可以获取）。
     * @param frameInfo 帧数信息，用于切分图片。
     */
    fun resolveUrl(project: Project, absPath: String, relPath: String? = null, frameInfo: ImageFrameInfo? = null): String? {
        try {
            //如果存在基于DDS文件绝对路径的缓存数据，则使用缓存的PNG文件绝对路径
            val finalAbsPath = absPath.normalizePath()
            val map = cache.computeIfAbsent(finalAbsPath) { ConcurrentHashMap() }
            val infoKey = getInfoKey(relPath, frameInfo)
            val pngAbsPath = map.computeIfAbsent(infoKey) { getPngAbsPath(project, finalAbsPath, relPath, frameInfo) }
            convertDdsToPngAsyncIfNecessary(project, absPath.toPath(), pngAbsPath, frameInfo)
            return pngAbsPath.toString()
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.warn("Resolve url for dds image failed. (dds absolute path: $absPath, dds relative path: $relPath, frame info: $frameInfo)", e)
            return null
        }
    }

    private fun getInfoKey(relPath: String? = null, frameInfo: ImageFrameInfo? = null): String {
        if (relPath == null && frameInfo == null) return ""
        return buildString {
            if (frameInfo != null) append(frameInfo.frame).append("_").append(frameInfo.frames)
            if (relPath != null) append("_").append(relPath.normalizePath())
        }
    }

    private fun getPngAbsPath(project: Project, absPath: String, relPath: String?, frameInfo: ImageFrameInfo?): Path {
        val pngAbsPath = resolvePngAbsPath(relPath, absPath, frameInfo)
        if (pngAbsPath.exists() && pngAbsPath.fileSize() == 0L) {
            runCatching { pngAbsPath.deleteIfExists() } //初始化缓存时，如果对应的PNG图片大小为空，需要安全地删除
        }
        return pngAbsPath
    }

    private fun resolvePngAbsPath(relPath: String?, absPath: String, frameInfo: ImageFrameInfo?): Path {
        val imagesPath = PlsPathConstants.images
        imagesPath.createDirectories()
        if (relPath != null) {
            //路径：~/.pls/images/${relPathWithoutExtension}@${frame}_${frames}@${uuid}.png
            //UUID：基于游戏或模组目录的绝对路径
            val relPathWithoutExtension = relPath.substringBeforeLast('.')
            val uuid = absPath.removeSuffix(relPath).trim('/').toUUID().toString()
            val frameText = if (frameInfo != null) "@${frameInfo.frame}_${frameInfo.frames}" else ""
            val finalPath = "${relPathWithoutExtension}${frameText}@${uuid}.png"
            return imagesPath.resolve(finalPath).toAbsolutePath()
        } else {
            //路径：~/.pls/images/_external/${fileNameWithoutExtension}@${frame}_${frames}@${uuid}.png
            //UUID：基于DDS文件所在目录
            val index = absPath.lastIndexOf('/')
            val parent = if (index == -1) "" else absPath.substring(0, index)
            val fileName = if (index == -1) absPath else absPath.substring(index + 1)
            val fileNameWithoutExtension = fileName.substringBeforeLast('.')
            val uuid = if (parent.isEmpty()) "" else parent.toUUID().toString()
            val frameText = if (frameInfo != null) "@${frameInfo.frame}_${frameInfo.frames}" else ""
            val finalPath = "_external/${fileNameWithoutExtension}${frameText}@${uuid}.png"
            return imagesPath.resolve(finalPath).toAbsolutePath()
        }
    }

    @Suppress("UnstableApiUsage")
    private fun convertDdsToPngAsyncIfNecessary(project: Project, absPath: Path, pngAbsPath: Path, frameInfo: ImageFrameInfo?) {
        if (pngAbsPath.exists()) return

        if (absPath.fileSize() <= PlsInternalSettings.largeDddSize) {
            convertDdsToPng(absPath, pngAbsPath, frameInfo)
            return
        }

        //如果DDS文件足够大，需要异步进行并显示可取消的进度条
        val coroutineScope = PlsFacade.getCoroutineScope()
        val job = coroutineScope.launch {
            withBackgroundProgress(project, PlsBundle.message("progress.convertDdsToPng.title"), true) {
                reportRawProgress { reporter ->
                    reporter.text(PlsBundle.message("progress.convertDdsToPng.step", absPath.name, pngAbsPath.name))
                    convertDdsToPngAsync(absPath, pngAbsPath, frameInfo)
                }
            }
        }
        job.invokeOnCompletion {
            if (it is CancellationException) {
                runCatching { pngAbsPath.deleteIfExists() } //如果被取消，需要安全地删除对应的PNG图片
            }
        }
    }

    private suspend fun convertDdsToPngAsync(absPath: Path, pngAbsPath: Path, frameInfo: ImageFrameInfo?) {
        withContext(Dispatchers.IO) {
            runCatchingCancelable {
                convertDdsToPng(absPath, pngAbsPath, frameInfo)
            }.onFailure { logger.warn(it) }
        }
        runCatchingCancelable {
            PlsManager.refreshInlayHintsImagesChangedIfNecessary() //注意这里可能需要刷新内嵌提示
        }
    }

    private fun convertDdsToPng(absPath: Path, pngAbsPath: Path, frameInfo: ImageFrameInfo?) {
        pngAbsPath.deleteIfExists()
        pngAbsPath.create()
        Files.newOutputStream(pngAbsPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { outputStream ->
            val inputStream = absPath.inputStream()
            DdsManager.convertImageFormat(inputStream, outputStream, "dds", "png")
            outputStream.flush()
        }
        if (frameInfo == null) return
        val image = ImageIO.read(pngAbsPath.toFile()) ?: return
        val newImage = frameInfo.sliceImage(image) ?: return
        ImageIO.write(newImage, "png", pngAbsPath.toFile())
    }

    fun clearCacheFiles(file: VirtualFile) {
        val finalAbsPath = file.toNioPath().absolutePathString().normalizePath()
        val cacheFilePaths = cache.get(finalAbsPath)?.values?.orNull() ?: return
        runCatching { cacheFilePaths.forEach { it.deleteIfExists() } } //安全地删除所有对应的PNG图片
    }
}
