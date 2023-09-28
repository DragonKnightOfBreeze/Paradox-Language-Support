package icu.windea.pls.core.index.hierarchy

import com.intellij.openapi.vfs.*
import com.intellij.util.indexing.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.*
import icu.windea.pls.script.psi.*
import java.io.*

private val NAME = ID.create<String, List<ParadoxEventInOnActionDefinitionHierarchyIndex.Info>>("paradox.eventInOnAction.definitionHierarchy.index")
private const val VERSION = 40 //1.1.12

class ParadoxEventInOnActionDefinitionHierarchyIndex : ParadoxDefinitionHierarchyIndex<ParadoxEventInOnActionDefinitionHierarchyIndex.Info>() {
    data class Info(
        val eventName: String,
        val typeExpression: String,
        val containingOnActionName: String,
        override val elementOffset: Int,
        override val gameType: ParadoxGameType
    ) : ParadoxExpressionInfo {
        @Volatile override var virtualFile: VirtualFile? = null
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<Info>>) {
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
        val info = Info(eventName, typeExpression, containingOnActionName, element.startOffset, definitionInfo.gameType)
        val list = fileData.getOrPut("") { mutableListOf() } as MutableList
        list.add(info)
    }
    
    override fun writeData(storage: DataOutput, value: List<Info>) {
        val size = value.size
        storage.writeIntFast(size)
        if(size == 0) return
        val firstInfo = value.first()
        storage.writeByte(firstInfo.gameType.toByte())
        value.forEachFast { info ->
            storage.writeUTFFast(info.eventName)
            storage.writeUTFFast(info.typeExpression)
            storage.writeUTFFast(info.containingOnActionName)
            storage.writeIntFast(info.elementOffset)
        }
    }
    
    override fun readData(storage: DataInput): List<Info> {
        val size = storage.readIntFast()
        if(size == 0) return emptyList()
        val gameType = storage.readByte().toGameType()
        val result = mutableListOf<Info>()
        repeat(size) {
            val eventName = storage.readUTFFast()
            val typeExpression = storage.readUTFFast()
            val containingOnActionName = storage.readUTFFast()
            val elementOffset = storage.readIntFast()
            val info = Info(eventName, typeExpression, containingOnActionName, elementOffset, gameType)
            result += info
        }
        return result
    }
}