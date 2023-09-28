package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.core.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import java.io.*

class ParadoxEventInEventIndexSupport: ParadoxExpressionIndexSupport<ParadoxEventInEventInfo> {
    override fun id() = ParadoxExpressionIndexIds.EventInEvent
    
    override fun type() = ParadoxEventInEventInfo::class.java
    
    override fun compress(value: List<ParadoxEventInEventInfo>): List<ParadoxEventInEventInfo> {
        return value
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxEventInEventInfo, previousInfo: ParadoxEventInEventInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeUTFFast(info.containingEventName)
        storage.writeUTFFast(info.containingEventScope.orEmpty())
        storage.writeIntFast(info.scopesElementOffset)
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxEventInEventInfo?, gameType: ParadoxGameType): ParadoxEventInEventInfo {
        val eventName = storage.readUTFFast()
        val containingEventName = storage.readUTFFast()
        val containingEventScope = storage.readUTFFast().orNull()
        val scopesElementOffset = storage.readIntFast()
        val elementOffset = storage.readIntFast()
        return ParadoxEventInEventInfo(eventName, containingEventName, containingEventScope, scopesElementOffset, elementOffset, gameType)
    }
}