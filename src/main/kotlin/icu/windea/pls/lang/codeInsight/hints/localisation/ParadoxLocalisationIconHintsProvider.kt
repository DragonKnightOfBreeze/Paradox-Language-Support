package icu.windea.pls.lang.codeInsight.hints.localisation

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toIconOrNull
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import javax.imageio.ImageIO

/**
 * 为本地化图标显示渲染后图标。
 *
 * 对应的图标需要大小合适且存在。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationIconHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.localisation.icon")

    override val name: String get() = PlsBundle.message("localisation.hints.localisationIcon")
    override val description: String get() = PlsBundle.message("localisation.hints.localisationIcon.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderIcon: Boolean get() = true

    // icu.windea.pls.tool.localisation.ParadoxLocalisationTextInlayRenderer.renderIconTo

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
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

            // 如果无法解析（包括对应文件不存在的情况）就直接跳过
            if (!ParadoxImageManager.canResolve(iconUrl)) return@r

            val iconFileUrl = iconUrl.toFileUrl()
            // 基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
            val icon = iconFileUrl.toIconOrNull() ?: return@r
            // 这里需要尝试使用图标的原始高度
            val originalIconHeight = runCatchingCancelable { ImageIO.read(iconFileUrl).height }.getOrElse { icon.iconHeight }
            if (originalIconHeight <= settings.iconHeightLimit) {
                // 点击可以导航到声明处（定义或DDS）
                val presentation = psiSingleReference(smallScaledIcon(icon)) { resolved }
                val finalPresentation = presentation.toFinalPresentation(this, project, smaller = true)
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }

        return true
    }
}
