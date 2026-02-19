package icu.windea.pls.ep.util.presentation

import icu.windea.pls.core.createPointer
import icu.windea.pls.script.psi.ParadoxDefinitionElement

abstract class ParadoxDefinitionPresentationBase(
    element: ParadoxDefinitionElement
) : ParadoxDefinitionPresentation {
    protected val pointer = element.createPointer()
    protected val element get() = pointer.element
}
