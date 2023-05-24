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
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

//这里应当使用Gist，因为可能需要在索引中访问其他索引
//这里不能使用PsiFileGist，否则可能会出现应当可以解析但有时无法解析的情况

object ParadoxComplexEnumValueIndex {
    private const val ID = "paradox.complexEnumValue.index"
    private const val VERSION = 24 //1.0.2
    
    class Data(
        val complexEnumValueInfoList: MutableList<ParadoxComplexEnumValueInfo> = SmartList()
    ) {
        val complexEnumValueInfoGroup by lazy {
            buildMap<String, Map<String, List<ParadoxComplexEnumValueInfo>>> {
                complexEnumValueInfoList.forEachFast { info ->
                    val map = getOrPut(info.enumName) { mutableMapOf() } as MutableMap
                    val list = map.getOrPut(info.name) { SmartList() } as MutableList
                    list.add(info)
                }
            }
        }
        val distinctComplexEnumValueInfoGroup by lazy {
            complexEnumValueInfoGroup.mapValues { (_, v1) -> v1.mapValues { (_, v2) -> v2.distinctBy { it.readWriteAccess } } }
        }
    }
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            DataInputOutputUtil.writeSeq(storage, value.complexEnumValueInfoList) {
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
    }
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileInfo == null) return@builder EmptyData
        if(file.fileType != ParadoxScriptFileType) return@builder EmptyData
        if(!matchesPath(file, project)) return@builder EmptyData
        val psiFile = file.toPsiFile(project) ?: return@builder EmptyData
        val data = Data()
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) {
                    val info = ParadoxComplexEnumValueHandler.getInfo(element)
                    if(info != null) data.complexEnumValueInfoList += info
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
    
    fun getData(file: VirtualFile, project: Project): Data {
        return gist.getFileData(project, file)
    }
}
