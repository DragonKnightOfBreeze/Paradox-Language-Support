package icu.windea.pls.core.index

import com.intellij.psi.*
import com.intellij.util.indexing.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

class ParadoxInlineScriptIndex : FileBasedIndexExtension<String, List<ParadoxInlineScriptInfo>>() {
    companion object {
        @JvmField val NAME = ID.create<String, List<ParadoxInlineScriptInfo>>("paradox.inlineScript.index")
        private const val VERSION = 27 //1.0.5
    }
    
    override fun getName() = NAME
    
    override fun getVersion() = VERSION
    
    override fun getIndexer(): DataIndexer<String, List<ParadoxInlineScriptInfo>, FileContent> {
        return DataIndexer { inputData -> //perf: 20000ms for indexing
            val file = inputData.psiFile
            buildMap {
                file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if(element is ParadoxScriptProperty) {
                            val info = ParadoxInlineScriptHandler.getInfo(element)
                            if(info != null) {
                                val list = getOrPut(info.expression) { mutableListOf() } as MutableList
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
    
    override fun getValueExternalizer(): DataExternalizer<List<ParadoxInlineScriptInfo>> {
        return object : DataExternalizer<List<ParadoxInlineScriptInfo>> {
            override fun save(storage: DataOutput, value: List<ParadoxInlineScriptInfo>) {
                storage.writeInt(value.size)
                value.forEachFast { info ->
                    storage.writeUTFFast(info.expression)
                    storage.writeInt(info.elementOffset)
                    storage.writeByte(info.gameType.toByte())
                }
            }
            
            override fun read(storage: DataInput): List<ParadoxInlineScriptInfo> {
                return MutableList(storage.readInt()) {
                    val expression = storage.readUTFFast()
                    val elementOffset = storage.readInt()
                    val gameType = storage.readByte().toGameType()
                    ParadoxInlineScriptInfo(expression, elementOffset, gameType)
                }
            }
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter filter@{ file ->
            if(file.fileInfo == null) return@filter false
            if(file.fileType != ParadoxScriptFileType) return@filter false
            true
        }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
}