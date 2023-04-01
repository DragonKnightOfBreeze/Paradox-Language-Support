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

object ParadoxComplexEnumValueIndex {
    class Data(
        val complexEnumValueList: MutableList<ParadoxComplexEnumValueInfo> = mutableListOf()
    ) {
        val complexEnumValues by lazy {
            buildMap<String, Map<String, ParadoxComplexEnumValueInfo>> {
                for(info in complexEnumValueList) {
                    val map = getOrPut(info.enumName) { mutableMapOf() } as MutableMap
                    map.putIfAbsent(info.name, info)
                }
            }
        }
    }
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.complexEnumValueList) {
                IOUtil.writeUTF(storage, it.name)
                IOUtil.writeUTF(storage, it.enumName)
                storage.writeByte(it.readWriteAccess.toByte())
                storage.writeByte(it.gameType.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val complexEnumValueInfos = DataInputOutputUtil.readSeq(storage) {
                val name = IOUtil.readUTF(storage)
                val enumName = IOUtil.readUTF(storage)
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                val gameType = storage.readByte().toGameType()
                ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, gameType)
            }
            return Data(complexEnumValueInfos)
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
    
    private const val id = "paradox.complexEnumValue.index"
    private const val version = 1 //0.9.6
    
    private val gist: PsiFileGist<Data> = GistManager.getInstance().newPsiFileGist(id, version, valueExternalizer) builder@{ file ->
        ProgressManager.checkCanceled()
        if(file !is ParadoxScriptFile) return@builder Data()
        if(!matchesPath(file)) return@builder Data()
        val data = Data()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) {
                    ParadoxComplexEnumValueHandler.getInfo(element)?.let { data.complexEnumValueList.add(it) }
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
        })
        data
    }
    
    private fun matchesPath(file: PsiFile): Boolean {
        val project = file.project
        val fileInfo = file.fileInfo ?: return false
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.entryPath //这里使用entryPath
        val configs = getCwtConfig(project).getValue(gameType).complexEnums
        for(config in configs.values) {
            if(ParadoxComplexEnumValueHandler.matchesComplexEnumByPath(config, path)) return true
        }
        return false
    }
    
    fun getData(enumName: String, file: PsiFile): Map<String, ParadoxComplexEnumValueInfo>? {
        return gist.getFileData(file).complexEnumValues[enumName]
    }
}

//class ParadoxComplexEnumValueIndex : FileBasedIndexExtension<String, Map<String, ParadoxComplexEnumValueInfo>>() { 
//    companion object {
//        @JvmField val NAME = ID.create<String, Map<String, ParadoxComplexEnumValueInfo>>("paradox.complexEnumValue.index")
//        private const val VERSION = 1
//        
//        fun getData(enumName: String, file: VirtualFile, project: Project): Map<String, ParadoxComplexEnumValueInfo>? {
//            return FileBasedIndex.getInstance().getFileData(NAME, file, project).get(enumName)
//        }
//    }
//    
//    override fun getName(): ID<String, Map<String, ParadoxComplexEnumValueInfo>> {
//        return NAME
//    }
//    
//    override fun getVersion(): Int {
//        return VERSION
//    }
//    
//    override fun getIndexer(): DataIndexer<String, Map<String, ParadoxComplexEnumValueInfo>, FileContent> {
//        return DataIndexer { inputData ->
//            val file = inputData.psiFile
//            buildMap {
//                file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
//                    override fun visitElement(element: PsiElement) {
//                        if(element is ParadoxScriptStringExpressionElement) {
//                            val info = ParadoxComplexEnumValueHandler.getInfo(element)
//                            if(info != null) {
//                                val map = getOrPut(info.enumName) { mutableMapOf() } as MutableMap
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
//    override fun getValueExternalizer(): DataExternalizer<Map<String, ParadoxComplexEnumValueInfo>> {
//        return object :DataExternalizer<Map<String, ParadoxComplexEnumValueInfo>> {
//            override fun save(storage: DataOutput, value: Map<String, ParadoxComplexEnumValueInfo>) {
//                DataInputOutputUtil.writeSeq(storage, value.values) {
//                    IOUtil.writeUTF(storage, it.name)
//                    IOUtil.writeUTF(storage, it.enumName)
//                    storage.writeByte(it.readWriteAccess.toByte())
//                    storage.writeByte(it.gameType.toByte())
//                }
//            }
//            
//            override fun read(storage: DataInput): Map<String, ParadoxComplexEnumValueInfo> {
//                return DataInputOutputUtil.readSeq(storage) {
//                    val name = IOUtil.readUTF(storage)
//                    val enumName = IOUtil.readUTF(storage)
//                    val readWriteAccess = storage.readByte().toReadWriteAccess()
//                    val gameType = storage.readByte().toGameType()
//                    ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, gameType)
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
//        return FileBasedIndex.InputFilter { it.fileInfo != null && !ParadoxFileManager.isLightFile(it) && it.fileType == ParadoxScriptFileType }
//    }
//    
//    override fun dependsOnFileContent(): Boolean {
//        return true
//    }
//}