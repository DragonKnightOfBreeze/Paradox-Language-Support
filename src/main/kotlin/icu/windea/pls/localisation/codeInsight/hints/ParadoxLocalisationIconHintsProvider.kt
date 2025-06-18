@file:Suppress("UnstableApiUsage")

package icu.windea.pls.localisation.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.localisation.codeInsight.hints.ParadoxLocalisationIconHintsProvider.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import javax.imageio.*
import javax.swing.*

/**
 * 本地化图标的内嵌提示（显示选用的图标，如果大小合适且存在，只是显示图标而已）。
 */
class ParadoxLocalisationIconHintsProvider : ParadoxLocalisationHintsProvider<Settings>() {
    data class Settings(
        var iconHeightLimit: Int = PlsInternalSettings.iconHeightLimit
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
        if (element is ParadoxLocalisationIcon) {
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
            if(!ParadoxImageManager.canResolve(iconUrl)) return true

            val iconFileUrl = iconUrl.toFileUrl()
            //基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val icon = iconFileUrl.toIconOrNull() ?: return true
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
