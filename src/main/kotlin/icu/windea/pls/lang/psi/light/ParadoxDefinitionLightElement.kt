package icu.windea.pls.lang.psi.light

import icu.windea.pls.PlsIcons
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import java.util.*

class ParadoxDefinitionLightElement(
    parent: ParadoxDefinitionElement,
    val definitionInfo: ParadoxDefinitionInfo,
) : ParadoxLightElementBase(parent) {
    override val gameType get() = definitionInfo.gameType

    override fun getIcon(flags: Int) = PlsIcons.Nodes.Definition(definitionInfo.type)

    override fun getName() = definitionInfo.name

    override fun getText() = null

    override fun getProject() = definitionInfo.project

    override fun equals(other: Any?): Boolean {
        return other is ParadoxDefinitionLightElement && definitionInfo == other.definitionInfo
    }

    override fun hashCode(): Int {
        return Objects.hash(name, definitionInfo)
    }
}
