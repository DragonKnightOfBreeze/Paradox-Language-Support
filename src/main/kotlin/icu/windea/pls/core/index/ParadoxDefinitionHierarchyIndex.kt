package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
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
class ParadoxDefinitionHierarchyIndex : FileBasedIndexExtension<String, List<ParadoxDefinitionHierarchyInfo>>() {
    companion object {
        @JvmField val NAME = ID.create<String, List<ParadoxDefinitionHierarchyInfo>>("paradox.definition.hierarchy.index")
        private const val VERSION = 27 //1.0.5
        
        fun getData(file: VirtualFile, project: Project): Map<String, List<ParadoxDefinitionHierarchyInfo>> {
            return FileBasedIndex.getInstance().getFileData(NAME, file, project)
        }
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, List<ParadoxDefinitionHierarchyInfo>, FileContent> {
        val matchOptions = ParadoxConfigMatcher.Options.SkipIndex or ParadoxConfigMatcher.Options.SkipScope
        return DataIndexer { inputData ->
            val file = inputData.psiFile
            val definitionInfoStack = LinkedList<ParadoxDefinitionInfo>()
            buildMap<String, MutableList<ParadoxDefinitionHierarchyInfo>> {
                file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if(element is ParadoxScriptDefinitionElement) {
                            val definitionInfo = element.definitionInfo //perf: 2.6%
                            if(definitionInfo != null) {
                                definitionInfoStack.addLast(definitionInfo)
                            }
                        }
                        val definitionInfo = definitionInfoStack.peekLast()
                        if(definitionInfo != null) {
                            //这里element作为定义的引用时也可能是ParadoxScriptInt，目前不需要考虑这种情况，因此忽略
                            if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                                val configs = ParadoxConfigResolver.getConfigs(element, matchOptions = matchOptions) //perf: 82.3%
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
    
    override fun getValueExternalizer(): DataExternalizer<List<ParadoxDefinitionHierarchyInfo>> {
        return object : DataExternalizer<List<ParadoxDefinitionHierarchyInfo>> {
            override fun save(storage: DataOutput, value: List<ParadoxDefinitionHierarchyInfo>) {
                storage.writeList(value) { info ->
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
            }
            
            override fun read(storage: DataInput): List<ParadoxDefinitionHierarchyInfo> {
                return storage.readList {
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