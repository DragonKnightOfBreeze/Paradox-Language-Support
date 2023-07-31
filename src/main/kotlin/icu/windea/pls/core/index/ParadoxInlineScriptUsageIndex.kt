package icu.windea.pls.core.index

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

private val NAME = ID.create<String, List<ParadoxInlineScriptUsageInfo>>("paradox.inlineScriptUsage.index")
private const val VERSION = 33 //1.1.5

/**
 * 用于索引内联脚本调用。
 *
 * * 这个索引兼容需要内联的情况（此时使用懒加载的索引）。
 *
 * @see ParadoxInlineScriptUsageInfo
 */
class ParadoxInlineScriptUsageIndex : ParadoxFileBasedIndex<List<ParadoxInlineScriptUsageInfo>>() {
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxInlineScriptUsageInfo>>) {
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty) {
                    val info = ParadoxInlineScriptHandler.getUsageInfo(element)
                    if(info != null) {
                        val list = fileData.getOrPut(info.expression) { mutableListOf() } as MutableList
                        list.add(info)
                    }
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
        })
    }
    
    //尝试减少实际需要索引的数据量以优化性能
    
    override fun writeData(storage: DataOutput, value: List<ParadoxInlineScriptUsageInfo>) {
        storage.writeList(value) { info ->
            storage.writeUTFFast(info.expression)
            storage.writeInt(info.elementOffset)
            storage.writeByte(info.gameType.toByte())
        }
    }
    
    override fun readData(storage: DataInput): List<ParadoxInlineScriptUsageInfo> {
        return storage.readList {
            val expression = storage.readUTFFast()
            val elementOffset = storage.readInt()
            val gameType = storage.readByte().toGameType()
            ParadoxInlineScriptUsageInfo(expression, elementOffset, gameType)
        }
    }
    
    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if(fileType != ParadoxScriptFileType) return false
        if(file.fileInfo == null) return false
        return true
    }
    
    override fun useLazyIndex(file: VirtualFile): Boolean {
        if(ParadoxFileManager.isInjectedFile(file)) return true
        if(ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null) return true
        return false
    }
}
