package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.gist.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
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
        private const val VERSION = 28 //1.0.6
        
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
                storage.writeList(value) { info -> writeDefinitionHierarchyInfo(storage, info) }
            }
            
            override fun read(storage: DataInput): List<ParadoxDefinitionHierarchyInfo> {
                return storage.readList { readDefinitionHierarchyInfo(storage) }
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
        private const val VERSION = 28 //1.0.6
        
        fun getFileData(file: VirtualFile, project: Project): Map<String, List<ParadoxDefinitionHierarchyInfo>> {
            return gist.getFileData(project, file)
        }
        
        private val valueExternalizer = object : DataExternalizer<Map<String, List<ParadoxDefinitionHierarchyInfo>>> {
            override fun save(storage: DataOutput, value: Map<String, List<ParadoxDefinitionHierarchyInfo>>) {
                storage.writeInt(value.size)
                value.forEach { (k, infos) ->
                    storage.writeString(k)
                    storage.writeList(infos) { info -> writeDefinitionHierarchyInfo(storage, info) }
                }
            }
            
            override fun read(storage: DataInput): Map<String, List<ParadoxDefinitionHierarchyInfo>> {
                return buildMap {
                    repeat(storage.readInt()) {
                        val k = storage.readString()
                        val infos = storage.readList { readDefinitionHierarchyInfo(storage) }
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

private fun writeDefinitionHierarchyInfo(storage: DataOutput, info: ParadoxDefinitionHierarchyInfo) {
    storage.writeString(info.supportId)
    storage.writeString(info.expression)
    storage.writeBoolean(info.configExpression is CwtKeyExpression)
    storage.writeString(info.configExpression.expressionString)
    storage.writeString(info.definitionName)
    storage.writeString(info.definitionType)
    storage.writeList(info.definitionSubtypes) { subtype ->
        storage.writeString(subtype)
    }
    storage.writeInt(info.elementOffset)
    storage.writeByte(info.gameType.toByte())
    ParadoxDefinitionHierarchySupport.saveData(storage, info)
}

private fun readDefinitionHierarchyInfo(storage: DataInput): ParadoxDefinitionHierarchyInfo {
    val supportId = storage.readString()
    val expression = storage.readString()
    val flag = storage.readBoolean()
    val configExpression = storage.readString().let { if(flag) CwtKeyExpression.resolve(it) else CwtValueExpression.resolve(it) }
    val definitionName = storage.readString()
    val definitionType = storage.readString()
    val definitionSubtypes = storage.readList { storage.readString() }
    val elementOffset = storage.readInt()
    val gameType = storage.readByte().toGameType()
    val contextInfo = ParadoxDefinitionHierarchyInfo(supportId, expression, configExpression, definitionName, definitionType, definitionSubtypes, elementOffset, gameType)
    ParadoxDefinitionHierarchySupport.readData(storage, contextInfo)
    return contextInfo
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