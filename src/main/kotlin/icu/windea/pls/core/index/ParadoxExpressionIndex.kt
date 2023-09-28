package icu.windea.pls.core.index

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.localisation.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.*
import java.io.*

private val NAME = ID.create<String, List<ParadoxExpressionInfo>>("paradox.expression.index")
private const val VERSION = 39 //1.1.11

/**
 * 用于基于文件层级索引各种表达式信息。
 *
 * * 这个索引兼容需要内联的情况（此时使用懒加载的索引）。
 */
class ParadoxExpressionIndex : ParadoxFileBasedIndex<List<ParadoxExpressionInfo>>() {
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        //TODO
        
        if(fileData.isEmpty()) return
        val extensionList = ParadoxExpressionIndexSupport.EP_NAME.extensionList
        fileData.mapValues { (k, v) ->
            val id = k.toByte()
            val support = extensionList.findFast { it.id() == id }
                ?.castOrNull<ParadoxExpressionIndexSupport<ParadoxExpressionInfo>>()
                ?: throw UnsupportedOperationException()
            support.compress(v)
        }
    }
    
    override fun writeData(storage: DataOutput, value: List<ParadoxExpressionInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if(value.isEmpty()) return
        
        val type = value.first().javaClass
        val support = ParadoxExpressionIndexSupport.EP_NAME.extensionList.findFast { it.type() == type }
            ?.castOrNull<ParadoxExpressionIndexSupport<ParadoxExpressionInfo>>()
            ?: throw UnsupportedOperationException()
        storage.writeByte(support.id().toInt())
        val gameType = value.first().gameType
        storage.writeByte(gameType.toByte())
        var previousInfo: ParadoxExpressionInfo? = null
        value.forEachFast { info ->
            storage.writeByte(support.id().toInt())
            support.writeData(storage, info, previousInfo, gameType)
            previousInfo = info
        }
    }
    
    override fun readData(storage: DataInput): List<ParadoxExpressionInfo> {
        val size = storage.readIntFast()
        if(size == 0) return emptyList()
        
        val id = storage.readByte()
        val support = ParadoxExpressionIndexSupport.EP_NAME.extensionList.findFast { it.id() == id }
            ?.castOrNull<ParadoxExpressionIndexSupport<ParadoxExpressionInfo>>()
            ?: throw UnsupportedOperationException()
        val gameType = storage.readByte().toGameType()
        var previousInfo: ParadoxExpressionInfo? = null
        return MutableList(size) {
            support.readData(storage, previousInfo, gameType)
                .also { previousInfo = it }
        }
    }
    
    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if(fileType != ParadoxScriptFileType && fileType != ParadoxLocalisationFileType) return false
        if(file.fileInfo == null) return false
        return true
    }
    
    override fun useLazyIndex(file: VirtualFile): Boolean {
        if(ParadoxFileManager.isInjectedFile(file)) return true
        if(ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null) return true
        return false
    }
}