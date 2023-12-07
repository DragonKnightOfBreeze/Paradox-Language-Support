package icu.windea.pls.localisation.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.codeInsight.hints.ParadoxLocalisationReferenceHintsProvider.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.util.localisation.*
import javax.swing.*

/**
 * 本地化引用的内嵌提示（对应的本地化的渲染后文本，如果过长则会截断）。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationReferenceHintsProvider : ParadoxLocalisationHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = 30,
        var iconHeightLimit: Int = 32
    )
    
    private val settingsKey = SettingsKey<Settings>("ParadoxLocalisationReferenceHintsSettingsKey")
    
    override val name: String get() = PlsBundle.message("localisation.hints.localisationReference")
    override val description: String get() = PlsBundle.message("localisation.hints.localisationReference.description")
    override val key: SettingsKey<Settings> get() = settingsKey
    
    override fun createSettings() = Settings()
    
    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                row {
                    label(PlsBundle.message("localisation.hints.settings.textLengthLimit")).widthGroup("left")
                        .applyToComponent { toolTipText = PlsBundle.message("localisation.hints.settings.textLengthLimit.tooltip") }
                    textField()
                        .bindIntText(settings::textLengthLimit)
                        .bindIntWhenTextChanged(settings::textLengthLimit)
                        .errorOnApply(PlsBundle.message("error.shouldBePositiveOrZero")) { (it.text.toIntOrNull() ?: 0) < 0 }
                }
                row {
                    label(PlsBundle.message("localisation.hints.settings.iconHeightLimit")).widthGroup("left")
                        .applyToComponent { toolTipText = PlsBundle.message("localisation.hints.settings.iconHeightLimit.tooltip") }
                    textField()
                        .bindIntText(settings::iconHeightLimit)
                        .bindIntWhenTextChanged(settings::iconHeightLimit)
                        .errorOnApply(PlsBundle.message("error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
                }
            }
        }
    }
    
    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
        if(element !is ParadoxLocalisationPropertyReference) return true
        if(isIgnored(element)) return true
        val resolved = element.reference?.resolveLocalisation() //直接解析为本地化以优化性能
        if(resolved is ParadoxLocalisationProperty) {
            val localisationInfo = resolved.localisationInfo
            if(localisationInfo != null) {
                val presentation = doCollect(resolved, editor, settings)
                val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }
        return true
    }
    
    private fun isIgnored(element: ParadoxLocalisationPropertyReference): Boolean {
        return element.firstChild.siblings().any { it is ParadoxLocalisationCommand || it is ParadoxLocalisationScriptedVariableReference }
    }
    
    private fun PresentationFactory.doCollect(localisation: ParadoxLocalisationProperty, editor: Editor, settings: Settings): InlayPresentation? {
        return ParadoxLocalisationTextInlayRenderer.render(localisation, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
    }
}

