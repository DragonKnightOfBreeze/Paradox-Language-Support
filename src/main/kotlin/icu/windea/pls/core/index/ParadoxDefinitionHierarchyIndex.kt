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
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*
import java.util.*

//这个索引的索引速度可能非常慢
//这个索引兼容需要内联的情况（此时使用懒加载的索引）

/**
 * 用于索引定义声明中的定义引用、参数引用、本地化参数引用等。
 *
 * @see ParadoxDefinitionHierarchySupport
 */
class ParadoxDefinitionHierarchyIndex : FileBasedIndexExtension<String, List<ParadoxDefinitionHierarchyInfo>>() {
    companion object {
        @JvmField val NAME = ID.create<String, List<ParadoxDefinitionHierarchyInfo>>("paradox.definition.hierarchy.index")
        private const val VERSION = 30 //1.0.8
        
        fun getFileData(file: VirtualFile, project: Project): Map<String, List<ParadoxDefinitionHierarchyInfo>> {
            val useLazyIndex = useLazyIndex(file)
            if(useLazyIndex) return LazyIndex.getFileData(file, project)
            return FileBasedIndex.getInstance().getFileData(NAME, file, project)
        }
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, List<ParadoxDefinitionHierarchyInfo>, FileContent> {
        return DataIndexer { inputData ->
            val file = inputData.psiFile
            buildMap { indexData(file, this, false) }
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getValueExternalizer(): DataExternalizer<List<ParadoxDefinitionHierarchyInfo>> {
        return object : DataExternalizer<List<ParadoxDefinitionHierarchyInfo>> {
            override fun save(storage: DataOutput, value: List<ParadoxDefinitionHierarchyInfo>) {
                writeDefinitionHierarchyInfos(storage, value)
            }
            
            override fun read(storage: DataInput): List<ParadoxDefinitionHierarchyInfo> {
                return readDefinitionHierarchyInfos(storage)
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
        private const val ID = "paradox.definition.hierarchy.index.lazy"
        private const val VERSION = 30 //1.0.8
        
        fun getFileData(file: VirtualFile, project: Project): Map<String, List<ParadoxDefinitionHierarchyInfo>> {
            return gist.getFileData(project, file)
        }
        
        private val valueExternalizer = object : DataExternalizer<Map<String, List<ParadoxDefinitionHierarchyInfo>>> {
            override fun save(storage: DataOutput, value: Map<String, List<ParadoxDefinitionHierarchyInfo>>) {
                storage.writeIntFast(value.size)
                value.forEach { (k, infos) ->
                    storage.writeUTFFast(k)
                    writeDefinitionHierarchyInfos(storage, infos)
                }
            }
            
            override fun read(storage: DataInput): Map<String, List<ParadoxDefinitionHierarchyInfo>> {
                return buildMap {
                    repeat(storage.readIntFast()) {
                        val k = storage.readUTFFast()
                        val infos = readDefinitionHierarchyInfos(storage)
                        put(k, infos)
                    }
                }
            }
        }
        
        private val gist = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
            if(!filterFile(file, true)) return@builder emptyMap()
            val psiFile = file.toPsiFile(project) ?: return@builder emptyMap()
            buildMap { indexData(psiFile, this, true) }
        }
    }
}

private val markKey = Key.create<Boolean>("paradox.definition.hierarchy.index.mark")

private fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxDefinitionHierarchyInfo>>, lazy: Boolean) {
    file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
        private val definitionInfoStack = LinkedList<ParadoxDefinitionInfo>()
        
        override fun visitElement(element: PsiElement) {
            if(lazy) {
                if(element is ParadoxScriptFile) {
                    val definitionInfo = element.findParentDefinition(link = true)?.definitionInfo
                    if(definitionInfo != null) {
                        element.putUserData(markKey, true)
                        definitionInfoStack.addLast(definitionInfo)
                    }
                }
            } else {
                if(element is ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo
                    if(definitionInfo != null) {
                        element.putUserData(markKey, true)
                        definitionInfoStack.addLast(definitionInfo)
                    }
                }
            }
            
            if(definitionInfoStack.isNotEmpty()) {
                //这里element作为定义的引用时也可能是ParadoxScriptInt，目前不需要考虑这种情况，因此忽略
                if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                    ParadoxDefinitionHierarchyHandler.indexData(element, fileData)
                }
            }
            if(element.isExpressionOrMemberContext()) super.visitElement(element)
        }
        
        override fun elementFinished(element: PsiElement) {
            if(element.getUserData(markKey) == true) {
                element.putUserData(markKey, null)
                definitionInfoStack.removeLast()
            }
        }
    })
    
    if(fileData.isEmpty()) return
    fileData.forEach { (_, value) -> (value as MutableList).sortWith(compareBy({ it.definitionName + ":" + it.definitionType }, { it.configExpression }, { it.expression })) }
}

//这个索引在通常情况下需要索引的数据可能非常多，需要进行优化
//尝试减少实际需要索引的数据量以优化性能

private fun writeDefinitionHierarchyInfos(storage: DataOutput, value: List<ParadoxDefinitionHierarchyInfo>) {
    val size = value.size
    storage.writeIntFast(size)
    if(size == 0) return
    val firstInfo = value.first()
    storage.writeUTFFast(firstInfo.supportId)
    storage.writeByte(firstInfo.gameType.toByte())
    var previousInfo: ParadoxDefinitionHierarchyInfo? = null
    value.forEachFast { info ->
        storage.writeOrWriteFrom(info, previousInfo, { it.expression }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.configExpression }, { storage.writeUTFFast(it) })
        storage.writeBoolean(info.isKey)
        storage.writeOrWriteFrom(info, previousInfo, { it.definitionName }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.definitionType }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.definitionSubtypes }, { storage.writeList(it) { e -> storage.writeUTFFast(e) } })
        storage.writeIntFast(info.elementOffset)
        ParadoxDefinitionHierarchySupport.saveData(storage, info, previousInfo)
        previousInfo = info
    }
}

private fun readDefinitionHierarchyInfos(storage: DataInput): List<ParadoxDefinitionHierarchyInfo> {
    //perf: 200s for inspect directory 'common'
    val size = storage.readIntFast()
    if(size == 0) return emptyList()
    val supportId = storage.readUTFFast()
    val gameType = storage.readByte().toGameType()
    var previousInfo: ParadoxDefinitionHierarchyInfo? = null
    val result = mutableListOf<ParadoxDefinitionHierarchyInfo>()
    repeat(size) {
        val expression = storage.readOrReadFrom(previousInfo, { it.expression }, { storage.readUTFFast() })
        val configExpression = storage.readOrReadFrom(previousInfo, { it.configExpression }, { storage.readUTFFast() })
        val isKey = storage.readBoolean()
        val definitionName = storage.readOrReadFrom(previousInfo, { it.definitionName }, { storage.readUTFFast() })
        val definitionType = storage.readOrReadFrom(previousInfo, { it.definitionType }, { storage.readUTFFast() })
        val definitionSubtypes = storage.readOrReadFrom(previousInfo, { it.definitionSubtypes }, { storage.readList{ storage.readUTFFast() } })
        val elementOffset = storage.readIntFast()
        val info = ParadoxDefinitionHierarchyInfo(supportId, expression, configExpression, isKey, definitionName, definitionType, definitionSubtypes, elementOffset, gameType)
        ParadoxDefinitionHierarchySupport.readData(storage, info, previousInfo)
        result += info
        previousInfo = info
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