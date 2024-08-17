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
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.codeInsight.hints.ParadoxComplexEnumValueLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 复杂枚举值的本地化名字的内嵌提示（来自扩展的CWT规则）。
 */
class ParadoxComplexEnumValueLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = 30,
        var iconHeightLimit: Int = 32
    )
    
    private val settingsKey = SettingsKey<Settings>("ParadoxComplexEnumValueLocalizedNameHintsSettingsKey")
    
    override val name: String get() = PlsBundle.message("script.hints.complexEnumValueLocalizedName")
    override val description: String get() = PlsBundle.message("script.hints.complexEnumValueLocalizedName.description")
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
        if(element !is ParadoxScriptStringExpressionElement) return true
        if(!element.isExpression()) return true
        val name = element.name
        if(name.isEmpty()) return true
        if(name.isParameterized()) return true
        
        val info = ParadoxComplexEnumValueManager.getInfo(element)
        if(info != null) {
            val configGroup = getConfigGroup(file.project, info.gameType)
            val presentation = doCollect(info.name, info.enumName, configGroup, file, editor, settings) ?: return true
            val finalPresentation = presentation.toFinalPresentation(this, file.project)
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
            return true
        }
        
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return true
        val configGroup = config.configGroup
        val type = config.expression.type
        if(type != CwtDataTypes.EnumValue) return true
        val enumName = config.expression.value ?: return true
        val presentation = doCollect(name, enumName, configGroup, file, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        
        return true
    }
    
    private fun PresentationFactory.doCollect(name: String, enumName: String, configGroup: CwtConfigGroup, file: PsiFile, editor: Editor, settings: Settings): InlayPresentation? {
        val configs = configGroup.extendedComplexEnumValues[enumName] ?: return null
        val config = configs.findFromPattern(name, file, configGroup) ?: return null //just use file as contextElement here
        val hint = config.hint ?: return null
        val hintElement = ParadoxLocalisationElementFactory.createProperty(configGroup.project, "hint", hint)
        //it's necessary to inject fileInfo (so that gameType can be got later)
        hintElement.containingFile.virtualFile.putUserData(PlsKeys.injectedFileInfo, file.fileInfo)
        return ParadoxLocalisationTextInlayRenderer.render(hintElement, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
    }
}
