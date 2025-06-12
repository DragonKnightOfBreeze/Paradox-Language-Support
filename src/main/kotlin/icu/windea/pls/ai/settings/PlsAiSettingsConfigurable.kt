package icu.windea.pls.ai.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.ai.*
import icu.windea.pls.ai.util.*

class PlsAiSettingsConfigurable : BoundConfigurable(PlsAiBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    private val groupNameOpenAI = "pls.ai.openAI"

    override fun createPanel(): DialogPanel {
        val settings = PlsAiManager.getSettings()
        return panel {
            //enable
            row {
                checkBox(PlsAiBundle.message("settings.ai.enable")).bindSelected(settings::enable)
                    .comment(PlsAiBundle.message("settings.ai.enable.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            }
            //withContext
            row {
                checkBox(PlsAiBundle.message("settings.ai.withContext")).bindSelected(settings::withContext)
                    .comment(PlsAiBundle.message("settings.ai.withContext.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            }

            //openAI
            group(PlsAiBundle.message("settings.ai.openAI")) {
                //modelName
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.modelName")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::modelName.toNonNullableProperty(""))
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.getDefaultOpenAiModelName()) }
                }
                //apiEndpoint
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.apiEndpoint")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::apiEndpoint.toNonNullableProperty("")).align(Align.FILL)
                        .applyToComponent { setEmptyState(PlsAiSettingsManager.getDefaultOpenAiApiEndpoint()) }
                }
                //apiKey
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.apiKey")).widthGroup(groupNameOpenAI)
                    passwordField().bindText(settings.openAI::apiKey.toNonNullableProperty("")).align(Align.FILL)
                        .validationOnInput { validateOpenAiApiKey(this, it) }
                }
            }

            //features
            group("") {
                //batchSizeOfLocalisations
                row {
                    label(PlsAiBundle.message("settings.ai.features.batchSizeOfLocalisations"))
                    intTextField(1..PlsAiSettingsManager.getMaxBatchSizeOfLocalisations(), 1).bindIntText(settings.features::batchSizeOfLocalisations)
                    contextHelp(PlsAiBundle.message("settings.ai.features.batchSizeOfLocalisations.tip"))
                }
                //translateLocalisationsWithDescription
                row {
                    checkBox(PlsAiBundle.message("settings.ai.features.translateLocalisationsWithDescription")).bindSelected(settings.features::translateLocalisationsWithDescription)
                    contextHelp(PlsAiBundle.message("settings.ai.features.translateLocalisationsWithDescription.tip"))
                }
                //polishLocalisationsWithDescription
                row {
                    checkBox(PlsAiBundle.message("settings.ai.features.polishLocalisationsWithDescription")).bindSelected(settings.features::polishLocalisationsWithDescription)
                    contextHelp(PlsAiBundle.message("settings.ai.features.polishLocalisationsWithDescription.tip"))
                }
            }
        }
    }

    @Suppress("DialogTitleCapitalization")
    private fun validateOpenAiApiKey(builder: ValidationInfoBuilder, field: JBPasswordField): ValidationInfo? {
        //目前仅在输入时验证，不在应用时验证
        //如果启用AI集成，但是这里的验证并未通过，相关功能仍然可用，只是使用后会给出警告
        if (field.password.isEmpty()) return builder.warning("settings.ai.openAI.apiKey.1")
        return null
    }
}
