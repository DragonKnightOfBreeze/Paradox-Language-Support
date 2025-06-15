package icu.windea.pls.model.constants

object PlsSettingConstants {
    /** 是否需要在IDE启动后首次打开某个项目时，刷新此项目已打开的脚本文件和本地化文件 */
    const val refreshOnProjectStartup = true

    /** 将DDS图片转换为PNG图片时，DDS图片大小达到多少时，异步进行并显示可取消的进度条 */
    const val largeDddSize = 512 * 1024 //512KB

    /** 渲染本地化文本时，使用的文本字体大小（这会影响在快速文档中渲染图标时，使用的最终缩放） */
    const val locFontSize = 18
    /** 渲染本地化文本时，视为文本图标的图标的大小限制（这会影响在快速文档中渲染图标时，使用的最终缩放） */
    const val locTextIconSizeLimit = 36

    /** 内嵌提示中的本地化文本的默认文本长度限制 */
    const val textLengthLimit = 30
    /** 内嵌提示中的本地化文本的默认图标高度限制 */
    const val iconHeightLimit = locTextIconSizeLimit

    /** 默认的封装变量的名字（执行重构与生成操作时会用到） */
    const val defaultScriptedVariableName = "var"
    /** 定义相对于脚本文件的最大深度（用于优化性能） */
    const val maxDefinitionDepth = 4
    /** 在提示信息中显示的条目的数量限制（某些提示文本会用到） */
    const val itemLimit = 5
}
