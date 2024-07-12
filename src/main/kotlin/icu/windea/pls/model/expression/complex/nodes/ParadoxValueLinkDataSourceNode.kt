package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.highlighter.*

class ParadoxValueLinkDataSourceNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val linkConfigs: List<CwtLinkConfig>,
    override val nodes: List<ParadoxComplexExpressionNode>
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_LINK_DATA_SOURCE_KEY
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxValueLinkDataSourceNode {
            val parameterRanges = CwtConfigHandler.getParameterRangesInExpression(text)
            
            //text may contain parameters
            //child node can be dynamicValueExpression / scriptValueExpression
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            run {
                val configs = linkConfigs.filter { it.dataSource?.type in CwtDataTypeGroups.DynamicValue }
                if(configs.isNotEmpty()) {
                    val configGroup = linkConfigs.first().configGroup
                    val node = ParadoxDynamicValueExpression.resolve(text, textRange, configGroup, configs)!!
                    nodes.add(node)
                }
            }
            run {
                if(nodes.isNotEmpty()) return@run
                val offset = textRange.startOffset
                var index: Int
                var tokenIndex = -1
                val textLength = text.length
                while(tokenIndex < textLength) {
                    index = tokenIndex + 1
                    tokenIndex = text.indexOf('|', index)
                    if(tokenIndex != -1 && CwtConfigHandler.inParameterRanges(parameterRanges, tokenIndex)) continue //这里需要跳过参数文本
                    if(tokenIndex == -1) break
                    val scriptValueConfig = linkConfigs.find { it.name == "script_value" }
                    if(scriptValueConfig == null) {
                        val dataText = text.substring(0, tokenIndex)
                        val dataRange = TextRange.create(offset, tokenIndex + offset)
                        val dataNode = ParadoxDataSourceNode.resolve(dataText, dataRange, linkConfigs)
                        nodes.add(dataNode)
                        val errorText = text.substring(tokenIndex)
                        val errorRange = TextRange.create(tokenIndex + offset, text.length + offset)
                        val errorNode = ParadoxErrorTokenNode(errorText, errorRange)
                        nodes.add(errorNode)
                    } else {
                        val configGroup = linkConfigs.first().configGroup
                        val node = ParadoxScriptValueExpression.resolve(text, textRange, configGroup, scriptValueConfig)
                        if(node != null) nodes.add(node)
                    }
                    break
                }
            }
            run {
                if(nodes.isNotEmpty()) return@run
                val node = ParadoxDataSourceNode.resolve(text, textRange, linkConfigs)
                nodes.add(node)
            }
            return ParadoxValueLinkDataSourceNode(text, textRange, linkConfigs, nodes)
        }
    }
}
