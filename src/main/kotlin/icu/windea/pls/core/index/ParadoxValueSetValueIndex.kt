package icu.windea.pls.core.index

import com.intellij.codeInsight.highlighting.*
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
        val valueSetValueList: MutableList<ParadoxValueSetValueInfo> = mutableListOf()
    ) {
        val valueSetValues = buildMap<String, Map<String, ParadoxValueSetValueInfo>> {
            for(info in valueSetValueList) {
                val map = getOrPut(info.valueSetName) { mutableMapOf() } as MutableMap
                map.putIfAbsent(info.name, info)
            }
        }
    }
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.valueSetValueList) {
                IOUtil.writeUTF(storage, it.name)
                IOUtil.writeUTF(storage, it.valueSetName)
                storage.writeByte(it.readWriteAccess.toByte())
                storage.writeByte(it.gameType.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val valueSetValueInfos = DataInputOutputUtil.readSeq(storage) {
                val name = IOUtil.readUTF(storage)
                val valueSetName = IOUtil.readUTF(storage)
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                val gameType = storage.readByte().toGameType()
                ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, gameType)
            }
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
    
    private const val id = "paradox.valueSetValue.index"
    private const val version = 1 //0.9.6
    
    private val gist: PsiFileGist<Data> = GistManager.getInstance().newPsiFileGist(id, version, valueExternalizer) builder@{ file ->
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
    
    fun getData(file: PsiFile): Data {
        return gist.getFileData(file)
    }
}