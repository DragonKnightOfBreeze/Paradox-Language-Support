package icu.windea.pls.ep.presentation

import javax.swing.*

/**
 * 定义的图形表示的数据。
 */
@Suppress("unused")
interface ParadoxDefinitionPresentation {
    fun createHtml(): String? = null

    fun createComponent(): JComponent? = null
}
