package icu.windea.pls.model.codeInsight

import com.intellij.psi.PsiElement
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxLocalisationParameterElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 目标信息。用于代码洞察。
 */
sealed class ParadoxTargetInfo {
    abstract val name: String

    class ScriptedVariable(
        override val name: String
    ) : ParadoxTargetInfo()

    class Definition(
        override val name: String,
        val type: String,
        val subtypes: List<String>
    ) : ParadoxTargetInfo()

    class Localisation(
        override val name: String,
        val type: ParadoxLocalisationType
    ) : ParadoxTargetInfo()

    class ComplexEnumValue(
        override val name: String,
        val enumName: String
    ) : ParadoxTargetInfo()

    class DynamicValue(
        override val name: String,
        val types: Set<String>
    ) : ParadoxTargetInfo()

    class Parameter(
        override val name: String
    ) : ParadoxTargetInfo()

    class LocalisationParameter(
        override val name: String
    ) : ParadoxTargetInfo()

    companion object {
        fun from(element: PsiElement): ParadoxTargetInfo? {
            return when (element) {
                is ParadoxScriptScriptedVariable -> {
                    val name = element.name?.orNull() ?: return null
                    ScriptedVariable(name)
                }
                is ParadoxScriptProperty -> {
                    val definitionInfo = element.definitionInfo ?: return null
                    Definition(definitionInfo.name, definitionInfo.type, definitionInfo.subtypes)
                }
                is ParadoxLocalisationProperty -> {
                    val name = element.name.orNull() ?: return null
                    val type = element.type ?: return null
                    Localisation(name, type)
                }
                is ParadoxComplexEnumValueElement -> {
                    val name = element.name.orNull() ?: return null
                    val enumName = element.enumName.orNull() ?: return null
                    ComplexEnumValue(name, enumName)
                }
                is ParadoxDynamicValueElement -> {
                    val name = element.name.orNull() ?: return null
                    val types = element.dynamicValueTypes.orNull() ?: return null
                    DynamicValue(name, types)
                }
                is ParadoxParameterElement -> {
                    val name = element.name.orNull() ?: return null
                    Parameter(name)
                }
                is ParadoxLocalisationParameterElement -> {
                    val name = element.name.orNull() ?: return null
                    LocalisationParameter(name)
                }
                else -> null
            }
        }
    }
}
