package icu.windea.pls.lang.expressionIndex.impl

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.psi.*
import java.io.*

private val compressComparator = compareBy<ParadoxInlineScriptUsageInfo> { it.expression }

class ParadoxInlineScriptUsageIndexSupport : ParadoxExpressionIndexSupport<ParadoxInlineScriptUsageInfo> {
    override fun id() = ParadoxExpressionIndexId.InlineScriptUsage.id
    
    override fun type() = ParadoxInlineScriptUsageInfo::class.java
    
    override fun indexElement(element: PsiElement, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        if(element !is ParadoxScriptProperty) return
        val info = ParadoxInlineScriptHandler.getUsageInfo(element) ?: return
        addToFileData(info, fileData)
    }
    
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
