package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.resolveElementWithConfig
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.core.util.values.singletonSetOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.editor.ParadoxSemanticAttributesKeys
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.references.CwtConfigBasedPsiReference
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.util.ParadoxExpressionManager

class ParadoxDatabaseObjectTypeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val config: CwtDatabaseObjectTypeConfig?
) : ParadoxComplexExpressionNodeBase(), ParadoxIdentifierNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return config.to.singletonSetOrEmpty()
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxSemanticAttributesKeys.databaseObjectType(element.language)
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val reference = getReference(element)
        if (reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrorBuilder.unresolvedDatabaseObjectType(rangeInExpression, text)
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        config.resolveElementWithConfig()
        val offset = ParadoxExpressionManager.getExpressionOffset(element)
        return Reference(element, rangeInExpression.shiftRight(offset), config)
    }

    class Reference(
        element: PsiElement,
        rangeInElement: TextRange,
        config: CwtDatabaseObjectTypeConfig?
    ) : CwtConfigBasedPsiReference<CwtProperty>(element, rangeInElement, config), ParadoxIdentifierNode.Reference

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDatabaseObjectTypeNode {
            val config = configGroup.databaseObjectTypes.get(text)
            return ParadoxDatabaseObjectTypeNode(text, textRange, configGroup, config)
        }
    }

    companion object : Resolver()
}
