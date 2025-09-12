package icu.windea.pls.ai.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import icu.windea.pls.ai.providers.ChatModelProviderType
import icu.windea.pls.core.getValue
import icu.windea.pls.core.setValue
import icu.windea.pls.model.constants.PlsConstants

/**
 * PLS AI设置。可以在插件的对应设置页面中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "PlsAiSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsAiSettings : SimplePersistentStateComponent<PlsAiSettingsState>(PlsAiSettingsState())

/**
 * @property enable 是否启用基于 AI 的各种功能。
 * @property providerType 当前使用的 AI 服务提供者的类型。
 * @property withContext 是否向提示中注入上下文信息。
 */
class PlsAiSettingsState : BaseState() {
    var enable by property(false)
    var providerType by enum<ChatModelProviderType>(ChatModelProviderType.OPEN_AI)
    var withContext by property(false)

    @get:Property(surroundWithTag = false)
    var features by property(FeaturesState())

    @get:Property(surroundWithTag = false)
    var openAI by property(OpenAiState())

    @get:Property(surroundWithTag = false)
    var anthropic by property(AnthropicState())

    @get:Property(surroundWithTag = false)
    var local by property(LocalState())

    /**
     * 功能相关的设置。
     *
     * @property localisationChunkSize 本地化条目的分块大小。即每次输入的本地化条目的最大数量。
     * @property localisationMemorySize 本地化条目的记忆大小。即会话记忆中本地化条目的最大数值。（TODO 更加精确的实现）
     */
    @Tag("features")
    class FeaturesState : BaseState() {
        var localisationChunkSize by property(PlsAiSettingsManager.defaultLocalisationChunkSize)
        var localisationMemorySize by property(PlsAiSettingsManager.defaultLocalisationMemorySize)
    }

    /**
     * OPEN AI API 相关的设置。
     *
     * @property modelName 模型名称。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiEndpoint API 端点。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiKey API 密钥。密文保存。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     */
    @Tag("openAI")
    class OpenAiState : BaseState() {
        var modelName by string() // default: deepseek-chat or gpt-4o-mini
        var apiEndpoint by string() // default: https://api.deepseek.com or https://api.openai.com/v1
        var apiKey by CredentialAttributes("PLS_AI_OPEN_AI_API_KEY")
        var fromEnv by property(false)
        var modelNameEnv by string() // default: OPENAI_MODEL
        var apiEndpointEnv by string() // default: OPENAI_BASE_URL
        var apiKeyEnv by string() // default: OPENAI_API_KEY
    }

    /**
     * ANTHROPIC API 相关的设置。
     *
     * @property modelName 模型名称。
     * @property apiEndpoint API 端点。
     * @property apiKey API 密钥。密文保存。
     */
    @Tag("anthropic")
    class AnthropicState : BaseState() {
        var modelName by string() // default: deepseek-chat or claude-3-5-sonnet-latest
        var apiEndpoint by string() // default: https://api.deepseek.com/anthropic or https://api.anthropic.com
        var apiKey by CredentialAttributes("PLS_AI_ANTHROPIC_API_KEY")
        var fromEnv by property(false)
        var modelNameEnv by string() // default: ANTHROPIC_MODEL
        var apiEndpointEnv by string() // default: ANTHROPIC_BASE_URL
        var apiKeyEnv by string() // default: ANTHROPIC_API_KEY
    }

    /**
     * 本地（Ollama）相关的设置。
     *
     * @property modelName 模型名称。
     * @property apiEndpoint API 端点。
     */
    @Tag("local")
    class LocalState : BaseState() {
        var modelName by string() // no default
        var apiEndpoint by string() // default: http://localhost:11434
        var fromEnv by property(false)
        var modelNameEnv by string() // default: OLLAMA_MODEL
        var apiEndpointEnv by string() // default: OLLAMA_BASE_URL
    }
}
