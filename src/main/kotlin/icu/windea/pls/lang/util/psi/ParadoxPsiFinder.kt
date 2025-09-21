package icu.windea.pls.lang.util.psi

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import com.intellij.util.BitUtil
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.findElementAt
import icu.windea.pls.core.findReferenceAt
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.findParentDefinition
import icu.windea.pls.script.psi.isDefinitionName
import icu.windea.pls.script.psi.isDefinitionRootKey
import icu.windea.pls.script.psi.isExpression

/**
 * 用于从指定的文件位置查找特定目标。其中一些方法可以指定选项，以仅从名字、引用等位置查找。
 */
@Suppress("unused")
object ParadoxPsiFinder {
    object ScriptedVariableOptions {
        const val DEFAULT = 0x01
        const val BY_NAME = 0x02
        const val BY_REFERENCE = 0x04
    }

    fun findScriptedVariable(file: PsiFile, offset: Int, options: Int = 1): ParadoxScriptScriptedVariable? {
        if (BitUtil.isSet(options, ScriptedVariableOptions.BY_REFERENCE) && !DumbService.isDumb(file.project)) {
            val reference = file.findReferenceAt(offset) {
                ParadoxResolveConstraint.ScriptedVariable.canResolve(it)
            }
            val resolved = reference?.resolve()?.castOrNull<ParadoxScriptScriptedVariable>()
            if (resolved != null) return resolved
        }
        if (file.language !is ParadoxScriptLanguage) return null
        if (BitUtil.isSet(options, ScriptedVariableOptions.DEFAULT)) {
            val result = file.findElementAt(offset) t@{
                it.parentOfType<ParadoxScriptScriptedVariable>()
            }
            if (result != null) return result
        } else {
            if (BitUtil.isSet(options, ScriptedVariableOptions.BY_NAME)) {
                val result = file.findElementAt(offset) p@{
                    it.parentOfType<ParadoxScriptScriptedVariableName>()?.parentOfType<ParadoxScriptScriptedVariable>()
                }
                if (result != null) return result
            }
        }
        return null
    }

    inline fun findScriptedVariable(file: PsiFile, offset: Int, optionsProvider: ScriptedVariableOptions.() -> Int): ParadoxScriptScriptedVariable? {
        return findScriptedVariable(file, offset, ScriptedVariableOptions.optionsProvider())
    }

    object DefinitionOptions {
        const val DEFAULT = 0x01
        const val BY_ROOT_KEY = 0x02
        const val BY_NAME = 0x04
        const val BY_REFERENCE = 0x08
    }

    fun findDefinition(file: PsiFile, offset: Int, options: Int = 1): ParadoxScriptDefinitionElement? {
        val expressionElement by lazy {
            file.findElementAt(offset) {
                it.parentOfType<ParadoxScriptExpressionElement>(false)
            }?.takeIf { it.isExpression() }
        }
        val expressionReference by lazy {
            file.findReferenceAt(offset) {
                it.element is ParadoxScriptExpressionElement && ParadoxResolveConstraint.Definition.canResolve(it)
            }
        }

        if (BitUtil.isSet(options, DefinitionOptions.BY_REFERENCE) && !DumbService.isDumb(file.project)) {
            val reference = expressionReference
            val resolved = reference?.resolve()?.castOrNull<ParadoxScriptDefinitionElement>()?.takeIf { it.definitionInfo != null }
            if (resolved != null) return resolved
        }
        if (file.language !is ParadoxScriptLanguage) return null
        if (BitUtil.isSet(options, DefinitionOptions.DEFAULT)) {
            val result = file.findElementAt(offset) t@{
                it.parents(false).findIsInstance<ParadoxScriptDefinitionElement> { p -> p.definitionInfo != null }
            }
            if (result != null) return result
        } else {
            if (BitUtil.isSet(options, DefinitionOptions.BY_ROOT_KEY)) {
                val element = expressionElement
                if (element is ParadoxScriptPropertyKey && element.isDefinitionRootKey()) {
                    return element.findParentDefinition()
                }
            }
            if (BitUtil.isSet(options, DefinitionOptions.BY_NAME)) {
                val element = expressionElement
                if (element is ParadoxScriptValue && element.isDefinitionName()) {
                    return element.findParentDefinition()
                }
            }
        }
        return null
    }

