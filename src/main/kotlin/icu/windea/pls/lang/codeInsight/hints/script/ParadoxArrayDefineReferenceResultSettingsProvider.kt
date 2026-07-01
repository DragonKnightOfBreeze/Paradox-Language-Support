package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayHintsCustomSettingsProvider
import com.intellij.lang.Language
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.smaller
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxArrayDefineReferenceResultSettingsProvider.*
import javax.swing.JComponent

/**
 * @see ParadoxArrayDefineReferenceResultHintsProvider
 */
class ParadoxArrayDefineReferenceResultSettingsProvider: InlayHintsCustomSettingsProvider<Settings> {
    data class Settings(
        val showArrayValue: Boolean,
        val truncateArrayValue: Int,
    )

    private val showArrayValueProperty = AtomicProperty(true)
    private val truncateArrayValueProperty = AtomicProperty(-1)
    private var showArrayValue by showArrayValueProperty
    private var truncateArrayValue by truncateArrayValueProperty

    private val component by lazy {
        panel {
            row {
                checkBox(ChronicleBundle.message("hints.settings.showArrayValueForDefines")).bindSelected(showArrayValueProperty).smaller()
            }
            row(ChronicleBundle.message("hints.settings.truncateArrayValueForDefines")) {
                intTextField().bindIntText(truncateArrayValueProperty).smaller()
            }
        }.also { it.border = JBUI.Borders.empty(5) }
    }

    override fun createComponent(project: Project, language: Language): JComponent {
        val s = ParadoxDeclarativeHintsSettings.getInstance(project)
        showArrayValue = s.showArrayValueForDefines
        truncateArrayValue = s.truncateArrayValueForDefines
        return component
    }

    override fun isDifferentFrom(project: Project, settings: Settings): Boolean {
        return settings.showArrayValue != showArrayValue
            && settings.truncateArrayValue != truncateArrayValue
    }

    override fun getSettingsCopy(): Settings {
        return Settings(showArrayValue, truncateArrayValue)
    }

    override fun putSettings(project: Project, settings: Settings, language: Language) {
        showArrayValue = settings.showArrayValue
        truncateArrayValue = settings.truncateArrayValue
    }

    override fun persistSettings(project: Project, settings: Settings, language: Language) {
        val s = ParadoxDeclarativeHintsSettings.getInstance(project)
        s.showArrayValueForDefines = settings.showArrayValue
        s.truncateArrayValueForDefines = settings.truncateArrayValue
    }
}
