package icu.windea.pls.ai.settings

import com.intellij.credentialStore.*
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.*
import icu.windea.pls.core.*
import icu.windea.pls.model.constants.*

/**
 * PLS AI设置。可以在插件的对应设置页面中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "PlsAiSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsAiSettings : SimplePersistentStateComponent<PlsAiSettingsState>(PlsAiSettingsState())

/**
 * @property enable 是否启用基于AI的各种功能。
 * @property withContext 是否在提示中附加上下文信息。
 */
class PlsAiSettingsState : BaseState() {
    var enable by property(false)
    var withContext by property(false)

    @get:Property(surroundWithTag = false)
    var openAI by property(OpenAiState())

    @get:Property(surroundWithTag = false)
    var features by property(FeaturesState())

    /**
     * @property modelName 模型名称。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiEndpoint API端点。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     * @property apiKey API密钥。密文保存。可以自由输入，保存设置时会发起请求以验证，但不强制通过验证。
     */
    @Tag("openAI")
    class OpenAiState : BaseState() {
        var modelName by string() // e.g., gpt-4o-mini or deepseek-chat
        var apiEndpoint by string() // e.g., https://api.openai.com or https://api.deepseek.com
        var apiKey by CredentialAttributes("PLS_AI_OPEN_AI_API_KEY")
    }

    /**
     * @property localisationChunkSize 本地化的分块大小。即每次输入的本地化条目的最大数量。
     * @property translateLocalisationsWithDescription 如果启用，翻译本地化时，可以输入额外需求的描述。
     * @property polishLocalisationsWithDescription 如果启用，润色本地化时，是否可以输入额外需求的描述。
     */
    @Tag("features")
    class FeaturesState : BaseState() {
        var localisationChunkSize by property(PlsAiSettingsManager.getDefaultLocalisationBatchSize())
        var translateLocalisationsWithDescription by property(false)
        var polishLocalisationsWithDescription by property(true)
    }
}
