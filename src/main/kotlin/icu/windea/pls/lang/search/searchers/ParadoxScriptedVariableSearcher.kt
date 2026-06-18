package icu.windea.pls.lang.search.searchers

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.startOffset
import com.intellij.util.Processor
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.base.PlsStates
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.index.PlsIndexService
import icu.windea.pls.lang.injection.ParadoxScriptInjectionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxInlineScriptUsageSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.scope.withFilePath
import icu.windea.pls.lang.search.scope.withFileTypes
import icu.windea.pls.lang.search.util.ParadoxSearchContext
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxScriptedVariableType
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 封装变量的查询器。
 *
 * - 本地封装变量：位于同一脚本文件中，且在当前位置之前的封装变量。兼容需要内联的情况（除非内联脚本表达式带有参数，或者需要传递内联脚本的传入参数）。
 * - 全局封装变量：位于特定位置（`common/scripted_variables/**/*.txt`）的脚本文件中的封装变量。
 */
class ParadoxScriptedVariableSearcher : QueryExecutorBase<ParadoxScriptScriptedVariable, ParadoxScriptedVariableSearch.Parameters>() {
    override fun processQuery(queryParameters: ParadoxScriptedVariableSearch.Parameters, consumer: Processor<in ParadoxScriptScriptedVariable>) {
        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsStates.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        val scope = queryParameters.scope.withFileTypes(ParadoxScriptFileType)
        val context = queryParameters.createContext(scope)
        processQuery(context, consumer)
    }

    private fun processQuery(context: Context, consumer: Processor<in ParadoxScriptScriptedVariable>): Boolean {
        if (!context.isValid()) return true
        when (context.type) {
            ParadoxScriptedVariableType.Local -> {
                val file = context.selectorFile ?: return true
                val fileInfo = file.fileInfo // NOTE fileInfo can be null here (e.g., injected files)
                if (fileInfo != null && ParadoxScriptedVariableManager.isGlobalFilePath(fileInfo.path)) return true // skip global scripted variables
                val startOffset = context.selectorContext?.castOrNull<PsiElement>()?.startOffset ?: -1
                val fileScope = GlobalSearchScope.fileScope(context.project, file) // limit to current file
                processScriptVariables(context.copy(scope = fileScope)) p@{ element ->
                    if (startOffset >= 0 && element.startOffset >= startOffset) return@p true // skip scripted variables after current position
                    consumer.process(element)
                }.let { if (!it) return false }

                val processedFiles = mutableSetOf(file)
                processInlineScripts(context, file, processedFiles, consumer).let { if (!it) return false }

                return true
            }
            ParadoxScriptedVariableType.Global -> {
                val globalScope = context.scope.withFilePath("common/scripted_variables", "txt") // limit to global scripted variable files
                return processScriptVariables(context.copy(scope = globalScope)) { element -> consumer.process(element) }
            }
            null -> {
                return processScriptVariables(context) { element -> consumer.process(element) }
            }
        }
    }

    private fun processInlineScripts(context: Context, file: VirtualFile, processedFiles: MutableSet<VirtualFile>, consumer: Processor<in ParadoxScriptScriptedVariable>): Boolean {
        // #93 in inline script files -> invoker file (SUPPORTED)
        // #151 in inline script arguments -> invoked inline script file (SUPPORTED)
        // #153 in passed inline script arguments -> outer invoked inline script file (UNSUPPORTED, but unresolved references can be suppressed)

        ProgressManager.checkCanceled()
        val psiFile = file.toPsiFile(context.project) ?: return true

        if (VirtualFileService.isInjectedFile(file)) {
            run {
                // input file is an injected file (from argument value)
                val injectionInfo = ParadoxScriptInjectionManager.getParameterValueInjectionInfoFromInjectedFile(psiFile) ?: return@run
                val parameterElement = injectionInfo.parameterElement ?: return@run
                if (parameterElement.parent !is ParadoxScriptStringExpressionElement) return@run // must be an argument value, rather than a parameter default value
                val inlineScriptExpression = parameterElement.contextKey.removePrefixOrNull("inline_script@")?.orNull() ?: return@run
                return processInlineScriptFiles(context, file, processedFiles, inlineScriptExpression, consumer)
            }
            return true
        }

        if (VirtualFileService.isLightFile(file)) {
            // skip for other in-memory files
            return true
        }

        // input file is an inline script file
        val inlineScriptExpression = ParadoxInlineScriptManager.getInlineScriptExpression(file) ?: return true
        return processInlineScriptUsageFiles(context, file, processedFiles, inlineScriptExpression, consumer)
    }

