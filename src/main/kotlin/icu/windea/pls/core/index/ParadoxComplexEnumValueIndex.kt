package icu.windea.pls.core.index

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import java.io.*

class ParadoxComplexEnumValueIndex : FileBasedIndexExtension<String, Map<String, ParadoxComplexEnumValueInfo>>() { 
    companion object {
        @JvmField val NAME = ID.create<String, Map<String, ParadoxComplexEnumValueInfo>>("paradox.complexEnumValue.index")
        private const val VERSION = 1
        
        fun getData(enumName: String, file: VirtualFile, project: Project): Map<String, ParadoxComplexEnumValueInfo>? {
            return FileBasedIndex.getInstance().getFileData(NAME, file, project).get(enumName)
        }
    }
    
    override fun getName(): ID<String, Map<String, ParadoxComplexEnumValueInfo>> {
        return NAME
    }
    
    override fun getVersion(): Int {
        return VERSION
    }
    
    override fun getIndexer(): DataIndexer<String, Map<String, ParadoxComplexEnumValueInfo>, FileContent> {
        return DataIndexer { inputData ->
            val file = inputData.psiFile
            buildMap {
                file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if(element is ParadoxScriptStringExpressionElement) {
                            val info = ParadoxComplexEnumValueHandler.getInfo(element)
                            if(info != null) {
                                val map = getOrPut(info.enumName) { mutableMapOf() } as MutableMap
                                map.put(info.name, info)
                            }
                        }
                        if(element.isExpressionOrMemberContext()) super.visitElement(element)
                    }
                })
            }
        }
    }
    
    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
    
    override fun getValueExternalizer(): DataExternalizer<Map<String, ParadoxComplexEnumValueInfo>> {
        return object :DataExternalizer<Map<String, ParadoxComplexEnumValueInfo>> {
            override fun save(storage: DataOutput, value: Map<String, ParadoxComplexEnumValueInfo>) {
                DataInputOutputUtil.writeSeq(storage, value.values) {
                    IOUtil.writeUTF(storage, it.name)
                    IOUtil.writeUTF(storage, it.enumName)
                    storage.writeByte(it.readWriteAccess.toByte())
                    storage.writeByte(it.gameType.toByte())
                }
            }
            
            override fun read(storage: DataInput): Map<String, ParadoxComplexEnumValueInfo> {
                return DataInputOutputUtil.readSeq(storage) {
                    val name = IOUtil.readUTF(storage)
                    val enumName = IOUtil.readUTF(storage)
                    val readWriteAccess = storage.readByte().toReadWriteAccess()
                    val gameType = storage.readByte().toGameType()
                    ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, gameType)
                }.associateBy { it.name }
            }
            
            private fun ReadWriteAccessDetector.Access.toByte() = this.ordinal
            
            private fun Byte.toReadWriteAccess() = when {
                this == 0.toByte() -> ReadWriteAccessDetector.Access.Read
                this == 1.toByte() -> ReadWriteAccessDetector.Access.Write
                else -> ReadWriteAccessDetector.Access.ReadWrite
            }
            
            private fun ParadoxGameType.toByte() = this.ordinal
            
            private fun Byte.toGameType() = ParadoxGameType.values[this.toInt()]
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { it.fileInfo != null && !ParadoxFileManager.isLightFile(it) && it.fileType == ParadoxScriptFileType }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
}