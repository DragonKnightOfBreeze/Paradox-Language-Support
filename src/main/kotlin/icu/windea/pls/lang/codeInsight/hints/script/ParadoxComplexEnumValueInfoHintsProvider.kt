@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 通过内嵌提示显示复杂枚举值信息，即枚举名。
 */
class ParadoxComplexEnumValueInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
    private val settingsKey = SettingsKey<NoSettings>("ParadoxComplexEnumValueInfoHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.complexEnumValueInfo")
    override val description: String get() = PlsBundle.message("script.hints.complexEnumValueInfo.description")
    override val key: SettingsKey<NoSettings> get() = settingsKey

    override fun createSettings() = NoSettings()

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxScriptStringExpressionElement) return true
        if (!element.isExpression()) return true
        val name = element.name
        if (name.isEmpty()) return true
        if (name.isParameterized()) return true

        val info = ParadoxComplexEnumValueManager.getInfo(element)
        if (info != null) {
            val configGroup = PlsFacade.getConfigGroup(file.project, info.gameType)
            val presentation = doCollect(info.enumName, configGroup) ?: return true
            val finalPresentation = presentation.toFinalPresentation(this, file.project)
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
            return true
        }

        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return true
        val configGroup = config.configGroup
        val type = config.configExpression.type
        if (type != CwtDataTypes.EnumValue) return true
        val enumName = config.configExpression.value ?: return true
        if (enumName in config.configGroup.enums) return true //only for complex enums
        val presentation = doCollect(enumName, configGroup) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)

        return true
    }

    private fun PresentationFactory.doCollect(enumName: String, configGroup: CwtConfigGroup): InlayPresentation? {
        val config = configGroup.complexEnums[enumName] ?: return null
        val presentations = mutableListOf<InlayPresentation>()
        presentations.add(smallText(": "))
        presentations.add(psiSingleReference(smallText(config.name)) { config.pointer.element })
        return SequencePresentation(presentations)
    }
}
