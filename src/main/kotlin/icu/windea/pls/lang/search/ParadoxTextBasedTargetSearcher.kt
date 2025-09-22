package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationString
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 基于本地化文本片段的目标（封装变量/定义/本地化）查询器。
 */
class ParadoxTextBasedTargetSearcher: QueryExecutorBase<PsiElement, ParadoxTextBasedTargetSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxTextBasedTargetSearch.SearchParameters, consumer: Processor<in PsiElement>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsCoreManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = queryParameters.scope
        if (SearchScope.isEmptyScope(scope)) return
        val gScope = scope

        // 输入的本地化文本片段
        val rawText = queryParameters.text?.trim()
        if (rawText.isNullOrEmpty()) return

        // 目标类型（未指定则默认全部）
        val requestedTypes = queryParameters.types ?: setOf(
            ParadoxSearchTargetType.ScriptedVariable,
            ParadoxSearchTargetType.Definition,
            ParadoxSearchTargetType.Localisation
        )

        // 先从本地化文件中定位包含该片段的纯文本（ParadoxLocalisationString），拿到所属本地化
        val localisationSet = linkedSetOf<ParadoxLocalisationProperty>()
        var terminated = false
        FileTypeIndex.processFiles(ParadoxLocalisationFileType, p@{ vFile ->
            if (terminated) return@p false
            ProgressManager.checkCanceled()
            val psiFile = vFile.toPsiFile(project) ?: return@p true
            val fileText = psiFile.text
            if (!fileText.contains(rawText, ignoreCase = true)) return@p true // 快速剪枝：文件文本不包含片段

            // 深入到 PSI：仅匹配 ParadoxLocalisationString（纯文本），忽略图标、格式等
            psiFile.accept(object : com.intellij.psi.PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (terminated) return
                    ProgressManager.checkCanceled()
                    if (element is ParadoxLocalisationString) {
                        val s = element.idElement.text
                        if (s.contains(rawText, ignoreCase = true)) {
                            val property = PsiTreeUtil.getParentOfType(element, ParadoxLocalisationProperty::class.java)
                            if (property != null) localisationSet += property
                        }
                    }
                    super.visitElement(element)
                }
            })
            true
        }, gScope)

        if (localisationSet.isEmpty()) return

        // 去重后输出本地化 / 相关封装变量 / 相关定义
        val emittedLocs = hashSetOf<ParadoxLocalisationProperty>()
        val emittedVars = hashSetOf<ParadoxScriptScriptedVariable>()
        val emittedDefs = hashSetOf<ParadoxScriptDefinitionElement>()

        // 输出本地化
        if (ParadoxSearchTargetType.Localisation in requestedTypes) {
            for (loc in localisationSet) {
                ProgressManager.checkCanceled()
                if (emittedLocs.add(loc)) {
                    if (!consumer.process(loc)) { terminated = true; break }
                }
            }
            if (terminated) return
        }

        // 输出相关封装变量
        if (ParadoxSearchTargetType.ScriptedVariable in requestedTypes) {
            for (loc in localisationSet) {
                ProgressManager.checkCanceled()
                val vars = ParadoxLocalisationManager.getRelatedScriptedVariables(loc)
                for (v in vars) {
                    if (emittedVars.add(v)) {
                        if (!consumer.process(v)) { terminated = true; break }
                    }
                }
                if (terminated) break
            }
            if (terminated) return
        }

        // 输出相关定义
        if (ParadoxSearchTargetType.Definition in requestedTypes) {
            for (loc in localisationSet) {
                ProgressManager.checkCanceled()
                val defs = ParadoxLocalisationManager.getRelatedDefinitions(loc)
                for (d in defs) {
                    if (emittedDefs.add(d)) {
                        if (!consumer.process(d)) { terminated = true; break }
                    }
                }
                if (terminated) break
            }
        }
    }
}
