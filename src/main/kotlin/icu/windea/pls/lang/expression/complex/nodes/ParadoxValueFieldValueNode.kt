package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.editor.*

class ParadoxValueFieldValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_FIELD_VALUE_KEY
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxValueFieldValueNode {
            //text may contain parameters
            //child node can be:
            //* ParadoxDynamicValueExpression
            //* ParadoxScopeFieldExpression
            //* ParadoxScriptValueExpression
            //* ParadoxDataSourceNode

            val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            run r1@{
                val configs = linkConfigs.filter { it.dataSourceExpression?.type in CwtDataTypeGroups.DynamicValue }
                if (configs.isEmpty()) return@r1
                val node = ParadoxDynamicValueExpression.resolve(text, textRange, configGroup, configs)
                if(node != null) nodes += node
            }
            run r1@{
                if (nodes.isNotEmpty()) return@r1
                val configs = linkConfigs.filter { it.dataSourceExpression?.type in CwtDataTypeGroups.ScopeField }
                if (configs.isEmpty()) return@r1
                val node = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup)
                if(node != null) nodes += node
            }
            run r1@{
                if (nodes.isNotEmpty()) return@r1
                if (!text.contains('|')) return@r1
                val offset = textRange.startOffset
                var index: Int
                var tokenIndex = -1
                val textLength = text.length
                while (tokenIndex < textLength) {
                    index = tokenIndex + 1
                    tokenIndex = text.indexOf('|', index)
                    if (tokenIndex != -1 && parameterRanges.any { tokenIndex in it }) continue //skip parameter text
                    if (tokenIndex == -1) break
                    val scriptValueConfig = linkConfigs.find { it.name == "script_value" }
                    if (scriptValueConfig == null) {
                        val dataText = text.substring(0, tokenIndex)
                        val dataRange = TextRange.create(offset, tokenIndex + offset)
                        val dataNode = ParadoxDataSourceNode.resolve(dataText, dataRange, configGroup, linkConfigs)
                        nodes += dataNode
                        val errorText = text.substring(tokenIndex)
                        val errorRange = TextRange.create(tokenIndex + offset, text.length + offset)
                        val errorNode = ParadoxErrorTokenNode(errorText, errorRange, configGroup)
                        nodes += errorNode
                    } else {
                        val node = ParadoxScriptValueExpression.resolve(text, textRange, configGroup, scriptValueConfig)
                        if (node != null) nodes += node
                    }
                    break
                }
            }
            run r1@{
                if (nodes.isNotEmpty()) return@r1
                val node = ParadoxDataSourceNode.resolve(text, textRange, configGroup, linkConfigs)
                nodes += node
            }
            return ParadoxValueFieldValueNode(text, textRange, nodes, configGroup, linkConfigs)
        }
    }
}
