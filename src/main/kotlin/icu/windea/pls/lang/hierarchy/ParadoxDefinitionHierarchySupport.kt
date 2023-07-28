package icu.windea.pls.lang.hierarchy

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.hierarchy.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

/**
 * 用于索引定义声明中特定的表达式。
 *
 * @see ParadoxDefinitionHierarchyIndex
 */
interface ParadoxDefinitionHierarchySupport {
    val id: String
    
    fun indexData(fileData: MutableMap<String, List<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo)
    
    fun saveData(storage: DataOutput, info: ParadoxDefinitionHierarchyInfo, previousInfo: ParadoxDefinitionHierarchyInfo?) {}
    
    fun readData(storage: DataInput, data: ParadoxDefinitionHierarchyInfo, previousInfo: ParadoxDefinitionHierarchyInfo?) {}
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDefinitionHierarchySupport>("icu.windea.pls.definitionHierarchySupport")
        
        //cache extensionList to optimize memory (currently only for this EP)
        val extensionList by lazy { EP_NAME.extensionList }
        
        fun indexData(fileData: MutableMap<String, List<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
            extensionList.forEachFast { ep ->
                ep.indexData(fileData, element, config, definitionInfo)
            }
        }
        
        fun saveData(storage: DataOutput, info: ParadoxDefinitionHierarchyInfo, previousInfo: ParadoxDefinitionHierarchyInfo?) {
            extensionList.forEachFast { ep ->
                if(ep.id == info.supportId) {
                    ep.saveData(storage, info, previousInfo)
                    return
                }
            }
        }
        
        fun readData(storage: DataInput, info: ParadoxDefinitionHierarchyInfo, previousInfo: ParadoxDefinitionHierarchyInfo?) {
            extensionList.forEachFast { ep ->
                if(ep.id == info.supportId) {
                    ep.readData(storage, info, previousInfo)
                    return
                }
            }
        }
    }
}