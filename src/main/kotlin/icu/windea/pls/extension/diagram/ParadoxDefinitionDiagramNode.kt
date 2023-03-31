package icu.windea.pls.extension.diagram

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.script.psi.*

open class ParadoxDefinitionDiagramNode(
    element: ParadoxScriptDefinitionElement,
    override val provider: ParadoxDefinitionDiagramProvider
) : ParadoxDiagramNode(element, provider) {
    override fun getIdentifyingElement(): ParadoxScriptDefinitionElement {
        return super.getIdentifyingElement() as ParadoxScriptDefinitionElement
    }
    
    override open fun getTooltip(): String? {
        return identifyingElement.definitionInfo?.name
    }
}