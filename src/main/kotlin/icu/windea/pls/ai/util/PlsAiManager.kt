package icu.windea.pls.ai.util

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import icu.windea.pls.ai.settings.*
import java.lang.invoke.*

object PlsAiManager {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    fun getSettings(): PlsAiSettingsState = service<PlsAiSettings>().state

    fun isEnabled(): Boolean = getSettings().enable

    fun isAvailable(): Boolean = isEnabled() && PlsAiSettingsManager.isValid()
}
