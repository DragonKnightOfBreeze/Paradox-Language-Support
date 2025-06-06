package icu.windea.pls.ai.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.ai.*

class PlsAiSettingsConfigurable : BoundConfigurable(PlsAiBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    override fun createPanel(): DialogPanel {
        val settings = PlsFacade.getAiSettings()
        return panel {
            row {
                checkBox(PlsAiBundle.message("settings.ai.enable")).bindSelected(settings::enable)
            }

            //openAI
            group(PlsAiBundle.message("settings.ai.openAI")) {
                //modelName
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.modelName"))
                    textField().bindText(settings.openAI::modelName.toNonNullableProperty(""))
                }
                //apiEndpoint
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.apiEndpoint"))
                    textField().bindText(settings.openAI::apiEndpoint.toNonNullableProperty("")).align(Align.FILL)
                }
                //apiKey
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.apiKey"))
                    passwordField().bindText(settings.openAI::apiKey.toNonNullableProperty("")).align(Align.FILL)
                }
            }
        }
    }
}
