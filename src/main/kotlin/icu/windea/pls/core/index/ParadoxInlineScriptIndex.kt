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
import icu.windea.pls.tool.*
import java.io.*

class ParadoxInlineScriptIndex : FileBasedIndexExtension<String, List<ParadoxInlineScriptInfo>>() {
    companion object {
        @JvmField val NAME = ID.create<String, List<ParadoxInlineScriptInfo>>("paradox.inlineScript.index")
        private const val VERSION = 19 //0.9.15
        
        fun getData(expression: String, file: VirtualFile, project: Project): List<ParadoxInlineScriptInfo>? {
            return FileBasedIndex.getInstance().getFileData(NAME, file, project).get(expression)
        }
    }
    
    override fun getName(): ID<String, List<ParadoxInlineScriptInfo>> {
        return NAME
    }
    
    override fun getVersion(): Int {
        return VERSION
    }
    
    override fun getIndexer(): DataIndexer<String, List<ParadoxInlineScriptInfo>, FileContent> {
        return DataIndexer { inputData ->
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
                DataInputOutputUtil.writeSeq(storage, value) {
                    IOUtil.writeUTF(storage, it.expression)
                    storage.writeInt(it.elementOffset)
                    storage.writeByte(it.gameType.toByte())
                }
            }
            
            override fun read(storage: DataInput): List<ParadoxInlineScriptInfo> {
                return DataInputOutputUtil.readSeq(storage) {
                    val expression = IOUtil.readUTF(storage)
                    val elementOffset = storage.readInt()
                    val gameType = storage.readByte().toGameType()
                    ParadoxInlineScriptInfo(expression, elementOffset, gameType)
                }
            }
            
            private fun ParadoxGameType.toByte() = this.ordinal
            
            private fun Byte.toGameType() = ParadoxGameType.values[this.toInt()]
        }
    }
    
    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter filter@{ file ->
            if(file.fileType != ParadoxScriptFileType) return@filter false
            if(ParadoxFileManager.isLightFile(file)) return@filter false //不索引内存中的文件
            if(file.fileInfo == null) return@filter false
            true
        }
    }
    
    override fun dependsOnFileContent(): Boolean {
        return true
    }
}