    inline fun findDefinition(file: PsiFile, offset: Int, optionsProvider: DefinitionOptions.() -> Int): ParadoxScriptDefinitionElement? {
        return findDefinition(file, offset, DefinitionOptions.optionsProvider())
    }

    object LocalisationOptions {
        const val DEFAULT = 0x01
        const val BY_NAME = 0x02
        const val BY_REFERENCE = 0x04
    }

    fun findLocalisation(file: PsiFile, offset: Int, options: Int = 1): ParadoxLocalisationProperty? {
        if (BitUtil.isSet(options, LocalisationOptions.BY_REFERENCE) && !DumbService.isDumb(file.project)) {
            val reference = file.findReferenceAt(offset) {
                ParadoxResolveConstraint.Localisation.canResolve(it)
            }
            val resolved = reference?.resolve()?.castOrNull<ParadoxLocalisationProperty>()
            if (resolved != null) return resolved
        }
        if (file.language !is ParadoxLocalisationLanguage) return null
        if (BitUtil.isSet(options, LocalisationOptions.DEFAULT)) {
            val result = file.findElementAt(offset) t@{
                it.parentOfType<ParadoxLocalisationProperty>()
            }
            if (result != null) return result
        } else {
            if (BitUtil.isSet(options, LocalisationOptions.BY_NAME)) {
                val result = file.findElementAt(offset) p@{
                    it.parentOfType<ParadoxLocalisationPropertyKey>()?.parentOfType<ParadoxLocalisationProperty>()
                }
                if (result != null) return result
            }
        }
        return null
    }

    inline fun findLocalisation(file: PsiFile, offset: Int, optionsProvider: LocalisationOptions.() -> Int): ParadoxLocalisationProperty? {
        return findLocalisation(file, offset, LocalisationOptions.optionsProvider())
    }

    fun findScriptExpression(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        if (file.language !is ParadoxScriptLanguage) return null
        return file.findElementAt(offset) {
            it.parentOfType<ParadoxScriptExpressionElement>(false)
        }?.takeIf { it.isExpression() }
    }

    fun findLocalisationExpression(file: PsiFile, offset: Int): ParadoxLocalisationExpressionElement? {
        if (file.language !is ParadoxLocalisationLanguage) return null
        return file.findElementAt(offset) {
            it.parentOfType<ParadoxLocalisationExpressionElement>(false)
        }?.takeIf { it.isComplexExpression() }
    }

    fun findCsvExpression(file: PsiFile, offset: Int): ParadoxCsvExpressionElement? {
        if (file.language !is ParadoxCsvLanguage) return null
        return file.findElementAt(offset) {
            it.parentOfType<ParadoxCsvExpressionElement>(false)
        }
    }

    fun findLocalisationColorfulText(file: PsiFile, offset: Int, fromNameToken: Boolean = false): ParadoxLocalisationColorfulText? {
        if (file.language !is ParadoxLocalisationLanguage) return null
        return file.findElementAt(offset) t@{
            if (fromNameToken && it.elementType != ParadoxLocalisationElementTypes.COLOR_TOKEN) return@t null
            it.parentOfType<ParadoxLocalisationColorfulText>(false)
        }
    }

    fun findLocalisationLocale(file: PsiFile, offset: Int, fromNameToken: Boolean = false): ParadoxLocalisationLocale? {
        if (file.language !is ParadoxLocalisationLanguage) return null
        return file.findElementAt(offset) p@{
            if (fromNameToken && it.elementType != ParadoxLocalisationElementTypes.LOCALE_TOKEN) return@p null
            it.parentOfType<ParadoxLocalisationLocale>(false)
        }
    }
}
