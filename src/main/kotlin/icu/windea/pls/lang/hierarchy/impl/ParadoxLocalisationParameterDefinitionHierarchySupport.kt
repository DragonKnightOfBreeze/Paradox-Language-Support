package icu.windea.pls.lang.hierarchy.impl

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.hierarchy.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxLocalisationParameterDefinitionHierarchySupport: ParadoxDefinitionHierarchySupport {
    companion object {
        const val ID = "localisationParameter"
        
        val localisationKey = Key.create<String>("paradox.definition.hierarchy.localisationParameter.localisation")
    }
    
    override val id: String = ID
    
    override fun indexData(fileData: MutableMap<String, MutableList<ParadoxDefinitionHierarchyInfo>>, element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo) {
        //val configExpression = config.expression
        //if(configExpression.type != CwtDataType.LocalisationParameter) return
        
        val localisationReferenceElement = ParadoxLocalisationParameterHandler.getLocalisationReferenceElement(element, config) ?: return
        val localisationName = localisationReferenceElement.name
        val info = ParadoxDefinitionHierarchyInfo(id, element.value, config.expression, definitionInfo.name, definitionInfo.type, definitionInfo.subtypes, element.startOffset, definitionInfo.gameType)
        info.putUserData(localisationKey, localisationName)
        fileData.getOrPut(id) { mutableListOf() }.add(info)
    }
    
    override fun saveData(storage: DataOutput, data: ParadoxDefinitionHierarchyInfo) {
        storage.writeString(data.getUserData(localisationKey).orEmpty())
    }
    
    override fun readData(storage: DataInput, data: ParadoxDefinitionHierarchyInfo) {
        data.putUserData(localisationKey, storage.readString()) 
    }
}