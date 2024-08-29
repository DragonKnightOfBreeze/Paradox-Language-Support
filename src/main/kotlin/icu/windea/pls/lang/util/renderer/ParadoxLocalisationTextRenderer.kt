package icu.windea.pls.lang.util.renderer

import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
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
                if(ParadoxLocalisationManager.isSpecialLocalisation(resolved)) {
                    context.builder.append(element.text)
                } else {
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
    
    @Suppress("UNUSED_PARAMETER")
    private fun renderIconTo(element: ParadoxLocalisationIcon, context: Context) {
        //忽略
        //builder.append(":${element.name}:")
    }
    
    private fun renderCommandTo(element: ParadoxLocalisationCommand, context: Context) {
        //显示解析后的概念文本
        run r1@{
            val concept = element.concept ?: return@r1
            val (_, textElement) = ParadoxGameConceptManager.getReferenceElementAndTextElement(concept)
            val richTextList = when {
                textElement is ParadoxLocalisationConceptText -> textElement.richTextList
                textElement is ParadoxLocalisationProperty -> textElement.propertyValue?.richTextList
                else -> null
            }
            run r2@{
                if(richTextList == null) return@r2
                for(v in richTextList) {
                    renderTo(v, context)
                }
                return
            }
            context.builder.append(concept.text)
            return
        }
        
        //直接显示命令文本
        context.builder.append(element.text)
    }
    
    private fun renderColorfulTextTo(element: ParadoxLocalisationColorfulText, context: Context) {
        //直接渲染其中的文本
        for(v in element.richTextList) {
            renderTo(v, context)
        }
    }
}
