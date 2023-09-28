package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.core.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import java.io.*

class ParadoxEventInOnActionIndexSupport: ParadoxExpressionIndexSupport<ParadoxEventInOnActionInfo> {
    override fun id() = ParadoxExpressionIndexIds.EventInOnAction
    
    override fun type() = ParadoxEventInOnActionInfo::class.java
    
    override fun compress(value: List<ParadoxEventInOnActionInfo>): List<ParadoxEventInOnActionInfo> {
        return value
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxEventInOnActionInfo, previousInfo: ParadoxEventInOnActionInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeUTFFast(info.typeExpression)
        storage.writeUTFFast(info.containingOnActionName)
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxEventInOnActionInfo?, gameType: ParadoxGameType): ParadoxEventInOnActionInfo {
        val eventName = storage.readUTFFast()
        val typeExpression = storage.readUTFFast()
        val containingOnActionName = storage.readUTFFast()
        val elementOffset = storage.readIntFast()
        return ParadoxEventInOnActionInfo(eventName, typeExpression, containingOnActionName, elementOffset, gameType)
    }
}