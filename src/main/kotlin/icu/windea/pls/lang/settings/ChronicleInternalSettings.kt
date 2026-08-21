package icu.windea.pls.lang.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.registry.Registry
import icu.windea.pls.core.orNull

/**
 * 插件的全局内部设置。可以通过 Registry 页面进行调整。
 */
@Service
class ChronicleInternalSettings {
    /**
     * 用于展示的文本的长度限制。适用于字符串字面量等。如果超出，则会被截断。
     */
    val presentableTextLimit get() = Registry.intValue("chronicle.settings.presentableTextLimit", 30).coerceAtLeast(0)

    /**
     * 在快速文档中渲染的图片的最大尺寸。如果超出，则会基于此尺寸进行缩放。
     *
     * @see org.intellij.images.fileTypes.ImageDocumentationProvider.MAX_IMAGE_SIZE
     */
    val maxImageSizeForQuickDoc get() = Registry.intValue("chronicle.settings.maxImageSizeForQuickDoc", 300).coerceAtLeast(0)

    /**
     * 渲染本地化文本时，使用的文本字体大小。这会影响在快速文档中渲染图标时，使用的最终缩放。
     */
    val localisationTextFontSizeForQuickDoc get() = Registry.intValue("chronicle.settings.localisationTextFontSizeForQuickDoc", 18).coerceAtLeast(0)

    /**
     * 渲染本地化文本时，文本图标的高度限制。这会影响在快速文档中渲染图标时，使用的最终缩放。
     */
    val textIconSizeLimitForQuickDoc get() = Registry.intValue("chronicle.settings.textIconSizeLimitForQuickDoc", 36).coerceAtLeast(0)

    /**
     * 内嵌提示中的本地化文本的默认长度限制。如果超出，则会被截断。
     */
    val localisationLengthLimitForInlay get() = Registry.intValue("chronicle.settings.localisationLengthLimitForInlay", 60).coerceAtLeast(0)

    /**
     * 内嵌提示中的文本图标的默认高度限制。如果超出，则不会被渲染。
     */
    val iconHeightLimitForInlay get() = Registry.intValue("chronicle.settings.iconHeightLimitForInlay", 36).coerceAtLeast(0)

    /**
     * 默认的封装变量的名字（执行重构与生成操作时会用到）。
     */
    val defaultScriptedVariableName get() = Registry.stringValue("chronicle.settings.defaultScriptedVariableName").orNull() ?: "var"

    companion object {
        @JvmStatic
        fun getInstance(): ChronicleInternalSettings = service()
    }
}
