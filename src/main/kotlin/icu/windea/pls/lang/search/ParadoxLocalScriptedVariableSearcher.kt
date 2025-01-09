package icu.windea.pls.lang.search

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 本地封装变量的查询器。
 *
 * 本地封装变量：位于同一脚本文件中，且在当前位置之前的封装变量。兼容需要内联的情况。
 */
class ParadoxLocalScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxLocalScriptedVariableSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxLocalScriptedVariableSearch.SearchParameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
        ProgressManager.checkCanceled()
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val project = queryParameters.project
        val name = queryParameters.name
        val selector = queryParameters.selector
        val file = selector.file ?: return
        val fileInfo = file.fileInfo ?: return
        if ("common/scripted_variables".matchesPath(fileInfo.path.path)) return //skip global scripted variables

        val startOffset = selector.context?.castOrNull<PsiElement>()?.startOffset ?: -1
        val fileScope = GlobalSearchScope.fileScope(project, file)
        doProcessAllElements(name, project, fileScope) p@{ element ->
            if (startOffset >= 0 && element.startOffset >= startOffset) return@p true //skip scripted variables after current position
            consumer.process(element)
        }.let { if (!it) return }

        val processedFiles = mutableSetOf(file)
        processQueryForInlineScriptUsageFiles(queryParameters, file, processedFiles, consumer)
    }

    private fun processQueryForInlineScriptUsageFiles(queryParameters: ParadoxLocalScriptedVariableSearch.SearchParameters, file: VirtualFile, processedFiles: MutableSet<VirtualFile>, consumer: Processor<in ParadoxScriptScriptedVariable>): Boolean {
        //see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/93
        ProgressManager.checkCanceled()
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return true
        val name = queryParameters.name
        val project = queryParameters.project
        val selector = selector(project, file).inlineScriptUsage()
        val uFile2StartOffsetMap = mutableMapOf<VirtualFile, Int>()
        ProgressManager.checkCanceled()
        ParadoxInlineScriptUsageSearch.search(inlineScriptExpression, selector).processQuery p@{ info ->
            ProgressManager.checkCanceled()
            val uFile = info.virtualFile ?: return@p true
            if (!processedFiles.add(uFile)) return@p true
            val startOffset = info.elementOffsets.lastOrNull() ?: return@p true
            uFile2StartOffsetMap.merge(uFile, startOffset) { a, b -> maxOf(a, b) }
            true
        }
        if (uFile2StartOffsetMap.isEmpty()) return true
        return uFile2StartOffsetMap.process p1@{ (uFile, startOffset) ->
            ProgressManager.checkCanceled()
            val fileScope = GlobalSearchScope.fileScope(project, uFile)
            doProcessAllElements(name, project, fileScope) p@{ element ->
                if (startOffset >= 0 && element.startOffset >= startOffset) return@p true //skip scripted variables after current inline script invocation
                consumer.process(element)
            }.let { if (!it) return@p1 false }

            processQueryForInlineScriptUsageFiles(queryParameters, uFile, processedFiles, consumer) //inline script invocation can be recursive
        }
    }

    private fun doProcessAllElements(name: String?, project: Project, scope: GlobalSearchScope, processor: Processor<ParadoxScriptScriptedVariable>): Boolean {
        if (name == null) {
            return ParadoxScriptedVariableNameIndex.KEY.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            return ParadoxScriptedVariableNameIndex.KEY.processAllElements(name, project, scope) { element -> processor.process(element) }
        }
    }
}
