package icu.windea.pls.core.index

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

object ParadoxValueSetValueIndex {
    class Data(
        val marker: Boolean = true,
        val valueSetValueList: MutableList<ParadoxValueSetValueInfo> = mutableListOf()
    ) {
        val valueSetValues by lazy {
            buildMap<String, Map<String, ParadoxValueSetValueInfo>> {
                for(info in valueSetValueList) {
                    val map = getOrPut(info.valueSetName) { mutableMapOf() } as MutableMap
                    map.putIfAbsent(info.name, info)
                }
            }
        }
    }
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            storage.writeBoolean(value.marker)
            DataInputOutputUtil.writeSeq(storage, value.valueSetValueList) {
                IOUtil.writeUTF(storage, it.name)
                IOUtil.writeUTF(storage, it.valueSetName)
                storage.writeByte(it.readWriteAccess.toByte())
                storage.writeByte(it.gameType.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val marker = storage.readBoolean()
            val valueSetValueInfos = DataInputOutputUtil.readSeq(storage) {
                val name = IOUtil.readUTF(storage)
                val valueSetName = IOUtil.readUTF(storage)
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                val gameType = storage.readByte().toGameType()
                ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, gameType)
            }
            return Data(marker, valueSetValueInfos)
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
    
    private const val id = "paradox.valueSetValue.index"
    private const val version = 2 //0.9.7
    
    private val gist: PsiFileGist<Data> = GistManager.getInstance().newPsiFileGist(id, version, valueExternalizer) builder@{ file ->
        ProgressManager.checkCanceled()
        if(file !is ParadoxScriptFile) return@builder Data()
        if(file.fileInfo == null) return@builder Data()
        val data = Data()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) {
                    ParadoxValueSetValueHandler.getInfo(element)?.let { data.valueSetValueList.add(it) }
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
        })
        data
    }
    
    fun getData(valueSetName: String, file: PsiFile): Map<String, ParadoxValueSetValueInfo>? {
        return gist.getFileData(file).valueSetValues[valueSetName]
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