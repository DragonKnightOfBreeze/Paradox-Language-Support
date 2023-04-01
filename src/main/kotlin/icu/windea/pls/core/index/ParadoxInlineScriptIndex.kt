package icu.windea.pls.core.index

import com.intellij.psi.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.io.*

object ParadoxInlineScriptIndex {
    class Data(
        val inlineScriptList: MutableList<ParadoxInlineScriptInfo> = mutableListOf()
    ) {
        val inlineScripts = buildMap<String, List<ParadoxInlineScriptInfo>> {
            for(info in inlineScriptList) {
                val list = getOrPut(info.expression) { mutableListOf() } as MutableList
                list.add(info)
            }
        }
    }
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.inlineScriptList) {
                IOUtil.writeUTF(storage, it.expression)
                storage.writeInt(it.offset)
                storage.writeByte(it.gameType.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val inlineScriptInfos = DataInputOutputUtil.readSeq(storage) {
                val expression = IOUtil.readUTF(storage)
                val offset = storage.readInt()
                val gameType = storage.readByte().toGameType()
                ParadoxInlineScriptInfo(expression, offset, gameType)
            }
            return Data(inlineScriptInfos)
        }
        
        private fun ParadoxGameType.toByte() = this.ordinal
        
        private fun Byte.toGameType() = ParadoxGameType.values[this.toInt()]
    }
    
    private const val id = "paradox.inlineScript.index"
    private const val version = 1 //0.9.6
    
    private val gist: PsiFileGist<Data> = GistManager.getInstance().newPsiFileGist(id, version, valueExternalizer) builder@{ file ->
        if(file !is ParadoxScriptFile) return@builder Data()
        if(file.fileInfo == null) return@builder Data()
        val data = Data()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty) {
                    ParadoxInlineScriptHandler.getInfo(element)?.let { data.inlineScriptList.add(it) }
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