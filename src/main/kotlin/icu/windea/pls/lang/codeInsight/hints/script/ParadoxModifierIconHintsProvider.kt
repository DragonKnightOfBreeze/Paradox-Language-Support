package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.base.settings.ChroniclePresentationSettings
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.codeInsight.hints.AsyncIconPresentation
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.resolve.ParadoxImageService
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxConfigManager
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
        val iconResolveResult = ParadoxImageService.resolveByFile(iconFile)
        if (iconResolveResult == null) return

        // 如果无法解析（包括对应文件不存在的情况）就直接跳过
        if (!iconResolveResult.canResolve()) return
        val pointer = iconFile.toPsiFile(context.project)?.createPointer(context.project)
        val maxHeight = ChroniclePresentationSettings.getInstance().state.inlayIconHeightLimit
        sink.addInlinePresentation(element.endOffset, smallInset = true) {
            // 点击可以导航到声明处（图片）
            add(AsyncIconPresentation(context, iconResolveResult.toRenderRequest(), maxHeight, pointer))
        }
    }
}
