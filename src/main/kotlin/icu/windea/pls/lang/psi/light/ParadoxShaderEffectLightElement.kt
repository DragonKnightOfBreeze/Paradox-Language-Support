package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.core.psi.PsiReadWriteAccessAwareElement
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
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner, PsiReadWriteAccessAwareElement {
    override val readWriteAccess: ReadWriteAccess get() = ReadWriteAccess.Read

    override fun getIcon(flags: Int): Icon = PlsIcons.Nodes.ShaderEffect

    override fun getName() = name

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
}
