package icu.windea.pls.model.type

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.match.TextMatcher
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInlineMathNumber
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue

@Suppress("unused")
object ParadoxTypeResolver {
    fun resolveType(text: String): ParadoxType {
        return when {
            text.isEmpty() -> ParadoxType.String
            text.isLeftQuoted() -> ParadoxType.String
            text == "yes" || text == "no" -> ParadoxType.Boolean
            TextMatcher.matchesInt(text) -> ParadoxType.Int
            TextMatcher.matchesFloat(text) -> ParadoxType.Float
            else -> ParadoxType.String
        }
    }

    fun resolveType(element: PsiElement): ParadoxType? {
        return when (element) {
            is ParadoxScriptScriptedVariable -> element.scriptedVariableValue?.let { resolveType(it) } ?: ParadoxType.Unknown
            is ParadoxScriptedVariableReference -> ParadoxType.ScriptedVariableReference
            is ParadoxScriptPropertyKey -> resolveType(element.text)
            is ParadoxScriptBoolean -> ParadoxType.Boolean
            is ParadoxScriptInt -> ParadoxType.Int
            is ParadoxScriptFloat -> ParadoxType.Float
            is ParadoxScriptString -> ParadoxType.String
            is ParadoxScriptBlock -> ParadoxType.Block
            is ParadoxScriptColor -> ParadoxType.Color
            is ParadoxScriptInlineMath -> ParadoxType.InlineMath
            is ParadoxLocalisationExpressionElement -> ParadoxType.String
            is ParadoxCsvColumn -> resolveType(element.text)
            is ParadoxCsvExpressionElement -> ParadoxType.String
            is ParadoxExpressionElement -> ParadoxType.Unknown
            is ParadoxScriptInlineMathNumber -> resolveType(element.text)
            is ParadoxLocalisationProperty -> ParadoxType.LocalisationProperty
            is ParadoxParameter -> ParadoxType.Parameter
            is ParadoxConditionParameter -> ParadoxType.ConditionParameter
            is ParadoxLocalisationParameter -> ParadoxType.LocalisationParameter
            else -> null
        }
    }

    fun resolveExpressionType(text: String): ParadoxExpressionType {
        return when {
            text.isEmpty() -> ParadoxExpressionType.String
            text.isLeftQuoted() -> ParadoxExpressionType.String
            text == "yes" || text == "no" -> ParadoxExpressionType.Boolean
            TextMatcher.matchesInt(text) -> ParadoxExpressionType.Int
            TextMatcher.matchesFloat(text) -> ParadoxExpressionType.Float
            else -> ParadoxExpressionType.String
        }
    }

    fun resolveExpressionType(element: ParadoxExpressionElement): ParadoxExpressionType {
        return when (element) {
            is ParadoxScriptPropertyKey -> resolveExpressionType(element.text)
            is ParadoxScriptBoolean -> ParadoxExpressionType.Boolean
            is ParadoxScriptInt -> ParadoxExpressionType.Int
            is ParadoxScriptFloat -> ParadoxExpressionType.Float
            is ParadoxScriptString -> ParadoxExpressionType.String
            is ParadoxScriptBlock -> ParadoxExpressionType.Block
            is ParadoxScriptColor -> ParadoxExpressionType.Color
            is ParadoxScriptInlineMath -> ParadoxExpressionType.InlineMath
            is ParadoxScriptedVariableReference -> ParadoxExpressionType.ScriptedVariableReference
            is ParadoxLocalisationExpressionElement -> ParadoxExpressionType.String
            is ParadoxCsvColumn -> resolveExpressionType(element.text)
            is ParadoxCsvExpressionElement -> ParadoxExpressionType.String
            else -> ParadoxExpressionType.Unknown
        }
    }

    fun resolveSeparatorType(text: String): ParadoxSeparatorType? {
        return when (text) {
            "=" -> ParadoxSeparatorType.Equal
            "!=", "<>" -> ParadoxSeparatorType.NotEqual
            "<" -> ParadoxSeparatorType.Lt
            ">" -> ParadoxSeparatorType.Gt
            "<=" -> ParadoxSeparatorType.Le
            ">=" -> ParadoxSeparatorType.Ge
            else -> {
                // 2.1.10 safe assign && safe call assign
                if (text == "?=") return ParadoxSeparatorType.SafeAssign
                if (text.length > 2 && text.removeSurroundingOrNull("?", "=")?.isBlank() == true) return ParadoxSeparatorType.SafeCallAssign
                null
            }
        }
    }

    fun resolveSeparatorType(element: PsiElement): ParadoxSeparatorType? {
        val elementType = element.elementType
        return when (elementType) {
            ParadoxScriptElementTypes.EQUAL_SIGN -> ParadoxSeparatorType.Equal
            ParadoxScriptElementTypes.NOT_EQUAL_SIGN -> ParadoxSeparatorType.NotEqual
            ParadoxScriptElementTypes.LT_SIGN -> ParadoxSeparatorType.Lt
            ParadoxScriptElementTypes.GT_SIGN -> ParadoxSeparatorType.Gt
            ParadoxScriptElementTypes.LE_SIGN -> ParadoxSeparatorType.Le
            ParadoxScriptElementTypes.GE_SIGN -> ParadoxSeparatorType.Ge
            ParadoxScriptElementTypes.SAFE_ASSIGN_SIGN -> ParadoxSeparatorType.SafeAssign
            ParadoxScriptElementTypes.SAFE_CALL_ASSIGN_SIGN -> ParadoxSeparatorType.SafeCallAssign
            else -> null
        }
    }

    fun resolveExpressionRole(element: ParadoxExpressionElement): ParadoxExpressionRole {
        return when (element) {
            is ParadoxScriptPropertyKey -> ParadoxExpressionRole.Key
            is ParadoxScriptValue -> ParadoxExpressionRole.Value
            else -> ParadoxExpressionRole.Other
        }
    }

    fun resolveMemberRole(element: ParadoxScriptMember): ParadoxMemberRole {
        return when (element) {
            is ParadoxScriptProperty -> ParadoxMemberRole.Property
            is ParadoxScriptValue -> when (element.parent) {
                is ParadoxScriptProperty -> ParadoxMemberRole.PropertyValue
                is ParadoxScriptRootBlock -> ParadoxMemberRole.LonelyValue
                is ParadoxScriptBlock -> ParadoxMemberRole.LonelyValue
                is ParadoxScriptScriptedVariable -> ParadoxMemberRole.ScriptedVariableValue
                else -> ParadoxMemberRole.Other
            }
            else -> ParadoxMemberRole.Other
        }
    }
}
