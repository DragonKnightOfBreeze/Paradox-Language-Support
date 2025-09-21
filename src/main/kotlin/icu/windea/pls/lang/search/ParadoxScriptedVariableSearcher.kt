package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.startOffset
import com.intellij.util.Processor
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.orNull
import icu.windea.pls.core.processAllElements
import icu.windea.pls.core.processAllElementsByKeys
import icu.windea.pls.core.processQuery
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.scope.withFilePath
import icu.windea.pls.lang.search.selector.inlineScriptUsage
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.PlsVfsManager
import icu.windea.pls.model.ParadoxScriptedVariableType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 封装变量的查询器。
 *
 * - 本地封装变量：位于同一脚本文件中，且在当前位置之前的封装变量。兼容需要内联的情况（除非内联脚本表达式带有参数，或者需要传递内联脚本的传入参数）。
 * - 全局封装变量：位于特定位置（`common/scripted_variables/**/*.txt`）的脚本文件中的封装变量。
 */
class ParadoxScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxScriptedVariableSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxScriptedVariableSearch.SearchParameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsCoreManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        when (queryParameters.type) {
            ParadoxScriptedVariableType.Local -> {
                val scope = queryParameters.selector.scope
                if (SearchScope.isEmptyScope(scope)) return
                val file = queryParameters.selector.file ?: return
                val fileInfo = file.fileInfo // NOTE fileInfo can be null here (e.g., injected files)
                if (fileInfo != null && ParadoxScriptedVariableManager.isGlobalFilePath(fileInfo.path)) return // skip global scripted variables
                val startOffset = queryParameters.selector.context?.castOrNull<PsiElement>()?.startOffset ?: -1
                val fileScope = GlobalSearchScope.fileScope(queryParameters.project, file)
                doProcessAllElements(queryParameters.name, queryParameters.project, fileScope) p@{ element ->
                    if (startOffset >= 0 && element.startOffset >= startOffset) return@p true // skip scripted variables after current position
                    consumer.process(element)
                }.let { if (!it) return }

                val processedFiles = mutableSetOf(file)
                doProcessQueryForInlineScripts(queryParameters, file, processedFiles, consumer)
            }
            ParadoxScriptedVariableType.Global -> {
                val scope = queryParameters.selector.scope.withFilePath("common/scripted_variables", "txt") // limit to global scripted variables
                if (SearchScope.isEmptyScope(scope)) return
                doProcessAllElements(queryParameters.name, queryParameters.project, scope) { element -> consumer.process(element) }
            }
            null -> {
                val scope = queryParameters.selector.scope
                if (SearchScope.isEmptyScope(scope)) return
                doProcessAllElements(queryParameters.name, queryParameters.project, scope) { element -> consumer.process(element) }
            }
        }
    }

    private fun doProcessQueryForInlineScripts(
        queryParameters: ParadoxScriptedVariableSearch.SearchParameters,
        file: VirtualFile,
        processedFiles: MutableSet<VirtualFile>,
        consumer: Processor<in ParadoxScriptScriptedVariable>
    ): Boolean {
        // #93 in inline script files -> invoker file (SUPPORTED)
        // #151 in inline script arguments -> invoked inline script file (SUPPORTED)
        // #153 in passed inline script arguments -> outer invoked inline script file (UNSUPPORTED, but unresolved references can be suppressed)

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
        queryParameters: ParadoxScriptedVariableSearch.SearchParameters,
        file: VirtualFile,
        inlineScriptExpression: String,
        processedFiles: MutableSet<VirtualFile>,
        consumer: Processor<in ParadoxScriptScriptedVariable>
    ): Boolean {
        if (inlineScriptExpression.isParameterized()) return true // skip if is inlineScriptExpression parameterized
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
        queryParameters: ParadoxScriptedVariableSearch.SearchParameters,
        file: VirtualFile,
        inlineScriptExpression: String,
        processedFiles: MutableSet<VirtualFile>,
        consumer: Processor<in ParadoxScriptScriptedVariable>
    ): Boolean {
        if (inlineScriptExpression.isParameterized()) return true // skip if is inlineScriptExpression parameterized
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

            doProcessQueryForInlineScripts(queryParameters, uFile, processedFiles, consumer) //inline script invocation can be recursive
        }
    }

    private fun doProcessAllElements(
        name: String?,
        project: Project,
        scope: GlobalSearchScope,
        processor: Processor<ParadoxScriptScriptedVariable>
    ): Boolean {
        val indexKey = ParadoxIndexKeys.ScriptedVariableName
        return if (name == null) {
            indexKey.processAllElementsByKeys(project, scope) { _, element -> processor.process(element) }
        } else {
            indexKey.processAllElements(name, project, scope) { element -> processor.process(element) }
        }
    }
}
