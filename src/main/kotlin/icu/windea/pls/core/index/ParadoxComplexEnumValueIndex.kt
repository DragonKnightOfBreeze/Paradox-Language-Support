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

object ParadoxComplexEnumValueIndex {
    private const val ID = "paradox.complexEnumValue.index"
    private const val VERSION = 22 //1.0.0
    
    class Data(
        val complexEnumValueList: MutableList<ParadoxComplexEnumValueInfo> = SmartList()
    ) {
        val complexEnumValueGroup by lazy {
            if(complexEnumValueList.isEmpty()) return@lazy emptyMap()
            buildMap<String, Map<String, List<ParadoxComplexEnumValueInfo>>> {
                complexEnumValueList.forEachFast { info ->
                    val map = getOrPut(info.enumName) { mutableMapOf() } as MutableMap
                    val list = map.getOrPut(info.name) { SmartList() } as MutableList
                    list.add(info)
                }
            }
        }
    }
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.complexEnumValueList) {
                IOUtil.writeUTF(storage, it.name)
                IOUtil.writeUTF(storage, it.enumName)
                storage.writeByte(it.readWriteAccess.toByte())
                storage.writeInt(it.elementOffset)
                storage.writeByte(it.gameType.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val complexEnumValueInfos = DataInputOutputUtil.readSeq(storage) {
                val name = IOUtil.readUTF(storage)
                val enumName = IOUtil.readUTF(storage)
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                val elementOffset = storage.readInt()
                val gameType = storage.readByte().toGameType()
                ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, elementOffset, gameType)
            }
            if(complexEnumValueInfos.isEmpty()) return EmptyData
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
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileInfo == null) return@builder EmptyData
        if(!matchesPath(file, project)) return@builder EmptyData
        val psiFile = file.toPsiFile(project) ?: return@builder EmptyData
        if(psiFile !is ParadoxScriptFile) return@builder EmptyData
        val data = Data()
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) {
                    ParadoxComplexEnumValueHandler.getInfo(element)?.let { data.complexEnumValueList.add(it) }
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
        })
        data
    }
    
    private fun matchesPath(file: VirtualFile, project: Project): Boolean {
        val fileInfo = file.fileInfo ?: return false
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.pathToEntry //这里使用pathToEntry
        val configs = getCwtConfig(project).get(gameType).complexEnums
        configs.values.forEach { config ->
            if(ParadoxComplexEnumValueHandler.matchesComplexEnumByPath(config, path)) return true
        }
        return false
    }
    
    fun getData(enumName: String, file: VirtualFile, project: Project): Map<String, List<ParadoxComplexEnumValueInfo>>? {
        val fileData = gist.getFileData(project, file)
        return fileData.complexEnumValueGroup[enumName]
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
//        return FileBasedIndex.InputFilter { it.fileType == ParadoxScriptFileType && it.fileInfo != null }
//    }
//    
//    override fun dependsOnFileContent(): Boolean {
//        return true
//    }
//}