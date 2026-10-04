package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.psi.PsiReadWriteAccessAwareElement
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.model.ParadoxGameType
import java.util.*

/**
 * @see CwtMemberConfig
 */
class CwtConfigSymbolLightElement(
    parent: CwtStringExpressionElement,
    private val name: String,
    val configType: CwtConfigType,
    override val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
    private val project: Project
) : CwtConfigLightElementBase(parent), PsiNameIdentifierOwner, PsiReadWriteAccessAwareElement {
    override fun getIcon(flags: Int) = configType.icon

    override fun getText() = name

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtConfigSymbolLightElement
            && name == other.name
            && configType == other.configType
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, configType, gameType, project)
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String): PsiElement? {
        throw IncorrectOperationException() // cannot rename
    }

    override fun getNameIdentifier(): PsiElement {
        return this // use self
    }
}

