package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
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
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

//这个索引的索引速度可能非常慢
//这个索引不会保存同一文件中重复的ParadoxValueSetValueInfo
//这个索引不会保存ParadoxValueSetValueInfo.elementOffset
//这个索引兼容需要内联的情况（此时使用懒加载的索引）

/**
 * 用于索引值集值。
 */
class ParadoxValueSetValueFastIndex : FileBasedIndexExtension<String, List<ParadoxValueSetValueInfo>>() {
    companion object {
        @JvmField val NAME = ID.create<String, List<ParadoxValueSetValueInfo>>("paradox.valueSetValue.fast.index")
        private const val VERSION = 30 //1.0.8
        
        fun getFileData(file: VirtualFile, project: Project): Map<String, List<ParadoxValueSetValueInfo>> {
            val useLazyIndex = useLazyIndex(file)
            if(useLazyIndex) return LazyIndex.getFileData(file, project)
            return FileBasedIndex.getInstance().getFileData(NAME, file, project)
        }
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, List<ParadoxValueSetValueInfo>, FileContent> {
        return DataIndexer { inputData ->
            val file = inputData.psiFile
            buildMap { indexData(file, this) }
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getValueExternalizer(): DataExternalizer<List<ParadoxValueSetValueInfo>> {
        return object : DataExternalizer<List<ParadoxValueSetValueInfo>> {
            override fun save(storage: DataOutput, value: List<ParadoxValueSetValueInfo>) {
                writeValueSetValueInfos(storage, value)
            }
            
            override fun read(storage: DataInput): List<ParadoxValueSetValueInfo> {
                return readValueSetValueInfos(storage)
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> filterFile(file, false) }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
    
    object LazyIndex {
        private const val ID = "paradox.valueSetValue.fast.index.lazy"
        private const val VERSION = 30 //1.0.8
        
        fun getFileData(file: VirtualFile, project: Project): Map<String, List<ParadoxValueSetValueInfo>> {
            return gist.getFileData(project, file)
        }
        
        private val valueExternalizer = object : DataExternalizer<Map<String, List<ParadoxValueSetValueInfo>>> {
            override fun save(storage: DataOutput, value: Map<String, List<ParadoxValueSetValueInfo>>) {
                storage.writeInt(value.size)
                value.forEach { (k, infos) ->
                    storage.writeUTFFast(k)
                    writeValueSetValueInfos(storage, infos)
                }
            }
            
            override fun read(storage: DataInput): Map<String, List<ParadoxValueSetValueInfo>> {
                return buildMap {
                    repeat(storage.readInt()) {
                        val k = storage.readUTFFast()
                        val infos = readValueSetValueInfos(storage)
                        put(k, infos)
                    }
                }
            }
        }
        
        private val gist = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
            if(!filterFile(file, true)) return@builder emptyMap()
            val psiFile = file.toPsiFile(project) ?: return@builder emptyMap()
            buildMap { indexData(psiFile, this) }
        }
    }
}

private val markKey = Key.create<Boolean>("paradox.definition.hierarchy.index.mark")

private fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxValueSetValueInfo>>) {
    val keys = mutableSetOf<String>()
    if(file.fileType == ParadoxScriptFileType) {
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                    val infos = ParadoxValueSetValueHandler.getInfos(element)
                    fileData.addInfos(infos, keys)
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
        })
    } else {
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxLocalisationCommandIdentifier) {
                    val infos = ParadoxValueSetValueHandler.getInfos(element)
                    fileData.addInfos(infos, keys)
                }
                if(element.isRichTextContext()) super.visitElement(element)
            }
        })
    }
    
    if(fileData.isEmpty()) return
    fileData.forEach { (_, value) -> (value as MutableList).sortBy { it.name } }
}

//目前不需要追踪在文件中的位置，因此可以先进行去重

private fun MutableMap<String, List<ParadoxValueSetValueInfo>>.addInfos(infos: List<ParadoxValueSetValueInfo>, keys: MutableSet<String>) {
    infos.forEachFast { info ->
        val key = info.valueSetName + "@" + info.name + "@" + info.readWriteAccess.ordinal
        if(keys.add(key)) {
            val list = getOrPut(info.valueSetName) { mutableListOf() } as MutableList
            list.add(info)
        }
    }
}

//尝试减少实际需要索引的数据量以优化性能

private fun writeValueSetValueInfos(storage: DataOutput, value: List<ParadoxValueSetValueInfo>) {
    if(value.isEmpty()) return storage.writeBoolean(true)
    storage.writeBoolean(false)
    val firstInfo = value.first()
    storage.writeUTFFast(firstInfo.valueSetName)
    storage.writeByte(firstInfo.gameType.toByte())
    val infoGroup = value.groupBy { it.name }
    storage.writeIntFast(infoGroup.size)
    infoGroup.forEach { (name, infos) ->
        storage.writeUTFFast(name)
        storage.writeIntFast(infos.size)
        infos.forEachFast { info ->
            storage.writeByte(info.readWriteAccess.toByte())
            storage.writeIntFast(info.elementOffset)
        }
    }
}

private fun readValueSetValueInfos(storage: DataInput): List<ParadoxValueSetValueInfo> {
    if(storage.readBoolean()) return emptyList()
    val result = mutableListOf<ParadoxValueSetValueInfo>()
    val valueSetName = storage.readUTFFast()
    val gameType = storage.readByte().toGameType()
    repeat(storage.readIntFast()) {
        val name = storage.readUTFFast()
        repeat(storage.readIntFast()) {
            val readWriteAccess = storage.readByte().toReadWriteAccess()
            val elementOffset = storage.readIntFast()
            result += ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
        }
    }
    return result
}

private fun filterFile(file: VirtualFile, lazy: Boolean): Boolean {
    val fileType = file.fileType
    if(fileType != ParadoxScriptFileType) return false
    if(file.fileInfo == null) return false
    val useLazyIndex = useLazyIndex(file)
    return if(lazy) useLazyIndex else !useLazyIndex
}

private fun useLazyIndex(file: VirtualFile): Boolean {
    return ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null
}