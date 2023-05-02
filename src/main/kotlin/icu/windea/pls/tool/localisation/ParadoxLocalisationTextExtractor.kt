package icu.windea.pls.tool.localisation

import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import java.util.*

@Suppress("unused", "UNUSED_PARAMETER")
object ParadoxLocalisationTextExtractor {
    class Context {
        val builder = StringBuilder()
        val guardStack = LinkedList<String>() //防止StackOverflow
    }
    
    fun extract(element: ParadoxLocalisationProperty): String {
        val context = Context()
        context.guardStack.addLast(element.name)
        extractTo(element, context)
        return context.builder.toString()
    }
    
    private fun extractTo(element: ParadoxLocalisationProperty, context: Context) {
        element.propertyValue?.richTextList?.forEach { extractTo(it, context) }
    }
    
    private fun extractTo(element: ParadoxLocalisationRichText, context: Context) {
        when(element) {
            is ParadoxLocalisationString -> extractStringTo(element, context)
            is ParadoxLocalisationEscape -> extractEscapeTo(element, context)
            is ParadoxLocalisationPropertyReference -> extractPropertyReferenceTo(element, context)
            is ParadoxLocalisationIcon -> extractIconTo(element, context)
            is ParadoxLocalisationCommand -> extractCommandTo(element, context)
            is ParadoxLocalisationColorfulText -> extractColorfulTextTo(element, context)
        }
    }
    
    private fun extractStringTo(element: ParadoxLocalisationString, context: Context) {
        context.builder.append(element.text)
    }
    
    private fun extractEscapeTo(element: ParadoxLocalisationEscape, context: Context) {
        val elementText = element.text
        when {
            elementText == "\\n" -> context.builder.append("\n")
            elementText == "\\t" -> context.builder.append("\t")
            elementText.length > 1 -> context.builder.append(elementText[1])
        }
    }
    
    private fun extractPropertyReferenceTo(element: ParadoxLocalisationPropertyReference, context: Context) {
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
					extractTo(resolved, context)
					context.guardStack.removeLast()
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
    
    private fun extractIconTo(element: ParadoxLocalisationIcon, context: Context) {
        //NOTE 不提取到结果中
        //builder.append(":${element.name}:")
    }
    
    private fun extractCommandTo(element: ParadoxLocalisationCommand, context: Context) {
        //使用原始文本
        context.builder.append(element.text)
    }
    
    private fun extractColorfulTextTo(element: ParadoxLocalisationColorfulText, context: Context) {
        //直接渲染其中的文本
        for(v in element.richTextList) {
            extractTo(v, context)
        }
    }
}