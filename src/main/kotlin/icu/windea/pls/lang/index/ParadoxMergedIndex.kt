package icu.windea.pls.lang.index

import com.google.common.collect.ImmutableSet
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.gist.VirtualFileGist
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.buildImmutableList
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.readUTFFast
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.core.withState
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.core.writeUTFFast
import icu.windea.pls.csv.ParadoxCsvFileType
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.ep.index.ParadoxMergedIndexOptimizer
import icu.windea.pls.ep.index.ParadoxMergedIndexSupport
import icu.windea.pls.lang.definitionCandidateInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiService
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression
import java.io.DataInput
import java.io.DataOutput

/**
 * 脚本文件和本地化文件中的各种信息的索引。
 *
 * 兼容需要内联的情况（此时使用懒加载的索引，即 [VirtualFileGist]）。
 *
 * @see ParadoxIndexInfo
 * @see ParadoxMergedIndexSupport
 * @see ParadoxMergedIndexOptimizer
 */
@Optimized
class ParadoxMergedIndex : ParadoxIndexInfoAwareFileBasedIndex<List<ParadoxIndexInfo>, ParadoxIndexInfo>() {
    object Keys : KeyRegistry() {
        val definitionCandidate by registerKey<Boolean>(Keys)
    }

    override fun getName() = ChronicleIndexKeys.Merged

    override fun getVersion() = ChronicleIndexVersions.Merged

    override fun filterFileType(fileType: FileType): Boolean {
        return fileType === ParadoxScriptFileType || fileType === ParadoxLocalisationFileType || fileType === ParadoxCsvFileType
    }

    override fun filterFile(file: VirtualFile): Boolean {
        return file.fileInfo != null
    }

    override fun indexData(psiFile: PsiFile): Map<String, List<ParadoxIndexInfo>> {
        return buildMap {
            buildData(psiFile, this)
        }
    }

    private fun buildData(file: PsiFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        ParadoxMergedIndexThreadContext.isProcessing.withState {
            when (file) {
                is ParadoxScriptFile -> buildDataForScriptFile(file, fileData)
                is ParadoxLocalisationFile -> buildDataForLocalisationFile(file, fileData)
                is ParadoxCsvFile -> buildDataForCsvFile(file, fileData)
            }
        }
    }

    private fun buildDataForScriptFile(file: ParadoxScriptFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        // NOTE 2.1.6 use lazy index -> config context root may not be a definition -> DO NOT skip on any level
        val useLazyIndex = useLazyIndex(file.virtualFile)

        // 3.0.1 optimize: restrict types and supports via strategies, config attributes, etc.
        val optimizers = ParadoxMergedIndexOptimizer.EP_NAME.extensionList
        val allTypes = ImmutableSet.copyOf(ParadoxMergedIndexType.entries.values)
        val availableTypes = if (useLazyIndex) allTypes else ParadoxMergedIndexService.getAvailableTypes(file, optimizers)
        if (availableTypes.isEmpty()) return // fast return
        val allSupports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        val supports = if (useLazyIndex) allSupports else allSupports.filterFast { it.type in availableTypes }
        if (supports.isEmpty()) return // fast return

        val context = ParadoxMergedIndexScriptContextBase(file, fileData)
        // definition or definition injection
        val definitionCandidateInfoStack = ArrayDeque<ParadoxDefinitionCandidateInfo>()
        val definitionCandidateAvailableTypesStack = ArrayDeque<Set<ParadoxMergedIndexType<*>>>()

        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                ParadoxMergedIndexService.buildData(element, context, supports)

                if (element is ParadoxScriptStringExpressionElement) {
                    visitStringExpressionElement(element) // 3.0.1 just for string expressions atm
                }

                handleContext(element)

                super.visitElement(element)
            }

