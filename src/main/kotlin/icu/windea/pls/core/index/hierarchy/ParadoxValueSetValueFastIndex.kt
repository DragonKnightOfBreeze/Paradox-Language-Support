package icu.windea.pls.core.index.hierarchy

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

/**
 * 用于索引值集值。
 *
 * * 这个索引不会保存同一文件中的重复数据。
 * * 这个索引不会记录数据在文件中的位置。
 * * 这个索引兼容需要内联的情况（此时使用懒加载的索引）。
 * 
 * @see ParadoxValueSetValueInfo
 */
class ParadoxValueSetValueFastIndex : ParadoxHierarchyIndex<List<ParadoxValueSetValueInfo>>() {
    companion object {
        val NAME = ID.create<String, List<ParadoxValueSetValueInfo>>("paradox.valueSetValue.fast.index")
        private const val VERSION = 32 //1.1.3
        private val INSTANCE by lazy { EXTENSION_POINT_NAME.findExtensionOrFail(ParadoxValueSetValueFastIndex::class.java) }
        
        @JvmStatic
        fun getInstance() = INSTANCE
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxValueSetValueInfo>>) {
        if(file.fileType == ParadoxScriptFileType) {
            file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                        val infos = ParadoxValueSetValueHandler.getInfos(element)
                        infos.forEachFast { info ->
                            val list = fileData.getOrPut(info.valueSetName) { mutableListOf() } as MutableList
                            list.add(info)
                        }
                    }
                    if(element.isExpressionOrMemberContext()) super.visitElement(element)
                }
            })
        } else {
            file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if(element is ParadoxLocalisationCommandIdentifier) {
                        val infos = ParadoxValueSetValueHandler.getInfos(element)
                        infos.forEachFast { info ->
                            val list = fileData.getOrPut(info.valueSetName) { mutableListOf() } as MutableList
                            list.add(info)
                        }
                    }
                    if(element.isRichTextContext()) super.visitElement(element)
                }
            })
        }
        
        //排序 & 去重
        if(fileData.isEmpty()) return
        fileData.mapValues { (_, v) -> 
            v.distinctBy { it.name + "@" + it.readWriteAccess.ordinal }.sortedBy { it.name }
        }
    }
    
    //尝试减少实际需要索引的数据量以优化性能
    
    override fun writeData(storage: DataOutput, value: List<ParadoxValueSetValueInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if(size == 0) return
        val firstInfo = value.first()
        storage.writeUTFFast(firstInfo.valueSetName)
        storage.writeByte(firstInfo.gameType.toByte())
        var previousInfo: ParadoxValueSetValueInfo? = null
        value.forEachFast { info ->
            storage.writeOrWriteFrom(info, previousInfo, { it.name }, { storage.writeUTFFast(it) })
            storage.writeByte(info.readWriteAccess.toByte())
            storage.writeIntFast(info.elementOffset)
            previousInfo = info
        }
    }
    
    override fun readData(storage: DataInput): List<ParadoxValueSetValueInfo> {
        val size = storage.readIntFast()
        if(size == 0) return emptyList()
        val valueSetName = storage.readUTFFast()
        val gameType = storage.readByte().toGameType()
        var previousInfo: ParadoxValueSetValueInfo? = null
        val result = mutableListOf<ParadoxValueSetValueInfo>()
        repeat(size) {
            val name = storage.readOrReadFrom(previousInfo, { it.name }, { storage.readUTFFast() })
            val readWriteAccess = storage.readByte().toReadWriteAccess()
            val elementOffset = storage.readIntFast()
            val info = ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
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
