package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.psi.PsiReadWriteAccessAwareElement
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.lang.psi.ParadoxExternalReferenceElement
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

/**
 * @see CwtDataTypes.ShaderEffect
 */
class ParadoxShaderEffectLightElement(
    parent: PsiElement,
    private val name: String,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), ParadoxExternalReferenceElement, PsiReadWriteAccessAwareElement {
    override val readWriteAccess: ReadWriteAccess get() = ReadWriteAccess.Read

    override fun getIcon(flags: Int): Icon = ChronicleIcons.Nodes.ShaderEffect

    override fun getText() = name

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return other is ParadoxShaderEffectLightElement
            && name == other.name
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, project, gameType)
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String): PsiElement {
        return this // do nothing (actual external references will not be updated)
    }

    override fun getNameIdentifier(): PsiElement {
        return this // use self
    }
}
