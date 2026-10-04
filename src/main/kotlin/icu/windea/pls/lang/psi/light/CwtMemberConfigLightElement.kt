package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

/**
 * @see CwtMemberConfig
 */
class CwtMemberConfigLightElement(
    parent: PsiElement,
    val config: CwtMemberConfig<*>,
    override val gameType: ParadoxGameType,
    private val project: Project
) : CwtConfigLightElementBase(parent), PsiNameIdentifierOwner {
    override fun getIcon(flags: Int): Icon {
        return when (config) {
            is CwtPropertyConfig -> ChronicleIcons.Nodes.Property
            is CwtValueConfig -> ChronicleIcons.Nodes.Value
        }
    }

    override fun getText() = config.toString()

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return other is CwtMemberConfigLightElement
            && config == other.config
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(config, gameType, project)
    }

    override fun getName(): String {
        return config.configExpression.expressionString
    }

    override fun setName(name: String): PsiElement? {
        throw IncorrectOperationException() // cannot rename
    }

    override fun getNameIdentifier(): PsiElement {
        return this // use self
    }
}
