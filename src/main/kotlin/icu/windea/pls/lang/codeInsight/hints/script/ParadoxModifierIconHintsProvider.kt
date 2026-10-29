package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.toFileUrl
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.ui.toIconOrNull
import icu.windea.pls.images.ImageMetadataService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxImageManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression

/**
 * 通过内嵌提示显示修正的渲染后的图标。
 */
@Suppress("UnstableApiUsage")
class ParadoxModifierIconHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.modifierIcon")

    override val name get() = ChronicleBundle.message("script.hints.modifierIcon")
    override val description get() = ChronicleBundle.message("script.hints.modifierIcon.description")
    override val key get() = settingsKey

    override val renderIcon get() = true

    // icu.windea.pls.tool.localisation.ParadoxLocalisationTextInlayRenderer.renderIconTo

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxScriptStringExpressionElement) return
        if (!element.isDataExpression()) return
        val name = element.name
        if (name.isEmpty()) return
        if (name.isParameterized()) return
        val config = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return
        if (config.configExpression.type != CwtDataTypes.Modifier) return

        val paths = ParadoxModifierManager.getModifierIconPaths(name, element)
        val iconFile = paths.firstNotNullOfOrNull { path ->
            val iconSelector = ParadoxFilePathSearch.selector(context.project, element).contextSensitive()
            ParadoxFilePathSearch.searchModifierIcon(path, iconSelector).find()
        } ?: return
        val image = ParadoxImageManager.resolveImageByFile(iconFile)

        // 如果无法解析（包括对应文件不存在的情况）就直接跳过
        if (!ParadoxImageManager.canResolve(image)) return
        // 原始高度是后续缩放和大小限制的依据（无法获取时直接跳过，避免使用可能已被缩放的 icon 高度）
        val iconHeight = ImageMetadataService.getInstance().getMetadata(image.sourcePath)?.height ?: return
        if (iconHeight > context.settings.iconHeightLimit) return
        // 找不到图标的话就直接跳过
        val iconUrl = ParadoxImageManager.resolveUrl(image, context.project) ?: return
        val icon = iconUrl.toFileUrl().toIconOrNull() ?: return

        sink.addInlinePresentation(element.endOffset, smallInset = true) {
            // 点击可以导航到声明处（图片）
            icon(icon, iconFile.toPsiFile(context.project)?.createPointer())
        }
    }
}
