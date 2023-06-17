package icu.windea.pls.core.index

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxInlineScriptIndex : FileBasedIndexExtension<String, List<ParadoxInlineScriptInfo>>() {
    companion object {
        @JvmField val NAME = ID.create<String, List<ParadoxInlineScriptInfo>>("paradox.inlineScript.index")
        private const val VERSION = 30 //1.0.8
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, List<ParadoxInlineScriptInfo>, FileContent> {
        return DataIndexer { inputData -> //perf: 20000ms for indexing
            val file = inputData.psiFile
            buildMap { indexData(file, this) }
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getValueExternalizer(): DataExternalizer<List<ParadoxInlineScriptInfo>> {
        return object : DataExternalizer<List<ParadoxInlineScriptInfo>> {
            override fun save(storage: DataOutput, value: List<ParadoxInlineScriptInfo>) {
                writeInlineScriptInfos(storage, value)
            }
            
            override fun read(storage: DataInput): List<ParadoxInlineScriptInfo> {
                return readInlineScriptInfos(storage)
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> filterFile(file) }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
}

private fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxInlineScriptInfo>>) {
    file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(element: PsiElement) {
            if(element is ParadoxScriptProperty) {
                val info = ParadoxInlineScriptHandler.getInfo(element)
                if(info != null) {
                    val list = fileData.getOrPut(info.expression) { mutableListOf() } as MutableList
                    list.add(info)
                }
            }
            if(element.isExpressionOrMemberContext()) super.visitElement(element)
        }
    })
}

private fun writeInlineScriptInfos(storage: DataOutput, value: List<ParadoxInlineScriptInfo>) {
    storage.writeList(value) { info ->
        storage.writeUTFFast(info.expression)
        storage.writeInt(info.elementOffset)
        storage.writeByte(info.gameType.toByte())
    }
}

private fun readInlineScriptInfos(storage: DataInput): MutableList<ParadoxInlineScriptInfo> {
    return storage.readList {
        val expression = storage.readUTFFast()
        val elementOffset = storage.readInt()
        val gameType = storage.readByte().toGameType()
        ParadoxInlineScriptInfo(expression, elementOffset, gameType)
    }
}

private fun filterFile(file: VirtualFile): Boolean {
    val fileType = file.fileType
    if(fileType != ParadoxScriptFileType) return false
    if(file.fileInfo == null) return false
    return true
}