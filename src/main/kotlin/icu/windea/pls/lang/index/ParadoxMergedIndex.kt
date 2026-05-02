package icu.windea.pls.lang.index

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.gist.VirtualFileGist
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerFactory
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.core.withState
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.ep.index.ParadoxMergedIndexOptimizer
import icu.windea.pls.ep.index.ParadoxMergedIndexSupport
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.definitionCandidateInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiUtil
import icu.windea.pls.model.ParadoxDefinitionCandidateInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.forParadoxGameType
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression
import java.io.DataInput
import java.io.DataOutput

/**
 * 脚本文件和本地化文件中的各种信息的索引。
 *
 * 兼容需要内联的情况（此时使用懒加载的索引，即 [VirtualFileGist]）。
 *
 * @see ParadoxIndexInfo
 * @see ParadoxMergedIndexOptimizer
 * @see ParadoxMergedIndexSupport
 */
@Optimized
class ParadoxMergedIndex : ParadoxIndexInfoAwareFileBasedIndex<List<ParadoxIndexInfo>, ParadoxIndexInfo>() {
    object Keys : KeyRegistry() {
        val definitionCandidate by registerKey<Boolean>(Keys)
    }

    override fun getName() = PlsIndexKeys.Merged

    override fun getVersion() = PlsIndexVersions.Merged

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType && fileType != ParadoxLocalisationFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (VirtualFileService.isInjectedFile(file)) return true
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return true // inline script files should be lazy indexed
        return false
    }

    override fun indexData(psiFile: PsiFile): Map<String, List<ParadoxIndexInfo>> {
        return buildMap {
            buildData(psiFile, this)
            compressData(this)
        }
    }

    private fun buildData(file: PsiFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        withState(PlsStates.processMergedIndex) {
            when (file) {
                is ParadoxScriptFile -> buildDataForScriptFile(file, fileData)
                is ParadoxLocalisationFile -> buildDataForLocalisationFile(file, fileData)
            }
        }
    }

    private fun buildDataForScriptFile(file: ParadoxScriptFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        // NOTE 2.1.6 use lazy index -> config context root may not be a definition -> DO NOT skip on any level
        val useLazyIndex = useLazyIndex(file.virtualFile)

        val optimizers = ParadoxMergedIndexOptimizer.EP_NAME.extensionList
        if (!useLazyIndex && !isAvailableForScriptFile(file, optimizers)) return

        val definitionCandidateInfoStack = ArrayDeque<ParadoxDefinitionCandidateInfo>() // definition or definition injection
        val definitionAvailableStatusStack = ArrayDeque<Boolean>()

        val supports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                buildDataFromSupports(element)

                checkContextRoot(element)
                if (element is ParadoxScriptStringExpressionElement) {
                    visitExpressionElement(element)
                }

                super.visitElement(element)
            }

            private fun checkContextRoot(element: PsiElement) {
                if (element !is ParadoxDefinitionElement) return
                val definitionCandidateInfo = element.definitionCandidateInfo ?: return

                // 忽略内联的定义
                if (definitionCandidateInfo.source == ParadoxDefinitionSource.Inline) return

                element.putUserData(Keys.definitionCandidate, true)
                definitionCandidateInfoStack.addLast(definitionCandidateInfo)
                val definitionAvailableStatus = isAvailableForDefinition(definitionCandidateInfo, optimizers)
                definitionAvailableStatusStack.addLast(definitionAvailableStatus)
            }

            private fun visitExpressionElement(element: ParadoxScriptStringExpressionElement) {
                if (!element.isExpression()) return

                val definitionCandidateInfo = definitionCandidateInfoStack.lastOrNull()
                val definitionAvailableStatus = definitionAvailableStatusStack.lastOrNull()
                if (!useLazyIndex && definitionAvailableStatus != true) return

                ProgressManager.checkCanceled()
                buildDataForExpressionFromSupports(element, definitionCandidateInfo)

                ProgressManager.checkCanceled()
                val options = ParadoxMatchOptions.DUMB
                val configs = ParadoxConfigManager.getConfigs(element, options)
                if (configs.isEmpty()) return
                buildDataForExpressionFromSupports(element, definitionCandidateInfo, configs)
            }

            private fun buildDataFromSupports(element: PsiElement) {
                supports.forEachFast { support -> support.buildData(element, fileData) }
            }

            private fun buildDataForExpressionFromSupports(element: ParadoxScriptStringExpressionElement, info: ParadoxDefinitionCandidateInfo?) {
                supports.forEachFast { support -> support.buildDataForExpression(element, fileData, info) }
            }

            private fun buildDataForExpressionFromSupports(element: ParadoxScriptStringExpressionElement, info: ParadoxDefinitionCandidateInfo?, configs: List<CwtMemberConfig<*>>) {
                supports.forEachFast { support -> support.buildDataForExpression(element, fileData, info, configs) }
            }

            override fun elementFinished(element: PsiElement) {
                if (element is ParadoxDefinitionElement) {
                    if (element.getUserData(Keys.definitionCandidate) == true) {
                        element.putUserData(Keys.definitionCandidate, null)
                        definitionCandidateInfoStack.removeLastOrNull()
                        definitionAvailableStatusStack.removeLastOrNull()
                        cleanUpDumbDefinitionCache(element)
                    }
                }
                if (element is ParadoxScriptStringExpressionElement) {
                    cleanUpDumbExpressionReferencesCache(element)
                }
            }
        })
    }

    private fun buildDataForLocalisationFile(file: ParadoxLocalisationFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        // NOTE 2.1.6 use lazy index -> config context root may not be a definition -> DO NOT skip on any level
        val useLazyIndex = useLazyIndex(file.virtualFile)

        val optimizers = ParadoxMergedIndexOptimizer.EP_NAME.extensionList
        if (!useLazyIndex && !isAvailableForLocalisationFile(file, optimizers)) return

        val supports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        file.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxLocalisationExpressionElement) {
                    visitExpressionElement(element)
                    return // optimize
                }

                if (!ParadoxLocalisationPsiUtil.isRichTextContextElement(element)) return // optimize
                super.visitElement(element)
            }

            private fun visitExpressionElement(element: ParadoxLocalisationExpressionElement) {
                buildDataForExpressionFromSupports(element)
            }

            private fun buildDataForExpressionFromSupports(element: ParadoxLocalisationExpressionElement) {
                supports.forEachFast { ep -> ep.buildDataForExpression(element, fileData) }
            }

            override fun elementFinished(element: PsiElement?) {
                if (element is ParadoxLocalisationExpressionElement) {
                    cleanUpDumbExpressionReferencesCache(element)
                }
            }
        })
    }

    private fun isAvailableForScriptFile(file: ParadoxScriptFile, optimizers: List<ParadoxMergedIndexOptimizer>): Boolean {
        optimizers.forEachFast { optimizer -> if (optimizer.isAvailableForScriptFile(file)) return true }
        return false
    }

    private fun isAvailableForLocalisationFile(file: ParadoxLocalisationFile, optimizers: List<ParadoxMergedIndexOptimizer>): Boolean {
        optimizers.forEachFast { optimizer -> if (optimizer.isAvailableForLocalisationFile(file)) return true }
        return false
    }

    private fun isAvailableForDefinition(definitionCandidateInfo: ParadoxDefinitionCandidateInfo, optimizers: List<ParadoxMergedIndexOptimizer>): Boolean {
        optimizers.forEachFast { optimizer -> if (optimizer.isAvailableForDefinition(definitionCandidateInfo)) return true }
        return false
    }

    private fun cleanUpDumbDefinitionCache(element: ParadoxDefinitionElement) {
        // clean up dumb definition caches (subtypeConfigs & declaration)
        element.putUserData(ParadoxDefinitionManager.Keys.cachedSubtypeConfigsDumb, null)
        element.putUserData(ParadoxDefinitionManager.Keys.cachedDeclarationDumb, null)
    }

    private fun cleanUpDumbExpressionReferencesCache(element: ParadoxExpressionElement) {
        // clean up dumb expression references caches
        element.putUserData(ParadoxExpressionManager.Keys.cachedExpressionReferencesDumb, null)
    }

    private fun compressData(fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        if (fileData.isEmpty()) return
        val supports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        for (key in fileData.keys) {
            val oldValue = fileData.getValue(key)
            if (oldValue.size <= 1) continue
            val support = getSupportOrUnsupported(supports, key.toByte())
            val newValue = support.compressData(oldValue)
            fileData[key] = newValue
        }
    }

    override fun indexLazyData(psiFile: PsiFile): Map<String, List<ParadoxIndexInfo>> {
        // 用于兼容懒加载的索引
        return buildMap {
            val supports = ParadoxMergedIndexSupport.EP_NAME.extensionList
            supports.forEachFast { ep -> put(ep.indexInfoType.key.toString(), emptyList()) }
        }
    }

    override fun saveValue(storage: DataOutput, value: List<ParadoxIndexInfo>) {
        val size = value.size
        storage.writeIntFast(size)
        if (value.isEmpty()) return

        val firstInfo = value.first()
        val type = firstInfo.javaClass
        val supports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        val support = getSupportOrUnsupported(supports, type)
        storage.writeByte(support.indexInfoType.key)
        val gameType = firstInfo.gameType
        storage.writeByte(gameType.optimized(OptimizerFactory.forParadoxGameType()))
        var previousInfo: ParadoxIndexInfo? = null
        value.forEach { info ->
            support.saveData(storage, info, previousInfo, gameType)
            previousInfo = info
        }
    }

    override fun readValue(storage: DataInput): List<ParadoxIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val key = storage.readByte()
        val supports = ParadoxMergedIndexSupport.EP_NAME.extensionList
        val support = getSupportOrUnsupported(supports, key)
        val gameType = storage.readByte().deoptimized(OptimizerFactory.forParadoxGameType())
        var previousInfo: ParadoxIndexInfo? = null
        return MutableList(size) {
            support.readData(storage, previousInfo, gameType).also { previousInfo = it }
        }
    }

    private fun getSupportOrUnsupported(supports: List<ParadoxMergedIndexSupport<*>>, key: Byte): ParadoxMergedIndexSupport<ParadoxIndexInfo> {
        return supports.findFast { it.indexInfoType.key == key }?.castOrNull() ?: throw UnsupportedOperationException()
    }

    private fun getSupportOrUnsupported(supports: List<ParadoxMergedIndexSupport<*>>, type: Class<ParadoxIndexInfo>): ParadoxMergedIndexSupport<ParadoxIndexInfo> {
        return supports.findFast { it.indexInfoType.type == type }?.castOrNull() ?: throw UnsupportedOperationException()
    }
}
