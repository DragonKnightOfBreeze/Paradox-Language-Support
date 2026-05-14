package icu.windea.pls.model.type

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtBoolean
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtExpressionElement
import icu.windea.pls.cwt.psi.CwtFloat
import icu.windea.pls.cwt.psi.CwtInt
import icu.windea.pls.cwt.psi.CwtMember
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtRootBlock
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtValue

@Suppress("unused")
object CwtTypeResolver {
    fun resolveType(element: PsiElement): CwtType? {
        return when (element) {
            is CwtPropertyKey -> CwtType.String
            is CwtBoolean -> CwtType.Boolean
            is CwtInt -> CwtType.Int
            is CwtFloat -> CwtType.Float
            is CwtString -> CwtType.String
            is CwtBlock -> CwtType.Block
            is CwtExpressionElement -> CwtType.Unknown
            else -> null
        }
    }

    fun resolveExpressionType(element: CwtExpressionElement): CwtExpressionType {
        return when (element) {
            is CwtPropertyKey -> CwtExpressionType.String
            is CwtBoolean -> CwtExpressionType.Boolean
            is CwtInt -> CwtExpressionType.Int
            is CwtFloat -> CwtExpressionType.Float
            is CwtString -> CwtExpressionType.String
            is CwtBlock -> CwtExpressionType.Block
            else -> CwtExpressionType.Unknown
        }
    }

    fun resolveSeparatorType(text: String): CwtSeparatorType? {
        return when (text) {
            "=" -> CwtSeparatorType.Equal
            "!=", "<>" -> CwtSeparatorType.NotEqual
            "==" -> CwtSeparatorType.DoubleEqual
            else -> null
        }
    }

    fun resolveSeparatorType(element: PsiElement): CwtSeparatorType? {
        val elementType = element.elementType
        return when (elementType) {
            CwtElementTypes.EQUAL_SIGN -> CwtSeparatorType.Equal
            CwtElementTypes.NOT_EQUAL_SIGN -> CwtSeparatorType.NotEqual
            CwtElementTypes.DOUBLE_EQUAL_SIGN -> CwtSeparatorType.DoubleEqual
            else -> null
        }
    }

    fun resolveExpressionRole(element: CwtExpressionElement): CwtExpressionRole {
        return when (element) {
            is CwtPropertyKey -> CwtExpressionRole.Key
            is CwtValue -> CwtExpressionRole.Value
            else -> CwtExpressionRole.Other
        }
    }

    fun resolveMemberRole(element: CwtMember): CwtMemberRole {
        return when (element) {
            is CwtProperty -> CwtMemberRole.Property
            is CwtValue -> when (val parent = element.parent) {
                is CwtOptionComment -> CwtMemberRole.OptionValue
                is CwtProperty -> CwtMemberRole.PropertyValue
                is CwtRootBlock -> CwtMemberRole.BlockValue
                is CwtBlock -> if (parent.parent is CwtOption) CwtMemberRole.OptionBlockValue else CwtMemberRole.BlockValue
                else -> CwtMemberRole.Other
            }
            else -> CwtMemberRole.Other
        }
    }
}
