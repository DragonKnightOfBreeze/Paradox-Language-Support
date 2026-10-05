package icu.windea.pls.ep.util.presentation

import icu.windea.pls.lang.presentation.ParadoxDefinitionPresentationService
import javax.swing.JComponent

/**
 * 定义的图形展示。
 *
 * @see ParadoxDefinitionPresentationProvider
 * @see ParadoxDefinitionPresentationService
 */
interface ParadoxDefinitionPresentation {
    @Suppress("unused")
    fun createHtml(): String? = null

    fun createComponent(): JComponent? = null
}
