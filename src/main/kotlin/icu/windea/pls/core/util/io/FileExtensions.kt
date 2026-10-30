@file:Suppress("unused")

package icu.windea.pls.core.util.io

import java.io.File

/** 将输入的字符串转为 [File]。 */
fun String.toFile() = File(this)

/** 尝试将输入的字符串转为 [File]，如果失败则直接返回 `null`。 */
fun String.toFileOrNull() = runCatching { File(this) }.getOrNull()

