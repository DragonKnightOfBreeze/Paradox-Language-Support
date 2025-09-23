package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.TextOccurenceProcessor
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
        val gScope: GlobalSearchScope = queryParameters.scope
        if (SearchScope.isEmptyScope(gScope)) return

        // 输入的本地化文本片段
        val rawText = queryParameters.text?.trim()
        if (rawText.isNullOrEmpty()) return

        // 目标类型（未指定则默认全部）
        val requestedTypes = queryParameters.types ?: setOf(
            ParadoxSearchTargetType.ScriptedVariable,
            ParadoxSearchTargetType.Definition,
            ParadoxSearchTargetType.Localisation
        )

        // 优化：借鉴 IntelliJ FindInProjectTask 两阶段与 Processor 思想
        // 第一阶段（索引）：用 PsiSearchHelper.processElementsWithWord 在“字符串字面量”上下文中直接定位命中元素
        // 第二阶段（验证/聚合）：最小化 PSI 访问，仅向上取 ParadoxLocalisationString 与所属的 ParadoxLocalisationProperty
        var terminated = false
        val emittedLocs = hashSetOf<ParadoxLocalisationProperty>()
        val emittedVars = hashSetOf<ParadoxScriptScriptedVariable>()
        val emittedDefs = hashSetOf<ParadoxScriptDefinitionElement>()
        val helper = PsiSearchHelper.getInstance(project)

        fun emitAllFor(loc: ParadoxLocalisationProperty): Boolean {
            // 本地化
            if (ParadoxSearchTargetType.Localisation in requestedTypes) {
                if (emittedLocs.add(loc)) {
                    if (!consumer.process(loc)) return false
                }
            }
            // 相关封装变量
            if (ParadoxSearchTargetType.ScriptedVariable in requestedTypes) {
                val vars = ParadoxLocalisationManager.getRelatedScriptedVariables(loc)
                for (v in vars) {
                    if (emittedVars.add(v)) {
                        if (!consumer.process(v)) return false
                    }
                }
            }
            // 相关定义
            if (ParadoxSearchTargetType.Definition in requestedTypes) {
                val defs = ParadoxLocalisationManager.getRelatedDefinitions(loc)
                for (d in defs) {
                    if (emittedDefs.add(d)) {
                        if (!consumer.process(d)) return false
                    }
                }
            }
            return true
        }

        // 选择锚词：提取最长的 [a-zA-Z0-9_] 片段，长度至少 3
        val anchorWord = Regex("[A-Za-z0-9_]+").findAll(rawText).map { it.value }.maxByOrNull { it.length }?.takeIf { it.length >= 3 }

        if (anchorWord != null) {
            val occurenceProcessor = TextOccurenceProcessor { element, _ ->
                if (terminated) return@TextOccurenceProcessor false
                ProgressManager.checkCanceled()
                // 仅接受 localisation 文件中的字符串上下文
                val file = element.containingFile ?: return@TextOccurenceProcessor true
                if (file.fileType != ParadoxLocalisationFileType) return@TextOccurenceProcessor true
                val str = PsiTreeUtil.getParentOfType(element, ParadoxLocalisationString::class.java)
                if (str != null) {
                    val s = str.idElement.text
                    if (s.contains(rawText, ignoreCase = true)) {
                        val loc = PsiTreeUtil.getParentOfType(str, ParadoxLocalisationProperty::class.java)
                        if (loc != null) {
                            if (!emitAllFor(loc)) { terminated = true; return@TextOccurenceProcessor false }
                        }
                    }
                }
                true
            }
            helper.processElementsWithWord(occurenceProcessor, gScope, anchorWord, UsageSearchContext.IN_STRINGS, false)
            if (terminated) return
        } else {
            // 无合适锚词：退回到本地化文件枚举 + 轻量匹配（仍然“边找边发”）
            FileTypeIndex.processFiles(ParadoxLocalisationFileType, p@{ vFile ->
                if (terminated) return@p false
                ProgressManager.checkCanceled()
                val psiFile = vFile.toPsiFile(project) ?: return@p true
                val fileText = psiFile.text
                if (!fileText.contains(rawText, ignoreCase = true)) return@p true
                psiFile.accept(object : com.intellij.psi.PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (terminated) return
                        if (element is ParadoxLocalisationString) {
                            val s = element.idElement.text
                            if (s.contains(rawText, ignoreCase = true)) {
                                val loc = PsiTreeUtil.getParentOfType(element, ParadoxLocalisationProperty::class.java)
                                if (loc != null) {
                                    if (!emitAllFor(loc)) { terminated = true; return }
                                }
                            }
                        }
                        super.visitElement(element)
                    }
                })
                true
            }, gScope)
            if (terminated) return
        }

        // 以上流程已在“命中即发射”的路径中完成输出，此处无需再集中输出。
    }
}
