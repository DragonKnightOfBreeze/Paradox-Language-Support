package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.expression.complex.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxDatabaseObjectNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
    override val configGroup: CwtConfigGroup,
    val expression: ParadoxDatabaseObjectExpression,
) : ParadoxComplexExpressionNode.Base() {
    val config = expression.typeNode?.config

    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return config.singleton.setOrEmpty()
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return when (element.language) {
            is ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_KEY
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT_KEY
        }
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, expression: ParadoxDatabaseObjectExpression, isBase: Boolean): ParadoxDatabaseObjectNode {
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            run {
                val node = ParadoxDatabaseObjectDataSourceNode.resolve(text, textRange, configGroup, expression, isBase)
                nodes += node
            }
            return ParadoxDatabaseObjectNode(text, textRange, nodes, configGroup, expression)
        }
    }
}
