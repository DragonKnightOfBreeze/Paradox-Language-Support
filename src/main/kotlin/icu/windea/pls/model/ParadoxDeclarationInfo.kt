package icu.windea.pls.model

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

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
        val category: ParadoxLocalisationCategory
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
                    val category = element.category ?: return null
                    Localisation(name, category)
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
