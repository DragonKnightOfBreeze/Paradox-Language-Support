package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

class ParadoxDynamicValueLightElement(
    parent: PsiElement,
    private val name: String,
    val types: Set<String>,
    val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner {
    val presentableIcon: Icon get() = ParadoxDynamicValueManager.getPresentableIcon(types)
    val presentableType: String get() = ParadoxDynamicValueManager.getPresentableType(types)

    constructor(parent: PsiElement, name: String, dynamicValueType: String, readWriteAccess: ReadWriteAccess, gameType: ParadoxGameType, project: Project)
        : this(parent, name, setOf(dynamicValueType), readWriteAccess, gameType, project)

    override fun getIcon(flags: Int): Icon = presentableIcon

    override fun getName() = name

    override fun getText() = name

    override fun getProject() = project

    override fun setName(name: String): PsiElement = this

    override fun equals(other: Any?): Boolean {
        return other is ParadoxDynamicValueLightElement
            && name == other.name
            && types.any { it in other.types }
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, project, gameType)
    }
}
