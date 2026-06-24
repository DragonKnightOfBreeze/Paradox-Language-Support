package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.castOrNull

@Suppress("unused")
interface ParadoxCommandScopeNode : ParadoxLinkNode {
    fun asSystem(): ParadoxSystemCommandScopeNode? = castOrNull()
    fun asStatic(): ParadoxStaticCommandScopeNode? = castOrNull()
    fun asDynamic(): ParadoxDynamicCommandScopeNode? = castOrNull()

    companion object {
        @JvmStatic
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandScopeNode {
            ParadoxSystemCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxStaticCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandScopeNode(text, textRange, configGroup)
        }
    }
}
