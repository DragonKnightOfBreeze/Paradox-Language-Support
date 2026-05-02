package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import java.util.*
import javax.swing.Icon
import icu.windea.pls.model.ParadoxGameType

class ParadoxDynamicValueLightElement(
    parent: PsiElement,
    private val name: String,
    val dynamicValueTypes: Set<String>,
    val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner {
    val dynamicValueType: String get() = ParadoxDynamicValueManager.getPresentableType(dynamicValueTypes)

    constructor(parent: PsiElement, name: String, dynamicValueType: String, readWriteAccess: ReadWriteAccess, gameType: ParadoxGameType, project: Project)
        : this(parent, name, setOf(dynamicValueType), readWriteAccess, gameType, project)

    override fun getIcon(flags: Int): Icon {
        val dynamicValueType = dynamicValueTypes.first() // first is ok
        return PlsIcons.Nodes.DynamicValue(dynamicValueType)
    }

    override fun getName() = name

    override fun getText() = name

    override fun getProject() = project

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement {
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxDynamicValueLightElement
            && name == other.name
            && dynamicValueTypes.any { it in other.dynamicValueTypes }
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, project, gameType)
    }
}
