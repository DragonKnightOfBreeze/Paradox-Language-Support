package icu.windea.pls.ep.util.presentation

import icu.windea.pls.lang.util.presentation.ParadoxPresentationService
import javax.swing.JComponent

/**
 * 定义的图形展示。
 *
 * @see ParadoxDefinitionPresentationProvider
 * @see ParadoxPresentationService
 */
interface ParadoxDefinitionPresentation {
    @Suppress("unused")
    fun createHtml(): String? = null

    fun createComponent(): JComponent? = null
}
