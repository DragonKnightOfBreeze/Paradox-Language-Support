package icu.windea.pls.lang.util.renderers

import icu.windea.pls.core.quoteIfNecessary
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.members
import icu.windea.pls.script.psi.value

class ParadoxScriptTextRenderer(
    val builder: StringBuilder = StringBuilder(),
    var renderInBlock: Boolean = false
) {
    fun render(element: ParadoxScriptMemberElement): String {
        renderTo(element)
        return builder.toString()
    }

    fun renderTo(element: ParadoxScriptMemberElement) {
        when (element) {
            is ParadoxScriptProperty -> renderPropertyTo(element)
            is ParadoxScriptValue -> renderValueTo(element)
            else -> throw UnsupportedOperationException()
        }
    }

    private fun renderPropertyTo(element: ParadoxScriptProperty) {
        val propertyKey = element.propertyKey
        builder.append(propertyKey.value()?.quoteIfNecessary() ?: PlsStringConstants.unresolved)
        builder.append(" = ")
        val propertyValue = element.propertyValue
        if (propertyValue != null) {
            renderValueTo(propertyValue)
        } else {
            builder.append(PlsStringConstants.unresolved)
        }
    }

    private fun renderValueTo(element: ParadoxScriptValue) {
        when {
            renderInBlock && element is ParadoxScriptBlock -> {
                builder.append("{ ")
                element.members().forEach f@{
                    when {
                        it is ParadoxScriptProperty -> renderPropertyTo(it)
                        it is ParadoxScriptValue -> renderValueTo(it)
                        else -> return@f
                    }
                    builder.append(" ")
                }
                builder.append("}")
            }
            else -> builder.append(element.value()?.quoteIfNecessary() ?: PlsStringConstants.unresolved)
        }
    }
}
