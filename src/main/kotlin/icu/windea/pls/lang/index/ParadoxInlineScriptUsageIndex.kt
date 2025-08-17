package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

/**
 * 用于索引内联脚本的使用信息。
 */
class ParadoxInlineScriptUsageIndex : ParadoxFileBasedIndex<ParadoxInlineScriptUsageIndexInfo.Compact>() {
    companion object {
        private const val VERSION = 72 //2.0.2
    }

    override fun getName() = ParadoxIndexManager.InlineScriptUsageName

    override fun getVersion() = VERSION

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
