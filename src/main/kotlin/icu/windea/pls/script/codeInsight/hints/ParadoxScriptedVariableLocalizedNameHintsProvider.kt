@file:Suppress("UnstableApiUsage")

package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.codeInsight.hints.ParadoxScriptedVariableLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

class ParadoxScriptedVariableLocalizedNameHintsProvider: ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = 30,
        var iconHeightLimit: Int = 32
    )
    
    private val settingsKey = SettingsKey<Settings>("ParadoxScriptedVariableLocalizedNameHintsSettingsKey")
    
    override val name: String get() = PlsBundle.message("script.hints.scriptedVariableLocalizedName")
    override val description: String get() = PlsBundle.message("script.hints.scriptedVariableLocalizedName.description")
    override val key: SettingsKey<Settings> get() = settingsKey
    
    override fun createSettings() = Settings()
    
    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                createTextLengthLimitRow(settings::textLengthLimit)
                createIconHeightLimitRow(settings::iconHeightLimit)
            }
        }
    }
    
    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
        //only for scripted variables, not for scripted variable references
        
        if(element !is ParadoxScriptScriptedVariable) return true
        val presentation = doCollect(element, file, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.scriptedVariableName.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }
    
    private fun PresentationFactory.doCollect(element: ParadoxScriptScriptedVariable, file: PsiFile, editor: Editor, settings: Settings): InlayPresentation? {
        val name = element.name
        if(name.isNullOrEmpty()) return null
        if(name.isParameterized()) return null
        val gameType = selectGameType(element) ?: return null
        val configGroup = getConfigGroup(element.project, gameType)
        val config = configGroup.extendedScriptedVariables.findFromPattern(name, element, configGroup) ?:return null
        val hint = config.hint ?: return null
        val hintElement = ParadoxLocalisationElementFactory.createProperty(configGroup.project, "hint", hint)
        //it's necessary to inject fileInfo (so that gameType can be got later)
        hintElement.containingFile.virtualFile.putUserData(PlsKeys.injectedFileInfo, file.fileInfo)
        return ParadoxLocalisationTextInlayRenderer.render(hintElement, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
    }
}