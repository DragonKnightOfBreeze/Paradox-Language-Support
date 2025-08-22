package icu.windea.pls.ai.settings

import com.intellij.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.ai.model.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.core.util.*
import java.util.*

object PlsAiSettingsManager {
    fun getDefaultLocalisationBatchSize(): Int {
        return 40
    }

    //region Open AI

    fun getDefaultOpenAiModelName(): String {
        //基于IDE界面语言
        return when (Locale.SIMPLIFIED_CHINESE) {
            DynamicBundle.getLocale() -> "deepseek-chat"
            else -> "gpt-4o-mini"
        }
    }

    fun getDefaultOpenAiApiEndpoint(): String {
        //基于IDE界面语言
        return when (Locale.SIMPLIFIED_CHINESE) {
            DynamicBundle.getLocale() -> "https://api.deepseek.com"
            else -> "https://api.openai.com"
        }
    }

    fun validateOpenAiApiKey(builder: ValidationInfoBuilder, field: JBPasswordField): ValidationInfo? {
        //目前仅在输入时验证，不在应用时验证
        //如果启用AI集成，但是这里的验证并未通过，相关功能仍然可用，只是使用后会给出警告
        if (field.password.isEmpty()) return builder.warning(PlsBundle.message("settings.ai.openAI.apiKey.1"))
        return null
    }

    fun onOpenAiSettingsChanged(callbackLock: CallbackLock) {
        if (!callbackLock.check("onOpenAiSettingsChanged")) return

        val chatModelType = ChatModelType.OPEN_AI
        PlsChatModelManager.invalidateChatModel(chatModelType)
        PlsChatModelManager.invalidateStreamingChatModel(chatModelType)
    }

    //endregion
}
