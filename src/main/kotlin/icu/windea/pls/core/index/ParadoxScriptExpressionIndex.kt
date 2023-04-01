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
        val valueSetValueList: List<ParadoxValueSetValueInfo> = listOf(),
        val complexEnumValueList: List<ParadoxComplexEnumValueInfo> = listOf(),
        val inlineScriptList: List<ParadoxInlineScriptInfo> = listOf(),
    ) {
        var file: PsiFile? = null
        
        val valueSetValues = buildMap<String, Map<String, ParadoxValueSetValueInfo>> {
            for(info in valueSetValueList) {
                val map = getOrPut(info.valueSetName) { mutableMapOf() } as MutableMap
                map.putIfAbsent(info.name, info)
            }
        }
        val complexEnumValues = buildMap<String, Map<String, ParadoxComplexEnumValueInfo>> {
            for(info in complexEnumValueList) {
                val map = getOrPut(info.enumName) { mutableMapOf() } as MutableMap
                map.putIfAbsent(info.name, info)
            }
        }
        val inlineScripts = buildMap<String, List<ParadoxInlineScriptInfo>> {
            for(info in inlineScriptList) {
                val list = getOrPut(info.expression) { mutableListOf() } as MutableList
                list.add(info)
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
            DataInputOutputUtil.writeSeq(storage, value.complexEnumValueList) {
                IOUtil.writeUTF(storage, it.name)
                IOUtil.writeUTF(storage, it.enumName)
                storage.writeByte(it.readWriteAccess.toByte())
                storage.writeByte(it.gameType.toByte())
            }
            DataInputOutputUtil.writeSeq(storage, value.inlineScriptList) {
                IOUtil.writeUTF(storage, it.expression)
                storage.writeInt(it.offset)
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
            val complexEnumValueInfos = DataInputOutputUtil.readSeq(storage) {
                val name = IOUtil.readUTF(storage)
                val enumName = IOUtil.readUTF(storage)
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                val gameType = storage.readByte().toGameType()
                ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, gameType)
            }
            val inlineScriptInfos = DataInputOutputUtil.readSeq(storage) {
                val expression = IOUtil.readUTF(storage)
                val offset = storage.readInt()
                val gameType = storage.readByte().toGameType()
                ParadoxInlineScriptInfo(expression, offset, gameType)
            }
            return Data(valueSetValueInfos, complexEnumValueInfos, inlineScriptInfos)
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
    
    private const val id = "paradox.script.expression.index"
    private const val version = 3 //0.9.6
    
    private val gist: PsiFileGist<Data> = GistManager.getInstance().newPsiFileGist(id, version, valueExternalizer) builder@{ file ->
        if(file !is ParadoxScriptFile) return@builder Data()
        if(file.fileInfo == null) return@builder Data()
        val valueSetValueInfos = mutableListOf<ParadoxValueSetValueInfo>()
        val complexEnumValueInfos = mutableListOf<ParadoxComplexEnumValueInfo>()
        val inlineScriptInfos = mutableListOf<ParadoxInlineScriptInfo>()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty) visitProperty(element)
                if(element is ParadoxScriptExpressionElement) visitExpression(element)
                if(element is ParadoxScriptStringExpressionElement) visitStringExpression(element)
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun visitProperty(element: ParadoxScriptProperty) {
                ParadoxInlineScriptHandler.getInfo(element)?.let { inlineScriptInfos.add(it) }
            }
            
            private fun visitExpression(element: ParadoxScriptExpressionElement) {
                
            }
            
            private fun visitStringExpression(element: ParadoxScriptStringExpressionElement) {
                ParadoxValueSetValueHandler.getInfo(element)?.let { valueSetValueInfos.add(it) }
                ParadoxComplexEnumValueHandler.getInfo(element)?.let { complexEnumValueInfos.add(it) }
            }
        })
        Data(valueSetValueInfos, complexEnumValueInfos)
    }
    
    fun getData(file: PsiFile): Data {
        val data = gist.getFileData(file)
        if(data.file == null) data.file = file
        return data
    }
}