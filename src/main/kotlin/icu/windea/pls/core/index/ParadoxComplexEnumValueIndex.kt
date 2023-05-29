package icu.windea.pls.core.index

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
import java.io.*

class ParadoxComplexEnumValueIndex : FileBasedIndexExtension<String, List<ParadoxComplexEnumValueInfo>>() { 
    companion object {
        @JvmField val NAME = ID.create<String, List<ParadoxComplexEnumValueInfo>>("paradox.complexEnumValue.index")
        private const val VERSION = 26 //1.0.4

        fun getData(file: VirtualFile, project: Project): Map<String, List<ParadoxComplexEnumValueInfo>> {
            return FileBasedIndex.getInstance().getFileData(NAME, file, project)
        }
    }

    override fun getName() = NAME

    override fun getVersion() = VERSION

    override fun getIndexer(): DataIndexer<String, List<ParadoxComplexEnumValueInfo>, FileContent> {
        return DataIndexer { inputData ->
            val file = inputData.psiFile
            buildMap {
                file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if(element is ParadoxScriptStringExpressionElement) {
                            val info = ParadoxComplexEnumValueHandler.getInfo(element)
                            if(info != null) {
                                val list = getOrPut(info.enumName) { mutableListOf() } as MutableList
                                list.add(info)
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

    override fun getValueExternalizer(): DataExternalizer<List<ParadoxComplexEnumValueInfo>> {
        return object : DataExternalizer<List<ParadoxComplexEnumValueInfo>> {
            override fun save(storage: DataOutput, value: List<ParadoxComplexEnumValueInfo>) {
                storage.writeInt(value.size)
                value.forEach { valueSetValueInfo ->
                    IOUtil.writeUTF(storage, valueSetValueInfo.name)
                    IOUtil.writeUTF(storage, valueSetValueInfo.enumName)
                    storage.writeByte(valueSetValueInfo.readWriteAccess.toByte())
                    storage.writeInt(valueSetValueInfo.elementOffset)
                    storage.writeByte(valueSetValueInfo.gameType.toByte())
                }
            }
            
            override fun read(storage: DataInput): List<ParadoxComplexEnumValueInfo> {
                val size = storage.readInt()
                return MutableList(size) {
                    val name = IOUtil.readUTF(storage)
                    val enumName = IOUtil.readUTF(storage)
                    val readWriteAccess = storage.readByte().toReadWriteAccess()
                    val elementOffset = storage.readInt()
                    val gameType = storage.readByte().toGameType()
                    ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, elementOffset, gameType)
                }
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter p@{ file ->
            val fileType = file.fileType
            if(fileType != ParadoxScriptFileType) return@p false
            if(file.fileInfo == null) return@p false
            true
        }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
}