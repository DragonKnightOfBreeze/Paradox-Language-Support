package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.defineInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxMeshLocatorLightElement
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxShaderEffectLightElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefineNamespaceInfo
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.script.psi.ParadoxScriptProperty

object ParadoxPsiDescriptionService {
    fun getName(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptProperty -> {
                element.definitionInfo?.let { return it.name.or.anonymous() }
                element.defineInfo?.let { return it.expression.or.anonymous() }
                null
            }
            is ParadoxLocalisationProperty -> {
                element.type?.let { return element.name }
                null
            }
            is ParadoxComplexEnumValueLightElement -> element.name
            is ParadoxDynamicValueLightElement -> element.name
            is ParadoxParameterLightElement -> element.name
            is ParadoxLocalisationParameterLightElement -> element.name
            is ParadoxModifierLightElement -> element.name
            is ParadoxShaderEffectLightElement -> element.name
            is ParadoxMeshLocatorLightElement -> element.name
            else -> null
        }
    }

    fun getType(element: PsiElement): String? {
        // should not be upper-cased
        return when (element) {
            is ParadoxScriptProperty -> {
                element.definitionInfo?.let {
                    return ChronicleBundle.message("description.type.definition")
                }
                element.defineInfo?.let {
                    return when (it) {
                        is ParadoxDefineNamespaceInfo -> ChronicleBundle.message("description.type.defineNamespace")
                        is ParadoxDefineVariableInfo -> ChronicleBundle.message("description.type.defineVariable")
                    }
                }
                null
            }
            is ParadoxLocalisationProperty -> {
                element.type?.let {
                    return when (it) {
                        ParadoxLocalisationType.Normal -> ChronicleBundle.message("description.type.localisation")
                        ParadoxLocalisationType.Synced -> ChronicleBundle.message("description.type.syncedLocalisation")
                    }
                }
                null
            }
            is ParadoxDynamicValueLightElement -> {
                val dynamicValueType = element.types.firstOrNull()
                when (dynamicValueType) {
                    "variable" -> ChronicleBundle.message("description.type.variable")
                    else -> ChronicleBundle.message("description.type.dynamicValue")
                }
            }
            is ParadoxComplexEnumValueLightElement -> ChronicleBundle.message("description.type.complexEnumValue")
            is ParadoxParameterLightElement -> ChronicleBundle.message("description.type.parameter")
            is ParadoxLocalisationParameterLightElement -> ChronicleBundle.message("description.type.localisationParameter")
            is ParadoxModifierLightElement -> ChronicleBundle.message("description.type.modifier")
            is ParadoxShaderEffectLightElement -> ChronicleBundle.message("description.type.shaderEffect")
            is ParadoxMeshLocatorLightElement -> ChronicleBundle.message("description.type.meshLocator")
            else -> null
        }
    }

    fun getNodeText(element: PsiElement): String? {
        // {type} {nameOrAnonymous}
        val type = getType(element) ?: return null
        val name = getName(element)
        return type + " " + name.or.anonymous()
    }

    fun getHighlightUsagesDescription(element: PsiElement): String? {
        return getNodeText(element)
    }
}
