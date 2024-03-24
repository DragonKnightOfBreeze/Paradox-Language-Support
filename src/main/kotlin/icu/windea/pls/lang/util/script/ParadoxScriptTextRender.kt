package icu.windea.pls.lang.util.script

import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

@Suppress("unused", "KotlinConstantConditions")
object ParadoxScriptTextRender {
    fun render(element: ParadoxScriptProperty, renderInBlock: Boolean = false, conditional: Boolean = false, inline: Boolean = false): String {
        return buildString { renderTo(element, this, renderInBlock, conditional, inline) }
    }
    
    fun render(element: ParadoxScriptValue, renderInBlock: Boolean = false, conditional: Boolean = false, inline: Boolean = false): String {
        return buildString { renderTo(element, this, renderInBlock, conditional, inline) }
    }
    
    fun renderTo(element: ParadoxScriptProperty, builder: StringBuilder, renderInBlock: Boolean = false, conditional: Boolean = false, inline: Boolean = false) {
        val propertyKey = element.propertyKey
        builder.append(propertyKey.value()?.quoteIfNecessary() ?: PlsConstants.unresolvedString)
        builder.append(" = ")
        val propertyValue = element.propertyValue
        if(propertyValue != null) {
            renderTo(propertyValue, builder, renderInBlock, conditional, inline)
        } else {
            builder.append(PlsConstants.unresolvedString)
        }
    }
    
   fun renderTo(element: ParadoxScriptValue, builder: StringBuilder, renderInBlock: Boolean = false, conditional: Boolean = false, inline: Boolean = false) {
        when {
            renderInBlock && element is ParadoxScriptBlock -> {
                builder.append("{ ")
                element.processData(conditional, inline) { 
                    when {
                        it is ParadoxScriptProperty -> renderTo(it, builder, renderInBlock, conditional, inline)
                        it is ParadoxScriptValue -> renderTo(it, builder, renderInBlock, conditional, inline)
                        else -> return@processData true 
                    }
                    builder.append(" ")
                    true
                }
                builder.append("}")
            }
            else -> builder.append(element.value()?.quoteIfNecessary() ?: PlsConstants.unresolvedString)
        }
    }
}