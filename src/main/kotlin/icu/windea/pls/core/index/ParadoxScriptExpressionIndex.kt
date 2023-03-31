package icu.windea.pls.core.index

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.diagnostic.*
import com.intellij.psi.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*
import java.lang.invoke.*

object ParadoxScriptExpressionIndex {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    class Data(
        val valueSetValueList: List<ParadoxValueSetValueInfo> = listOf()
    ) {
        var file: PsiFile? = null
        
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
                storage.writeByte(it.gameType.toByte())
                storage.writeByte(it.readWriteAccess.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val valueSetValueInfos = DataInputOutputUtil.readSeq(storage) {
                val name = IOUtil.readUTF(storage)
                val valueSetName = IOUtil.readUTF(storage)
                val gameType = storage.readByte().toGameType()
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                ParadoxValueSetValueInfo(name, valueSetName, gameType, readWriteAccess)
            }
            return Data(valueSetValueInfos)
        }
        
        private fun ParadoxGameType.toByte() = this.ordinal
        
        private fun Byte.toGameType() = ParadoxGameType.values[this.toInt()]
        
        private fun ReadWriteAccessDetector.Access.toByte() = this.ordinal
        
        private fun Byte.toReadWriteAccess() = when {
            this == 0.toByte() -> ReadWriteAccessDetector.Access.Read
            this == 1.toByte() -> ReadWriteAccessDetector.Access.Write
            else -> ReadWriteAccessDetector.Access.ReadWrite
        }
    }
    
    private const val id = "ParadoxScriptExpressionIndexData"
    private const val version = 1 //0.9.6
    
    private val gist: PsiFileGist<Data> = GistManager.getInstance().newPsiFileGist(id, version, valueExternalizer) builder@{ file ->
        if(file !is ParadoxScriptFile) return@builder Data()
        if(file.fileInfo == null) return@builder Data()
        val valueSetValueInfos = mutableListOf<ParadoxValueSetValueInfo>()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptExpressionElement) visitScriptExpression(element)
                if(element is ParadoxScriptStringExpressionElement) visitStringScriptExpression(element)
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun visitScriptExpression(element: ParadoxScriptExpressionElement) {
                
            }
            
            private fun visitStringScriptExpression(element: ParadoxScriptStringExpressionElement) {
                val valueSetValueInfo = ParadoxValueSetValueHandler.getInfo(element)
                if(valueSetValueInfo != null) valueSetValueInfos.add(valueSetValueInfo)
            } 
        })
        Data(valueSetValueInfos)
    }
    
    fun getData(file: PsiFile): Data {
        val data = gist.getFileData(file)
        if(data.file == null) data.file = file
        return data
    }
}