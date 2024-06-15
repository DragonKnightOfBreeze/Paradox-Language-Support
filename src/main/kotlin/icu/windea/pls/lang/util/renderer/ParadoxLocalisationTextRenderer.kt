package icu.windea.pls.lang.util.renderer

import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.util.*

object ParadoxLocalisationTextRenderer {
    class Context(
        var builder: StringBuilder
    ) {
        val guardStack = LinkedList<String>() //防止StackOverflow
    }
    
    fun render(element: ParadoxLocalisationProperty): String {
        return buildString { renderTo(this, element) }
    }
    
    fun renderTo(builder: StringBuilder, element: ParadoxLocalisationProperty) {
        val context = Context(builder)
        context.guardStack.addLast(element.name)
        renderTo(element, context)
    }
    
    private fun renderTo(element: ParadoxLocalisationProperty, context: Context) {
        val richTextList = element.propertyValue?.richTextList
        if(richTextList.isNullOrEmpty()) return
        for(richText in richTextList) {
            renderTo(richText, context)
        }
    }
    
    private fun renderTo(element: ParadoxLocalisationRichText, context: Context) {
        when(element) {
            is ParadoxLocalisationString -> renderStringTo(element, context)
            is ParadoxLocalisationPropertyReference -> renderPropertyReferenceTo(element, context)
            is ParadoxLocalisationIcon -> renderIconTo(element, context)
            is ParadoxLocalisationCommand -> renderCommandTo(element, context)
            is ParadoxLocalisationColorfulText -> renderColorfulTextTo(element, context)
        }
    }
    
    private fun renderStringTo(element: ParadoxLocalisationString, context: Context) {
        ParadoxEscapeManager.unescapeLocalisationString(element.text, context.builder, ParadoxEscapeManager.Type.Default)
    }
    
    private fun renderPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, context: Context) {
        val resolved = element.reference?.resolve()
            ?: element.scriptedVariableReference?.reference?.resolve()
        when {
            resolved is ParadoxLocalisationProperty -> {
                val resolvedName = resolved.name
                if(context.guardStack.contains(resolvedName)) {
                    //infinite recursion, do not render context
                    context.builder.append(element.text)
                } else {
                    context.guardStack.addLast(resolvedName)
                    try {
                        renderTo(resolved, context)
                    } finally {
                        context.guardStack.removeLast()
                    }
                }
            }
            resolved is CwtProperty -> {
                context.builder.append(resolved.value)
            }
            resolved is ParadoxScriptScriptedVariable && resolved.value != null -> {
                context.builder.append(resolved.value)
            }
            else -> {
                context.builder.append(element.text)
            }
        }
    }
    
    private fun renderIconTo(element: ParadoxLocalisationIcon, context: Context) {
        //忽略
        //builder.append(":${element.name}:")
    }
    
    private fun renderCommandTo(element: ParadoxLocalisationCommand, context: Context) {
        val concept = element.concept
        if(concept != null) {
            //使用要显示的文本
            val conceptTextElement = ParadoxGameConceptHandler.getTextElement(concept)
            val richTextList = when {
                conceptTextElement is ParadoxLocalisationConceptText -> conceptTextElement.richTextList
                conceptTextElement is ParadoxLocalisationProperty -> conceptTextElement.propertyValue?.richTextList
                else -> null
            }
            if(richTextList != null) {
                for(v in richTextList) {
                    renderTo(v, context)
                }
            } else {
                context.builder.append(concept.text)
            }
            return
        }
        //使用原始文本
        context.builder.append(element.text)
    }
    
    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, context: Context) {
        //直接渲染其中的文本
        for(v in element.richTextList) {
            renderTo(v, context)
        }
    }
}