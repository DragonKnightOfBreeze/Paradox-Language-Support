package icu.windea.pls.core.index

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import java.io.*

private val NAME = ID.create<String, List<ParadoxParameterInfo>>("paradox.parameter.index")
private const val VERSION = 32 //1.1.5

class ParadoxParameterIndex : ParadoxFileBasedIndex<List<ParadoxParameterInfo>>() {
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxParameterInfo>>) {
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                doIndexData(element, fileData)
                super.visitElement(element)
            }
        })
        
        //排序
        if(fileData.isEmpty()) return
        fileData.mapValues { (_, v) ->
            v.sortedBy { it.name }
        }
    }
    
    private fun doIndexData(element: PsiElement, fileData: MutableMap<String, List<ParadoxParameterInfo>>) {
        val constraint = ParadoxResolveConstraint.Parameter
        if(!constraint.canResolveReference(element)) return
        element.references.forEachFast f@{ reference ->
            if(!constraint.canResolve(reference)) return@f
            val resolved = reference.resolve()
            if(resolved !is ParadoxParameterElement) return@f
            //note that element.startOffset may not equal to actual parameterElement.startOffset (e.g. in a script value expression)
            val info = ParadoxParameterInfo(resolved.name, resolved.contextKey, resolved.readWriteAccess, element.startOffset, resolved.gameType)
            val list = fileData.getOrPut(resolved.contextKey) { mutableListOf() } as MutableList
            list.add(info)
        }
    }
    
    //尝试减少实际需要索引的数据量以优化性能
    
    override fun writeData(storage: DataOutput, value: List<ParadoxParameterInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if(size == 0) return
        val firstInfo = value.first()
        storage.writeUTFFast(firstInfo.contextKey)
        storage.writeByte(firstInfo.gameType.toByte())
        var previousInfo: ParadoxParameterInfo? = null
        value.forEachFast { info ->
            storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
            storage.writeByte(info.readWriteAccess.toByte())
            storage.writeIntFast(info.elementOffset)
            previousInfo = info
        }
    }
    
    override fun readData(storage: DataInput): List<ParadoxParameterInfo> {
        val size = storage.readIntFast()
        if(size == 0) return emptyList()
        val contextKey = storage.readUTFFast()
        val gameType = storage.readByte().toGameType()
        var previousInfo: ParadoxParameterInfo? = null
        val result = mutableListOf<ParadoxParameterInfo>()
        repeat(size) {
            val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
            val readWriteAccess = storage.readByte().toReadWriteAccess()
            val elementOffset = storage.readIntFast()
            val info = ParadoxParameterInfo(name, contextKey, readWriteAccess, elementOffset, gameType)
            result += info
            previousInfo = info
        }
        return result
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