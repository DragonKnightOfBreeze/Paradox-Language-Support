@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.localisation

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
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toIconOrNull
import icu.windea.pls.lang.codeInsight.hints.localisation.ParadoxLocalisationIconHintsProvider.Settings
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.model.ImageFrameInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import javax.imageio.ImageIO
import javax.swing.JComponent

/**
 * 为本地化图标显示渲染后图标。
 *
 * 对应的图标需要大小合适且存在。
 */
class ParadoxLocalisationIconHintsProvider : ParadoxLocalisationHintsProvider<Settings>() {
    data class Settings(
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimit
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxLocalisationIconHintsSettingsKey")

    override val name: String get() = PlsBundle.message("localisation.hints.localisationIcon")
    override val description: String get() = PlsBundle.message("localisation.hints.localisationIcon.description")
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
        if (element !is ParadoxLocalisationIcon) return true
        val name = element.name ?: return true
        if (name.isEmpty()) return true
        if (name.isParameterized()) return true

        runCatchingCancelable r@{
            val resolved = element.reference?.resolve()
            val iconFrame = element.frame
            val frameInfo = ImageFrameInfo.of(iconFrame)
            val project = file.project
            val iconUrl = when {
                resolved is ParadoxScriptDefinitionElement -> ParadoxImageManager.resolveUrlByDefinition(resolved, frameInfo)
                resolved is PsiFile -> ParadoxImageManager.resolveUrlByFile(resolved.virtualFile, project, frameInfo)
                else -> null
            }

            //如果无法解析（包括对应文件不存在的情况）就直接跳过
            if (!ParadoxImageManager.canResolve(iconUrl)) return@r

            val iconFileUrl = iconUrl.toFileUrl()
            //基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val icon = iconFileUrl.toIconOrNull() ?: return@r
            //这里需要尝试使用图标的原始高度
            val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrElse { icon.iconHeight }
            if (originalIconHeight <= settings.iconHeightLimit) {
                //点击可以导航到声明处（定义或DDS）
                val presentation = psiSingleReference(smallScaledIcon(icon)) { resolved }
                val finalPresentation = presentation.toFinalPresentation(this, project, smaller = true)
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }

        return true
    }
}
