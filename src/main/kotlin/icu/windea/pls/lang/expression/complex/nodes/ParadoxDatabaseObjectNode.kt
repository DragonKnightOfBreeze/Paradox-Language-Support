package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.editor.*
import icu.windea.pls.script.editor.*

class ParadoxDatabaseObjectNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
    override val configGroup: CwtConfigGroup,
    val expression: ParadoxDatabaseObjectExpression,
) : ParadoxComplexExpressionNode.Base() {
    val config = expression.typeNode?.config

    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return config.toSingletonSetOrEmpty()
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
