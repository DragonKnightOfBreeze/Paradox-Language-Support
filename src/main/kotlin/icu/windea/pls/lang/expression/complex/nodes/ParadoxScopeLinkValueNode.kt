package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.script.editor.*

class ParadoxScopeLinkValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>,
) : ParadoxComplexExpressionNode.Base() {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SCOPE_LINK_VALUE_KEY
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxScopeLinkValueNode {
            //text may contain parameters
            //child node can be ParadoxScopeLinkNode / ParadoxDynamicValueExpression / ParadoxDataSourceNode

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            run {
                val scopeFieldConfig = linkConfigs.find { it.configExpression?.type in CwtDataTypeGroups.ScopeField }
                if (scopeFieldConfig == null) return@run
                val node = ParadoxScopeLinkNode.resolve(text, textRange, configGroup)
                nodes += node
            }
            run {
                if (nodes.isNotEmpty()) return@run
                val configs = linkConfigs.filter { it.dataSourceExpression?.type in CwtDataTypeGroups.DynamicValue }
                if (configs.isEmpty()) return@run
                val node = ParadoxDynamicValueExpression.resolve(text, textRange, configGroup, configs)!!
                nodes += node
            }
            run {
                if (nodes.isNotEmpty()) return@run
                val node = ParadoxDataSourceNode.resolve(text, textRange, configGroup, linkConfigs)
                nodes += node
            }
            return ParadoxScopeLinkValueNode(text, textRange, nodes, configGroup, linkConfigs)
        }
    }
}
