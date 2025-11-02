package icu.windea.pls.lang.index

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.gist.VirtualFileGist
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.readIntFast
import icu.windea.pls.core.writeByte
import icu.windea.pls.core.writeIntFast
import icu.windea.pls.ep.index.ParadoxIndexInfoSupport
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.lang.withState
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiUtil
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ValueOptimizers.ForParadoxGameType
import icu.windea.pls.model.deoptimized
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.model.optimized
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
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
 * @see ParadoxIndexInfoSupport
 */
class ParadoxMergedIndex : IndexInfoAwareFileBasedIndex<List<ParadoxIndexInfo>>() {
    override fun getName() = PlsIndexKeys.Merged

    override fun getVersion() = PlsIndexVersions.Merged

    override fun filterFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType && fileType != ParadoxLocalisationFileType) return false
        if (file.fileInfo == null) return false
        return true
    }

    override fun useLazyIndex(file: VirtualFile): Boolean {
        if (PlsFileManager.isInjectedFile(file)) return true
        if (ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return true // inline script files should be lazy indexed
        return false
    }

    override fun indexData(psiFile: PsiFile): Map<String, List<ParadoxIndexInfo>> {
        return buildMap { buildData(psiFile, this) }
    }

    private fun buildData(file: PsiFile, fileData: MutableMap<String, List<ParadoxIndexInfo>>) {
        withState(PlsCoreManager.processMergedIndex) {
            when (file) {
                is ParadoxScriptFile -> indexDataForScriptFile(file, fileData)
                is ParadoxLocalisationFile -> indexDataForLocalisationFile(file, fileData)
            }
            compressData(fileData)
        }
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
                        element.putUserData(PlsIndexUtil.indexInfoMarkerKey, true)
                        definitionInfoStack.addLast(definitionInfo)
                    }
                }

                run {
                    if (definitionInfoStack.isEmpty()) return@run
                    if (element is ParadoxScriptStringExpressionElement && element.isExpression()) {
                        ProgressManager.checkCanceled()
                        val matchOptions = ParadoxMatchOptions.SkipIndex or ParadoxMatchOptions.SkipScope
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
                if (element.getUserData(PlsIndexUtil.indexInfoMarkerKey) == true) {
                    element.putUserData(PlsIndexUtil.indexInfoMarkerKey, null)
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
                if (!ParadoxLocalisationPsiUtil.isRichTextContextElement(element)) return // optimize
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

    override fun indexLazyData(psiFile: PsiFile): Map<String, List<ParadoxIndexInfo>> {
        // 用于兼容懒加载的索引
        return buildMap {
            val extensionList = ParadoxIndexInfoSupport.EP_NAME.extensionList
            extensionList.forEach { ep -> put(ep.id.toString(), emptyList()) }
        }
    }

    override fun saveValue(storage: DataOutput, value: List<ParadoxIndexInfo>) {
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
        storage.writeByte(gameType.optimized(ForParadoxGameType))
        var previousInfo: ParadoxIndexInfo? = null
        value.forEach { info ->
            support.saveData(storage, info, previousInfo, gameType)
            previousInfo = info
        }
    }

    override fun readValue(storage: DataInput): List<ParadoxIndexInfo> {
        val size = storage.readIntFast()
        if (size == 0) return emptyList()

        val id = storage.readByte()
        val support = ParadoxIndexInfoSupport.EP_NAME.extensionList.find { it.id == id }
            ?.castOrNull<ParadoxIndexInfoSupport<ParadoxIndexInfo>>()
            ?: throw UnsupportedOperationException()
        val gameType = storage.readByte().deoptimized(ForParadoxGameType)
        var previousInfo: ParadoxIndexInfo? = null
        return MutableList(size) {
            support.readData(storage, previousInfo, gameType).also { previousInfo = it }
        }
    }
}
