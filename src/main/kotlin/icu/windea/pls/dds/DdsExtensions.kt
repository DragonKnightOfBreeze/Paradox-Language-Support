package icu.windea.pls.dds

import com.intellij.openapi.vfs.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.image.*
import kotlin.io.path.*

//icu.windea.pls.tool.ParadoxImageResolver.doResolveByFile(com.intellij.openapi.vfs.VirtualFile)

fun invalidateDdsFile(ddsFile: VirtualFile) {
    if (ddsFile.fileType != DdsFileType) return
    //如果可以得到相对于游戏或模组目录的文件路径，则使用绝对根路径+相对路径定位，否则直接使用绝对路径
    val fileInfo = ddsFile.fileInfo
    val rootPath = fileInfo?.rootInfo?.rootPath
    val ddsRelPath = fileInfo?.relPath?.path
    val ddsAbsPath = if (rootPath != null && ddsRelPath != null) {
        rootPath.absolutePathString() + "/" + ddsRelPath
    } else {
        ddsFile.toNioPath().absolutePathString()
    }
    ParadoxDdsResolver.invalidateUrl(ddsAbsPath)
}
