@file:Suppress("unused")

package icu.windea.pls.core.util.io

import icu.windea.pls.core.orNull
import java.nio.file.Path

/** 将输入的字符串转为 [Path]。 */
fun String.toPath(): Path = Path.of(this)

/** 尝试将输入的字符串转为 [Path]，如果失败则直接返回 `null`。 */
fun String.toPathOrNull() = runCatching { Path.of(this) }.getOrNull()

/**
 * 尝试将输入的字符串解析为 [Path]，如果失败则直接返回 `null`。
 *
 * 首先去除首尾空白，其次再排除空字符串。
 */
fun String?.parsePath() = this?.trim()?.orNull()?.toPathOrNull()

/**
 * 将输入的路径转为清理后的 [Path]。
 *
 * 首先转为绝对路径，接着再转为规范化后的路径。
 */
fun Path.clean(): Path = toAbsolutePath().normalize()
