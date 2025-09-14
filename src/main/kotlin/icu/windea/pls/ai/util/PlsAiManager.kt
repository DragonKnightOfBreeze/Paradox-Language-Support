package icu.windea.pls.ai.util

import com.fasterxml.jackson.module.kotlin.readValue
import dev.langchain4j.exception.LangChain4jException
import icu.windea.pls.ai.model.ErrorInfos
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.util.ObjectMappers

object PlsAiManager {
    fun getOptimizedDescription(description: String?): String? {
        return description?.orNull()?.substringBefore('\n')?.trim() //去除首尾空白，且截断换行符之后的文本
    }

    fun getOptimizedErrorMessage(e: Throwable?): String? {
        if (e == null) return null
        val message = e.message
        when (e) {
            is LangChain4jException -> {
                if (message.isNotNullOrEmpty()) {
                    runCatchingCancelable {
                        val errorInfo = ObjectMappers.jsonMapper.readValue<ErrorInfos.OpenAiErrorInfo>(message)
                        return "[${errorInfo.error.code}] ${errorInfo.error.message}"
                    }
                    runCatchingCancelable {
                        val errorInfo = ObjectMappers.jsonMapper.readValue<ErrorInfos.AnthropicErrorInfo>(message)
                        return "[${errorInfo.error.type}] ${errorInfo.error.message}"
                    }
                }
                return e.message
            }
            else -> return e.message
        }
    }
}
