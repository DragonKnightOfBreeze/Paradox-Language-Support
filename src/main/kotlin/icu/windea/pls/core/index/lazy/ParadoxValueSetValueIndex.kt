package icu.windea.pls.core.index.lazy

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.concurrency.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

//这个索引的索引速度可能非常慢，考虑并发索引
//这个索引不会保存同一文件中重复的ParadoxValueSetValueInfo
//这个索引不会保存ParadoxValueSetValueInfo.elementOffset

/**
 * 用于索引值集值。
 */
object ParadoxValueSetValueIndex {
    private const val ID = "paradox.valueSetValue.index"
    private const val VERSION = 27 //1.0.5
    
    val executor by lazy { AppExecutorUtil.createBoundedApplicationPoolExecutor("ParadoxValueSetValueIndex Pool", 4) }
    
    fun getFileData(file: VirtualFile, project: Project): Data {
        return gist.getFileData(project, file)
    }
    
    class Data(
        val valueSetValueInfoList: MutableList<ParadoxValueSetValueInfo> = mutableListOf()
    ) {
        val valueSetValueInfoGroup by lazy {
            val group = mutableMapOf<String, List<ParadoxValueSetValueInfo>>()
            valueSetValueInfoList.forEachFast { info ->
                val list = group.getOrPut(info.valueSetName) { mutableListOf() } as MutableList
                list.add(info)
            }
            group
        }
    }
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            storage.writeList(value.valueSetValueInfoList) { complexEnumValueInfo ->
                storage.writeString(complexEnumValueInfo.name)
                storage.writeString( complexEnumValueInfo.valueSetName)
                storage.writeByte(complexEnumValueInfo.readWriteAccess.toByte())
                storage.writeInt(complexEnumValueInfo.elementOffset)
                storage.writeByte(complexEnumValueInfo.gameType.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val complexEnumValueInfoList = storage.readList {
                val name = storage.readString()
                val valueSetName = storage.readString()
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                val elementOffset = storage.readInt()
                val gameType = storage.readByte().toGameType()
                ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
            }
            if(complexEnumValueInfoList.isEmpty()) return EmptyData
            return Data(complexEnumValueInfoList)
        }
    }
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        val fileType = file.fileType
        if(fileType != ParadoxScriptFileType && fileType != ParadoxLocalisationFileType) return@builder EmptyData
        if(!matchesPath(file)) return@builder EmptyData
        val psiFile = file.toPsiFile(project) ?: return@builder EmptyData
        val data = Data()
        if(fileType == ParadoxScriptFileType) {
            psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() { //perf: 95%
                override fun visitElement(element: PsiElement) {
                    if(element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                        val infos = ParadoxValueSetValueHandler.getInfos(element)
                        data.valueSetValueInfoList.addInfos(infos)
                    }
                    if(element.isExpressionOrMemberContext()) super.visitElement(element)
                }
            })
        } else {
            psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() { //perf: 5%
                override fun visitElement(element: PsiElement) {
                    if(element is ParadoxLocalisationCommandIdentifier) {
                        val infos = ParadoxValueSetValueHandler.getInfos(element)
                        data.valueSetValueInfoList.addInfos(infos)
                    }
                    if(element.isRichTextContext()) super.visitElement(element)
                }
            })
        }
        data
    }
    
    private fun MutableList<ParadoxValueSetValueInfo>.addInfos(infos: List<ParadoxValueSetValueInfo>) {
        addAll(infos)
    }
    
    private fun matchesPath(file: VirtualFile): Boolean {
        return file.fileInfo != null
    }
}