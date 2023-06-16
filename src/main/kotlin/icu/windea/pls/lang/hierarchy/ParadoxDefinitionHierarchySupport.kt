package icu.windea.pls.lang.hierarchy

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

/**
 * 用于索引定义声明中的定义引用、参数引用、本地化参数引用等。
 *
 * @see ParadoxDefinitionHierarchyIndex
 */
interface ParadoxDefinitionHierarchySupport {
    val id: String
    
    fun indexData(fileData: MutableMap<String, List<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo)
    
    fun saveData(storage: DataOutput, data: ParadoxDefinitionHierarchyInfo) {}
    
    fun readData(storage: DataInput, data: ParadoxDefinitionHierarchyInfo) {}
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxDefinitionHierarchySupport>("icu.windea.pls.definitionHierarchySupport")
        
        val extensionList by lazy { EP_NAME.extensionList }
        
        fun indexData(fileData: MutableMap<String, List<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
            extensionList.forEachFast { ep ->
                ep.indexData(fileData, element, config, definitionInfo)
            }
        }
        
        fun saveData(storage: DataOutput, data: ParadoxDefinitionHierarchyInfo) {
            extensionList.forEachFast { ep ->
                if(ep.id == data.supportId) {
                    ep.saveData(storage, data)
                    return
                }
            }
        }
        
        fun readData(storage: DataInput, data: ParadoxDefinitionHierarchyInfo) {
            extensionList.forEachFast { ep ->
                if(ep.id == data.supportId) {
                    ep.readData(storage, data)
                    return
                }
            }
        }
    }
}