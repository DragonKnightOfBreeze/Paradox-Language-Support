package icu.windea.pls.model

import com.intellij.psi.PsiElement
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxLocalisationParameterElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

sealed class ParadoxDeclarationInfo {
    abstract val name: String

    class ScriptedVariable(
        override val name: String
    ) : ParadoxDeclarationInfo()

    class Definition(
        override val name: String,
        val type: String,
        val subtypes: List<String>
    ) : ParadoxDeclarationInfo()

    class Localisation(
        override val name: String,
        val type: ParadoxLocalisationType
    ) : ParadoxDeclarationInfo()

    class ComplexEnumValue(
        override val name: String,
        val enumName: String
    ) : ParadoxDeclarationInfo()

    class DynamicValue(
        override val name: String,
        val types: Set<String>
    ) : ParadoxDeclarationInfo()

    class Parameter(
        override val name: String
    ) : ParadoxDeclarationInfo()

    class LocalisationParameter(
        override val name: String
    ) : ParadoxDeclarationInfo()

    companion object {
        fun from(element: PsiElement): ParadoxDeclarationInfo? {
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
