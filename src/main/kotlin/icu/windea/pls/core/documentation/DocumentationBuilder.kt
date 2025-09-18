package icu.windea.pls.core.documentation

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry

/**
 * 文档构建器，用于按 IntelliJ 的 [com.intellij.lang.documentation.DocumentationMarkup] 约定拼接说明文本。
 *
 * @property content 文本缓冲区。
 */
class DocumentationBuilder : UserDataHolderBase() {
    val content: StringBuilder = StringBuilder()

    /** 追加字符串 [string] 后返回自身（便于链式调用）。*/
    fun append(string: String) = apply { content.append(string) }

    /** 追加任意值 [value] 的字符串表示后返回自身。*/
    fun append(value: Any?) = apply { content.append(value) }

    override fun toString(): String {
        return content.toString()
    }

    /** 用户数据键注册表，用于在构建期间挂载上下文数据。*/
    object Keys : KeyRegistry()
}
