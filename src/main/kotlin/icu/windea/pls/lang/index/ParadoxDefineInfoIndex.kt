package icu.windea.pls.lang.index

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.indexInfo.*
import java.io.*

/**
 * 用于索引预定义的命名空间与变量。
 */
class ParadoxDefineInfoIndex : ParadoxFileBasedIndex<Map<String, ParadoxDefineInfo.Compact>>() {
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findFileBasedIndex<ParadoxDefineInfoIndex>() }
        val NAME = ID.create<String, Map<String, ParadoxDefineInfo.Compact>>("paradox.define.info.index")

        private const val VERSION = 56 //1.3.25
    }

    override fun getName() = NAME

    override fun getVersion() = VERSION

    override fun indexData(file: PsiFile, fileData: MutableMap<String, Map<String, ParadoxDefineInfo.Compact>>) {
        //TODO 1.3.25
    }

    override fun writeData(storage: DataOutput, value: Map<String, ParadoxDefineInfo.Compact>) {
        //TODO 1.3.25
    }

    override fun readData(storage: DataInput): Map<String, ParadoxDefineInfo.Compact> {
        TODO() //TODO 1.3.25
    }

    override fun filterFile(file: VirtualFile): Boolean {
        return ParadoxDefineManager.isDefineFile(file)
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        return false
    }
}
