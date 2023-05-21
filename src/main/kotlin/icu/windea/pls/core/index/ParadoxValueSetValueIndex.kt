package icu.windea.pls.core.index

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

//这里应当使用Gist，因为可能需要在索引中访问其他索引
//这里不能使用PsiFileGist，否则可能会出现应当可以解析但有时无法解析的情况

//TODO 1.0.0+ 包含仅在本地化文件中使用到的event_target和variable

object ParadoxValueSetValueIndex {
    private const val ID = "paradox.valueSetValue.index"
    private const val VERSION = 22 //1.0.0
    
    class Data(
        val valueSetValueList: MutableList<ParadoxValueSetValueInfo> = SmartList()
    ) {
        val valueSetValueGroup by lazy {
            if(valueSetValueList.isEmpty()) return@lazy emptyMap()
            buildMap<String, Map<String, List<ParadoxValueSetValueInfo>>> {
                valueSetValueList.forEachFast { info ->
                    val map = getOrPut(info.valueSetName) { mutableMapOf() } as MutableMap
                    val list = map.getOrPut(info.name) { SmartList() } as MutableList
                    list.add(info)
                }
            }
        }
    }
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.valueSetValueList) {
                IOUtil.writeUTF(storage, it.name)
                IOUtil.writeUTF(storage, it.valueSetName)
                storage.writeByte(it.readWriteAccess.toByte())
                storage.writeInt(it.elementOffset)
                storage.writeByte(it.gameType.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val valueSetValueInfos = DataInputOutputUtil.readSeq(storage) {
                val name = IOUtil.readUTF(storage)
                val valueSetName = IOUtil.readUTF(storage)
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                val elementOffset = storage.readInt()
                val gameType = storage.readByte().toGameType()
                ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
            }
            if(valueSetValueInfos.isEmpty()) return EmptyData
            return Data(valueSetValueInfos)
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
    
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileInfo == null) return@builder EmptyData
        val psiFile = file.toPsiFile<ParadoxScriptFile>(project) ?: return@builder EmptyData
        val data = Data()
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) {
                    ParadoxValueSetValueHandler.getInfos(element)?.let { data.valueSetValueList.addAll(it) }
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
        })
        data
    }
    
    fun getData(valueSetName: String, file: VirtualFile, project: Project): Map<String, List<ParadoxValueSetValueInfo>>? {
        val fileData = gist.getFileData(project, file)
        return fileData.valueSetValueGroup[valueSetName]
    }
}

//class ParadoxValueSetValueIndex : FileBasedIndexExtension<String, Map<String, ParadoxValueSetValueInfo>>() {
//    companion object {
//        @JvmField val NAME = ID.create<String, Map<String, ParadoxValueSetValueInfo>>("paradox.valueSetValue.index")
//        private const val VERSION = 1
//        
//        fun getData(valueSetName: String, file: VirtualFile, project: Project): Map<String, ParadoxValueSetValueInfo>? {
//            return FileBasedIndex.getInstance().getFileData(NAME, file, project).get(valueSetName)
//        }
//    }
//    
//    override fun getName(): ID<String, Map<String, ParadoxValueSetValueInfo>> {
//        return NAME
//    }
//    
//    override fun getVersion(): Int {
//        return VERSION
//    }
//    
//    override fun getIndexer(): DataIndexer<String, Map<String, ParadoxValueSetValueInfo>, FileContent> {
//        return DataIndexer { inputData ->
//            val file = inputData.psiFile
//            buildMap {
//                file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
//                    override fun visitElement(element: PsiElement) {
//                        if(element is ParadoxScriptStringExpressionElement) {
//                            val info = ParadoxValueSetValueHandler.getInfo(element)
//                            if(info != null) {
//                                val map = getOrPut(info.valueSetName) { mutableMapOf() } as MutableMap
//                                map.put(info.name, info)
//                            }
//                        }
//                        if(element.isExpressionOrMemberContext()) super.visitElement(element)
//                    }
//                })
//            }
//        }
//    }
//    
//    override fun getKeyDescriptor(): KeyDescriptor<String> {
//        return EnumeratorStringDescriptor.INSTANCE
//    }
//    
//    override fun getValueExternalizer(): DataExternalizer<Map<String, ParadoxValueSetValueInfo>> {
//        return object :DataExternalizer<Map<String, ParadoxValueSetValueInfo>> {
//            override fun save(storage: DataOutput, value: Map<String, ParadoxValueSetValueInfo>) {
//                DataInputOutputUtil.writeSeq(storage, value.values) {
//                    IOUtil.writeUTF(storage, it.name)
//                    IOUtil.writeUTF(storage, it.valueSetName)
//                    storage.writeByte(it.readWriteAccess.toByte())
//                    storage.writeByte(it.gameType.toByte())
//                }
//            }
//            
//            override fun read(storage: DataInput): Map<String, ParadoxValueSetValueInfo> {
//                return DataInputOutputUtil.readSeq(storage) {
//                    val name = IOUtil.readUTF(storage)
//                    val enumName = IOUtil.readUTF(storage)
//                    val readWriteAccess = storage.readByte().toReadWriteAccess()
//                    val gameType = storage.readByte().toGameType()
//                    ParadoxValueSetValueInfo(name, enumName, readWriteAccess, gameType)
//                }.associateBy { it.name }
//            }
//            
//            private fun ReadWriteAccessDetector.Access.toByte() = this.ordinal
//            
//            private fun Byte.toReadWriteAccess() = when {
//                this == 0.toByte() -> ReadWriteAccessDetector.Access.Read
//                this == 1.toByte() -> ReadWriteAccessDetector.Access.Write
//                else -> ReadWriteAccessDetector.Access.ReadWrite
//            }
//            
//            private fun ParadoxGameType.toByte() = this.ordinal
//            
//            private fun Byte.toGameType() = ParadoxGameType.values[this.toInt()]
//        }
//    }
//    
//    override fun getInputFilter(): FileBasedIndex.InputFilter {
//        return FileBasedIndex.InputFilter { it.fileType == ParadoxScriptFileType && it.fileInfo != null }
//    }
//    
//    override fun dependsOnFileContent(): Boolean {
//        return true
//    }
//}