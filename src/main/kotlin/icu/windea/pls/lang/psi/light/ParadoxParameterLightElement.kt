package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.psi.PsiReadWriteAccessAwareElement
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

/**
 * @see CwtDataTypes.Parameter
 * @see ParadoxParameterSupport
 */
class ParadoxParameterLightElement(
    parent: PsiElement,
    private val name: String,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    override val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner, PsiReadWriteAccessAwareElement {
    var canRename: Boolean = true

    override fun getIcon(flags: Int) = ChronicleIcons.Nodes.Parameter

    override fun getText() = name

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return other is ParadoxParameterLightElement
            && name == other.name
            && contextKey == other.contextKey
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, contextKey, gameType, project)
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String): PsiElement {
        if (!canRename) throw IncorrectOperationException() // cannot rename
        return this // do nothing
    }

    override fun getNameIdentifier(): PsiElement {
        return this // use self
    }
}

