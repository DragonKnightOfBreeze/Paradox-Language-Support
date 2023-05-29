package icu.windea.pls.core.index

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
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

//这里不能使用PsiFileGist，否则可能会出现应当可以解析但有时无法解析的情况
//包含仅在本地化文件中使用到的event_target和variable

@Deprecated("Use ParadoxValueSetValueFastIndex")
object ParadoxValueSetValueLazyIndex {
    private const val ID = "paradox.valueSetValue.index"
    private const val VERSION = 25 //1.0.2
    
    class Data(
        val valueSetValueInfoList: MutableList<ParadoxValueSetValueInfo> = mutableListOf()
    ) {
        val valueSetValueInfoGroup by lazy {
            buildMap<String, Map<String, List<ParadoxValueSetValueInfo>>> {
                valueSetValueInfoList.forEachFast { info ->
                    val map = getOrPut(info.valueSetName) { mutableMapOf() } as MutableMap
                    val list = map.getOrPut(info.name) { mutableListOf() } as MutableList
                    list.add(info)
                }
            }
        }
        val distinctValueSetValueInfoGroup by lazy {
            valueSetValueInfoGroup.mapValues { (_, v1) -> v1.mapValues { (_, v2) -> v2.distinctBy { it.readWriteAccess } } }
        }
    }
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.valueSetValueInfoList) {
                IOUtil.writeUTF(storage, it.name)
                IOUtil.writeUTF(storage, it.valueSetName)
                storage.writeByte(it.readWriteAccess.toByte())
                storage.writeInt(it.elementOffset)
                storage.writeByte(it.gameType.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val valueSetValueInfos = DataInputOutputUtil.readSeq(storage) {
                val name = IOUtil.readUTF(storage)
                val valueSetName = IOUtil.readUTF(storage)
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                val elementOffset = storage.readInt()
                val gameType = storage.readByte().toGameType()
                ParadoxValueSetValueInfo(name, valueSetName, readWriteAccess, elementOffset, gameType)
            }
            if(valueSetValueInfos.isEmpty()) return EmptyData
            return Data(valueSetValueInfos)
        }
    }
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileInfo == null) return@builder EmptyData
        if(file.fileType != ParadoxScriptFileType && file.fileType != ParadoxLocalisationFileType) return@builder EmptyData
        val psiFile = file.toPsiFile(project) ?: return@builder EmptyData
        val data = Data()
        if(file.fileType == ParadoxScriptFileType) {
            psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if(element is ParadoxScriptStringExpressionElement) {
                        element.references.forEachFast { reference ->
                            if(reference.canResolveValueSetValue()) {
                                val resolved = reference?.resolve()
                                if(resolved is ParadoxValueSetValueElement) handleValueSetValueElement(data, resolved)
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
                                if(resolved is ParadoxValueSetValueElement) handleValueSetValueElement(data, resolved)
                            }
                        }
                    }
                    if(element.isRichTextContext()) super.visitElement(element)
                }
            })
        }
        data
    }
    
    private fun handleValueSetValueElement(data: Data, element: ParadoxValueSetValueElement) {
        element.valueSetNames.forEach { valueSetName ->
            val valueSetValueInfo = ParadoxValueSetValueInfo(element.name, valueSetName, element.readWriteAccess, element.startOffset, element.gameType)
            data.valueSetValueInfoList.add(valueSetValueInfo)
        }
    }
    
    fun getData(file: VirtualFile, project: Project): Data {
        return gist.getFileData(project, file)
    }
}

