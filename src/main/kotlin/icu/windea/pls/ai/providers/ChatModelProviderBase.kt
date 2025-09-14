package icu.windea.pls.ai.providers

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import icu.windea.pls.PlsBundle

/**
 *  - 缓存得到的 [ChatModel] 与 [StreamingChatModel]。当实际的 [options] 发生变化时，会重新缓存。
 *  - 测试 AI 服务状态时，首先检查是否缺失必须的选项。
 */
abstract class ChatModelProviderBase<S : ChatModelProvider.Options> : ChatModelProvider<S> {
    @Volatile private var cachedOptions: S? = null
    @Volatile private var cachedChatModel: ChatModel? = null
    @Volatile private var cachedStreamingChatModel: StreamingChatModel? = null

    final override fun getChatModel(): ChatModel? {
        val options = options ?: return null
        if (cachedChatModel == null || options != cachedOptions) {
            synchronized(this) {
                if (cachedChatModel == null || options != cachedOptions) {
                    cachedOptions = options
                    cachedChatModel = doGetChatModel(options)
                }
            }
        }
        return cachedChatModel
    }

    protected abstract fun doGetChatModel(options: S): ChatModel

    final override fun getStreamingChatModel(): StreamingChatModel? {
        val options = options ?: return null
        if (cachedStreamingChatModel == null || options != cachedOptions) {
            synchronized(this) {
                if (cachedStreamingChatModel == null || options != cachedOptions) {
                    cachedOptions = options
                    cachedStreamingChatModel = doGetStreamingChatModel(options)
                }
            }
        }
        return cachedStreamingChatModel
    }

    protected abstract fun doGetStreamingChatModel(options: S): StreamingChatModel

    final override fun checkStatus(options: S?): ChatModelProvider.StatusResult {
        val options = options
        if (options == null) {
            return ChatModelProvider.StatusResult(false, PlsBundle.message("ai.test.error.title"), PlsBundle.message("ai.test.error.missingConfig"))
        }
        return doCheckStatus(options)
    }

    protected abstract fun doCheckStatus(options: S): ChatModelProvider.StatusResult
}
