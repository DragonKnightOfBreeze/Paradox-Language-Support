package icu.windea.pls.core.codeInsight

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*

fun interface TemplateEditingFinishedListener : TemplateEditingListener {
    override fun beforeTemplateFinished(state: TemplateState, template: Template?) {}
    
    override fun templateCancelled(template: Template) {
        templateFinished(template, false)
    }
    
    override fun currentVariableChanged(templateState: TemplateState, template: Template, oldIndex: Int, newIndex: Int) {}
    
    override fun waitingForInput(template: Template) {}
}
