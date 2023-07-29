package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.script.codeInsight.hints.ParadoxModifierIconHintsProvider.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import javax.swing.*

/**
 * 修正的图标的内嵌提示
 */
@Suppress("UnstableApiUsage")
class ParadoxModifierIconHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    companion object {
        private val settingsKey = SettingsKey<Settings>("ParadoxModifierIconHintsSettingsKey")
    }
    
    data class Settings(
        var iconHeightLimit: Int = 32
    )
    
    override val name: String get() = PlsBundle.message("script.hints.modifierIcon")
    override val description: String get() = PlsBundle.message("script.hints.modifierIcon.description")
    override val key: SettingsKey<Settings> get() = settingsKey
    
    override fun createSettings() = Settings()
    
    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                row {
                    label(PlsBundle.message("script.hints.settings.iconHeightLimit")).widthGroup("left")
                        .applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.iconHeightLimit.tooltip") }
                    textField()
                        .bindIntText(settings::iconHeightLimit)
                        .bindIntWhenTextChanged(settings::iconHeightLimit)
                        .errorOnApply(PlsBundle.message("error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
                }
            }
        }
    }
    
    //icu.windea.pls.tool.localisation.ParadoxLocalisationTextInlayRenderer.renderIconTo
    
    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
        if(element !is ParadoxScriptStringExpressionElement) return true
        if(!element.isExpression()) return true
        val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return true
        val type = config.expression.type
        if(type == CwtDataType.Modifier) {
            val name = element.value
            val configGroup = config.info.configGroup
            val project = configGroup.project
            val iconPath = ParadoxModifierHandler.getModifierIconPath(name)
            val iconFile = run {
                val iconSelector = fileSelector(project, element).contextSensitive()
                ParadoxFilePathSearch.search(iconPath, null, iconSelector).find()
            } ?: return true
            val iconUrl = ParadoxImageResolver.resolveUrlByFile(iconFile, defaultToUnknown = false)
            if(iconUrl.isNotEmpty()) {
                //忽略异常
                runCatching {
                    //找不到图标的话就直接跳过
                    val icon = IconLoader.findIcon(iconUrl.toFileUrl()) ?: return true
                    //基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
                    if(icon.iconHeight <= settings.iconHeightLimit) {
                        //点击可以导航到声明处（DDS）
                        val presentation = psiSingleReference(smallScaledIcon(icon)) { iconFile.toPsiFile(project) }
                        val finalPresentation = presentation.toFinalPresentation(this, file.project, smaller = true)
                        val endOffset = element.endOffset
                        sink.addInlineElement(endOffset, true, finalPresentation, false)
                    }
                }
            }
        }
        return true
    }
}