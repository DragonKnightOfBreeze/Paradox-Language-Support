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
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxDefinitionInfoSettingsProvider.*
import javax.swing.JComponent

// com.intellij.codeInsight.hints.chain.AbstractDeclarativeCallChainCustomSettingsProvider

/**
 * @see ParadoxDefinitionInfoHintsProvider
 */
class ParadoxDefinitionInfoSettingsProvider : InlayHintsCustomSettingsProvider<Settings> {
    data class Settings(
        val showName: Boolean,
        val showSubtypes: Boolean,
        val truncateSubtypes: Int,
    )

    private val showNameProperty = AtomicProperty(true)
    private val showSubtypesProperty = AtomicProperty(true)
    private val truncateSubtypesProperty = AtomicProperty(-1)
    private var showName by showNameProperty
    private var showSubtypes by showSubtypesProperty
    private var truncateSubtypes by truncateSubtypesProperty

    private val component by lazy {
        panel {
            row {
                checkBox(ChronicleBundle.message("hints.settings.showDefinitionName")).bindSelected(showNameProperty).smaller()
            }
            row {
                checkBox(ChronicleBundle.message("hints.settings.showDefinitionType")).selected(true).enabled(false).smaller()
            }
            row {
                checkBox(ChronicleBundle.message("hints.settings.showDefinitionSubtypes")).bindSelected(showSubtypesProperty).smaller()
            }
            row(ChronicleBundle.message("hints.settings.truncateDefinitionSubtypes")) {
                intTextField().bindIntText(truncateSubtypesProperty).smaller()
            }
        }.also { it.border = JBUI.Borders.empty(5) }
    }

    override fun createComponent(project: Project, language: Language): JComponent {
        val s = getSettingsInstance(project)
        showName = s.showDefinitionName
        showSubtypes = s.showDefinitionSubtypes
        truncateSubtypes = s.truncateDefinitionSubtypes
        return component
    }

    override fun isDifferentFrom(project: Project, settings: Settings): Boolean {
        return settings.showName != showName
            && settings.showSubtypes != showSubtypes
            && settings.truncateSubtypes != truncateSubtypes
    }

    override fun getSettingsCopy(): Settings {
        return Settings(showName, showSubtypes, truncateSubtypes)
    }

    override fun putSettings(project: Project, settings: Settings, language: Language) {
        showName = settings.showName
        showSubtypes = settings.showSubtypes
        truncateSubtypes = settings.truncateSubtypes
    }

    override fun persistSettings(project: Project, settings: Settings, language: Language) {
        val s = getSettingsInstance(project)
        s.showDefinitionName = settings.showName
        s.showDefinitionSubtypes = settings.showSubtypes
        s.truncateDefinitionSubtypes = settings.truncateSubtypes
    }

    private fun getSettingsInstance(project: Project): ParadoxDeclarativeHintsSettings {
        return ParadoxDeclarativeHintsSettings.getInstance(project)
    }
}
