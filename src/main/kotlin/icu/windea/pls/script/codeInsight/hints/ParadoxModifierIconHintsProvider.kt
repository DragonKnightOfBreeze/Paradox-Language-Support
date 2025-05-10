@file:Suppress("UnstableApiUsage")

package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.image.*
import icu.windea.pls.script.codeInsight.hints.ParadoxModifierIconHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 修正的图标的内嵌提示。
 */
class ParadoxModifierIconHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var iconHeightLimit: Int = 36
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxModifierIconHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.modifierIcon")
    override val description: String get() = PlsBundle.message("script.hints.modifierIcon.description")
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
        if (element !is ParadoxScriptStringExpressionElement) return true
        if (!element.isExpression()) return true
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return true
        val type = config.expression.type
        if (type == CwtDataTypes.Modifier) {
            val name = element.value
            if (name.isEmpty()) return true
            if (name.isParameterized()) return true
            val configGroup = config.configGroup
            val project = configGroup.project
            val paths = ParadoxModifierManager.getModifierIconPaths(name, element)
            val iconFile = paths.firstNotNullOfOrNull { path ->
                val iconSelector = selector(project, element).file().contextSensitive()
                ParadoxFilePathSearch.searchIcon(path, iconSelector).find()
            } ?: return true
            val iconUrl = ParadoxImageResolver.resolveUrlByFile(iconFile)
            if (iconUrl == null) return true

            //基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val icon = iconUrl.toFileUrl().toIconOrNull() ?: return true
            //这里需要先尝试获取原始高度
            if (icon.originalHeight <= settings.iconHeightLimit) {
                //点击可以导航到声明处（DDS）
                val presentation = psiSingleReference(smallScaledIcon(icon)) { iconFile.toPsiFile(project) }
                val finalPresentation = presentation.toFinalPresentation(this, file.project, smaller = true)
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }
        return true
    }
}
