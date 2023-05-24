package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import com.intellij.util.gist.*
import com.intellij.util.io.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

//这个索引相比ParadoxValueSetValueIndex应当拥有更好的性能
//目前这个索引仅用于进行代码提示和代码检查，因此需要尽可能地仅收集必要的信息

object ParadoxValueSetValueFastIndex {
    private const val ID = "paradox.valueSetValue.fast.index"
    private const val VERSION = 25 //1.0.2
    
    class Data(
        val valueSetValueInfoGroup: MutableMap<String, MutableList<ParadoxValueSetValueInfo>> = mutableMapOf()
    )
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            storage.writeInt(value.valueSetValueInfoGroup.size)
            value.valueSetValueInfoGroup.forEach { (valueSetName, valueSetValueInfoList) -> 
                IOUtil.writeUTF(storage, valueSetName)
                storage.writeInt(valueSetValueInfoList.size)
                valueSetValueInfoList.forEachFast { valueSetValueInfo ->
                    IOUtil.writeUTF(storage, valueSetValueInfo.name)
                    storage.writeByte(valueSetValueInfo.readWriteAccess.toByte())
                    storage.writeInt(valueSetValueInfo.elementOffset)
                    storage.writeByte(valueSetValueInfo.gameType.toByte())
                }
            }
        }
        
        override fun read(storage: DataInput): Data {
            val valueSetValueInfoGroupSize = storage.readInt()
            val data = Data()
            repeat(valueSetValueInfoGroupSize) {
                val valueSetName = IOUtil.readUTF(storage)
                val valueSetValueInfoListSize = storage.readInt()
                val valueSetValueInfoList = SmartList<ParadoxValueSetValueInfo>()
                data.valueSetValueInfoGroup[valueSetName] = valueSetValueInfoList
                repeat(valueSetValueInfoListSize) {
                    val valueSetValueInfo = run {
                        val name = IOUtil.readUTF(storage)
                        val readWriteAccess = storage.readByte().toReadWriteAccess()
                        val elementOffset = storage.readInt()
                        val gameType = storage.readByte().toGameType()
                        ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
                    }
                    valueSetValueInfoList += valueSetValueInfo
                }
            }
            return data
        }
    }
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileInfo == null) return@builder EmptyData
        if(file.fileType != ParadoxScriptFileType && file.fileType != ParadoxLocalisationFileType) return@builder EmptyData
        val psiFile = file.toPsiFile(project) ?: return@builder EmptyData
        val data = Data()
        val keys = mutableSetOf<String>()
        if(file.fileType == ParadoxScriptFileType) {
            psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if(element is ParadoxScriptStringExpressionElement) {
                        element.references.forEachFast { reference ->
                            if(reference.canResolveValueSetValue()) {
                                val resolved = reference?.resolve()
                                if(resolved is ParadoxValueSetValueElement) {
                                    val key = getKeyToDistinct(resolved)
                                    if(keys.add(key)) handleValueSetValueElement(data, resolved)
                                }
                            }
                        }
                    }
                    if(element.isExpressionOrMemberContext()) super.visitElement(element)
                }
            })
        } else {
            psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if(element is ParadoxLocalisationCommandIdentifier) {
                        element.references.forEachFast { reference ->
                            if(reference.canResolveValueSetValue()) {
                                val resolved = reference?.resolve()
                                if(resolved is ParadoxValueSetValueElement) {
                                    val key = getKeyToDistinct(resolved)
                                    if(keys.add(key)) handleValueSetValueElement(data, resolved)
                                }
                            }
                        }
                    }
                    if(element.isRichTextContext()) super.visitElement(element)
                }
            })
        }
        data
    }
    
    private fun getKeyToDistinct(element: ParadoxValueSetValueElement): String {
        return element.valueSetNames.joinToString(",") + "@" + element.name + "@" + element.readWriteAccess.ordinal
    }
    
    private fun handleValueSetValueElement(data: Data, element: ParadoxValueSetValueElement) {
        element.valueSetNames.forEach { valueSetName ->
            val valueSetValueInfoList = data.valueSetValueInfoGroup.getOrPut(valueSetName) { SmartList() }
            val valueSetValueInfo = ParadoxValueSetValueInfo(element.name, valueSetName, element.readWriteAccess, -1, element.gameType)
            valueSetValueInfoList.add(valueSetValueInfo)
        }
    }
    
    fun getData(file: VirtualFile, project: Project): Data {
        return gist.getFileData(project, file)
    }
}