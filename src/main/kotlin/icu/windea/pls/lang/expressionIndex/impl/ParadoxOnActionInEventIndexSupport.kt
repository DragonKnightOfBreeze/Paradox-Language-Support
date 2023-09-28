package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.core.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import java.io.*

class ParadoxOnActionInEventIndexSupport: ParadoxExpressionIndexSupport<ParadoxOnActionInEventInfo> {
    override fun id() = ParadoxExpressionIndexIds.OnActionInEvent
    
    override fun type() = ParadoxOnActionInEventInfo::class.java
    
    override fun compress(value: List<ParadoxOnActionInEventInfo>): List<ParadoxOnActionInEventInfo> {
        return value
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxOnActionInEventInfo, previousInfo: ParadoxOnActionInEventInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.onActionName)
        storage.writeUTFFast(info.containingEventName)
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxOnActionInEventInfo?, gameType: ParadoxGameType): ParadoxOnActionInEventInfo {
        val onActionName = storage.readUTFFast()
        val containingEventName = storage.readUTFFast()
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        val elementOffset = storage.readIntFast()
        return ParadoxOnActionInEventInfo(onActionName, containingEventName, containingEventScope, scopesElementOffset, elementOffset, gameType)
    }
}