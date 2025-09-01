package icu.windea.pls.core.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 将流按固定大小分块。
 *
 * @param chunkSize 分块大小（必须大于0）
 * @return 每次发出一个大小不超过 [chunkSize] 的列表
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
 * 将字符串流按换行符切分为行流。
 *
 * - 支持跨 chunk 的行拼接。
 * - 末尾无换行符时，会发出最后一行。
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
