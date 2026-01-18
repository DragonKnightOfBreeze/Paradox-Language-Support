package icu.windea.pls.lang.psi.mock

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.model.CwtMemberType
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

/**
 * 用于为合成的（注入的/合并后的）CWT规则提供声明处。
 */
class CwtMemberConfigElement(
    parent: PsiElement,
    val config: CwtMemberConfig<*>,
    override val gameType: ParadoxGameType,
    private val project: Project
) : CwtConfigMockPsiElement(parent) {
    override fun getIcon(): Icon {
        return when (config.memberType) {
            CwtMemberType.PROPERTY -> PlsIcons.Nodes.CwtProperty
            CwtMemberType.VALUE -> PlsIcons.Nodes.CwtValue
        }
    }

    override fun getName(): String {
        return config.configExpression.expressionString
    }

    override fun getTypeName(): String {
        return when (config.memberType) {
            CwtMemberType.PROPERTY -> PlsBundle.message("cwt.description.property")
            CwtMemberType.VALUE -> PlsBundle.message("cwt.description.value")
        }
    }

    override fun getText(): String {
        return config.toString()
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is CwtMemberConfigElement
            && config == other.config
            && project == other.project
            && gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(config, project, gameType)
    }
}
