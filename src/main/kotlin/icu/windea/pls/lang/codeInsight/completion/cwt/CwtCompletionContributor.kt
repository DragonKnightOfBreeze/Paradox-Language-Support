package icu.windea.pls.lang.codeInsight.completion.cwt

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns.psiElement
import icu.windea.pls.cwt.psi.CwtTokenSets
import icu.windea.pls.model.constants.PlsConstants

class CwtCompletionContributor : CompletionContributor() {
    init {
        // 当用户可能正在输入关键字时提示 - 仅限当前文件非规则文件的场合
        val keywordPattern = psiElement()
            .withElementType(CwtTokenSets.STRING_TOKENS)
        extend(CompletionType.BASIC, keywordPattern, CwtKeywordCompletionProvider())

        // 提供规则文件中的代码补全
        val configPattern = psiElement()
            .withElementType(CwtTokenSets.KEY_OR_STRING_TOKENS)
        extend(null, configPattern, CwtConfigCompletionProvider())
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = PlsConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}
