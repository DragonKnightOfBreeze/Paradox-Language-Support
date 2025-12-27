package icu.windea.pls.lang.codeInsight.hints.localisation

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference

/**
 * 通过内嵌提示显示封装变量引用的解析结果（如果可以解析）。默认不启用。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationScriptedVariableReferenceValueHintsProvider : ParadoxLocalisationHintsProvider<NoSettings>() {
    private val settingsKey = SettingsKey<NoSettings>("ParadoxLocalisationScriptedVariableReferenceValueHintsSettingsKey")

    override val name: String get() = PlsBundle.message("localisation.hints.scriptedVariableReferenceValue")
    override val description: String get() = PlsBundle.message("localisation.hints.scriptedVariableReferenceValue.description")
    override val key: SettingsKey<NoSettings> get() = settingsKey

    override fun createSettings() = NoSettings()

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxScriptedVariableReference) return true
        if (element.name.isNullOrEmpty()) return true
        val presentation = doCollect(element) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxScriptedVariableReference): InlayPresentation? {
        val value = element.resolved()?.value ?: return null
        return smallText("=> ${value}".optimized())
    }
}
