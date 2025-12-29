package icu.windea.pls.lang.codeInsight.hints.localisation

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference

/**
 * 通过内嵌提示显示封装变量引用的解析结果（如果可以解析）。默认不启用。
 */
@Deprecated("Use `ParadoxLocalisationScriptedVariableReferenceValueHintsProviderNew` instead.")
@Suppress("UnstableApiUsage")
class ParadoxLocalisationScriptedVariableReferenceValueHintsProvider: ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.localisation.scriptedVariableReferenceValue")

    override val name: String get() = PlsBundle.message("localisation.hints.scriptedVariableReferenceValue")
    override val description: String get() = PlsBundle.message("localisation.hints.scriptedVariableReferenceValue.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxScriptedVariableReference) return true
        if (element.name.isNullOrEmpty()) return true
        val presentation = collect(element) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.collect(element: ParadoxScriptedVariableReference): InlayPresentation? {
        val value = element.resolved()?.value ?: return null
        val text = "=> ${value}".optimized()
        return smallText(text)
    }
}
