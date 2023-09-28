package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.core.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import java.io.*

class ParadoxInferredScopeContextAwareDefinitionIndexSupport: ParadoxExpressionIndexSupport<ParadoxInferredScopeContextAwareDefinitionInfo> {
    override fun id() = ParadoxExpressionIndexIds.InferredScopeContextAwareDefinition
    
    override fun type() = ParadoxInferredScopeContextAwareDefinitionInfo::class.java
    
    override fun compress(value: List<ParadoxInferredScopeContextAwareDefinitionInfo>): List<ParadoxInferredScopeContextAwareDefinitionInfo> {
        return value
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxInferredScopeContextAwareDefinitionInfo, previousInfo: ParadoxInferredScopeContextAwareDefinitionInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.definitionName)
        storage.writeUTFFast(info.typeExpression)
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxInferredScopeContextAwareDefinitionInfo?, gameType: ParadoxGameType): ParadoxInferredScopeContextAwareDefinitionInfo {
        val definitionName = storage.readUTFFast()
        val typeExpression = storage.readUTFFast()
        val elementOffset = storage.readIntFast()
        return ParadoxInferredScopeContextAwareDefinitionInfo(definitionName, typeExpression, elementOffset, gameType)
    }
}