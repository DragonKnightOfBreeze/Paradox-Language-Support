package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

interface ParadoxScopeNode : ParadoxLinkNode {
    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeNode {
            ParadoxSystemScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxStaticScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorScopeNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}

