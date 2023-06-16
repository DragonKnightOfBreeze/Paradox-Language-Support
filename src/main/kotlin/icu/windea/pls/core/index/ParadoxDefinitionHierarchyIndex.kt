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
                val infos = value
                val size = infos.size
                storage.writeInt(size)
                if(size == 0) return
                var previousInfo: ParadoxDefinitionHierarchyInfo? = null
                infos.forEachFast { info ->
                    writeDefinitionHierarchyInfo(storage, info, previousInfo)
                    previousInfo = info
                }
            }
            
            override fun read(storage: DataInput): List<ParadoxDefinitionHierarchyInfo> {
                val size = storage.readInt()
                if(size == 0) return emptyList()
                var previousInfo: ParadoxDefinitionHierarchyInfo? = null
                return MutableList(size) {
                    val info = readDefinitionHierarchyInfo(storage, previousInfo)
                    previousInfo = info
                    info
                }
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
                storage.writeInt(value.size)
                value.forEach { (k, infos) ->
                    storage.writeUTFFast(k)
                    run {
                        val size = infos.size
                        storage.writeInt(size)
                        if(size == 0) return@run
                        var previousInfo: ParadoxDefinitionHierarchyInfo? = null
                        infos.forEachFast { info ->
                            writeDefinitionHierarchyInfo(storage, info, previousInfo)
                            previousInfo = info
                        }
                    }
                }
            }
            
            override fun read(storage: DataInput): Map<String, List<ParadoxDefinitionHierarchyInfo>> {
                return buildMap {
                    repeat(storage.readInt()) {
                        val k = storage.readUTFFast()
                        val infos = run {
                            val size = storage.readInt()
                            if(size == 0) return@run emptyList()
                            var previousInfo: ParadoxDefinitionHierarchyInfo? = null
                            MutableList(size) {
                                val info = readDefinitionHierarchyInfo(storage, previousInfo)
                                previousInfo = info
                                info
                            }
                        }
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
}

//这个索引在通常情况下需要索引的数据可能非常多，需要采用一些特殊的优化方式减少实际需要索引的数据

private fun writeDefinitionHierarchyInfo(storage: DataOutput, info: ParadoxDefinitionHierarchyInfo, previousInfo: ParadoxDefinitionHierarchyInfo?) {
    storage.writeOrWriteFrom(info, previousInfo, { it.supportId }, { storage.writeUTFFast(it) })
    storage.writeOrWriteFrom(info, previousInfo, { it.expression }, { storage.writeUTFFast(it) })
    storage.writeOrWriteFrom(info, previousInfo, { it.configExpression }, { storage.writeUTFFast(it) })
    storage.writeOrWriteFrom(info, previousInfo, { it.isKey }, { storage.writeBoolean(it) })
    storage.writeOrWriteFrom(info, previousInfo, { it.definitionName }, { storage.writeUTFFast(it) })
    storage.writeOrWriteFrom(info, previousInfo, { it.definitionType }, { storage.writeUTFFast(it) })
    storage.writeOrWriteFrom(info, previousInfo, { it.definitionSubtypes }, { storage.writeList(it) { e -> storage.writeUTFFast(e) } })
    storage.writeInt(info.elementOffset)
    storage.writeByte(info.gameType.toByte())
    ParadoxDefinitionHierarchySupport.saveData(storage, info)
    
    //storage.writeUTFFast(info.supportId)
    //storage.writeUTFFast(info.expression)
    //storage.writeUTFFast(info.configExpression)
    //storage.writeBoolean(info.isKey)
    //storage.writeUTFFast(info.definitionName)
    //storage.writeUTFFast(info.definitionType)
    //storage.writeInt(info.definitionSubtypes.size)
    //storage.writeList(info.definitionSubtypes) { storage.writeUTFFast(it) }
    //storage.writeInt(info.elementOffset)
    //storage.writeByte(info.gameType.toByte())
    //ParadoxDefinitionHierarchySupport.saveData(storage, info)
}

private fun readDefinitionHierarchyInfo(storage: DataInput, previousInfo: ParadoxDefinitionHierarchyInfo?): ParadoxDefinitionHierarchyInfo {
    val supportId = storage.readOrReadFrom(previousInfo, { it.supportId }, { storage.readUTFFast() })
    val expression = storage.readOrReadFrom(previousInfo, { it.expression }, { storage.readUTFFast() })
    val configExpression = storage.readOrReadFrom(previousInfo, { it.configExpression }, { storage.readUTFFast() })
    val isKey = storage.readOrReadFrom(previousInfo, { it.isKey }, { storage.readBoolean() })
    val definitionName = storage.readOrReadFrom(previousInfo, { it.definitionName }, { storage.readUTFFast() })
    val definitionType = storage.readOrReadFrom(previousInfo, { it.definitionType }, { storage.readUTFFast() })
    val definitionSubtypes = storage.readOrReadFrom(previousInfo, { it.definitionSubtypes }, { storage.readList{ storage.readUTFFast() } })
    val elementOffset = storage.readInt()
    val gameType = storage.readByte().toGameType()
    val contextInfo = ParadoxDefinitionHierarchyInfo(supportId, expression, configExpression, isKey, definitionName, definitionType, definitionSubtypes, elementOffset, gameType)
    ParadoxDefinitionHierarchySupport.readData(storage, contextInfo)
    return contextInfo
    
    //val supportId = storage.readUTFFast()
    //val expression = storage.readUTFFast()
    //val configExpression = storage.readUTFFast()
    //val isKey = storage.readBoolean()
    //val definitionName = storage.readUTFFast()
    //val definitionType = storage.readUTFFast()
    //val definitionSubtypes = MutableList(storage.readInt()) { storage.readUTFFast() }
    //val elementOffset = storage.readInt()
    //val gameType = storage.readByte().toGameType()
    //val contextInfo = ParadoxDefinitionHierarchyInfo(supportId, expression, configExpression, isKey, definitionName, definitionType, definitionSubtypes, elementOffset, gameType)
    //ParadoxDefinitionHierarchySupport.readData(storage, contextInfo)
    //return contextInfo
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