@file:Suppress("unused")

package icu.windea.pls.core.util.io

import icu.windea.pls.core.orNull
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.moveTo

/** 将输入的字符串转为 [Path]。 */
fun String.toPath(): Path = Path.of(this)

/** 尝试将输入的字符串转为 [Path]，如果失败则直接返回 `null`。 */
fun String.toPathOrNull() = runCatching { Path.of(this) }.getOrNull()

/**
 * 尝试将输入的字符串解析为 [Path]，如果失败则直接返回 `null`。
 *
 * 首先去除首尾空白，其次再排除空字符串。
 */
fun String?.parsePath() = this?.trim().orNull()?.toPathOrNull()

/**
 * 将输入的路径转为清理后的 [Path]。
 *
 * 首先转为绝对路径，接着再转为规范化后的路径。
 */
fun Path.clean(): Path = toAbsolutePath().normalize()

/**
 * 将当前路径原子地移动到目标路径。
 *
 * 优先尝试 `ATOMIC_MOVE`（以及 [overwrite] 为 `true` 时的 `REPLACE_EXISTING`）；
 * 当文件系统不支持原子移动时，回退为普通移动。返回目标路径 [target]。
 */
fun Path.moveToAtomically(target: Path, overwrite: Boolean = true): Path {
    val options = if (overwrite) {
        arrayOf(StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    } else {
        arrayOf(StandardCopyOption.ATOMIC_MOVE)
    }
    try {
        moveTo(target, *options)
    } catch (_: AtomicMoveNotSupportedException) {
        if (overwrite) {
            moveTo(target, StandardCopyOption.REPLACE_EXISTING)
        } else {
            moveTo(target)
        }
    }
    return target
}

/**
 * 静默地删除当前路径，忽略删除过程中抛出的异常。
 *
 * 当 [recursive] 为 `true` 时递归删除目录；否则仅删除文件或空目录。
 * 返回清理完成后当前路径是否已不存在（删除失败或路径原本存在但未能删除时返回 `false`）。
 */
fun Path.deleteQuietly(recursive: Boolean = false): Boolean {
    if (!recursive) {
        return runCatching { deleteIfExists() }.getOrDefault(false)
    }
    return runCatching {
        if (Files.exists(this, LinkOption.NOFOLLOW_LINKS)) {
            deleteRecursively()
        }
        !Files.exists(this, LinkOption.NOFOLLOW_LINKS)
    }.getOrDefault(false)
}
