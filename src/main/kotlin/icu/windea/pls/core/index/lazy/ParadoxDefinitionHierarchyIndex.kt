package icu.windea.pls.core.index.lazy

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.concurrency.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

//这个索引的索引速度可能非常慢
//这个索引兼容需要内联的情况

/**
 * 用于索引定义声明中的定义引用、参数引用、本地化参数引用等。
 *
 * @see ParadoxDefinitionHierarchySupport
 */
object ParadoxDefinitionHierarchyIndex {
    private const val ID = "paradox.definition.hierarchy.index"
    private const val VERSION = 27 //1.0.5
    
    val executorService by lazy { AppExecutorUtil.createBoundedApplicationPoolExecutor("ParadoxDefinitionHierarchyIndex Pool", 4) }
    
    fun getFileData(file: VirtualFile, project: Project): Data {
        return gist.getFileData(project, file)
    }
    
    class Data(
        val definitionHierarchyInfoList: MutableList<ParadoxDefinitionHierarchyInfo> = mutableListOf()
    ) {
        val definitionHierarchyInfoGroup by lazy {
            val group = mutableMapOf<String, List<ParadoxDefinitionHierarchyInfo>>()
            definitionHierarchyInfoList.forEachFast { info ->
                val list = group.getOrPut(info.supportId) { mutableListOf() } as MutableList
                list.add(info)
            }
            group
        }
    }
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            storage.writeList(value.definitionHierarchyInfoList) { info ->
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
        
        override fun read(storage: DataInput): Data {
            val definitionHierarchyInfoList = storage.readList {
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
            if(definitionHierarchyInfoList.isEmpty()) return EmptyData
            return Data(definitionHierarchyInfoList)
        }
    }
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileType != ParadoxScriptFileType) return@builder EmptyData
        if(!matchesPath(file)) return@builder EmptyData
        val psiFile = file.toPsiFile(project) ?: return@builder EmptyData
        val data = Data()
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                //这里element作为定义的引用时也可能是ParadoxScriptInt，目前不需要考虑这种情况，因此忽略
                if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                    visitExpression(element)
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun visitExpression(element: ParadoxScriptStringExpressionElement) {
                //兼容需要内联的情况，因此不能直接向上查找得到definitionInfo
                val matchOptions = ParadoxConfigMatcher.Options.SkipScope
                val configs = ParadoxConfigResolver.getConfigs(element, matchOptions = matchOptions)
                if(configs.isEmpty()) return
                val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
                val definitionMemberInfo = memberElement.definitionMemberInfo ?: return
                val definitionInfo = definitionMemberInfo.definitionInfo
                configs.forEachFast { config ->
                    ParadoxDefinitionHierarchySupport.indexData(data.definitionHierarchyInfoList, element, config, definitionInfo)
                }
            }
        })
        
        data
    }
    
    private fun matchesPath(file: VirtualFile): Boolean {
        return file.fileInfo != null
    }
}