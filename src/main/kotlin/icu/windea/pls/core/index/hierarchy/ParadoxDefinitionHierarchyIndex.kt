package icu.windea.pls.core.index.hierarchy

import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
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
 * * 这个索引可能不会记录数据在文件中的位置。
 * * 这个索引目前需要兼容需要内联的情况（此时使用懒加载的索引）。
 *
 * @see ParadoxDefinitionHierarchyInfo
 * @see ParadoxDefinitionHierarchySupport
 */
class ParadoxDefinitionHierarchyIndex : ParadoxHierarchyIndex<List<ParadoxDefinitionHierarchyInfo>>() {
    companion object {
        val NAME = ID.create<String, List<ParadoxDefinitionHierarchyInfo>>("paradox.definition.hierarchy.index")
        private const val VERSION = 32 //1.1.3
        private val INSTANCE by lazy { EXTENSION_POINT_NAME.findExtensionOrFail(ParadoxDefinitionHierarchyIndex::class.java) }
        
        @JvmStatic
        fun getInstance() = INSTANCE
        
        private val markKey = Key.create<Boolean>("paradox.definition.hierarchy.index.mark")
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxDefinitionHierarchyInfo>>) {
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            private val definitionInfoStack = LinkedList<ParadoxDefinitionInfo>()
            
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo
                    if(definitionInfo != null) {
                        element.putUserData(markKey, true)
                        definitionInfoStack.addLast(definitionInfo)
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
        
        //排序
        if(fileData.isEmpty()) return
        fileData.mapValues { (_, v) ->
            v.sortedWith(compareBy({ it.definitionType + " " + it.definitionName }, { it.configExpression }, { it.expression }))
        }
    }

    //通常情况下需要索引的数据可能非常多，需要进行优化
    //尝试减少实际需要索引的数据量以优化性能
    
    override fun writeData(storage: DataOutput, value: List<ParadoxDefinitionHierarchyInfo>) {
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
    
    override fun readData(storage: DataInput): List<ParadoxDefinitionHierarchyInfo> {
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
            val definitionSubtypes = storage.readOrReadFrom(previousInfo, { it.definitionSubtypes }, { storage.readList { storage.readUTFFast() } })
            val elementOffset = storage.readIntFast()
            val info = ParadoxDefinitionHierarchyInfo(supportId, expression, configExpression, isKey, definitionName, definitionType, definitionSubtypes, elementOffset, gameType)
            ParadoxDefinitionHierarchySupport.readData(storage, info, previousInfo)
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
        return false
        //if(ParadoxFileManager.isInjectedFile(file)) return true
        //if(ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null) return true
        //return false
    }
}
