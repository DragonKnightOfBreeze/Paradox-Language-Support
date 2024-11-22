package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
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
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findFileBasedIndex<ParadoxInlineScriptUsageIndex>() }
        val NAME = ID.create<String, ParadoxInlineScriptUsageIndexInfo.Compact>("paradox.inlineScriptUsage.index")

        private const val VERSION = 57 //1.3.25
    }

    override fun getName() = NAME

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
        storage.writeList(value.elementOffsets) { storage.writeIntFast(it) }
        storage.writeByte(value.gameType.optimizeValue())
    }

    override fun readData(storage: DataInput): ParadoxInlineScriptUsageIndexInfo.Compact {
        val expression = storage.readUTFFast()
        val elementOffsets = storage.readList { storage.readIntFast() }
        val gameType = storage.readByte().deoptimizeValue<ParadoxGameType>()
        return ParadoxInlineScriptUsageIndexInfo.Compact(expression, elementOffsets, gameType)
    }

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (ParadoxFileManager.isInjectedFile(file)) return true
        //if (ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return true //unnecessary
        return false
    }
}
