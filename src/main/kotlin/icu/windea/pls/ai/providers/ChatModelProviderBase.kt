package icu.windea.pls.ai.providers

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import icu.windea.pls.PlsBundle

/**
 *  - 缓存得到的 [ChatModel] 与 [StreamingChatModel]。当实际的 [options] 发生变化时，会重新缓存。
 *  - 测试 AI 服务状态时，首先检查是否缺失必须的选项。
 */
abstract class ChatModelProviderBase<T : ChatModelProvider.Options> : ChatModelProvider<T> {
    @Volatile private var cachedOptions: T? = null
    @Volatile private var cachedChatModel: ChatModel? = null
    @Volatile private var cachedStreamingChatModel: StreamingChatModel? = null

    final override fun getChatModel(): ChatModel? {
        val opts = options ?: return null
        ensureCache(opts)
        if (cachedChatModel == null) {
            cachedChatModel = doGetChatModel(opts)
        }
        return cachedChatModel
    }

    protected abstract fun doGetChatModel(options: T): ChatModel?

    final override fun getStreamingChatModel(): StreamingChatModel? {
        val opts = options ?: return null
        ensureCache(opts)
        if (cachedStreamingChatModel == null) {
            cachedStreamingChatModel = doGetStreamingChatModel(opts)
        }
        return cachedStreamingChatModel
    }

    protected abstract fun doGetStreamingChatModel(options: T): StreamingChatModel?

    private fun ensureCache(newOptions: T) {
        if (cachedOptions != newOptions) {
            cachedOptions = newOptions
            cachedChatModel = null
            cachedStreamingChatModel = null
        }
    }

    final override fun checkStatus(): ChatModelProvider.StatusResult {
        val opts = options
        if (opts == null) {
            return ChatModelProvider.StatusResult(false, PlsBundle.message("ai.test.error.title"), PlsBundle.message("ai.test.error.missingConfig"))
        }
        return doCHeckStatus()
    }

    protected abstract fun doCHeckStatus(): ChatModelProvider.StatusResult
}
