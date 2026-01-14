package icu.windea.pls.lang.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.registry.Registry
import icu.windea.pls.core.orNull

/**
 * PLS 内部设置。可以通过 Registry 页面进行调整。
 */
@Service
class PlsInternalSettings {
    /**
     * 渲染本地化文本时，使用的文本字体大小（这会影响在快速文档中渲染图标时，使用的最终缩放）。
     */
    val localisationFontSize get() = Registry.intValue("pls.settings.localisationFontSize", 18)

    /**
     * 渲染本地化文本时，视为文本图标的图标的大小限制（这会影响在快速文档中渲染图标时，使用的最终缩放）。
     */
    val localisationTextIconSizeLimit get() = Registry.intValue("pls.settings.localisationTextIconSizeLimit", 36)

    /**
     * 内嵌提示中的本地化文本的默认文本长度限制。
     */
    val textLengthLimitForInlay get() = Registry.intValue("pls.settings.textLengthLimit.for.inlay", 60)

    /**
     * 内嵌提示中的本地化文本的默认图标高度限制。
     */
    val iconHeightLimitForInlay get() = Registry.intValue("pls.settings.iconHeightLimit.for.inlay", 36)

    /**
     * 面包屑导航、导航栏、结构视图中的字符串字面量的文本长度限制。
     */
    val textLengthLimitForPresentation get() = Registry.intValue("pls.settings.textLengthLimit.for.presentation", 60)

    /**
     * 在快速文档中渲染的图片（DDS/TGA）的最大尺寸。如果超出，则会基于此尺寸进行缩放。
     *
     * @see org.intellij.images.fileTypes.ImageDocumentationProvider.MAX_IMAGE_SIZE
     */
    val maxImageSizeForDocumentation get() = Registry.intValue("pls.settings.maxImageSize.for.documentation", 300)

    /**
     * 在提示信息中显示的条目的数量限制（某些提示文本会用到）。
     */
    val itemLimit get() = Registry.intValue("pls.settings.itemLimit", 5)

    /**
     * 定义相对于脚本文件的最大深度（用于优化性能）。从0开始。
     */
    val maxDefinitionDepth get() = Registry.intValue("pls.settings.maxDefinitionDepth", 4)

    /**
     * 默认的封装变量的名字（执行重构与生成操作时会用到）。
     */
    val defaultScriptedVariableName get() = Registry.stringValue("pls.settings.defaultScriptedVariableName").orNull() ?: "var"

    companion object {
        @JvmStatic
        fun getInstance(): PlsInternalSettings = service()
    }
}
