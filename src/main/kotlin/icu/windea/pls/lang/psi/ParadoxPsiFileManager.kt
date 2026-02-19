package icu.windea.pls.lang.psi

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
import icu.windea.pls.csv.psi.ParadoxCsvTokenSets
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey
import icu.windea.pls.localisation.psi.ParadoxLocalisationTokenSets
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDefinitionName
import icu.windea.pls.script.psi.isDefinitionTypeKey
import icu.windea.pls.script.psi.isExpression

object ParadoxPsiFileManager {
    // region Find Extensions (from elementOffset)

    fun findStringExpressionElementFromStartOffset(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        if (offset < 0) return null
        if (file.language != ParadoxScriptLanguage) return null
        return file.findElementAt(offset)
            ?.takeIf { it.elementType in ParadoxScriptTokenSets.STRING_EXPRESSION_TOKENS }
            ?.parentOfType<ParadoxScriptStringExpressionElement>()
    }

    fun findPropertyFromStartOffset(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        if (offset < 0) return null
        if (file.language != ParadoxScriptLanguage) return null
        return file.findElementAt(offset)
            ?.takeIf { it.elementType == ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN }
            ?.parentOfType<ParadoxScriptProperty>()
    }

    // endregion

    // region Find Extensions

    object ScriptedVariableOptions {
        const val DEFAULT = 0x01
        const val BY_NAME = 0x02
        const val BY_REFERENCE = 0x04
    }

    object DefinitionOptions {
        const val DEFAULT = 0x01
        const val BY_TYPE_KEY = 0x02
        const val BY_NAME = 0x04
        const val BY_REFERENCE = 0x08
    }

    object LocalisationOptions {
        const val DEFAULT = 0x01
        const val BY_NAME = 0x02
        const val BY_REFERENCE = 0x04
    }

