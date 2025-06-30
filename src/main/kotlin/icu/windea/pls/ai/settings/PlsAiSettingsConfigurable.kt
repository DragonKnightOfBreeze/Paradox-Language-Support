package icu.windea.pls.ai.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.util.*

class PlsAiSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    private val groupNameOpenAI = "pls.ai.openAI"
    private val callbackLock = mutableSetOf<String>()

    override fun createPanel(): DialogPanel {
        callbackLock.clear()
        val settings = PlsAiManager.getSettings()
        return panel {
            //enable
            row {
                checkBox(PlsBundle.message("settings.ai.enable")).bindSelected(settings::enable)
                    .comment(PlsBundle.message("settings.ai.enable.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            }
            //withContext
            row {
                checkBox(PlsBundle.message("settings.ai.withContext")).bindSelected(settings::withContext)
                    .comment(PlsBundle.message("settings.ai.withContext.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            }

            //openAI
            group(PlsBundle.message("settings.ai.openAI")) {
                //modelName
                row {
                    label(PlsBundle.message("settings.ai.openAI.modelName")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::modelName.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.getDefaultOpenAiModelName()) }
                        .onApply { onOpenAiSettingsChanged() }
                }
                //apiEndpoint
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiEndpoint")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::apiEndpoint.toNonNullableProperty("")).align(Align.FILL)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.getDefaultOpenAiApiEndpoint()) }
                        .onApply { onOpenAiSettingsChanged() }
                }
                //apiKey
                row {
                    label(PlsBundle.message("settings.ai.openAI.apiKey")).widthGroup(groupNameOpenAI)
                    passwordField().bindText(settings.openAI::apiKey.toNonNullableProperty("")).align(Align.FILL)
                        .validationOnInput { validateOpenAiApiKey(this, it) }
                        .onApply { onOpenAiSettingsChanged() }
                }
            }

            //features
            collapsibleGroup(PlsBundle.message("settings.ai.features")) {
                //batchSizeOfLocalisations
                row {
                    label(PlsBundle.message("settings.ai.features.batchSizeOfLocalisations"))
                    intTextField(1..PlsAiSettingsManager.getMaxBatchSizeOfLocalisations(), 1).bindIntText(settings.features::batchSizeOfLocalisations)
                    contextHelp(PlsBundle.message("settings.ai.features.batchSizeOfLocalisations.tip"))
                }
                //translateLocalisationsWithDescription
                row {
                    checkBox(PlsBundle.message("settings.ai.features.translateLocalisationsWithDescription")).bindSelected(settings.features::translateLocalisationsWithDescription)
                    contextHelp(PlsBundle.message("settings.ai.features.translateLocalisationsWithDescription.tip"))
                }
                //polishLocalisationsWithDescription
                row {
                    checkBox(PlsBundle.message("settings.ai.features.polishLocalisationsWithDescription")).bindSelected(settings.features::polishLocalisationsWithDescription)
                    contextHelp(PlsBundle.message("settings.ai.features.polishLocalisationsWithDescription.tip"))
                }
            }
        }
    }

    private fun validateOpenAiApiKey(builder: ValidationInfoBuilder, field: JBPasswordField): ValidationInfo? {
        //目前仅在输入时验证，不在应用时验证
        //如果启用AI集成，但是这里的验证并未通过，相关功能仍然可用，只是使用后会给出警告
        if (field.password.isEmpty()) return builder.warning(PlsBundle.message("settings.ai.openAI.apiKey.1"))
        return null
    }

    private fun onOpenAiSettingsChanged() {
        if (!callbackLock.add("onOpenAiSettingsChanged")) return

        PlsChatModelManager.invalidateChatModel(PlsChatModelType.OPEN_AI)
        PlsChatModelManager.invalidateStreamingChatModel(PlsChatModelType.OPEN_AI)
    }
}
