package icu.windea.pls.lang.hierarchy.impl

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxLocalisationParameterDefinitionHierarchySupport: ParadoxDefinitionHierarchySupport {
    companion object {
        const val ID = "localisationParameter"
        
        val localisationNameKey = Key.create<String>("paradox.definition.hierarchy.localisationParameter.localisationName")
    }
    
    override val id: String = ID
    
    override fun indexData(fileData: MutableMap<String, List<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
        //val configExpression = config.expression
        //if(configExpression.type != CwtDataType.LocalisationParameter) return
        
        val localisationReferenceElement = ParadoxLocalisationParameterHandler.getLocalisationReferenceElement(element, config) ?: return
        val localisationName = localisationReferenceElement.name.takeIfNotEmpty()
        if(localisationName == null) return
        
        //elementOffset has not been used yet by this support
        val info = ParadoxDefinitionHierarchyInfo(id, element.value, config, definitionInfo, -1 /*element.startOffset*/)
        info.putUserData(localisationNameKey, localisationName)
        val list = fileData.getOrPut(id) { mutableListOf() } as MutableList
        list.add(info)
    }
    
    override fun saveData(storage: DataOutput, info: ParadoxDefinitionHierarchyInfo, previousInfo: ParadoxDefinitionHierarchyInfo?) {
        storage.writeUTFFast(info.getUserData(localisationNameKey).orEmpty())
    }
    
    override fun readData(storage: DataInput, data: ParadoxDefinitionHierarchyInfo, previousInfo: ParadoxDefinitionHierarchyInfo?) {
        storage.readUTFFast()
            .takeIfNotEmpty()?.let { data.putUserData(localisationNameKey, it) }
    }
}
