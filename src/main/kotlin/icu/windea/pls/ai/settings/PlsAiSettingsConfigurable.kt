package icu.windea.pls.ai.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiConstants
import icu.windea.pls.ai.providers.AnthropicChatModelProvider
import icu.windea.pls.ai.providers.ChatModelProvider
import icu.windea.pls.ai.providers.ChatModelProviderType
import icu.windea.pls.ai.providers.LocalChatModelProvider
import icu.windea.pls.ai.providers.OpenAiChatModelProvider
import icu.windea.pls.ide.help.PlsHelpTopics

class PlsAiSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    override fun getHelpTopic() = PlsHelpTopics.aiSettings

    override fun createPanel(): DialogPanel {
        return panel {
            val settings = PlsAiSettings.getInstance().state

            // enable
            row {
                checkBox(PlsBundle.message("settings.ai.enable")).bindSelected(settings::enable)
                contextHelp(PlsBundle.message("settings.ai.enable.tip"))
            }
            // withContext
            row {
                checkBox(PlsBundle.message("settings.ai.withContext")).bindSelected(settings::withContext)
                contextHelp(PlsBundle.message("settings.ai.withContext.tip"))
            }
            // providerType
            row {
                label(PlsBundle.message("settings.ai.providerType"))
                comboBox(ChatModelProviderType.entries, textListCellRenderer { it?.text })
                    .bindItem(settings::providerType.toNullableProperty())
            }

            // features
            collapsibleGroup(PlsBundle.message("settings.ai.features")) { configureGroupForFeatures() }
            // openAI
            collapsibleGroup(PlsBundle.message("settings.ai.openAI")) { configureGroupForOpenAi() }
            // anthropic
            collapsibleGroup(PlsBundle.message("settings.ai.anthropic")) { configureGroupForAnthropic() }
            // local (Ollama)
            collapsibleGroup(PlsBundle.message("settings.ai.local")) { configureGroupForLocal() }
        }
    }

    private fun Panel.configureGroupForFeatures() {
        val group = "pls.ai.features"
        val settings = PlsAiSettings.getInstance().state.features

        // localisationBatchSize
        row {
            label(PlsBundle.message("settings.ai.features.localisationChunkSize")).widthGroup(group)
            intTextField(1..Int.MAX_VALUE, 1).bindIntText(settings::localisationChunkSize)
            contextHelp(PlsBundle.message("settings.ai.features.localisationChunkSize.tip"))
        }
        // localisationMemorySize
        row {
            label(PlsBundle.message("settings.ai.features.localisationMemorySize")).widthGroup(group)
            intTextField(0..Int.MAX_VALUE, 1).bindIntText(settings::localisationMemorySize)
            contextHelp(PlsBundle.message("settings.ai.features.localisationMemorySize.tip"))
        }
    }

    private fun Panel.configureGroupForOpenAi() {
        val group = "pls.ai.openAI"
        val properties = OpenAiChatModelProvider.Options.AtomicProperties()
        val settings = PlsAiSettings.getInstance().state.openAI

        // modelName
        row {
            label(PlsBundle.message("settings.ai.openAI.modelName")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.modelName)
                .bindText(settings::modelName.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultModelName) }

            label(PlsBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.modelNameEnv)
                .bindText(settings::modelNameEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultModelNameEnv) }
        }
        // apiEndpoint
        row {
            label(PlsBundle.message("settings.ai.openAI.apiEndpoint")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiEndpoint)
                .bindText(settings::apiEndpoint.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultApiEndpoint) }

            label(PlsBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiEndpointEnv)
                .bindText(settings::apiEndpointEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultApiEndpointEnv) }
        }
        // apiKey
        row {
            label(PlsBundle.message("settings.ai.openAI.apiKey")).widthGroup(group)
            passwordField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiKey)
                .bindText(settings::apiKey.toNonNullableProperty(""))
                .validationOnInput { validateApiKey(this, it, properties.fromEnv.get()) }

            label(PlsBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            passwordField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiKeyEnv)
                .bindText(settings::apiKeyEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.OpenAi.defaultApiKeyEnv) }
        }
        // operations
        row {
            button(PlsBundle.message("settings.ai.test")) {
                // 注意这里需要基于可能尚未保存的配置项进行测试
                val options = OpenAiChatModelProvider.Options.fromProperties(properties.toProperties())
                val r = OpenAiChatModelProvider.INSTANCE.checkStatus(options)
                showTestMessage(r)
            }
            checkBox(PlsBundle.message("settings.ai.fromEnv"))
                .bindSelected(properties.fromEnv)
                .bindSelected(settings::fromEnv)
        }
    }

    private fun Panel.configureGroupForAnthropic() {
        val group = "pls.ai.anthropic"
        val properties = AnthropicChatModelProvider.Options.AtomicProperties()
        val settings = PlsAiSettings.getInstance().state.anthropic

        // modelName
        row {
            label(PlsBundle.message("settings.ai.anthropic.modelName")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.modelName)
                .bindText(settings::modelName.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultModelName) }

            label(PlsBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.modelNameEnv)
                .bindText(settings::modelNameEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultModelNameEnv) }
        }
        // apiEndpoint
        row {
            label(PlsBundle.message("settings.ai.anthropic.apiEndpoint")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiEndpoint)
                .bindText(settings::apiEndpoint.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultApiEndpoint) }

            label(PlsBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiEndpointEnv)
                .bindText(settings::apiEndpointEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultApiEndpointEnv) }
        }
        // apiKey
        row {
            label(PlsBundle.message("settings.ai.anthropic.apiKey")).widthGroup(group)
            passwordField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiKey)
                .bindText(settings::apiKey.toNonNullableProperty(""))
                .validationOnInput { validateApiKey(this, it, properties.fromEnv.get()) }

            label(PlsBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            passwordField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiKeyEnv)
                .bindText(settings::apiKeyEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.Anthropic.defaultApiKeyEnv) }
        }
        // operations
        row {
            button(PlsBundle.message("settings.ai.test")) {
                // 注意这里需要基于可能尚未保存的配置项进行测试
                val options = AnthropicChatModelProvider.Options.fromProperties(properties.toProperties())
                val r = AnthropicChatModelProvider.INSTANCE.checkStatus(options)
                showTestMessage(r)
            }
            checkBox(PlsBundle.message("settings.ai.fromEnv"))
                .bindSelected(properties.fromEnv)
                .bindSelected(settings::fromEnv)
        }
    }

    private fun Panel.configureGroupForLocal() {
        val group = "pls.ai.local"
        val properties = LocalChatModelProvider.Options.AtomicProperties()
        val settings = PlsAiSettings.getInstance().state.local

        // modelName
        row {
            label(PlsBundle.message("settings.ai.local.modelName")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.modelName)
                .bindText(settings::modelName.toNonNullableProperty(""))
                .validationOnInput { validateModelName(this, it, properties.fromEnv.get()) }

            label(PlsBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.modelNameEnv)
                .bindText(settings::modelNameEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.Local.defaultModelNameEnv) }
        }
        // apiEndpoint
        row {
            label(PlsBundle.message("settings.ai.local.apiEndpoint")).widthGroup(group)
            textField().columns(COLUMNS_MEDIUM)
                .bindText(properties.apiEndpoint)
                .bindText(settings::apiEndpoint.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.Local.defaultApiEndpoint) }

            label(PlsBundle.message("settings.ai.env")).visibleIf(properties.fromEnv)
            textField().columns(COLUMNS_SHORT).visibleIf(properties.fromEnv)
                .bindText(properties.apiEndpointEnv)
                .bindText(settings::apiEndpointEnv.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(PlsAiConstants.Local.defaultApiEndpointEnv) }
        }
        // operations
        row {
            button(PlsBundle.message("settings.ai.test")) {
                // 注意这里需要基于可能尚未保存的配置项进行测试
                val options = LocalChatModelProvider.Options.fromProperties(properties.toProperties())
                val r = LocalChatModelProvider.INSTANCE.checkStatus(options)
                showTestMessage(r)
            }
            checkBox(PlsBundle.message("settings.ai.fromEnv"))
                .bindSelected(properties.fromEnv)
                .bindSelected(settings::fromEnv)
        }
    }

    private fun validateModelName(builder: ValidationInfoBuilder, field: JBTextField, skip: Boolean): ValidationInfo? {
        if (!skip && field.text.isEmpty()) return builder.warning(PlsBundle.message("ai.validation.missingModelName"))
        return null
    }

    // private fun validateApiEndpoint(builder: ValidationInfoBuilder, field: JBTextField, skip: Boolean): ValidationInfo? {
    //     if (!skip && field.text.isEmpty()) return builder.warning(PlsBundle.message("ai.validation.missingApiEndpoint"))
    //     return null
    // }

    private fun validateApiKey(builder: ValidationInfoBuilder, field: JBPasswordField, skip: Boolean): ValidationInfo? {
        // 目前仅在输入时验证，不在应用时验证
        // 如果启用 AI 集成，但是这里的验证并未通过，相关功能仍然可用，只是使用后会给出警告
        if (!skip && field.password.isEmpty()) return builder.warning(PlsBundle.message("ai.validation.missingApiKey"))
        return null
    }

    private fun showTestMessage(statusResult: ChatModelProvider.StatusResult) {
        if (statusResult.status) {
            Messages.showInfoMessage(statusResult.message, statusResult.title)
        } else {
            Messages.showWarningDialog(statusResult.message, statusResult.title)
        }
    }
}
