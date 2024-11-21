package icu.windea.pls.lang.index

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.usageInfo.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.ParadoxScriptProperty
import java.io.*

/**
 * 用于索引内联脚本的使用信息。
 */
class ParadoxInlineScriptUsageIndex : ParadoxFileBasedIndex<ParadoxInlineScriptUsageInfo.Compact>() {
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findFileBasedIndex<ParadoxInlineScriptUsageIndex>() }
        val NAME = ID.create<String, ParadoxInlineScriptUsageInfo.Compact>("paradox.inlineScript.usage.index")

        private const val VERSION = 55 //1.3.24

        private val markerKey = createKey<Boolean>("paradox.expression.index.marker")

        fun processQuery(
            project: Project,
            gameType: ParadoxGameType,
            scope: GlobalSearchScope,
            processor: (file: VirtualFile, fileData: Map<String, ParadoxInlineScriptUsageInfo.Compact>) -> Boolean
        ): Boolean {
            ProgressManager.checkCanceled()
            if (SearchScope.isEmptyScope(scope)) return true

            return FileTypeIndex.processFiles(ParadoxScriptFileType, p@{ file ->
                ProgressManager.checkCanceled()
                if (selectGameType(file) != gameType) return@p true //check game type at file level

                val fileData = INSTANCE.getFileData(file, project)
                if (fileData.isEmpty()) return@p true
                processor(file, fileData)
            }, scope)
        }
    }

    override fun getName() = NAME

    override fun getVersion() = VERSION

    override fun indexData(file: PsiFile, fileData: MutableMap<String, ParadoxInlineScriptUsageInfo.Compact>) {
        file.acceptChildren(object: PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptProperty) {
                    val r = visitProperty(element)
                    if(r) return
                }
                
                super.visitElement(element)
            }
            
            private fun visitProperty(element: ParadoxScriptProperty): Boolean {
                val info = ParadoxInlineScriptManager.getUsageInfo(element) ?: return false
                val compactInfo = fileData.getOrPut(info.expression) { ParadoxInlineScriptUsageInfo.Compact(info.expression, sortedSetOf()) }
                compactInfo.elementOffsets.castOrNull<MutableSet<Int>>()?.let { it += info.elementOffset }
                return true
            }
        })
    }

    override fun writeData(storage: DataOutput, value: ParadoxInlineScriptUsageInfo.Compact) {
        storage.writeUTFFast(value.expression)
        storage.writeList(value.elementOffsets) { storage.writeIntFast(it) }
    }

    override fun readData(storage: DataInput): ParadoxInlineScriptUsageInfo.Compact {
        val expression = storage.readUTFFast()
        val elementOffsets = storage.readList { storage.readIntFast() }
        return ParadoxInlineScriptUsageInfo.Compact(expression, elementOffsets)
    }

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (ParadoxFileManager.isInjectedFile(file)) return true
        //if (ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return true //unnecessary
        return false
    }
}
