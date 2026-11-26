package icu.windea.pls.lang.vfs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.encoding.Utf8BomOptionProvider
import com.intellij.util.ThreeState
import icu.windea.pls.lang.fileInfo

/**
 * 基于文件的扩展名以及相对于入口目录的路径，判断创建新的脚本文件或本地化文件时，是否需要添加BOM。
 *
 * 具体的 BOM 决策委托给 [ParadoxFileEncodingService.useBom]，以与文件编码检查保持一致。
 */
class ParadoxUtf8BomOptionProvider : Utf8BomOptionProvider {
    override fun getBOMDecisionForNewUtf8File(file: VirtualFile): ThreeState {
        val fileInfo = file.fileInfo ?: return ThreeState.UNSURE
        return when (ParadoxFileEncodingService.useBom(file, fileInfo)) {
            true -> ThreeState.YES
            false -> ThreeState.NO
            null -> ThreeState.UNSURE
        }
    }
}
