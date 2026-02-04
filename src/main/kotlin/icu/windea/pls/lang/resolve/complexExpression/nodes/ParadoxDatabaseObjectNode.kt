package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.values.singletonSetOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxDatabaseObjectNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val expression: ParadoxDatabaseObjectExpression,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionNodeBase() {
    val config = expression.typeNode?.config

    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return config.to.singletonSetOrEmpty()
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return when (element.language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_KEY
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT_KEY
        }
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, expression: ParadoxDatabaseObjectExpression, isBase: Boolean): ParadoxDatabaseObjectNode {
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            run {
                val node = ParadoxDatabaseObjectDataNode.resolve(text, textRange, configGroup, expression, isBase)
                nodes += node
            }
            return ParadoxDatabaseObjectNode(text, textRange, configGroup, expression, nodes)
        }
    }

    companion object : Resolver()
}
