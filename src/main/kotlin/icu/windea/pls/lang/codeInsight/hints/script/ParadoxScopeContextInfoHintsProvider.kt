package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.codeInsight.hints.mergePresentations
import icu.windea.pls.core.findChild
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.toScopeMap
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 通过内嵌提示显示定义及其成员的作用域上下文信息。
 *
 * 示例：`this = owner root = country from = ?`
 */
@Suppress("UnstableApiUsage")
class ParadoxScopeContextInfoHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.scopeContextInfo")

    override val name: String get() = PlsBundle.message("script.hints.scopeContext")
    override val description: String get() = PlsBundle.message("script.hints.scopeContext.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val showScopeContextInfo: Boolean get() = true

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean {
        if (context.file !is ParadoxScriptFile) return true
        if (element !is ParadoxScriptProperty) return true
        // 要求属性的值是一个块（block），且块的左花括号位于行尾（忽略空白和注释）
        val block = element.propertyValue as? ParadoxScriptBlock ?: return true
        val leftCurlyBrace = block.findChild { it.elementType == ParadoxScriptElementTypes.LEFT_BRACE } ?: return true
        val offset = leftCurlyBrace.endOffset
        val document = context.editor.document
        val lineEndOffset = document.getLineEndOffset(document.getLineNumber(offset))
        val s = document.immutableCharSequence.subSequence(offset, lineEndOffset).toString().substringBefore("#")
        if (s.isNotBlank()) return true
        if (!ParadoxScopeManager.isScopeContextSupported(element, indirect = true)) return true
        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element)
        if (scopeContext != null) {
            if (context.settings.showOnlyIfScopeIsChanged && !ParadoxScopeManager.isScopeContextChanged(element, scopeContext)) return true

            val gameType = selectGameType(context.file) ?: return true
            val configGroup = PlsFacade.getConfigGroup(context.file.project, gameType)
            val presentation = collect(scopeContext, configGroup)
            val finalPresentation = presentation?.toFinalPresentation() ?: return true
            sink.addInlineElement(offset, true, finalPresentation, false) // 不再固定放到行尾，因为如果行尾有注释，需要放到注释之前
        }
        return true
    }

    context(context: ParadoxHintsContext)
    private fun collect(scopeInfo: ParadoxScopeContext, configGroup: CwtConfigGroup): InlayPresentation? {
        val presentations = mutableListOf<InlayPresentation>()
        var appendSeparator = false
        scopeInfo.toScopeMap(showPrev = false).forEach { (key, value) ->
            if (appendSeparator) {
                presentations.add(context.factory.smallText(" "))
            } else {
                appendSeparator = true
            }
            presentations.add(context.factory.systemScopePresentation(key, configGroup))
            presentations.add(context.factory.smallText(" = "))
            presentations.add(context.factory.scopeLinkPresentation(value, configGroup))
        }
        return presentations.mergePresentations()
    }

    private fun PresentationFactory.systemScopePresentation(scope: String, configGroup: CwtConfigGroup): InlayPresentation {
        return psiSingleReference(smallText(scope.optimized())) { getSystemScopeElement(configGroup, scope) }
    }

    private fun getSystemScopeElement(configGroup: CwtConfigGroup, scope: String): CwtProperty? {
        return configGroup.systemScopes[scope]?.pointer?.element
    }

    private fun PresentationFactory.scopeLinkPresentation(scope: ParadoxScope, configGroup: CwtConfigGroup): InlayPresentation {
        return when {
            ParadoxScopeManager.isUnsureScopeId(scope.id) -> smallText(scope.id)
            else -> psiSingleReference(smallText(scope.id)) { getScopeElement(configGroup, scope) }
        }
    }

    private fun getScopeElement(configGroup: CwtConfigGroup, scope: ParadoxScope): CwtProperty? {
        return configGroup.scopeAliasMap[scope.id]?.pointer?.element
    }
}
