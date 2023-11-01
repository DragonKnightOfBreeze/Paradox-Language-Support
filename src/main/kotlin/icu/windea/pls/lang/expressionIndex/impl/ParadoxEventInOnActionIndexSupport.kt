package icu.windea.pls.lang.expressionIndex.impl

import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

private val compressComparator = compareBy<ParadoxEventInOnActionInfo> { it.containingOnActionName }

class ParadoxEventInOnActionIndexSupport : ParadoxExpressionIndexSupport<ParadoxEventInOnActionInfo> {
    override fun id() = ParadoxExpressionIndexId.EventInOnAction.id
    
    override fun type() = ParadoxEventInOnActionInfo::class.java
    
    override fun indexScriptExpression(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxExpressionInfo>>) {
        run {
            if(definitionInfo.type != "on_action") return
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if(dataType != CwtDataType.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if(definitionType != "event") return
        }
        
        val eventName = element.value
        val typeExpression = config.expression.value ?: return
        val containingOnActionName = definitionInfo.name
        val info = ParadoxEventInOnActionInfo(eventName, typeExpression, containingOnActionName, element.startOffset, definitionInfo.gameType)
        addToFileData(info, fileData)
    }
    
    override fun compressData(value: List<ParadoxEventInOnActionInfo>): List<ParadoxEventInOnActionInfo> {
        return value.sortedWith(compressComparator)
    }
    
    override fun writeData(storage: DataOutput, info: ParadoxEventInOnActionInfo, previousInfo: ParadoxEventInOnActionInfo?, gameType: ParadoxGameType) {
        storage.writeUTFFast(info.eventName)
        storage.writeUTFFast(info.typeExpression)
        storage.writeOrWriteFrom(info, previousInfo, { it.containingOnActionName }, { storage.writeUTFFast(it) })
        storage.writeIntFast(info.elementOffset)
    }
    
    override fun readData(storage: DataInput, previousInfo: ParadoxEventInOnActionInfo?, gameType: ParadoxGameType): ParadoxEventInOnActionInfo {
        val eventName = storage.readUTFFast()
        val typeExpression = storage.readUTFFast()
        val containingOnActionName = storage.readOrReadFrom(previousInfo, { it.containingOnActionName }, { storage.readUTFFast() })
        val elementOffset = storage.readIntFast()
        return ParadoxEventInOnActionInfo(eventName, typeExpression, containingOnActionName, elementOffset, gameType)
    }
}