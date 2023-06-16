package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.gist.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

//这个索引不需要兼容需要内联的情况

/**
 * 用于索引复杂枚举值。
 */
class ParadoxComplexEnumValueIndex : FileBasedIndexExtension<String, List<ParadoxComplexEnumValueInfo>>() {
    companion object {
        @JvmField val NAME = ID.create<String, List<ParadoxComplexEnumValueInfo>>("paradox.complexEnumValue.index")
        private const val VERSION = 30 //1.0.8
        
        fun getFileData(file: VirtualFile, project: Project): Map<String, List<ParadoxComplexEnumValueInfo>> {
            return FileBasedIndex.getInstance().getFileData(NAME, file, project)
        }
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, List<ParadoxComplexEnumValueInfo>, FileContent> {
        return DataIndexer { inputData ->
            val file = inputData.psiFile
            buildMap { indexData(file, this) }
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getValueExternalizer(): DataExternalizer<List<ParadoxComplexEnumValueInfo>> {
        return object : DataExternalizer<List<ParadoxComplexEnumValueInfo>> {
            override fun save(storage: DataOutput, value: List<ParadoxComplexEnumValueInfo>) {
                storage.writeInt(value.size)
                value.forEachFast { info -> writeComplexEnumValueInfo(storage, info) }
            }
            
            override fun read(storage: DataInput): List<ParadoxComplexEnumValueInfo> {
                return MutableList(storage.readInt()) { readComplexEnumValueInfo(storage) }
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> filterFile(file) }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
    
    object LazyIndex {
        private const val ID = "paradox.complexEnumValue.index.lazy"
        private const val VERSION = 30 //1.0.8
        
        private val valueExternalizer = object : DataExternalizer<Map<String, List<ParadoxComplexEnumValueInfo>>> {
            override fun save(storage: DataOutput, value: Map<String, List<ParadoxComplexEnumValueInfo>>) {
                storage.writeInt(value.size)
                value.forEach { (k, infos) ->
                    storage.writeUTF(k)
                    storage.writeInt(infos.size)
                    infos.forEachFast { info -> writeComplexEnumValueInfo(storage, info) }
                }
            }
            
            override fun read(storage: DataInput): Map<String, List<ParadoxComplexEnumValueInfo>> {
                return buildMap {
                    repeat(storage.readInt()) {
                        val k = storage.readUTF()
                        val infos = MutableList(storage.readInt()) { readComplexEnumValueInfo(storage) }
                        put(k, infos)
                    }
                }
            }
        }
        
        private val gist = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
            if(!filterFile(file)) return@builder emptyMap()
            val psiFile = file.toPsiFile(project) ?: return@builder emptyMap()
            buildMap { indexData(psiFile, this) }
        }
    }
}

//不需要兼容需要内联的情况

private fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxComplexEnumValueInfo>>) {
    file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(element: PsiElement) {
            if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                val info = ParadoxComplexEnumValueHandler.getInfo(element)
                if(info != null) {
                    val list = fileData.getOrPut(info.enumName) { mutableListOf() } as MutableList
                    list.add(info)
                }
            }
            if(element.isExpressionOrMemberContext()) super.visitElement(element)
        }
    })
}

private fun writeComplexEnumValueInfo(storage: DataOutput, valueSetValueInfo: ParadoxComplexEnumValueInfo) {
    storage.writeUTF(valueSetValueInfo.name)
    storage.writeUTF(valueSetValueInfo.enumName)
    storage.writeByte(valueSetValueInfo.readWriteAccess.toByte())
    storage.writeInt(valueSetValueInfo.elementOffset)
    storage.writeByte(valueSetValueInfo.gameType.toByte())
}

private fun readComplexEnumValueInfo(storage: DataInput): ParadoxComplexEnumValueInfo {
    val name = storage.readUTF()
    val enumName = storage.readUTF()
    val readWriteAccess = storage.readByte().toReadWriteAccess()
    val elementOffset = storage.readInt()
    val gameType = storage.readByte().toGameType()
    return ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, elementOffset, gameType)
}

private fun filterFile(file: VirtualFile): Boolean {
    val fileType = file.fileType
    if(fileType != ParadoxScriptFileType) return false
    if(file.fileInfo == null) return false
    return true
}