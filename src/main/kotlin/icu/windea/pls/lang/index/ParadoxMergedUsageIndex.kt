package icu.windea.pls.lang.index

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.usageInfo.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.io.*

/**
 * 用于索引各种使用信息。
 * 
 * 兼容需要内联的情况（此时使用懒加载的索引）。
 *
 * @see ParadoxUsageInfo
 * @see ParadoxUsageIndexSupport
 */
class ParadoxMergedUsageIndex : ParadoxFileBasedIndex<List<ParadoxUsageInfo>>() {
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findFileBasedIndex<ParadoxMergedUsageIndex>() }
        val NAME = ID.create<String, List<ParadoxUsageInfo>>("paradox.merged.usage.index")

        private const val VERSION = 55 //1.3.24

        private val markerKey = createKey<Boolean>("paradox.merged.usage.index.marker")

        fun <ID : ParadoxUsageIndexType<T>, T : ParadoxUsageInfo> processQuery(
            fileType: LanguageFileType,
            id: ID,
            project: Project,
            gameType: ParadoxGameType,
            scope: GlobalSearchScope,
            processor: (file: VirtualFile, fileData: List<T>) -> Boolean
        ): Boolean {
            ProgressManager.checkCanceled()
            if (SearchScope.isEmptyScope(scope)) return true

            return FileTypeIndex.processFiles(fileType, p@{ file ->
                ProgressManager.checkCanceled()
                if (selectGameType(file) != gameType) return@p true //check game type at file level

                val fileData = INSTANCE.getFileData(file, project, id)
                if (fileData.isEmpty()) return@p true
                processor(file, fileData)
            }, scope)
        }
    }

    override fun getName() = NAME

    override fun getVersion() = VERSION

    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        when (file) {
            is ParadoxScriptFile -> indexDataForScriptFile(file, fileData)
            is ParadoxLocalisationFile -> indexDataForLocalisationFile(file, fileData)
        }
        compressData(fileData)
    }

    private fun indexDataForScriptFile(file: ParadoxScriptFile, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        val extensionList = ParadoxUsageIndexSupport.EP_NAME.extensionList
        val definitionInfoStack = ArrayDeque<ParadoxDefinitionInfo>()
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                extensionList.forEach { ep ->
                    ep.indexScriptElement(element, fileData)
                }

                if (element is ParadoxScriptDefinitionElement) {
                    val definitionInfo = element.definitionInfo
                    if (definitionInfo != null) {
                        element.putUserData(markerKey, true)
                        definitionInfoStack.addLast(definitionInfo)
                    }
                }

                run {
                    if (definitionInfoStack.isEmpty()) return@run
                    if (element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                        ProgressManager.checkCanceled()
                        val matchOptions = ParadoxExpressionMatcher.Options.SkipIndex or ParadoxExpressionMatcher.Options.SkipScope
                        val configs = ParadoxExpressionManager.getConfigs(element, matchOptions = matchOptions)
                        if (configs.isEmpty()) return@run
                        val definitionInfo = definitionInfoStack.lastOrNull() ?: return@run
                        extensionList.forEach { ep ->
                            configs.forEach { config ->
                                ep.indexScriptExpression(element, config, definitionInfo, fileData)
                            }
                        }
                    }
                }

                super.visitElement(element)
            }

            override fun elementFinished(element: PsiElement) {
                if (element.getUserData(markerKey) == true) {
                    element.putUserData(markerKey, null)
                    definitionInfoStack.removeLastOrNull()
                }
            }
        })
    }

    private fun indexDataForLocalisationFile(file: ParadoxLocalisationFile, fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        val extensionList = ParadoxUsageIndexSupport.EP_NAME.extensionList
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationCommandText) {
                    extensionList.forEach f@{ ep ->
                        ep.indexLocalisationCommandText(element, fileData)
                    }
                }
                if (element.isRichTextContext()) super.visitElement(element)
            }
        })
    }

    private fun compressData(fileData: MutableMap<String, List<ParadoxUsageInfo>>) {
        if (fileData.isEmpty()) return
        val extensionList = ParadoxUsageIndexSupport.EP_NAME.extensionList
        fileData.mapValues { (k, v) ->
            val id = k.toByte()
            val support = extensionList.find { it.id() == id }
                ?.castOrNull<ParadoxUsageIndexSupport<ParadoxUsageInfo>>()
                ?: throw UnsupportedOperationException()
            support.compressData(v)
        }
    }

    override fun writeData(storage: DataOutput, value: List<ParadoxUsageInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if (value.isEmpty()) return

        val type = value.first().javaClass
        val support = ParadoxUsageIndexSupport.EP_NAME.extensionList.find { it.type() == type }
            ?.castOrNull<ParadoxUsageIndexSupport<ParadoxUsageInfo>>()
            ?: throw UnsupportedOperationException()
        storage.writeByte(support.id())
        val gameType = value.first().gameType
        storage.writeByte(gameType.optimizeValue())
        var previousInfo: ParadoxUsageInfo? = null
        value.forEach { info ->
            support.writeData(storage, info, previousInfo, gameType)
            previousInfo = info
        }
    }

    override fun readData(storage: DataInput): List<ParadoxUsageInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val id = storage.readByte()
        val support = ParadoxUsageIndexSupport.EP_NAME.extensionList.find { it.id() == id }
            ?.castOrNull<ParadoxUsageIndexSupport<ParadoxUsageInfo>>()
            ?: throw UnsupportedOperationException()
        val gameType = storage.readByte().deoptimizeValue<ParadoxGameType>()
        var previousInfo: ParadoxUsageInfo? = null
        return MutableList(size) {
            support.readData(storage, previousInfo, gameType)
                .also { previousInfo = it }
        }
    }

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType && fileType != ParadoxLocalisationFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (ParadoxFileManager.isInjectedFile(file)) return true
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return true
        return false
    }

    fun <T : ParadoxUsageInfo> getFileData(file: VirtualFile, project: Project, id: ParadoxUsageIndexType<T>): List<T> {
        val allFileData = getFileData(file, project)
        return allFileData.get(id.id.toString())?.castOrNull<List<T>>().orEmpty()
    }
}
