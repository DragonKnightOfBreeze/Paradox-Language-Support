package icu.windea.pls.lang.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.util.registry.*
import icu.windea.pls.core.*

/**
 * PLS的内部设置。可以通过 Registry 页面进行调整。
 */
@Service(Service.Level.APP)
class PlsInternalSettings {
    /**
     * 是否需要在IDE启动后首次打开某个项目时，刷新此项目已打开的脚本文件和本地化文件。
     */
    val refreshOnProjectStartup get() = Registry.`is`("pls.settings.refreshOnProjectStartup", true)

    /**
     * 渲染本地化文本时，使用的文本字体大小（这会影响在快速文档中渲染图标时，使用的最终缩放）。
     */
    val locFontSize get() = Registry.intValue("pls.settings.locFontSize", 18)

    /**
     * 渲染本地化文本时，视为文本图标的图标的大小限制（这会影响在快速文档中渲染图标时，使用的最终缩放）。
     */
    val locTextIconSizeLimit get() = Registry.intValue("pls.settings.locTextIconSizeLimit", 36)

    /**
     * 内嵌提示中的本地化文本的默认文本长度限制。
     */
    val textLengthLimit get() = Registry.intValue("pls.settings.textLengthLimit", 36)

    /**
     * 内嵌提示中的本地化文本的默认图标高度限制。
     */
    val iconHeightLimit get() = Registry.intValue("pls.settings.iconHeightLimit", 36)

    /**
     * 默认的封装变量的名字（执行重构与生成操作时会用到）。
     */
    val defaultScriptedVariableName get() = Registry.stringValue("pls.settings.defaultScriptedVariableName").orNull() ?: "var"

    /**
     * 定义相对于脚本文件的最大深度（用于优化性能）。
     */
    val maxDefinitionDepth get() = Registry.intValue("pls.settings.maxDefinitionDepth", 4)

    /**
     * 面包屑导航、导航栏、结构视图中的字符串字面量的文本长度限制。
     */
    val presentableTextLengthLimit get() = Registry.intValue("pls.settings.presentableTextLengthLimit", 36)

    /**
     * 在提示信息中显示的条目的数量限制（某些提示文本会用到）。
     */
    val itemLimit get() = Registry.intValue("pls.settings.itemLimit", 5)

    /**
     * 在快速文档中渲染的图片（DDS/TGA）的最大尺寸。如果超出，则会基于此尺寸进行缩放。
     *
     * @see org.intellij.images.fileTypes.ImageDocumentationProvider.MAX_IMAGE_SIZE
     */
    val maxImageSizeInDocumentation get() = Registry.intValue("pls.settings.maxImageSizeInDocumentation", 300)
}
