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
import icu.windea.pls.lang.index.ParadoxIndexKeys
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
        if (queryParameters.project.isDefault) return
        val scope = queryParameters.selector.scope
        if (SearchScope.isEmptyScope(scope)) return
        val project = queryParameters.project
        val name = queryParameters.name
        val selector = queryParameters.selector
        val file = selector.file ?: return
        val fileInfo = file.fileInfo //NOTE fileInfo can be null here (e.g., injected files)
        if (fileInfo != null && "common/scripted_variables".matchesPath(fileInfo.path.path)) return //skip global scripted variables

        val startOffset = selector.context?.castOrNull<PsiElement>()?.startOffset ?: -1
        val fileScope = GlobalSearchScope.fileScope(project, file)
        doProcessAllElements(name, project, fileScope) p@{ element ->
            if (startOffset >= 0 && element.startOffset >= startOffset) return@p true //skip scripted variables after current position
            consumer.process(element)
        }.let { if (!it) return }

        val processedFiles = mutableSetOf(file)
        processQueryForInlineScripts(queryParameters, file, processedFiles, consumer)
    }

    private fun processQueryForInlineScripts(
        queryParameters: ParadoxLocalScriptedVariableSearch.SearchParameters,
        file: VirtualFile,
        processedFiles: MutableSet<VirtualFile>,
        consumer: Processor<in ParadoxScriptScriptedVariable>
    ): Boolean {
        //see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/93 - inline script files -> invoker file
        //see: https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/151 - inline script arguments -> related inline script file

        ProgressManager.checkCanceled()

        val psiFile = file.toPsiFile(queryParameters.project) ?: return true

        if (PlsVfsManager.isInjectedFile(file)) {
            run {
                //input file is an injected file (from argument value)
                val injectionInfo = ParadoxParameterManager.getParameterValueInjectionInfoFromInjectedFile(psiFile) ?: return@run
                val parameterElement = injectionInfo.parameterElement ?: return@run
                if (parameterElement.parent !is ParadoxScriptStringExpressionElement) return@run //must be argument value, rather than parameter default value
                val inlineScriptExpression = parameterElement.contextKey.removePrefixOrNull("inline_script@")?.orNull() ?: return@run
                return doProcessQueryForInlineScriptFiles(queryParameters, file, inlineScriptExpression, processedFiles, consumer)
            }
            return true
        }

        if (PlsVfsManager.isLightFile(file)) return true //skip for other in-memory files

        //input file is an inline script file
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return true
        return doProcessQueryForInlineScriptUsageFiles(queryParameters, file, inlineScriptExpression, processedFiles, consumer)
    }

    private fun doProcessQueryForInlineScriptFiles(
        queryParameters: ParadoxLocalScriptedVariableSearch.SearchParameters,
        file: VirtualFile,
        inlineScriptExpression: String,
        processedFiles: MutableSet<VirtualFile>,
        consumer: Processor<in ParadoxScriptScriptedVariable>
    ): Boolean {
        val name = queryParameters.name
        val project = queryParameters.project
        val context = queryParameters.selector.context
        ProgressManager.checkCanceled()
        ParadoxInlineScriptManager.processInlineScriptFile(inlineScriptExpression, project, context) p@{ inlineScriptFile ->
            ProgressManager.checkCanceled()
            val fileScope = GlobalSearchScope.fileScope(project, inlineScriptFile.virtualFile)
            doProcessAllElements(name, project, fileScope) p@{ element ->
                //do not skip scripted variables after related parameter, do not check that currently
                consumer.process(element)
            }.let { if (!it) return@p false }
            true
        }

        return doProcessQueryForInlineScriptUsageFiles(queryParameters, file, inlineScriptExpression, processedFiles, consumer)
    }

    private fun doProcessQueryForInlineScriptUsageFiles(
        queryParameters: ParadoxLocalScriptedVariableSearch.SearchParameters,
        file: VirtualFile,
        inlineScriptExpression: String,
        processedFiles: MutableSet<VirtualFile>,
        consumer: Processor<in ParadoxScriptScriptedVariable>
    ): Boolean {
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
        return uFile2StartOffsetMap.process p@{ (uFile, startOffset) ->
            ProgressManager.checkCanceled()
            val fileScope = GlobalSearchScope.fileScope(project, uFile)
            doProcessAllElements(name, project, fileScope) p@{ element ->
                if (startOffset >= 0 && element.startOffset >= startOffset) return@p true //skip scripted variables after current inline script invocation
                consumer.process(element)
            }.let { if (!it) return@p false }

            processQueryForInlineScripts(queryParameters, uFile, processedFiles, consumer) //inline script invocation can be recursive
        }
    }

    private fun doProcessAllElements(name: String?, project: Project, scope: GlobalSearchScope, processor: Processor<ParadoxScriptScriptedVariable>): Boolean {
        if (name == null) {
            return ParadoxIndexKeys.ScriptedVariableName.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            return ParadoxIndexKeys.ScriptedVariableName.processAllElements(name, project, scope) { element -> processor.process(element) }
        }
    }
}
