package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.indexInfo.ParadoxInlineScriptUsageIndexInfo
import icu.windea.pls.model.optimizeValue
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import java.io.DataInput
import java.io.DataOutput

/**
 * 用于索引内联脚本的使用信息。
 */
class ParadoxInlineScriptUsageIndex : ParadoxFileBasedIndex<ParadoxInlineScriptUsageIndexInfo.Compact>() {
    override fun getName() = ParadoxIndexKeys.InlineScriptUsage

    override fun getVersion() = 75 // VERSION for 2.0.5

    override fun indexData(file: PsiFile, fileData: MutableMap<String, ParadoxInlineScriptUsageIndexInfo.Compact>) {
        file.acceptChildren(object: PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty) {
                    val r = visitProperty(element)
                    if(r) return
                }

                super.visitElement(element)
            }

            private fun visitProperty(element: ParadoxScriptProperty): Boolean {
                val info = ParadoxInlineScriptManager.getUsageInfo(element) ?: return false
                val compactInfo = fileData.getOrPut(info.expression) { ParadoxInlineScriptUsageIndexInfo.Compact(info.expression, sortedSetOf(), info.gameType) }
                (compactInfo.elementOffsets as MutableSet) += info.elementOffset
                return true
            }
        })
    }

    override fun writeData(storage: DataOutput, value: ParadoxInlineScriptUsageIndexInfo.Compact) {
        storage.writeUTFFast(value.expression)
        storage.writeIntFast(value.elementOffsets.size)
        value.elementOffsets.forEach { storage.writeIntFast(it) }
        storage.writeByte(value.gameType.optimizeValue())
    }

    override fun readData(storage: DataInput): ParadoxInlineScriptUsageIndexInfo.Compact {
        val expression = storage.readUTFFast()
        val elementOffsetsSize = storage.readIntFast()
        val elementOffsets = if (elementOffsetsSize != 0) sortedSetOf<Int>().apply { repeat(elementOffsetsSize) { this += storage.readIntFast() } } else emptySet()
        val gameType = storage.readByte().deoptimizeValue<ParadoxGameType>()
        return ParadoxInlineScriptUsageIndexInfo.Compact(expression, elementOffsets, gameType)
    }

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType !is ParadoxScriptFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (PlsVfsManager.isInjectedFile(file)) return true
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return true //inline script files should be lazy indexed
        return false
    }
}
