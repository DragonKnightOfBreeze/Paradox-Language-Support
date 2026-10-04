package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.lang.psi.ParadoxSnippetElement
import icu.windea.pls.model.ParadoxGameType
import java.util.*

/**
 * @see CwtDataTypes.DefinitionSnippet
 */
class ParadoxDefinitionSnippetLightElement(
    parent: PsiElement,
    private val name: String, // TODO 3.0.4
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner, ParadoxSnippetElement {
    override fun getIcon(flags: Int) = ChronicleIcons.Nodes.Snippet

    override fun getText() = name

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return other is ParadoxDefinitionSnippetLightElement
            && name == other.name
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, gameType, project)
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String): PsiElement {
        return this // do nothing
    }

    override fun getNameIdentifier(): PsiElement {
        return this // use self
    }
}
