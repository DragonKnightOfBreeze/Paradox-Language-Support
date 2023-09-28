package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.core.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import java.io.*

//private val compressComparator = compareBy<ParadoxLocalisationParameterInfo>({ it.localisationName }, { it.name })
private val compressComparator = compareBy<ParadoxLocalisationParameterInfo> { it.localisationName }

class ParadoxLocalisationParameterIndexSupport : ParadoxExpressionIndexSupport<ParadoxLocalisationParameterInfo> {
    override fun id() = ParadoxExpressionIndexIds.LocalisationParameter
    
    override fun type() = ParadoxLocalisationParameterInfo::class.java
    
    override fun compress(value: List<ParadoxLocalisationParameterInfo>): List<ParadoxLocalisationParameterInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxLocalisationParameterInfo, previousInfo: ParadoxLocalisationParameterInfo?, gameType: ParadoxGameType) {
        storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
        storage.writeOrWriteFrom(info, previousInfo, { it.localisationName }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxLocalisationParameterInfo?, gameType: ParadoxGameType): ParadoxLocalisationParameterInfo {
        val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
        val localisationName = storage.readOrReadFrom(previousInfo, { it.localisationName }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxLocalisationParameterInfo(name, localisationName, elementOffset, gameType)
    }
}