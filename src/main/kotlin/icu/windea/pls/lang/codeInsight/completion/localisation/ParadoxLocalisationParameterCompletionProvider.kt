package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.LimitedCompletionProcessor
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionFactory
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.addToResult
import icu.windea.pls.lang.resolve.ParadoxLocalisationParameterService
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxLocalisationParameterManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 提供本地化参数的名字的代码补全。
 */
class ParadoxLocalisationParameterCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement(PARAMETER_TOKEN)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationParameter>() ?: return

        val file = parameters.originalFile.castOrNull<ParadoxLocalisationFile>() ?: return
        val type = ParadoxLocalisationType.resolve(file) ?: return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)
        val project = context.project

        // 提示本地化参数
        val property = parameters.position.parentOfType<ParadoxLocalisationProperty>()
        if (property != null) {
            val parameterNames = ParadoxLocalisationParameterManager.getParameterNames(property)
            if (parameterNames.isNotEmpty()) {
                for (parameterName in parameterNames) {
                    val parameter = ParadoxLocalisationParameterService.resolveParameter(property, parameterName) ?: continue
                    ParadoxCompletionFactory.forLocalisationParameter(parameter).addToResult(context, result)
                }
            }
        }

        // 本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnAnyPrefixChange()

        // 提示本地化
        val selector = ParadoxLocalisationSearch.selector(project, file)
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> { localisation ->
            ParadoxCompletionFactory.forLocalisationName(localisation).addToResult(context, result)
        }
        // 保证索引在此 readAction 中可用
        runSmartReadAction(project, inSmartMode = true) {
            ParadoxLocalisationSearch.processVariants(type, result.prefixMatcher, selector, processor)
        }
    }
}
