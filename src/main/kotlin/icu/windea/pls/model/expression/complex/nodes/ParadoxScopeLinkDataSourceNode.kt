package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.highlighter.*

class ParadoxScopeLinkDataSourceNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val linkConfigs: List<CwtLinkConfig>,
    override val nodes: List<ParadoxComplexExpressionNode>
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return ParadoxScriptAttributesKeys.SCOPE_LINK_DATA_SOURCE_KEY
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxScopeLinkDataSourceNode {
            //val parameterRanges = CwtConfigHandler.getParameterRangesInExpression(text)
            
            //text may contain parameters
            //child node can be dynamicValueExpression
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            run {
                val scopeFieldConfig = linkConfigs.find { it.expression?.type in CwtDataTypeGroups.ScopeField }
                if(scopeFieldConfig != null) {
                    val configGroup = linkConfigs.first().configGroup
                    val node = ParadoxScopeFieldNode.resolve(text, textRange, configGroup)
                    nodes.add(node)
                }
            }
            run {
                if(nodes.isNotEmpty()) return@run
                val configs = linkConfigs.filter { it.dataSource?.type in CwtDataTypeGroups.DynamicValue }
                if(configs.isNotEmpty()) {
                    val configGroup = linkConfigs.first().configGroup
                    val node = ParadoxDynamicValueExpression.resolve(text, textRange, configGroup, configs)!!
                    nodes.add(node)
                }
            }
            run {
                if(nodes.isNotEmpty()) return@run
                val node = ParadoxDataSourceNode.resolve(text, textRange, linkConfigs)
                nodes.add(node)
            }
            return ParadoxScopeLinkDataSourceNode(text, textRange, linkConfigs, nodes)
        }
    }
}
