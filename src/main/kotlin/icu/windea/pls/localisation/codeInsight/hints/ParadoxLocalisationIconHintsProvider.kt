@file:Suppress("UnstableApiUsage")

package icu.windea.pls.localisation.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.image.*
import icu.windea.pls.localisation.codeInsight.hints.ParadoxLocalisationIconHintsProvider.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 本地化图标的内嵌提示（显示选用的图标，如果大小合适且存在，只是显示图标而已）。
 */
class ParadoxLocalisationIconHintsProvider : ParadoxLocalisationHintsProvider<Settings>() {
    data class Settings(
        var iconHeightLimit: Int = 32
    )
    
    private val settingsKey = SettingsKey<Settings>("ParadoxLocalisationIconHintsSettingsKey")
    
    override val name: String get() = PlsBundle.message("localisation.hints.localisationIcon")
    override val description: String get() = PlsBundle.message("localisation.hints.localisationIcon.description")
    override val key: SettingsKey<Settings> get() = settingsKey
    
    override fun createSettings() = Settings()
    
    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                createIconHeightLimitRow(settings::iconHeightLimit)
            }
        }
    }
    
    //icu.windea.pls.tool.localisation.ParadoxLocalisationTextInlayRenderer.renderIconTo
    
    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
        if(element is ParadoxLocalisationIcon) {
            val resolved = element.reference?.resolve()
            val iconFrame = element.frame
            val frameInfo = FrameInfo.of(iconFrame)
            val iconUrl = when {
                resolved is ParadoxScriptDefinitionElement -> ParadoxImageResolver.resolveUrlByDefinition(resolved, frameInfo)
                resolved is PsiFile -> ParadoxImageResolver.resolveUrlByFile(resolved.virtualFile, frameInfo)
                else -> null
            } ?: return true //找不到图标的话就直接跳过
            
            val icon = iconUrl.toFileUrl().toIconOrNull() ?: return true
            //基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            if(icon.iconHeight <= settings.iconHeightLimit) {
                //点击可以导航到声明处（定义或DDS）
                val presentation = psiSingleReference(smallScaledIcon(icon)) { resolved }
                val finalPresentation = presentation.toFinalPresentation(this, file.project, smaller = true)
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }
        return true
    }
}
