package icu.windea.pls.core

import kotlinx.coroutines.flow.*

/**
 * 输入的[Flow]经过转换[inputTransform]后会发射不定长度的字符串，
 * 输出的[Flow]在转换[outputTransform]前会发射整行的字符串。
 */
fun <T, R> Flow<T>.toLineFlow(
    inputTransform: (T) -> String,
    outputTransform: (String) -> R
): Flow<R> = flow {
    val buffer = StringBuilder()
    collect { input ->
        val input0 = inputTransform(input)
        if (input0.isEmpty()) return@collect
        buffer.append(input0)
        var lineEnd: Int
        while (buffer.indexOf('\n').also { lineEnd = it } != -1) {
            val line = buffer.substring(0, lineEnd)
            val output0 = outputTransform(line)
            emit(output0)
            buffer.delete(0, lineEnd + 1)
        }
    }
    // 处理最后可能剩余的不完整行（如果没有换行符结尾）
    if (buffer.isNotEmpty()) {
        val output0 = outputTransform(buffer.toString())
        emit(output0)
    }
}

/**
 * 输入的[Flow]会发射不定长度的字符串，
 * 输出的[Flow]会发射整行的字符串。
 */
fun Flow<String>.toLineFlow(): Flow<String> {
    return toLineFlow({ it }, { it })
}
