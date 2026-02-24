package icu.windea.pls.lang.psi.light

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

class ParadoxDynamicValueElement(
    parent: PsiElement,
    private val name: String,
    val dynamicValueTypes: Set<String>,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxMockPsiElement(parent) {
    val dynamicValueType: String get() = ParadoxDynamicValueManager.getPresentableType(dynamicValueTypes)

    constructor(parent: PsiElement, name: String, dynamicValueType: String, readWriteAccess: ReadWriteAccessDetector.Access, gameType: ParadoxGameType, project: Project)
        : this(parent, name, setOf(dynamicValueType), readWriteAccess, gameType, project)

    override fun getIcon(): Icon {
        val dynamicValueType = dynamicValueTypes.first() // first is ok
        return PlsIcons.Nodes.DynamicValue(dynamicValueType)
    }

    override fun getName(): String {
        return name
    }

    override fun getTypeName(): String {
        val dynamicValueType = dynamicValueTypes.first() // first is ok
        return when (dynamicValueType) {
            "variable" -> PlsBundle.message("type.variable")
            else -> PlsBundle.message("type.dynamicValue")
        }
    }

    override fun getText(): String {
        return name
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxDynamicValueElement
            && name == other.name
            && dynamicValueTypes.any { it in other.dynamicValueTypes }
            && project == other.project
            && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(name, project, gameType)
    }
}
