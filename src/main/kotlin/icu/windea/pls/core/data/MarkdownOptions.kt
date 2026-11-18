package icu.windea.pls.core.data

import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor

/**
 * Markdown 转 HTML 选项。
 *
 * @property flavour 使用的 Markdown 方言解析器（[MarkdownFlavourDescriptor]）。
 * @property skipUtf8Bom 是否跳过 Markdown 文本开头的 UTF-8 BOM 字符。
 */
data class MarkdownOptions(
    val flavour: MarkdownFlavourDescriptor = GFMFlavourDescriptor(),
    val skipUtf8Bom: Boolean = true,
) {
    companion object {
        @JvmField val DEFAULT = MarkdownOptions()

        /** GFM 风格（GitHub Flavored Markdown）。 */
        @JvmField val GFM = MarkdownOptions()

        /** CommonMark 风格。 */
        @JvmField val COMMON_MARK = MarkdownOptions(CommonMarkFlavourDescriptor())
    }
}
