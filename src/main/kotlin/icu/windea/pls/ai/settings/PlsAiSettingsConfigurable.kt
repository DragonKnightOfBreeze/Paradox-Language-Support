package icu.windea.pls.ai.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.ai.*

class PlsAiSettingsConfigurable : BoundConfigurable(PlsAiBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    private val groupNameOpenAI = "pls.ai.openAI"

    override fun createPanel(): DialogPanel {
        val settings = PlsFacade.getAiSettings()
        return panel {
            row {
                checkBox(PlsAiBundle.message("settings.ai.enable")).bindSelected(settings::enable)
                    .comment(PlsAiBundle.message("settings.ai.enable.comment", MAX_LINE_LENGTH_WORD_WRAP))
            }

            //openAI
            group(PlsAiBundle.message("settings.ai.openAI")) {
                //modelName
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.modelName")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::modelName.toNonNullableProperty(""))
                }
                //apiEndpoint
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.apiEndpoint")).widthGroup(groupNameOpenAI)
                    textField().bindText(settings.openAI::apiEndpoint.toNonNullableProperty("")).align(Align.FILL)
                }
                //apiKey
                row {
                    label(PlsAiBundle.message("settings.ai.openAI.apiKey")).widthGroup(groupNameOpenAI)
                    passwordField().bindText(settings.openAI::apiKey.toNonNullableProperty("")).align(Align.FILL)
                }
            }
        }
    }
}
