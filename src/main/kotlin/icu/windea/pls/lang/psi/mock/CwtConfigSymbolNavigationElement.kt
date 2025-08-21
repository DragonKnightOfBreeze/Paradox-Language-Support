package icu.windea.pls.lang.psi.mock

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import java.util.Objects
import javax.swing.Icon

/**
 * 用于在 *随处搜索* 中查找CWT规则符号。
 */
class CwtConfigSymbolNavigationElement(
    parent: PsiElement,
    private val name: String,
    val configType: CwtConfigType,
    val gameType: ParadoxGameType,
    private val project: Project
) : ParadoxMockPsiElement(parent) {
    override fun getIcon(): Icon? {
        return configType.icon
    }

    override fun getName(): String {
        return name
    }

    override fun getTypeName(): String? {
        return configType.description
    }

    override fun getText(): String {
        return name
    }

    override fun getProject(): Project {
        return project
    }

    override fun getNavigationElement(): PsiElement {
        return parent
    }

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtConfigSymbolNavigationElement
            && name == other.name
            && configType == other.configType
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, configType, gameType, project)
    }
}
