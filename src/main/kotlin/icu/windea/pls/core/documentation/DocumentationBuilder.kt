package icu.windea.pls.core.documentation

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry

/**
 * 用于构建 IDE 文档（Documentation）的简易构建器。
 *
 * 支持通过扩展方法以 `DocumentationMarkup` 片段拼接内容。
 */
class DocumentationBuilder : UserDataHolderBase() {
    /** 文档内容缓冲区。 */
    val content: StringBuilder = StringBuilder()

    /** 追加字符串。 */
    fun append(string: String) = apply { content.append(string) }

    /** 追加任意值（通过 toString）。 */
    fun append(value: Any?) = apply { content.append(value) }

    override fun toString(): String {
        return content.toString()
    }

    /** 用于在构建过程中存取上下文数据的 Key 注册表。 */
    object Keys : KeyRegistry()
}
