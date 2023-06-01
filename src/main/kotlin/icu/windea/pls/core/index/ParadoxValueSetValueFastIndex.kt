package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

//这个索引不会保存同一文件中重复的ParadoxValueSetValueInfo
//这个索引不会保存ParadoxValueSetValueInfo.elementOffset

class ParadoxValueSetValueFastIndex : FileBasedIndexExtension<String, List<ParadoxValueSetValueInfo>>() {
    companion object {
        @JvmField val NAME = ID.create<String, List<ParadoxValueSetValueInfo>>("paradox.valueSetValue.fast.index")
        private const val VERSION = 27 //1.0.5
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, List<ParadoxValueSetValueInfo>, FileContent> {
        return DataIndexer { inputData -> //perf: 148000ms+ for indexing
            val file = inputData.psiFile
            buildMap { 
                val keys = mutableSetOf<String>()
                if(file.fileType == ParadoxScriptFileType) {
                    file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() { //perf: 95%
                        override fun visitElement(element: PsiElement) {
                            if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                                val infos = ParadoxValueSetValueHandler.getInfos(element)
                                addInfos(infos, keys)
                            }
                            if(element.isExpressionOrMemberContext()) super.visitElement(element)
                        }
                    })
                } else {
                    file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() { //perf: 5%
                        override fun visitElement(element: PsiElement) {
                            if(element is ParadoxLocalisationCommandIdentifier) {
                                val infos = ParadoxValueSetValueHandler.getInfos(element)
                                addInfos(infos, keys)
                            }
                            if(element.isRichTextContext()) super.visitElement(element)
                        }
                    })
                }
            }
        }
    }
    
    private fun MutableMap<String, List<ParadoxValueSetValueInfo>>.addInfos(infos: List<ParadoxValueSetValueInfo>, keys: MutableSet<String>) {
        infos.forEachFast { info ->
            val key = info.valueSetName + "@" + info.name + "@" + info.readWriteAccess.ordinal
            if(keys.add(key)) {
                val map = getOrPut(info.valueSetName) { mutableListOf() } as MutableList
                map.add(info)
            }
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getValueExternalizer(): DataExternalizer<List<ParadoxValueSetValueInfo>> {
        return object : DataExternalizer<List<ParadoxValueSetValueInfo>> {
            override fun save(storage: DataOutput, value: List<ParadoxValueSetValueInfo>) {
                storage.writeList(value) { valueSetValueInfo ->
                    storage.writeString(valueSetValueInfo.name)
                    storage.writeString(valueSetValueInfo.valueSetName)
                    storage.writeByte(valueSetValueInfo.readWriteAccess.toByte())
                    storage.writeInt(valueSetValueInfo.elementOffset)
                    storage.writeByte(valueSetValueInfo.gameType.toByte())
                }
            }
            
            override fun read(storage: DataInput): List<ParadoxValueSetValueInfo> {
                return storage.readList {
                    val name = storage.readString()
                    val valueSetName = storage.readString()
                    val readWriteAccess = storage.readByte().toReadWriteAccess()
                    val elementOffset = storage.readInt()
                    val gameType = storage.readByte().toGameType()
                    ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
                }
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter p@{ file ->
            val fileType = file.fileType
            if(fileType != ParadoxScriptFileType && fileType != ParadoxLocalisationFileType) return@p false
            if(file.fileInfo == null) return@p false
            true
        }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
}