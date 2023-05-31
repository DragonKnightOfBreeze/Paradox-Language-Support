package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.ParadoxDefinitionHierarchyIndex.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*
import java.util.*

/**
 * 用于索引定义声明中的定义引用、参数引用、本地化参数引用等。
 * 
 * @see ParadoxDefinitionHierarchySupport
 */
class ParadoxDefinitionHierarchyIndex : FileBasedIndexExtension<String, List<ContextInfo>>() {
    companion object {
        @JvmField val NAME = ID.create<String, List<ContextInfo>>("paradox.definition.hierarchy.index")
        private const val VERSION = 27 //1.0.5
        
        fun getData(file: VirtualFile, project: Project): Map<String, List<ContextInfo>> {
            return FileBasedIndex.getInstance().getFileData(NAME, file, project)
        }
    }
    
    data class ContextInfo(
        val supportId: String,
        val expression: String,
        val configExpression: CwtDataExpression,
        val definitionInfo: DefinitionInfo
    ) : UserDataHolderBase()
    
    data class DefinitionInfo(
        val name: String,
        val type: String,
        val subtypes: List<String>
    )
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, List<ContextInfo>, FileContent> {
        return DataIndexer { inputData ->
            val file = inputData.psiFile
            val definitionInfoStack = LinkedList<ParadoxDefinitionInfo>()
            buildMap {
                file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if(element is ParadoxScriptDefinitionElement) {
                            val definitionInfo = element.definitionInfo
                            if(definitionInfo != null) {
                                definitionInfoStack.addLast(definitionInfo)
                            }
                        }
                        val definitionInfo = definitionInfoStack.peekLast()
                        if(definitionInfo != null) {
                            //这里element作为定义的引用时也可能是ParadoxScriptInt，目前不需要考虑这种情况，因此忽略
                            if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                                val matchOptions = ParadoxConfigMatcher.Options.SkipScope //这里不需要检测作用域是否匹配
                                val configs = ParadoxConfigResolver.getConfigs(element, orDefault = true, matchOptions = matchOptions)
                                if(configs.isNotEmpty()) {
                                    configs.forEachFast { config ->
                                        ParadoxDefinitionHierarchySupport.indexData(this@buildMap, element, config, definitionInfo)
                                    }
                                }
                            }
                        }
                        if(element.isExpressionOrMemberContext()) super.visitElement(element)
                    }
                    
                    override fun elementFinished(element: PsiElement) {
                        if(element is ParadoxScriptDefinitionElement) {
                            val definitionInfo = element.definitionInfo
                            if(definitionInfo != null) {
                                definitionInfoStack.removeLast()
                            }
                        }
                    }
                })
            }
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getValueExternalizer(): DataExternalizer<List<ContextInfo>> {
        return object : DataExternalizer<List<ContextInfo>> {
            override fun save(storage: DataOutput, value: List<ContextInfo>) {
                storage.writeList(value) { contextInfo ->
                    storage.writeString(contextInfo.supportId)
                    storage.writeString(contextInfo.expression)
                    storage.writeBoolean(contextInfo.configExpression is CwtKeyExpression)
                    storage.writeString(contextInfo.configExpression.expressionString)
                    run {
                        storage.writeString(contextInfo.definitionInfo.name)
                        storage.writeString(contextInfo.definitionInfo.type)
                        storage.writeInt(contextInfo.definitionInfo.subtypes.size)
                        storage.writeList(contextInfo.definitionInfo.subtypes) { subtype ->
                            storage.writeString(subtype)
                        }
                    }
                    ParadoxDefinitionHierarchySupport.saveData(storage, contextInfo)
                }
            }
            
            override fun read(storage: DataInput): List<ContextInfo> {
                return storage.readList {
                    val supportId = storage.readString()
                    val expression = storage.readString()
                    val flag = storage.readBoolean()
                    val configExpression = storage.readString().let { if(flag) CwtKeyExpression.resolve(it) else CwtValueExpression.resolve(it) }
                    val definitionInfo = run {
                        val name = storage.readString()
                        val type = storage.readString()
                        val subtypes = storage.readList { storage.readString() }
                        DefinitionInfo(name, type, subtypes)
                    }
                    val contextInfo = ContextInfo(supportId, expression, configExpression, definitionInfo)
                    ParadoxDefinitionHierarchySupport.readData(storage, contextInfo)
                    contextInfo
                }
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter p@{ file ->
            val fileType = file.fileType
            if(fileType != ParadoxScriptFileType) return@p false
            if(file.fileInfo == null) return@p false
            true
        }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
}