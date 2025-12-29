package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayHintsCustomSettingsProvider
import com.intellij.lang.Language
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsSettings
import javax.swing.JComponent

/**
 * @see ParadoxScopeContextInfoHintsProvider
 */
class ParadoxScopeContextInfoSettingsProvider : InlayHintsCustomSettingsProvider<Boolean> {
    private val showOnlyIfChangedProperty = AtomicProperty(true)
    private var showOnlyIfChanged by showOnlyIfChangedProperty

    private val component by lazy {
        panel {
            row {
                checkBox(PlsBundle.message("hints.settings.showScopeContextOnlyIfIsChanged")).bindSelected(showOnlyIfChangedProperty)
            }
        }.also { it.border = JBUI.Borders.empty(5) }
    }

    override fun createComponent(project: Project, language: Language): JComponent {
        showOnlyIfChanged = getSettingsInstance(project).showScopeContextOnlyIfIsChanged
        return component
    }

    override fun isDifferentFrom(project: Project, settings: Boolean): Boolean {
        return settings != showOnlyIfChanged
    }

    override fun getSettingsCopy(): Boolean {
        return showOnlyIfChanged
    }

    override fun putSettings(project: Project, settings: Boolean, language: Language) {
        showOnlyIfChanged = settings
    }

    override fun persistSettings(project: Project, settings: Boolean, language: Language) {
        getSettingsInstance(project).showScopeContextOnlyIfIsChanged = settings
    }

    private fun getSettingsInstance(project: Project): ParadoxDeclarativeHintsSettings {
        return ParadoxDeclarativeHintsSettings.getInstance(project)
    }
}
