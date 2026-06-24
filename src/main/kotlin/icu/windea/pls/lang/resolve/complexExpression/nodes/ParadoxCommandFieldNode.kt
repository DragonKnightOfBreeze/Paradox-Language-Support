package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.castOrNull

@Suppress("unused")
interface ParadoxCommandFieldNode : ParadoxLinkNode {
    fun asStatic(): ParadoxStaticCommandFieldNode? = castOrNull()
    fun asDynamic(): ParadoxDynamicCommandFieldNode? = castOrNull()

    companion object {
        @JvmStatic
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandFieldNode {
            ParadoxStaticCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedCommandFieldNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandFieldNode(text, textRange, configGroup)
        }
    }
}
