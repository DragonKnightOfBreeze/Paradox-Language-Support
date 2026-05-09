package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import java.util.*
import javax.swing.Icon
import icu.windea.pls.model.ParadoxGameType

class CwtMemberConfigLightElement(
    parent: PsiElement,
    val config: CwtMemberConfig<*>,
    override val gameType: ParadoxGameType,
    private val project: Project
) : CwtConfigLightElementBase(parent) {
    override fun getIcon(flags: Int): Icon {
        return when (config) {
            is CwtPropertyConfig -> PlsIcons.Nodes.Property
            is CwtValueConfig -> PlsIcons.Nodes.Value
        }
    }

    override fun getName() = config.configExpression.expressionString

    override fun getText() = config.toString()

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return other is CwtMemberConfigLightElement
            && config == other.config
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(config, project, gameType)
    }
}
