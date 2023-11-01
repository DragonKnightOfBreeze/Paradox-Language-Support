package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

private val compressComparator = compareBy<ParadoxInferredScopeContextAwareDefinitionInfo> { it.typeExpression }

class ParadoxInferredScopeContextAwareDefinitionIndexSupport : ParadoxExpressionIndexSupport<ParadoxInferredScopeContextAwareDefinitionInfo> {
    override fun id() = ParadoxExpressionIndexId.InferredScopeContextAwareDefinition.id
    
    override fun type() = ParadoxInferredScopeContextAwareDefinitionInfo::class.java
    
    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        run {
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if(dataType != CwtDataType.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if(definitionType !in ParadoxExpressionIndexHandler.inferredScopeContextAwareDefinitionTypes) return
        }
        
        val definitionName = element.value
        val typeExpression = config.expression.value ?: return
        val info = ParadoxInferredScopeContextAwareDefinitionInfo(definitionName, typeExpression, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }
    
    override fun compressData(value: List<ParadoxInferredScopeContextAwareDefinitionInfo>): List<ParadoxInferredScopeContextAwareDefinitionInfo> {
        return value
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxInferredScopeContextAwareDefinitionInfo, previousInfo: ParadoxInferredScopeContextAwareDefinitionInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.definitionName)
        storage.writeOrWriteFrom(info, previousInfo, { it.typeExpression }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxInferredScopeContextAwareDefinitionInfo?, gameType: ParadoxGameType): ParadoxInferredScopeContextAwareDefinitionInfo {
        val definitionName = storage.readUTFFast()
        val typeExpression = storage.readOrReadFrom(previousInfo, { it.typeExpression }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxInferredScopeContextAwareDefinitionInfo(definitionName, typeExpression, elementOffset, gameType)
    }
}