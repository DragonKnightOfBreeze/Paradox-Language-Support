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
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.codeInsight.hints.ParadoxDynamicValueLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 动态值的本地化名字的内嵌提示（来自扩展的CWT规则）。
 */
class ParadoxDynamicValueLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = 30,
        var iconHeightLimit: Int = 32
    )
    
    private val settingsKey = SettingsKey<Settings>("ParadoxDynamicValueLocalizedNameHintsSettingsKey")
    
    override val name: String get() = PlsBundle.message("script.hints.dynamicValueLocalizedName")
    override val description: String get() = PlsBundle.message("script.hints.dynamicValueLocalizedName.description")
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
        //ignored for value_field or variable_field or other variants
        
        if(element !is ParadoxScriptStringExpressionElement) return true
        if(!element.isExpression()) return true
        val resolveConstraint = ParadoxResolveConstraint.DynamicValueStrictly
        val resolved = element.references.filter { resolveConstraint.canResolve(it) }.mapNotNull { it.resolve() }.lastOrNull()
            ?.castOrNull<ParadoxDynamicValueElement>()
            ?: return true
        val presentation = doCollect(resolved, file, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }
    
    private fun PresentationFactory.doCollect(element: ParadoxDynamicValueElement, file: PsiFile, editor: Editor, settings: Settings): InlayPresentation? {
        val name = element.name
        if(name.isEmpty()) return null
        if(name.isParameterized()) return null
        val type = element.dynamicValueType
        val configGroup = getConfigGroup(element.project, element.gameType)
        val configs = configGroup.extendedDynamicValues[type] ?: return null
        val config = configs.findFromPattern(name, element, configGroup) ?: return null
        val hint = config.hint ?: return null
        val hintElement = ParadoxLocalisationElementFactory.createProperty(configGroup.project, "hint", hint)
        //it's necessary to inject fileInfo (so that gameType can be got later)
        hintElement.containingFile.virtualFile.putUserData(PlsKeys.injectedFileInfo, file.fileInfo)
        return ParadoxLocalisationTextInlayRenderer.render(hintElement, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
    }
}
