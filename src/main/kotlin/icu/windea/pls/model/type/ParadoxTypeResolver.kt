package icu.windea.pls.model.type

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.text.TextMatcher
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.isCommandExpression
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
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
    fun resolveType(text: String): ParadoxExpressionType {
        return when {
            text.isEmpty() -> ParadoxExpressionType.String
            text.isLeftQuoted() -> ParadoxExpressionType.String
            text == "yes" || text == "no" -> ParadoxExpressionType.Boolean
            TextMatcher.matchesInt(text) -> ParadoxExpressionType.Int
            TextMatcher.matchesFloat(text) -> ParadoxExpressionType.Float
            else -> ParadoxExpressionType.String
        }
    }

    fun resolveExpression(element: ParadoxExpressionElement): String {
        return when (element) {
            is ParadoxScriptBlock -> PlsStrings.blockFolder
            is ParadoxScriptInlineMath -> PlsStrings.inlineMathFolder
            else -> element.text
        }
    }

    fun resolveExpressionType(element: ParadoxExpressionElement): ParadoxExpressionType {
        return when (element) {
            is ParadoxScriptPropertyKey -> resolveType(element.text)
            is ParadoxScriptBoolean -> ParadoxExpressionType.Boolean
            is ParadoxScriptInt -> ParadoxExpressionType.Int
            is ParadoxScriptFloat -> ParadoxExpressionType.Float
            is ParadoxScriptString -> ParadoxExpressionType.String
            is ParadoxScriptBlock -> ParadoxExpressionType.Block
            is ParadoxScriptColor -> ParadoxExpressionType.Color
            is ParadoxScriptInlineMath -> ParadoxExpressionType.InlineMath
            is ParadoxLocalisationCommandText -> {
                if (element.isCommandExpression()) return ParadoxExpressionType.CommandExpression
                ParadoxExpressionType.String
            }
            is ParadoxLocalisationConceptName -> {
                if (element.isDatabaseObjectExpression(strict = true)) return ParadoxExpressionType.DatabaseObjectExpression
                ParadoxExpressionType.String
            }
            is ParadoxLocalisationExpressionElement -> ParadoxExpressionType.String
            is ParadoxCsvColumn -> resolveType(element.text)
            is ParadoxCsvExpressionElement -> ParadoxExpressionType.String
            else -> ParadoxExpressionType.Unknown
        }
    }

    fun resolveSeparatorType(text: String): ParadoxSeparatorType? {
        return when (text) {
            "=" -> ParadoxSeparatorType.EQUAL
            "!=", "<>" -> ParadoxSeparatorType.NOT_EQUAL
            "?=" -> ParadoxSeparatorType.SAFE_EQUAL
            "<" -> ParadoxSeparatorType.LT
            ">" -> ParadoxSeparatorType.GT
            "<=" -> ParadoxSeparatorType.LE
            ">=" -> ParadoxSeparatorType.GE
            else -> null
        }
    }

    fun resolveSeparatorType(element: PsiElement): ParadoxSeparatorType? {
        val elementType = element.elementType
        return when (elementType) {
            ParadoxScriptElementTypes.EQUAL_SIGN -> ParadoxSeparatorType.EQUAL
            ParadoxScriptElementTypes.NOT_EQUAL_SIGN -> ParadoxSeparatorType.NOT_EQUAL
            ParadoxScriptElementTypes.SAFE_EQUAL_SIGN -> ParadoxSeparatorType.SAFE_EQUAL
            ParadoxScriptElementTypes.LT_SIGN -> ParadoxSeparatorType.LT
            ParadoxScriptElementTypes.GT_SIGN -> ParadoxSeparatorType.GT
            ParadoxScriptElementTypes.LE_SIGN -> ParadoxSeparatorType.LE
            ParadoxScriptElementTypes.GE_SIGN -> ParadoxSeparatorType.GE
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
            is ParadoxScriptProperty -> ParadoxMemberRole.PROPERTY
            is ParadoxScriptValue -> when (element.parent) {
                is ParadoxScriptProperty -> ParadoxMemberRole.PROPERTY_VALUE
                is ParadoxScriptRootBlock -> ParadoxMemberRole.BLOCK_VALUE
                is ParadoxScriptBlock -> ParadoxMemberRole.BLOCK_VALUE
                is ParadoxScriptScriptedVariable -> ParadoxMemberRole.SCRIPTED_VARIABLE_VALUE
                else -> ParadoxMemberRole.OTHER
            }
            else -> ParadoxMemberRole.OTHER
        }
    }
}
