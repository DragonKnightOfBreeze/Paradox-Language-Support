package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.lang.complexEnumValueInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import javax.swing.Icon

object ParadoxElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        run {
            // mod descriptor file
            if (!ParadoxPsiMatchService.isModDescriptorFile(element)) return@run
            return ChronicleIcons.FileTypes.ModDescriptor
        }
        run {
            // inline script usage
            if (!ParadoxPsiMatchService.isInlineScriptUsage(element, selectGameType(element))) return@run
            return ChronicleIcons.Nodes.Macro
        }
        run {
            // definition injection
            if (!ParadoxPsiMatchService.isDefinitionInjectionUsage(element, selectGameType(element))) return@run
            return ChronicleIcons.Nodes.Macro
        }
        run {
            // definition
            if (element !is ParadoxDefinitionElement) return@run
            val definitionInfo = element.definitionInfo ?: return@run
            return ChronicleIcons.Nodes.Definition(definitionInfo.type)
        }
        run {
            // localisation
            if (element !is ParadoxLocalisationProperty) return@run
            if (element.type == null) return@run
            return ChronicleIcons.Nodes.Localisation
        }
        run {
            // complex enum value
            if (element !is ParadoxExpressionElement) return@run
            val complexEnumValueInfo = element.complexEnumValueInfo ?: return@run
            return ChronicleIcons.Nodes.ComplexEnumValue(complexEnumValueInfo.enumName)
        }

        return null
    }

    fun getPresentableText(element: PsiElement): String? {
        run {
            // definition (exclude mod descriptor file)
            if (element !is ParadoxDefinitionElement) return@run
            if (ParadoxPsiMatchService.isModDescriptorFile(element)) return@run
            val definitionInfo = element.definitionInfo ?: return@run
            return definitionInfo.name.or.anonymous()
        }
        run {
            // localisation
            if (element !is ParadoxLocalisationProperty) return@run
            if (element.type == null) return@run
            return element.name
        }
        run {
            // complex enum value
            if (element !is ParadoxExpressionElement) return@run
            val complexEnumValueInfo = element.complexEnumValueInfo ?: return@run
            return complexEnumValueInfo.name
        }

        return null
    }

    fun getLocationString(element: PsiElement): String? {
        return getFileInfoText(element)
    }

    fun getTreeLocationString(element: PsiElement): String? {
        run {
            // inline script usage - inline script expression
            if (!ParadoxPsiMatchService.isInlineScriptUsage(element, selectGameType(element))) return@run
            val expression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(element, resolve = true)
            return expression.or.unresolved()
        }
        run {
            // definition (exclude mod descriptor file) - type info + (optional) presentable name
            if (element !is ParadoxDefinitionElement) return@run
            if (ParadoxPsiMatchService.isModDescriptorFile(element)) return@run
            val definitionInfo = element.definitionInfo ?: return@run
            val typeInfo = definitionInfo.typeText
            val presentableName = ParadoxDefinitionManager.getPresentableName(element)
            return buildString {
                append(": ").append(typeInfo)
                if (presentableName != null) append(" ").append(presentableName)
            }
        }
        run {
            // complex enum value - type info + (optional) presentable name
            if (element !is ParadoxExpressionElement) return@run
            val complexEnumValueInfo = element.complexEnumValueInfo ?: return@run
            val typeInfo = complexEnumValueInfo.enumName
            val presentableName = ParadoxComplexEnumValueManager.getPresentableName(complexEnumValueInfo.name, element)
            return buildString {
                append(": ").append(typeInfo)
                if (presentableName != null) append(" ").append(presentableName)
            }
        }
        run {
            // scripted variable - (optional) value info + (optional) presentable name
            if (element !is ParadoxScriptScriptedVariable) return@run
            val valueInfo = element.scriptedVariableValue?.presentableText
            val presentableName = ParadoxScriptedVariableManager.getPresentableName(element)
            return buildString {
                if (valueInfo != null) append(" = ").append(valueInfo)
                if (presentableName != null) append(" ").append(presentableName)
            }.orNull()
        }

        return null
    }

    fun getFileInfoText(element: PsiElement): String? {
        val fileInfo = element.fileInfo ?: return null
        val path = fileInfo.path.path
        val entry = fileInfo.entry
        return when {
            entry.isEmpty() -> path
            else -> "$path of $entry"
        }
    }

    fun getLongFileInfoText(element: PsiElement): String? {
        val fileInfo = element.fileInfo ?: return null
        val qualifiedName = fileInfo.rootInfo.qualifiedName
        val path = fileInfo.path.path
        val entry = fileInfo.entry
        return when {
            entry.isEmpty() -> "$path in $qualifiedName"
            else -> "$path of $entry in $qualifiedName"
        }
    }
}