    private fun processInlineScriptFiles(context: Context, file: VirtualFile, processedFiles: MutableSet<VirtualFile>, inlineScriptExpression: String, consumer: Processor<in ParadoxScriptScriptedVariable>): Boolean {
        if (inlineScriptExpression.isParameterized()) return true // skip if is inlineScriptExpression parameterized
        ProgressManager.checkCanceled()

        ParadoxInlineScriptManager.processInlineScriptFile(inlineScriptExpression, context.project, context.selectorContext) p@{ inlineScriptFile ->
            ProgressManager.checkCanceled()
            val fileScope = GlobalSearchScope.fileScope(context.project, inlineScriptFile.virtualFile) // limit to current file
            processScriptVariables(context.copy(scope = fileScope)) p@{ element ->
                // do not skip scripted variables after related parameter, do not check that currently
                consumer.process(element)
            }.let { if (!it) return@p false }
            true
        }

        return processInlineScriptUsageFiles(context, file, processedFiles, inlineScriptExpression, consumer)
    }

    private fun processInlineScriptUsageFiles(context: Context, file: VirtualFile, processedFiles: MutableSet<VirtualFile>, inlineScriptExpression: String, consumer: Processor<in ParadoxScriptScriptedVariable>): Boolean {
        if (inlineScriptExpression.isParameterized()) return true // skip if inlineScriptExpression is parameterized
        ProgressManager.checkCanceled()

        val uFile2StartOffsetMap = mutableMapOf<VirtualFile, Int>()
        val selector = ParadoxInlineScriptUsageSearch.selector(context.project, file)
        ParadoxInlineScriptUsageSearch.search(inlineScriptExpression, selector).process p@{ p ->
            ProgressManager.checkCanceled()
            val uFile = p.containingFile?.virtualFile ?: return@p true
            if (!processedFiles.add(uFile)) return@p true
            val startOffset = p.startOffset
            uFile2StartOffsetMap.merge(uFile, startOffset) { a, b -> maxOf(a, b) }
            true
        }
        if (uFile2StartOffsetMap.isEmpty()) return true
        return uFile2StartOffsetMap.process p@{ (uFile, startOffset) ->
            ProgressManager.checkCanceled()
            val fileScope = GlobalSearchScope.fileScope(context.project, uFile) // limit to current file
            processScriptVariables(context.copy(scope = fileScope)) p@{ element ->
                if (startOffset >= 0 && element.startOffset >= startOffset) return@p true // skip scripted variables after current inline script usage
                consumer.process(element)
            }.let { if (!it) return@p false }

            processInlineScripts(context, uFile, processedFiles, consumer) // inline script usage can be recursive
        }
    }

    private fun processScriptVariables(context: Context, processor: Processor<ParadoxScriptScriptedVariable>): Boolean {
        val indexKey = PlsIndexKeys.ScriptedVariableName
        return if (context.name == null) {
            PlsIndexService.processElementsByKeys(indexKey, context.project, context.scope) { _, element -> processor.process(element) }
        } else {
            PlsIndexService.processElements(indexKey, context.name, context.project, context.scope) { element -> processor.process(element) }
        }
    }

    private fun ParadoxScriptedVariableSearch.Parameters.createContext(scope: GlobalSearchScope = this.scope): Context {
        return Context(name, type, selector.file, selector.context, gameType, project, scope)
    }

    private data class Context(
        val name: String?,
        val type: ParadoxScriptedVariableType?,
        val selectorFile: VirtualFile?,
        val selectorContext: Any?,
        override val gameType: ParadoxGameType?,
        override val project: Project,
        override val scope: GlobalSearchScope,
    ) : ParadoxSearchContext
}
