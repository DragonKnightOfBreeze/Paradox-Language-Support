package icu.windea.pls.ai.providers

import icu.windea.pls.ai.model.chatFlow
import icu.windea.pls.ai.model.toCompletionResult
import icu.windea.pls.test.AssumePredicates
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume
import org.junit.Before
import org.junit.Test

class ChatModelProviderTest {
    @Before
    fun before() = AssumePredicates.includeAi()

    @Test
    fun testOpenAi() {
        val provider = ChatModelManager.getProvider(ChatModelProviderType.OPEN_AI)
        Assume.assumeTrue("Provider options is null", provider.options != null)

        val r = provider.checkStatus()
        assertTrue(r.status)

        val chatModel = provider.getChatModel()
        assertNotNull(chatModel)
        chatModel ?: return
        val m1 = chatModel.chat("Say 'hello world'")
        println("m1: $m1")
        assertTrue(m1.isNotEmpty())

        val streamingChatModel = provider.getStreamingChatModel()
        assertNotNull(streamingChatModel)
        streamingChatModel ?: return
        val cr = runBlocking { streamingChatModel.chatFlow("Say 'hello world'").toCompletionResult() }
        val m2 = cr.text
        println("m2: $m2")
        assertTrue(m2.isNotEmpty())
    }

    @Test
    fun testAnthropic() {
        val provider = ChatModelManager.getProvider(ChatModelProviderType.ANTHROPIC)
        val options = provider.options
        Assume.assumeTrue("Provider options is null", options != null)

        val r = provider.checkStatus()
        assertTrue(r.status)

        val chatModel = provider.getChatModel()
        assertNotNull(chatModel)
        chatModel ?: return
        val m1 = chatModel.chat("Say 'hello world'")
        println("m1: $m1")
        assertTrue(m1.isNotEmpty())

        val streamingChatModel = provider.getStreamingChatModel()
        assertNotNull(streamingChatModel)
        streamingChatModel ?: return
        val cr = runBlocking { streamingChatModel.chatFlow("Say 'hello world'").toCompletionResult() }
        val m2 = cr.text
        println("m2: $m2")
        assertTrue(m2.isNotEmpty())
    }

    @Test
    fun testLocal() {
        val provider = ChatModelManager.getProvider(ChatModelProviderType.LOCAL)
        Assume.assumeTrue("Provider options is null", provider.options != null)

        val r = provider.checkStatus()
        assertTrue(r.status)

        val chatModel = provider.getChatModel()
        assertNotNull(chatModel)
        chatModel ?: return
        val m1 = chatModel.chat("Say 'he llo world'")
        println("m1: $m1")
        assertTrue(m1.isNotEmpty())

        val streamingChatModel = provider.getStreamingChatModel()
        assertNotNull(streamingChatModel)
        streamingChatModel ?: return
        val cr = runBlocking { streamingChatModel.chatFlow("Say 'hello world'").toCompletionResult() }
        val m2 = cr.text
        println("m2: $m2")
        assertTrue(m2.isNotEmpty())
    }
}
