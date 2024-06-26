package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.highlighter.*

class ParadoxParameterizedScopeFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val configGroup: CwtConfigGroup
) : ParadoxScopeFieldNode {
    override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_KEY
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxParameterizedScopeFieldNode? {
            if(!text.isParameterized()) return null
            return ParadoxParameterizedScopeFieldNode(text, textRange, configGroup)
        }
    }
}
