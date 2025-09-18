package icu.windea.pls.core.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 将上游 [Flow] 按固定大小 [chunkSize] 分块输出为 [List]。
 *
 * - 要求 [chunkSize] > 0；
 * - 末尾不足一整块时会以实际大小输出最后一块。
 */
fun <T> Flow<T>.chunked(chunkSize: Int): Flow<List<T>> = flow {
    require(chunkSize > 0) { "chunkSize must be positive, but was $chunkSize" }
    val buffer = mutableListOf<T>()
    collect { value ->
        buffer += value
        if (buffer.size == chunkSize) {
            emit(buffer.toList())
            buffer.clear()
        }
    }
    if (buffer.isNotEmpty()) {
        emit(buffer.toList())
    }
}

/**
 * 将字符串流合并并按换行符分割为“逐行” [Flow]。
 *
 * - 会累积上游分片，遇到 `\n` 时输出一行；
 * - 最后不存在换行结尾时，会输出剩余的非空尾行。
 */
fun Flow<String>.toLineFlow(): Flow<String> = flow {
    val buffer = StringBuilder()
    collect { input ->
        if (input.isEmpty()) return@collect
        buffer.append(input)
        var lineEnd: Int
        while (buffer.indexOf('\n').also { lineEnd = it } != -1) {
            val line = buffer.substring(0, lineEnd)
            emit(line)
            buffer.delete(0, lineEnd + 1)
        }
    }
    // 处理最后可能剩余的不完整行（如果没有换行符结尾）
    if (buffer.isNotEmpty()) {
        emit(buffer.toString())
    }
}