    fun findScriptedVariable(file: PsiFile, offset: Int, options: Int = 1): ParadoxScriptScriptedVariable? {
        if (offset < 0) return null
        if (BitUtil.isSet(options, ScriptedVariableOptions.BY_REFERENCE) && !DumbService.isDumb(file.project)) {
            val reference = file.findReferenceAt(offset) {
                ParadoxResolveConstraint.ScriptedVariable.canResolve(it)
            }
            val resolved = reference?.resolve()?.castOrNull<ParadoxScriptScriptedVariable>()
            if (resolved != null) return resolved
        }
        if (file.language != ParadoxScriptLanguage) return null
        if (BitUtil.isSet(options, ScriptedVariableOptions.DEFAULT)) {
            val result = file.findElementAt(offset) t@{
                it.parentOfType<ParadoxScriptScriptedVariable>()
            }
            if (result != null) return result
        } else {
            if (BitUtil.isSet(options, ScriptedVariableOptions.BY_NAME)) {
                val result = file.findElementAt(offset) t@{
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

    fun findDefinition(file: PsiFile, offset: Int, options: Int = 1): ParadoxDefinitionElement? {
        if (offset < 0) return null
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
            val resolved = reference?.resolve()?.castOrNull<ParadoxDefinitionElement>()?.takeIf { it.definitionInfo != null }
            if (resolved != null) return resolved
        }
        if (file.language != ParadoxScriptLanguage) return null
        if (BitUtil.isSet(options, DefinitionOptions.DEFAULT)) {
            val result = file.findElementAt(offset) t@{
                it.parents(false).findIsInstance<ParadoxDefinitionElement> { p -> p.definitionInfo != null }
            }
            if (result != null) return result
        } else {
            if (BitUtil.isSet(options, DefinitionOptions.BY_TYPE_KEY)) {
                val element = expressionElement
                if (element is ParadoxScriptPropertyKey && element.isDefinitionTypeKey()) {
                    return selectScope { element.parentDefinition() }
                }
            }
            if (BitUtil.isSet(options, DefinitionOptions.BY_NAME)) {
                val element = expressionElement
                if (element is ParadoxScriptValue && element.isDefinitionName()) {
                    return selectScope { element.parentDefinition() }
                }
            }
        }
        return null
    }

    inline fun findDefinition(file: PsiFile, offset: Int, optionsProvider: DefinitionOptions.() -> Int): ParadoxDefinitionElement? {
        return findDefinition(file, offset, DefinitionOptions.optionsProvider())
    }

    fun findLocalisation(file: PsiFile, offset: Int, options: Int = 1): ParadoxLocalisationProperty? {
        if (offset < 0) return null
        if (BitUtil.isSet(options, LocalisationOptions.BY_REFERENCE) && !DumbService.isDumb(file.project)) {
            val reference = file.findReferenceAt(offset) {
                ParadoxResolveConstraint.Localisation.canResolve(it)
            }
            val resolved = reference?.resolve()?.castOrNull<ParadoxLocalisationProperty>()
            if (resolved != null) return resolved
        }
        if (file.language != ParadoxLocalisationLanguage) return null
        if (BitUtil.isSet(options, LocalisationOptions.DEFAULT)) {
            val result = file.findElementAt(offset) t@{
                it.parentOfType<ParadoxLocalisationProperty>()
            }
            if (result != null) return result
        } else {
            if (BitUtil.isSet(options, LocalisationOptions.BY_NAME)) {
                val result = file.findElementAt(offset) t@{
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

    fun findLocalisationColorfulText(file: PsiFile, offset: Int, fromNameToken: Boolean = false): ParadoxLocalisationColorfulText? {
        if (offset < 0) return null
        if (file.language != ParadoxLocalisationLanguage) return null
        return file.findElementAt(offset) t@{
            if (fromNameToken && it.elementType != ParadoxLocalisationElementTypes.COLOR_TOKEN) return@t null
            it.parentOfType<ParadoxLocalisationColorfulText>(false)
        }
    }

    fun findLocalisationLocale(file: PsiFile, offset: Int, fromNameToken: Boolean = false): ParadoxLocalisationLocale? {
        if (offset < 0) return null
        if (file.language != ParadoxLocalisationLanguage) return null
        return file.findElementAt(offset) t@{
            if (fromNameToken && it.elementType != ParadoxLocalisationElementTypes.LOCALE_TOKEN) return@t null
            it.parentOfType<ParadoxLocalisationLocale>(false)
        }
    }

    fun findScriptProperty(file: PsiFile, offset: Int, fromToken: Boolean = false): ParadoxScriptProperty? {
        if (offset < 0) return null
        if (file.language != ParadoxScriptLanguage) return null
        return file.findElementAt(offset) t@{
            if (fromToken && it.elementType != ParadoxScriptElementTypes.PROPERTY_KEY_TOKEN) return@t null
            it.parentOfType<ParadoxScriptProperty>(false)
        }
    }

    fun findScriptExpression(file: PsiFile, offset: Int, fromToken: Boolean = false): ParadoxScriptExpressionElement? {
        if (offset < 0) return null
        if (file.language != ParadoxScriptLanguage) return null
        return file.findElementAt(offset) t@{
            if (fromToken && it.elementType !in ParadoxScriptTokenSets.STRING_EXPRESSION_TOKENS) return@t null
            it.parentOfType<ParadoxScriptExpressionElement>(false)
        }?.takeIf { it.isExpression() }
    }

    fun findLocalisationExpression(file: PsiFile, offset: Int, fromToken: Boolean = false): ParadoxLocalisationExpressionElement? {
        if (offset < 0) return null
        if (file.language != ParadoxLocalisationLanguage) return null
        return file.findElementAt(offset) t@{
            if (fromToken && it.elementType !in ParadoxLocalisationTokenSets.EXPRESSION_TOKENS) return@t null
            it.parentOfType<ParadoxLocalisationExpressionElement>(false)
        }?.takeIf { it.isComplexExpression() }
    }

    fun findCsvExpression(file: PsiFile, offset: Int, fromToken: Boolean = false): ParadoxCsvExpressionElement? {
        if (offset < 0) return null
        if (file.language != ParadoxCsvLanguage) return null
        return file.findElementAt(offset) t@{
            if (fromToken && it.elementType !in ParadoxCsvTokenSets.EXPRESSION_TOKENS) return@t null
            it.parentOfType<ParadoxCsvExpressionElement>(false)
        }
    }

    fun findExpressionForComplexExpression(file: PsiFile, offset: Int, fromToken: Boolean = false): ParadoxExpressionElement? {
        if (offset < 0) return null
        return when (file.language) {
            is ParadoxScriptLanguage -> findScriptExpression(file, offset, fromToken)
            is ParadoxLocalisationLanguage -> findLocalisationExpression(file, offset, fromToken)
            else -> null
        }
    }

    // endregion
}
