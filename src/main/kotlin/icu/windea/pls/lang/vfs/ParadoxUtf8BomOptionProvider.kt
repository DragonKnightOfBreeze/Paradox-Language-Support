package icu.windea.pls.lang.vfs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.encoding.Utf8BomOptionProvider
import com.intellij.util.ThreeState
import icu.windea.pls.core.toThreeState
import icu.windea.pls.lang.fileInfo

/**
 * 基于文件的扩展名以及相对于入口目录的路径，判断创建新的脚本文件或本地化文件时，是否需要添加 BOM。
 *
 * @see ParadoxFileEncodingService
 */
class ParadoxUtf8BomOptionProvider : Utf8BomOptionProvider {
    override fun getBOMDecisionForNewUtf8File(file: VirtualFile): ThreeState {
        val fileInfo = file.fileInfo ?: return ThreeState.UNSURE
        val useBom = ParadoxFileEncodingService.useBom(file, fileInfo)
        return useBom.toThreeState()
    }
}
