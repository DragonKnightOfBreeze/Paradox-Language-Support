package icu.windea.pls.lang.util.renderer

import icu.windea.pls.core.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

object ParadoxScriptTextRenderer {
    fun render(element: ParadoxScriptProperty, renderInBlock: Boolean = false, conditional: Boolean = false, inline: Boolean = false): String {
        return buildString { renderTo(element, this, renderInBlock, conditional, inline) }
    }

    fun render(element: ParadoxScriptValue, renderInBlock: Boolean = false, conditional: Boolean = false, inline: Boolean = false): String {
        return buildString { renderTo(element, this, renderInBlock, conditional, inline) }
    }

    fun renderTo(element: ParadoxScriptProperty, builder: StringBuilder, renderInBlock: Boolean = false, conditional: Boolean = false, inline: Boolean = false) {
        val propertyKey = element.propertyKey
        builder.append(propertyKey.value()?.quoteIfNecessary() ?: PlsStringConstants.unresolved)
        builder.append(" = ")
        val propertyValue = element.propertyValue
        if (propertyValue != null) {
            renderTo(propertyValue, builder, renderInBlock, conditional, inline)
        } else {
            builder.append(PlsStringConstants.unresolved)
        }
    }

    fun renderTo(element: ParadoxScriptValue, builder: StringBuilder, renderInBlock: Boolean = false, conditional: Boolean = false, inline: Boolean = false) {
        when {
            renderInBlock && element is ParadoxScriptBlock -> {
                builder.append("{ ")
                element.processMember(conditional, inline) {
                    when {
                        it is ParadoxScriptProperty -> renderTo(it, builder, renderInBlock, conditional, inline)
                        it is ParadoxScriptValue -> renderTo(it, builder, renderInBlock, conditional, inline)
                        else -> return@processMember true
                    }
                    builder.append(" ")
                    true
                }
                builder.append("}")
            }
            else -> builder.append(element.value()?.quoteIfNecessary() ?: PlsStringConstants.unresolved)
        }
    }
}
