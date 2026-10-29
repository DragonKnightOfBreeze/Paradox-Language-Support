package icu.windea.pls.lang.codeInsight.hints.localisation

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.ui.toIconOrNull
import icu.windea.pls.images.ImageFrameInfo
import icu.windea.pls.images.ImageMetadataService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxLocalisationIconManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

/**
 * 为本地化图标显示渲染后图标。
 *
 * 对应的图标需要大小合适且存在。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationIconHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.localisation.localisationIcon")

    override val name get() = ChronicleBundle.message("localisation.hints.localisationIcon")
    override val description get() = ChronicleBundle.message("localisation.hints.localisationIcon.description")
    override val key get() = settingsKey

    override val renderIcon get() = true

    // icu.windea.pls.tool.localisation.ParadoxLocalisationTextInlayRenderer.renderIconTo

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxLocalisationIcon) return
        val name = element.name ?: return
        if (name.isEmpty()) return
        if (name.isParameterized()) return

        val resolved = element.reference?.resolve()
        val iconFrame = ParadoxLocalisationIconManager.getFrame(element)
        val frameInfo = ImageFrameInfo.of(iconFrame)
        val image = when {
            resolved is ParadoxDefinitionElement -> ParadoxImageManager.resolveImageByDefinition(resolved, frameInfo)
            resolved is PsiFile -> ParadoxImageManager.resolveImageByFile(resolved.virtualFile, context.project, frameInfo)
            else -> null
        }

        // 如果无法解析（包括对应文件不存在的情况）就直接跳过
        if (!ParadoxImageManager.canResolve(image)) return

        // 原始高度是后续缩放和大小限制的依据（无法获取时直接跳过，避免使用可能已被缩放的 icon 高度）
        val originalIconHeight = ImageMetadataService.getInstance().getMetadata(image.sourcePath)?.height ?: return
        if (originalIconHeight > context.settings.iconHeightLimit) return
        // 基于内嵌提示的字体大小缩放图标，直到图标宽度等于字体宽度
        val iconUrl = ParadoxImageManager.resolveUrl(image, context.project) ?: return
        val iconFileUrl = iconUrl.toFileUrl()
        val icon = iconFileUrl.toIconOrNull() ?: return

        sink.addInlinePresentation(element.endOffset, smallInset = true) {
            // 点击可以导航到声明处（定义或图片）
            icon(icon, resolved?.createPointer())
        }
    }
}
