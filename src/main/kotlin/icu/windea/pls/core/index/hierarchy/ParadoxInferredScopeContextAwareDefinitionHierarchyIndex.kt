package icu.windea.pls.core.index.hierarchy

import com.intellij.openapi.vfs.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.scope.impl.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxInferredScopeContextAwareDefinitionHierarchyIndex : ParadoxDefinitionHierarchyIndex<ParadoxInferredScopeContextAwareDefinitionHierarchyIndex.Info>() {
    data class Info(
        val definitionName: String,
        val typeExpression: String,
        override val elementOffset: Int,
        override val gameType: ParadoxGameType
    ) : ParadoxExpressionInfo {
        @Volatile override var virtualFile: VirtualFile? = null
    }
    
    companion object {
        @JvmField val NAME = ID.create<String, List<Info>>("paradox.inferredScopeContextAware.definitionHierarchy.index")
        private const val VERSION = 32 //1.1.3
        private val INSTANCE by lazy { EXTENSION_POINT_NAME.findExtensionOrFail(ParadoxInferredScopeContextAwareDefinitionHierarchyIndex::class.java) }
        
        @JvmStatic
        fun getInstance() = INSTANCE
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<Info>>) {
        run {
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return //skip if expression is empty or parameterized
            val dataType = config.expression.type
            if(dataType != CwtDataType.Definition) return
            val definitionType = config.expression.value?.substringBefore('.') ?: return
            if(definitionType !in ParadoxBaseDefinitionInferredScopeContextProvider.DEFINITION_TYPES) return
        }
        
        val definitionName = element.value
        val typeExpression = config.expression.value ?: return
        val info = Info(definitionName, typeExpression, element.startOffset, definitionInfo.gameType)
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
            storage.writeUTFFast(info.definitionName)
            storage.writeUTFFast(info.typeExpression)
            storage.writeIntFast(info.elementOffset)
        }
    }
    
    override fun readData(storage: DataInput): List<Info> {
        val size = storage.readIntFast()
        if(size == 0) return emptyList()
        val gameType = storage.readByte().toGameType()
        val result = mutableListOf<Info>()
        repeat(size) {
            val definitionName = storage.readUTFFast()
            val typeExpression = storage.readUTFFast()
            val elementOffset = storage.readIntFast()
            val info = Info(definitionName, typeExpression, elementOffset, gameType)
            result += info
        }
        return result
    }
}