package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

interface ParadoxCommandScopeNode : ParadoxLinkNode {
    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxCommandScopeNode {
            ParadoxSystemCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxStaticCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedCommandScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorCommandScopeNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}

