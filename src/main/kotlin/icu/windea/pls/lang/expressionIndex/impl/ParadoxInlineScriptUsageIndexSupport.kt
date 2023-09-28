package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.core.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import java.io.*

private val compressComparator = compareBy<ParadoxInlineScriptUsageInfo> { it.expression }

class ParadoxInlineScriptUsageIndexSupport : ParadoxExpressionIndexSupport<ParadoxInlineScriptUsageInfo> {
    override fun id() = ParadoxExpressionIndexIds.InlineScriptUsage
    
    override fun type() = ParadoxInlineScriptUsageInfo::class.java
    
    override fun compress(value: List<ParadoxInlineScriptUsageInfo>): List<ParadoxInlineScriptUsageInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxInlineScriptUsageInfo, previousInfo: ParadoxInlineScriptUsageInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.expression)
        storage.writeInt(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxInlineScriptUsageInfo?, gameType: ParadoxGameType): ParadoxInlineScriptUsageInfo {
        val expression = storage.readUTFFast()
        val elementOffset = storage.readInt()
        return ParadoxInlineScriptUsageInfo(expression, elementOffset, gameType)
    }
}