            override fun elementFinished(element: PsiElement) {
                handleContextFinished(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val definitionCandidateInfo = context.definitionCandidateInfo
                if (definitionCandidateInfo != null && context.definitionCandidateAvailableTypes.isEmpty()) return // fast return

                if (element.value.isEmpty()) return // skip if expression is empty
                if (!element.isDataExpression()) return // fast return
                // skip for definition type keys (and definition injection expressions), atm
                if (element is ParadoxScriptPropertyKey && element.getUserData(Keys.definitionCandidate) == true) return

                ProgressManager.checkCanceled()
                context.expressionElement = element
                ParadoxMergedIndexService.buildDataForExpression(element, context, supports)
                context.expressionElement = null
                context.resetCache()
            }

            private fun handleContext(element: PsiElement) {
                if (element is ParadoxDefinitionElement) {
                    val definitionCandidateInfo = element.definitionCandidateInfo ?: return
                    if (definitionCandidateInfo.source == ParadoxDefinitionSource.Inline) return  // 忽略内联的定义
                    element.putUserData(Keys.definitionCandidate, true) // 标记
                    if (element is ParadoxScriptProperty) element.propertyKey.putUserData(Keys.definitionCandidate, true) // 标记
                    val definitionCandidateAvailableTypes = if (useLazyIndex) allTypes else ParadoxMergedIndexService.getAvailableTypes(definitionCandidateInfo, optimizers)
                    definitionCandidateInfoStack.addLast(definitionCandidateInfo) // 进栈
                    definitionCandidateAvailableTypesStack.addLast(definitionCandidateAvailableTypes) // 进栈
                    context.definitionCandidateInfo = definitionCandidateInfo
                    context.definitionCandidateAvailableTypes = definitionCandidateAvailableTypes
                    context.definitionCandidateAvailableTypesUnchanged = definitionCandidateAvailableTypes == availableTypes
                }
            }

            private fun handleContextFinished(element: PsiElement) {
                if (element is ParadoxDefinitionElement && element.getUserData(Keys.definitionCandidate) == true) {
                    element.putUserData(Keys.definitionCandidate, null) // 清空标记
                    if (element is ParadoxScriptProperty) element.propertyKey.putUserData(Keys.definitionCandidate, null) // 清空标记
                    definitionCandidateInfoStack.removeLastOrNull() // 出栈
                    definitionCandidateAvailableTypesStack.removeLastOrNull() // 出栈
                    context.definitionCandidateInfo = null
                    context.definitionCandidateAvailableTypes = emptySet()
                    context.definitionCandidateAvailableTypesUnchanged = true

                    cleanUpDumbDefinitionCache(element) // 清空缓存
                }
            }
        })

        ParadoxMergedIndexService.compressData(fileData, supports)
    }

    private fun buildDataForLocalisationFile(file: ParadoxLocalisationFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        // 3.0.1 optimize: restrict types and supports via strategies, config attributes, etc.
        val optimizers = ParadoxMergedIndexOptimizer.EP_NAME.extensionList
        val availableTypes = ParadoxMergedIndexService.getAvailableTypes(file, optimizers)
        if (availableTypes.isEmpty()) return // fast return
        val allSupports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        val supports = allSupports.filterFast { it.type in availableTypes }
        if (supports.isEmpty()) return // fast return

        val context = ParadoxMergedIndexLocalisationContextBase(file, fileData)

        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationExpressionElement) {
                    visitExpressionElement(element)
                    return // optimize
                }

                handleContext(element)

                if (!ParadoxLocalisationPsiService.isStrictRichTextContext(element)) return // optimize
                super.visitElement(element)
            }

            override fun elementFinished(element: PsiElement) {
                handleContextFinished(element)
            }

            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                if (element.value.isEmpty()) return // skip if expression is empty

                ProgressManager.checkCanceled()
                context.expressionElement = element
                ParadoxMergedIndexService.buildDataForExpression(element, context, supports)
                context.expressionElement = null
                context.resetCache()
            }

            private fun handleContext(element: PsiElement) {
                if (element is ParadoxLocalisationProperty) {
                    context.localisation = element
                }
            }

            private fun handleContextFinished(element: PsiElement) {
                if (element is ParadoxLocalisationProperty) {
                    context.localisation = null
                }
            }
        })

        ParadoxMergedIndexService.compressData(fileData, supports)
    }

    private fun buildDataForCsvFile(file: ParadoxCsvFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        // 3.0.1 optimize: restrict types and supports via strategies, config attributes, etc.
        val optimizers = ParadoxMergedIndexOptimizer.EP_NAME.extensionList
        val availableTypes = ParadoxMergedIndexService.getAvailableTypes(file, optimizers)
        if (availableTypes.isEmpty()) return // fast return
        val allSupports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        val supports = allSupports.filterFast { it.type in availableTypes }
        if (supports.isEmpty()) return // fast return

        val context = ParadoxMergedIndexCsvContextBase(file, fileData)

        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxCsvExpressionElement) {
                    visitExpressionElement(element)
                    return // optimize
                }

                super.visitElement(element)
            }

            private fun visitExpressionElement(element: ParadoxCsvExpressionElement) {
                if (element.value.isEmpty()) return // skip if expression is empty

                ProgressManager.checkCanceled()
                context.expressionElement = element
                ParadoxMergedIndexService.buildDataForExpression(element, context, supports)
                context.expressionElement = null
                context.resetCache()
            }
        })

        ParadoxMergedIndexService.compressData(fileData, supports)
    }

    private fun cleanUpDumbDefinitionCache(element: ParadoxDefinitionElement) {
        // clean up dumb definition caches (subtypeConfigs & declaration)
        element.putUserData(ParadoxDefinitionManager.Keys.cachedSubtypeConfigsDumb, null)
        element.putUserData(ParadoxDefinitionManager.Keys.cachedDeclarationDumb, null)
    }

    // 3.0.1 unnecessary since expression references will be cached directly in context (`ParadoxMergedIndexCsvContext`)
    // private fun cleanUpDumbExpressionReferencesCache(element: ParadoxExpressionElement) {
    //     // clean up dumb expression references caches
    //     element.putUserData(ParadoxExpressionManager.Keys.cachedExpressionReferencesDumb, null)
    // }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (VirtualFileService.isInjectedFile(file)) return true
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return true // inline script files should be lazy indexed
        return false
    }

    override fun indexLazyData(psiFile: PsiFile): Map<String, List<ParadoxIndexInfo>> {
        // 用于兼容懒加载的索引
        return buildMap {
            val supports = ParadoxMergedIndexSupport.EP_NAME.extensionList
            supports.forEachFast { support -> put(support.type.key, emptyList()) }
        }
    }

    override fun saveValue(storage: DataOutput, value: List<ParadoxIndexInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if (value.isEmpty()) return

        val firstInfo = value.first()
        val type = firstInfo.javaClass
        val supports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        val support = ParadoxMergedIndexService.getSupportOrUnsupported(type, supports)
        storage.writeUTFFast(support.type.key)
        val gameType = firstInfo.gameType
        storage.writeByte(gameType.optimized())

        var previousInfo: ParadoxIndexInfo? = null
        value.forEachFast { info ->
            support.saveData(storage, info, previousInfo, gameType)
            previousInfo = info
        }
    }

    override fun readValue(storage: DataInput): List<ParadoxIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val key = storage.readUTFFast()
        val supports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        val support = ParadoxMergedIndexService.getSupportOrUnsupported(key, supports)
        val gameType = storage.readByte().let { ParadoxGameType.deoptimized(it) }

        // 2.1.9 optimize: create sized immutable list directly
        var previousInfo: ParadoxIndexInfo? = null
        return buildImmutableList(size) {
            support.readData(storage, previousInfo, gameType).also { previousInfo = it }
        }
    }
}
