package icu.windea.pls.ep.presentation

import javax.swing.JComponent

/**
 * 定义的图形表示的数据。
 */
@Suppress("unused")
interface ParadoxDefinitionPresentationData {
    fun createHtml(): String? = null

    fun createComponent(): JComponent? = null
}
