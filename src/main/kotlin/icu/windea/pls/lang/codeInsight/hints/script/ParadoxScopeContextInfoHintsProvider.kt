@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.codeInsight.hints.presentation.SequencePresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.scopeAliasMap
import icu.windea.pls.config.configGroup.systemScopes
import icu.windea.pls.core.findChild
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxScopeContextInfoHintsProvider.Settings
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.toScopeMap
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import javax.swing.JComponent

/**
 * 通过内嵌提示显示定义及其成员的作用域上下文信息。
 *
 * 示例：`this = owner root = country from = ?`
 */
class ParadoxScopeContextInfoHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var showOnlyIfScopeIsChanged: Boolean = true
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxScopeContextInfoHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.scopeContext")
    override val description: String get() = PlsBundle.message("script.hints.scopeContext.description")
    override val key: SettingsKey<Settings> get() = settingsKey

    override fun createSettings() = Settings()

    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                row {
                    checkBox(PlsBundle.message("script.hints.scopeContext.settings.showOnlyIfChanged"))
                        .bindSelected(settings::showOnlyIfScopeIsChanged)
                        .actionListener { _, component -> settings.showOnlyIfScopeIsChanged = component.isSelected }
                }
            }
        }
    }

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
        if (file !is ParadoxScriptFile) return true
        if (element !is ParadoxScriptProperty) return true
        // 要求属性的值是一个块（block），且块的左花括号位于行尾（忽略空白和注释）
        val block = element.propertyValue as? ParadoxScriptBlock ?: return true
        val leftCurlyBrace = block.findChild { it.elementType == ParadoxScriptElementTypes.LEFT_BRACE } ?: return true
        val offset = leftCurlyBrace.endOffset
        val document = editor.document
        val lineEndOffset = document.getLineEndOffset(document.getLineNumber(offset))
        val s = document.immutableCharSequence.subSequence(offset, lineEndOffset).toString().substringBefore("#")
        if (s.isNotBlank()) return true
        if (!ParadoxScopeManager.isScopeContextSupported(element, indirect = true)) return true
        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element)
        if (scopeContext != null) {
            if (settings.showOnlyIfScopeIsChanged && !ParadoxScopeManager.isScopeContextChanged(element, scopeContext)) return true

            val gameType = selectGameType(file) ?: return true
            val configGroup = PlsFacade.getConfigGroup(file.project, gameType)
            val presentation = doCollect(scopeContext, configGroup)
            val finalPresentation = presentation.toFinalPresentation(this, file.project)
            sink.addInlineElement(offset, true, finalPresentation, false) // 不再固定放到行尾，因为如果行尾有注释，需要放到注释之前
        }
        return true
    }

    private fun PresentationFactory.doCollect(scopeInfo: ParadoxScopeContext, configGroup: CwtConfigGroup): InlayPresentation {
        val presentations = mutableListOf<InlayPresentation>()
        var appendSeparator = false
        scopeInfo.toScopeMap(showPrev = false).forEach { (key, value) ->
            if (appendSeparator) {
                presentations.add(smallText(" "))
            } else {
                appendSeparator = true
            }
            presentations.add(systemScopePresentation(key, configGroup))
            presentations.add(smallText(" = "))
            presentations.add(scopeLinkPresentation(value, configGroup))
        }
        return SequencePresentation(presentations)
    }

    private fun PresentationFactory.systemScopePresentation(scope: String, configGroup: CwtConfigGroup): InlayPresentation {
        return psiSingleReference(smallText(scope)) { configGroup.systemScopes[scope]?.pointer?.element }
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
