package icu.windea.pls.core.codeInsight

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingListener
import com.intellij.codeInsight.template.impl.TemplateState

fun interface TemplateEditingFinishedListener : TemplateEditingListener {
    override fun beforeTemplateFinished(state: TemplateState, template: Template?) {}

    override fun templateCancelled(template: Template) {
        templateFinished(template, false)
    }

    override fun currentVariableChanged(templateState: TemplateState, template: Template, oldIndex: Int, newIndex: Int) {}

    override fun waitingForInput(template: Template) {}
}
