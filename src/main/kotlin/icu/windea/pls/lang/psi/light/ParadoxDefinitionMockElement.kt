package icu.windea.pls.lang.psi.light

import icu.windea.pls.PlsIcons
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import java.util.*

class ParadoxDefinitionMockElement(
    parent: ParadoxDefinitionElement,
    private val definitionInfo: ParadoxDefinitionInfo,
) : ParadoxMockPsiElement(parent) {
    override val gameType get() = definitionInfo.gameType

    override fun getIcon() = PlsIcons.Nodes.Definition(definitionInfo.type)

    override fun getName() = definitionInfo.name

    override fun getTypeName() = definitionInfo.type

    override fun getProject() = definitionInfo.project

    override fun equals(other: Any?): Boolean {
        return other is ParadoxDefinitionMockElement && definitionInfo == other.definitionInfo
    }

    override fun hashCode(): Int {
        return Objects.hash(name, definitionInfo)
    }
}
