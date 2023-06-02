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
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

/**
 * 用于索引复杂枚举值。
 */
object ParadoxComplexEnumValueIndex {
    private const val ID = "paradox.complexEnumValue.index"
    private const val VERSION = 27 //1.0.5
    
    val executor by lazy { AppExecutorUtil.createBoundedApplicationPoolExecutor("ParadoxComplexEnumValueIndex Pool", PlsConstants.lazyIndexThreadPoolSize) }
    
    fun getFileData(file: VirtualFile, project: Project): Data {
        return gist.getFileData(project, file)
    }
    
    class Data(
        val complexEnumValueInfoList: MutableList<ParadoxComplexEnumValueInfo> = mutableListOf()
    ) {
        val complexEnumValueInfoGroup by lazy {
            val group = mutableMapOf<String, List<ParadoxComplexEnumValueInfo>>()
            complexEnumValueInfoList.forEachFast { info ->
                val list = group.getOrPut(info.enumName) { mutableListOf() } as MutableList
                list.add(info)
            }
            group
        }
    }
    
    private val EmptyData = Data()
    
    private val valueExternalizer: DataExternalizer<Data> = object : DataExternalizer<Data> {
        override fun save(storage: DataOutput, value: Data) {
            storage.writeList(value.complexEnumValueInfoList) { complexEnumValueInfo ->
                storage.writeString(complexEnumValueInfo.name)
                storage.writeString( complexEnumValueInfo.enumName)
                storage.writeByte(complexEnumValueInfo.readWriteAccess.toByte())
                storage.writeInt(complexEnumValueInfo.elementOffset)
                storage.writeByte(complexEnumValueInfo.gameType.toByte())
            }
        }
        
        override fun read(storage: DataInput): Data {
            val complexEnumValueInfoList = storage.readList {
                val name = storage.readString()
                val enumName = storage.readString()
                val readWriteAccess = storage.readByte().toReadWriteAccess()
                val elementOffset = storage.readInt()
                val gameType = storage.readByte().toGameType()
                ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, elementOffset, gameType)
            }
            if(complexEnumValueInfoList.isEmpty()) return EmptyData
            return Data(complexEnumValueInfoList)
        }
    }
    
    private val gist: VirtualFileGist<Data> = GistManager.getInstance().newVirtualFileGist(ID, VERSION, valueExternalizer) builder@{ project, file ->
        if(file.fileType != ParadoxScriptFileType) return@builder EmptyData
        if(!matchesPath(file, project)) return@builder EmptyData
        val psiFile = file.toPsiFile(project) ?: return@builder EmptyData
        val data = Data()
        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) {
                    val info = ParadoxComplexEnumValueHandler.getInfo(element)
                    if(info != null) data.complexEnumValueInfoList.add(info)
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
}

