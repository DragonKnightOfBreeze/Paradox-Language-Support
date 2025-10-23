package icu.windea.pls.ep.presentation

import javax.swing.JComponent

/**
 * 定义的图形表示。
 */
interface ParadoxDefinitionPresentation {
    @Suppress("unused")
    fun createHtml(): String? = null

    fun createComponent(): JComponent? = null
}
