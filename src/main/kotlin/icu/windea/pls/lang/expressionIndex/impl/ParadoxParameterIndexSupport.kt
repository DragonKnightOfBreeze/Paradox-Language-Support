package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import java.io.*

//private val compressComparator = compareBy<ParadoxParameterInfo>({ it.contextKey }, { it.name })
private val compressComparator = compareBy<ParadoxParameterInfo>{ it.contextKey }

class ParadoxParameterIndexSupport : ParadoxExpressionIndexSupport<ParadoxParameterInfo> {
    override fun id() = ParadoxExpressionIndexIds.Parameter
    
    override fun type() = ParadoxParameterInfo::class.java
    
    override fun compress(value: List<ParadoxParameterInfo>): List<ParadoxParameterInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxParameterInfo, previousInfo: ParadoxParameterInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.contextKey }, { storage.writeUTFFast(it) })
        storage.writeByte(info.readWriteAccess.toByte())
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxParameterInfo?, gameType: ParadoxGameType): ParadoxParameterInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val contextKey = storage.readOrReadFrom(previousInfo, { it.contextKey }, { storage.readUTFFast() })
        val readWriteAccess = storage.readByte().toReadWriteAccess()
        val elementOffset = storage.readIntFast()
        return ParadoxParameterInfo(name, contextKey, readWriteAccess, elementOffset, gameType)
    }
}