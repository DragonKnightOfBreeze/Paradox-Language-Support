package icu.windea.pls.lang.index

import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*
import java.io.*

/**
 * 用于索引各种信息。
 *
 * 兼容需要内联的情况（此时使用懒加载的索引）。
 *
 * @see ParadoxIndexInfo
 * @see ParadoxIndexInfoSupport
 */
class ParadoxMergedIndex : ParadoxFileBasedIndex<List<ParadoxIndexInfo>>() {
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findFileBasedIndex<ParadoxMergedIndex>() }
        val NAME = ID.create<String, List<ParadoxIndexInfo>>("paradox.merged.index")

        private const val VERSION = 59 //1.3.35

        private val markerKey = createKey<Boolean>("paradox.merged.info.index.marker")

        fun <ID : ParadoxIndexInfoType<T>, T : ParadoxIndexInfo> processQuery(
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

    override fun indexData(file: PsiFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        when (file) {
            is ParadoxScriptFile -> indexDataForScriptFile(file, fileData)
            is ParadoxLocalisationFile -> indexDataForLocalisationFile(file, fileData)
        }
        compressData(fileData)
    }

    private fun indexDataForScriptFile(file: ParadoxScriptFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val extensionList = ParadoxIndexInfoSupport.EP_NAME.extensionList
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

    private fun indexDataForLocalisationFile(file: ParadoxLocalisationFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        val extensionList = ParadoxIndexInfoSupport.EP_NAME.extensionList
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationExpressionElement) {
                    extensionList.forEach f@{ ep ->
                        ep.indexLocalisationExpression(element, fileData)
                    }
                }
                if (element.elementType !in ParadoxLocalisationTokenSets.RICH_TEXT_CONTEXT) return //optimize
                super.visitElement(element)
            }
        })
    }

    private fun compressData(fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        if (fileData.isEmpty()) return
        val extensionList = ParadoxIndexInfoSupport.EP_NAME.extensionList
        fileData.mapValues { (k, v) ->
            val id = k.toByte()
            val support = extensionList.find { it.id == id }
                ?.castOrNull<ParadoxIndexInfoSupport<ParadoxIndexInfo>>()
                ?: throw UnsupportedOperationException()
            support.compressData(v)
        }
    }

    override fun writeData(storage: DataOutput, value: List<ParadoxIndexInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if (value.isEmpty()) return

        val firstInfo = value.first()
        val type = firstInfo.javaClass
        val support = ParadoxIndexInfoSupport.EP_NAME.extensionList.find { it.type == type }
            ?.castOrNull<ParadoxIndexInfoSupport<ParadoxIndexInfo>>()
            ?: throw UnsupportedOperationException()
        storage.writeByte(support.id)
        val gameType = firstInfo.gameType
        storage.writeByte(gameType.optimizeValue())
        var previousInfo: ParadoxIndexInfo? = null
        value.forEach { info ->
            support.writeData(storage, info, previousInfo, gameType)
            previousInfo = info
        }
    }

    override fun readData(storage: DataInput): List<ParadoxIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val id = storage.readByte()
        val support = ParadoxIndexInfoSupport.EP_NAME.extensionList.find { it.id == id }
            ?.castOrNull<ParadoxIndexInfoSupport<ParadoxIndexInfo>>()
            ?: throw UnsupportedOperationException()
        val gameType = storage.readByte().deoptimizeValue<ParadoxGameType>()
        var previousInfo: ParadoxIndexInfo? = null
        return MutableList(size) {
            support.readData(storage, previousInfo, gameType)
                .also { previousInfo = it }
        }
    }

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType !is ParadoxBaseFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (ParadoxFileManager.isInjectedFile(file)) return true
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return true //inline script files should be lazy indexed
        if (file.fileType is ParadoxLocalisationFileType) return true //to prevent recursion, see #127
        return false
    }

    fun <T : ParadoxIndexInfo> getFileData(file: VirtualFile, project: Project, id: ParadoxIndexInfoType<T>): List<T> {
        val allFileData = getFileData(file, project)
        return allFileData.get(id.id.toString())?.castOrNull<List<T>>().orEmpty()
    }
}
