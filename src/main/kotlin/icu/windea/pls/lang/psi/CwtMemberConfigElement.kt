package icu.windea.pls.lang.psi

import com.intellij.lang.*
import com.intellij.navigation.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.navigation.*
import icu.windea.pls.model.*
import java.util.*
import javax.swing.*

/**
 * 用于为合成的（注入的/合并后的）CWT规则提供声明处。
 */
class CwtMemberConfigElement(
    parent: PsiElement,
    val config: CwtMemberConfig<*>,
    val gameType: ParadoxGameType,
    private val project: Project
) : ParadoxFakePsiElement(parent) {
    override fun getIcon(): Icon {
        return when (config) {
            is CwtPropertyConfig -> PlsIcons.Nodes.CwtProperty
            is CwtValueConfig -> PlsIcons.Nodes.CwtValue
        }
    }

    override fun getName(): String {
        return config.expression.expressionString
    }

    override fun getTypeName(): String {
        return when (config) {
            is CwtPropertyConfig -> PlsBundle.message("cwt.description.property")
            is CwtValueConfig -> PlsBundle.message("cwt.description.value")
        }
    }

    override fun getText(): String {
        return config.toString()
    }

    override fun getPresentation(): ItemPresentation {
        return CwtItemPresentation(this)
    }

    override fun getLanguage(): Language {
        return CwtLanguage
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is CwtMemberConfigElement &&
            config == other.config &&
            project == other.project &&
            gameType == other.gameType
    }

    override fun hashCode(): Int {
        return Objects.hash(config, project, gameType)
    }
}
