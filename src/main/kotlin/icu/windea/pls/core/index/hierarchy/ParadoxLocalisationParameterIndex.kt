package icu.windea.pls.core.index.hierarchy

import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.model.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxLocalisationParameterIndex: ParadoxDefinitionHierarchyIndex<ParadoxLocalisationParameterInfo>() {
    companion object {
        val NAME = ID.create<String, List<ParadoxLocalisationParameterInfo>>("paradox.localisationParameter.index")
        private const val VERSION = 32 //1.1.3
        private val INSTANCE by lazy { EXTENSION_POINT_NAME.findExtensionOrFail(ParadoxLocalisationParameterIndex::class.java) }
        
        @JvmStatic
        fun getInstance() = INSTANCE
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, definitionInfo: ParadoxDefinitionInfo, fileData: MutableMap<String, List<ParadoxLocalisationParameterInfo>>) {
        val localisationReferenceElement = ParadoxLocalisationParameterHandler.getLocalisationReferenceElement(element, config) ?: return
        val localisationName = localisationReferenceElement.name.takeIfNotEmpty()
        if(localisationName == null) return
        val name = element.value
        val info = ParadoxLocalisationParameterInfo(name, localisationName, element.startOffset, definitionInfo.gameType)
        val list = fileData.getOrPut(localisationName) { mutableListOf() } as MutableList
        list.add(info)
    }
    
    override fun afterIndexData(fileData: MutableMap<String, List<ParadoxLocalisationParameterInfo>>) {
        if(fileData.isEmpty()) return
        fileData.mapValues { (_,v) ->
            v.sortedBy { it.name }
        }
    }
    
    override fun writeData(storage: DataOutput, value: List<ParadoxLocalisationParameterInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if(size == 0) return
        val firstInfo = value.first()
        storage.writeUTFFast(firstInfo.localisationName)
        storage.writeByte(firstInfo.gameType.toByte())
        var previousInfo: ParadoxLocalisationParameterInfo? = null
        value.forEachFast { info ->
            storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
            storage.writeIntFast(info.elementOffset)
            previousInfo = info
        }
    }
    
    override fun readData(storage: DataInput): List<ParadoxLocalisationParameterInfo> {
        val size = storage.readIntFast()
        if(size == 0) return emptyList()
        val localisationName = storage.readUTFFast()
        val gameType = storage.readByte().toGameType()
        var previousInfo: ParadoxLocalisationParameterInfo? = null
        val result = mutableListOf<ParadoxLocalisationParameterInfo>()
        repeat(size) {
            val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
            val elementOffset = storage.readIntFast()
            val info = ParadoxLocalisationParameterInfo(name, localisationName, elementOffset, gameType)
            result += info
            previousInfo = info
        }
        return result
    }
}