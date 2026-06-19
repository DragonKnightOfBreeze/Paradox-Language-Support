package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.ep.resolve.config.CwtInlineScriptUsageConfigContextProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.KEY_OR_STRING_TOKENS
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.propertyValue

/**
 * 提供内联脚本用法的代码补全。
 */
object ParadoxInlineScriptUsageCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(KEY_OR_STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsSettings.getInstance().state.completion.completeInlineScriptUsages) return

        val file = parameters.originalFile
        if (file !is ParadoxScriptFile || selectRootFile(file) == null) return
        val gameType = selectGameType(file) ?: return
        if (!ParadoxPsiFileMatcher.isScriptFile(file, ParadoxPathConstraint.AcceptInlineScriptUsage, injectable = true)) return
        if (!ParadoxInlineScriptManager.isSupported(gameType)) return

        // see: icu.windea.pls.lang.inspections.script.inlineScript.UnsupportedInlineScriptUsageInspection
        val extension = file.name.substringAfterLast('.').lowercase()
        if (extension == "asset") return

        val position = parameters.position
        val element = position.parent.castOrNull<ParadoxScriptStringExpressionElement>() ?: return
        if (element.text.isParameterized()) return

        when (element) {
            is ParadoxScriptString -> {
                if (!element.isBlockMember()) return
            }
            is ParadoxScriptPropertyKey -> {
                // if element is property key, related property value should be a string or clause (after resolving)
                val propertyValue = element.propertyValue
                if (propertyValue != null && propertyValue.resolved().let { it != null && it !is ParadoxScriptString && it !is ParadoxScriptBlock }) return
            }
            else -> return
        }

        // inline script usage cannot be nested directly
        val configContext = ParadoxConfigManager.getConfigContext(element)
        if (configContext != null && configContext.provider is CwtInlineScriptUsageConfigContextProvider) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext).copy(
            expressionOffset = ParadoxExpressionManager.getExpressionOffset(element)
        )

        ParadoxCompletionManager.completeInlineScriptUsage(context, result)
    }
}
