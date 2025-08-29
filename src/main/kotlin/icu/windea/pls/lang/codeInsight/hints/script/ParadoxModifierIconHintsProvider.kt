@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toIconOrNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxModifierIconHintsProvider.Settings
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression
import javax.imageio.ImageIO
import javax.swing.JComponent

/**
 * 通过内嵌提示显示修正的渲染后的图标。
 */
class ParadoxModifierIconHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimit
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxModifierIconHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.modifierIcon")
    override val description: String get() = PlsBundle.message("script.hints.modifierIcon.description")
    override val key: SettingsKey<Settings> get() = settingsKey

    override val renderIcon: Boolean get() = true

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
        val name = element.name
        if (name.isEmpty()) return true
        if (name.isParameterized()) return true
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return true
        if (config.configExpression.type != CwtDataTypes.Modifier) return true
        val configGroup = config.configGroup
        val project = configGroup.project

        runCatchingCancelable r@{
            val paths = ParadoxModifierManager.getModifierIconPaths(name, element)
            val iconFile = paths.firstNotNullOfOrNull { path ->
                val iconSelector = selector(project, element).file().contextSensitive()
                ParadoxFilePathSearch.searchIcon(path, iconSelector).find()
            }
            val iconUrl = when {
                iconFile != null -> ParadoxImageManager.resolveUrlByFile(iconFile, project)
                else -> null
            }

            //如果无法解析（包括对应文件不存在的情况）就直接跳过
            if (!ParadoxImageManager.canResolve(iconUrl)) return@r

            //基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val iconFileUrl = iconUrl.toFileUrl()
            val icon = iconFileUrl.toIconOrNull() ?: return@r
            //这里需要尝试使用图标的原始高度
            val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrElse { icon.iconHeight }
            if (originalIconHeight <= settings.iconHeightLimit) {
                //点击可以导航到声明处（DDS）
                val presentation = psiSingleReference(smallScaledIcon(icon)) { iconFile?.toPsiFile(project) }
                val finalPresentation = presentation.toFinalPresentation(this, project, smaller = true)
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }
        return true
    }
}
