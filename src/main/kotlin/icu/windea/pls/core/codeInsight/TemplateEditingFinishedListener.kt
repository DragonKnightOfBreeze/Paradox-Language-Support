package icu.windea.pls.core.codeInsight

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingListener
import com.intellij.codeInsight.template.impl.TemplateState

/**
 * 仅关心“模板完成”事件的 [TemplateEditingListener] 简化版。
 *
 * 声明为 `fun interface`，单一抽象方法为 `templateFinished(template, brokenOff)`，
 * 其余回调均提供空实现（或转换为调用 `templateFinished(..., false)`）。
 */
fun interface TemplateEditingFinishedListener : TemplateEditingListener {
    /** 模板完成前的回调：默认无操作。*/
    override fun beforeTemplateFinished(state: TemplateState, template: Template?) {}

    /** 模板取消：默认转为调用 `templateFinished(template, false)`。*/
    override fun templateCancelled(template: Template) {
        templateFinished(template, false)
    }

    /** 当前变量索引变化：默认无操作。*/
    override fun currentVariableChanged(templateState: TemplateState, template: Template, oldIndex: Int, newIndex: Int) {}

    /** 等待输入：默认无操作。*/
    override fun waitingForInput(template: Template) {}
}
