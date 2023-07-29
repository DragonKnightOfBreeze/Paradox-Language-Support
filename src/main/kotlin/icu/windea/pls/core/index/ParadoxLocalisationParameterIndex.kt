package icu.windea.pls.core.index

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

private val NAME = ID.create<String, List<ParadoxLocalisationParameterInfo>>("paradox.localisationParameter.index")
private const val VERSION = 32 //1.1.3

class ParadoxLocalisationParameterIndex : ParadoxFileBasedIndex<List<ParadoxLocalisationParameterInfo>>() {
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxLocalisationParameterInfo>>) {
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                    val matchOptions = ParadoxConfigMatcher.Options.SkipIndex or ParadoxConfigMatcher.Options.SkipScope
                    val configs = ParadoxConfigHandler.getConfigs(element, matchOptions = matchOptions)
                    configs.forEachFast { config ->
                        doIndexData(element, config, fileData)
                    }
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
        })
        
        //排序
        if(fileData.isEmpty()) return
        fileData.mapValues { (_, v) ->
            v.sortedBy { it.name }
        }
    }
    
    private fun doIndexData(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, fileData: MutableMap<String, List<ParadoxLocalisationParameterInfo>>) {
        val localisationReferenceElement = ParadoxLocalisationParameterHandler.getLocalisationReferenceElement(element, config) ?: return
        val name = element.value
        if(name.isEmpty() || name.isParameterized()) return
        val localisationName = localisationReferenceElement.name
        if(localisationName.isEmpty() || localisationName.isParameterized()) return
        val elementOffset = element.startOffset
        val gameType = config.info.configGroup.gameType ?: return
        val info = ParadoxLocalisationParameterInfo(name, localisationName, elementOffset, gameType)
        val list = fileData.getOrPut(localisationName) { mutableListOf() } as MutableList
        list.add(info)
    }
    
    //尝试减少实际需要索引的数据量以优化性能
    
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
    
    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if(fileType != ParadoxScriptFileType && fileType != ParadoxLocalisationFileType) return false
        if(file.fileInfo == null) return false
        return true
    }
    
    override fun useLazyIndex(file: VirtualFile): Boolean {
        if(ParadoxFileManager.isInjectedFile(file)) return true
        if(ParadoxInlineScriptHandler.getInlineScriptExpression(file) != null) return true
        return false
    }
}