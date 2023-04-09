package icu.windea.pls.dds

import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.tool.*
import org.intellij.images.editor.ImageDocument.*
import org.intellij.images.vfs.*
import kotlin.io.path.*

fun VirtualFile.getDdsImageProvider(): ScaledImageProvider? {
	val pngFile = ParadoxDdsUrlResolver.getPngFile(this) ?: return null
	return IfsUtil.getImageProvider(pngFile)
}

//icu.windea.pls.tool.ParadoxDdsUrlResolver.doResolveByFile(com.intellij.openapi.vfs.VirtualFile)
fun doInvalidateDdsFile(ddsFile: VirtualFile) {
    if(ddsFile.fileType != DdsFileType) return
    //如果可以得到相对于游戏或模组根路径的文件路径，则使用绝对根路径+相对路径定位，否则直接使用绝对路径
    val fileInfo = ddsFile.fileInfo
    val rootPath = fileInfo?.let { it.rootInfo.gameRootPath }
    val ddsRelPath = fileInfo?.path?.path
    val ddsAbsPath = if(rootPath != null && ddsRelPath != null) {
        rootPath.absolutePathString() + "/" + ddsRelPath
    } else {
        ddsFile.toNioPath().absolutePathString()
    }
    DdsToPngConverter.invalidate(ddsAbsPath)
}