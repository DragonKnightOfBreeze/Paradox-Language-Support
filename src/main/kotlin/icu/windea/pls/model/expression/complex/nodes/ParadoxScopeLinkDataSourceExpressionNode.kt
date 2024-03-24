package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.highlighter.*

class ParadoxScopeLinkDataSourceExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val linkConfigs: List<CwtLinkConfig>,
    override val nodes: List<ParadoxExpressionNode>
) : ParadoxExpressionNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_LINK_DATA_SOURCE_KEY
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxScopeLinkDataSourceExpressionNode {
            //val parameterRanges = CwtConfigHandler.getParameterRangesInExpression(text)
            
            //text may contain parameters
            //child node can be dynamicValueExpression
            val nodes = mutableListOf<ParadoxExpressionNode>()
            run {
                val scopeFieldConfig = linkConfigs.find { it.expression?.type in CwtDataTypeGroups.ScopeField }
                if(scopeFieldConfig != null) {
                    val configGroup = linkConfigs.first().info.configGroup
                    val node = ParadoxScopeFieldExpressionNode.resolve(text, textRange, configGroup)
                    nodes.add(node)
                }
            }
            run {
                if(nodes.isNotEmpty()) return@run
                val configs = linkConfigs.filter { it.dataSource?.type in CwtDataTypeGroups.DynamicValue }
                if(configs.isNotEmpty()) {
                    val configGroup = linkConfigs.first().info.configGroup
                    val node = ParadoxDynamicValueExpression.resolve(text, textRange, configGroup, configs)!!
                    nodes.add(node)
                }
            }
            run {
                if(nodes.isNotEmpty()) return@run
                val node = ParadoxDataExpressionNode.resolve(text, textRange, linkConfigs)
                nodes.add(node)
            }
            return ParadoxScopeLinkDataSourceExpressionNode(text, textRange, linkConfigs, nodes)
        }
    }
}
