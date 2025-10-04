package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup

sealed interface ParadoxScopeLinkNode : ParadoxLinkNode {
    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeLinkNode {
            ParadoxSystemScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxScopeNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxDynamicScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            ParadoxParameterizedScopeLinkNode.resolve(text, textRange, configGroup)?.let { return it }
            return ParadoxErrorScopeLinkNode(text, textRange, configGroup)
        }
    }

    companion object : Resolver()
}

