package icu.windea.pls.core.coroutines

import kotlinx.coroutines.flow.*

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
