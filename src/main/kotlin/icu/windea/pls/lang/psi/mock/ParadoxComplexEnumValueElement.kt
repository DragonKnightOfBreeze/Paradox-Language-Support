package icu.windea.pls.lang.psi.mock

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeType
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeTypes
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

/**
 * 复杂枚举值其实也不存在一个真正意义上的声明处，用这个模拟。（通过complexEnum规则匹配到的那些是可以同名的）
 */
class ParadoxComplexEnumValueElement(
    parent: PsiElement,
    private val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxMockPsiElement(parent) {
    val searchScopeType: ParadoxSearchScopeType
        get() = ParadoxSearchScopeTypes.get(PlsFacade.getConfigGroup(project, gameType).complexEnums.get(enumName)?.searchScopeType)

    override fun getIcon(): Icon {
        return PlsIcons.Nodes.EnumValue
    }

    override fun getName(): String {
        return name
    }

    override fun getTypeName(): String {
        return PlsBundle.message("script.description.complexEnumValue")
    }

    override fun getText(): String {
        return name
    }

    override fun getProject(): Project {
        return project
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxComplexEnumValueElement &&
            name == other.name &&
            enumName == other.enumName &&
            project == other.project &&
            gameType == other.gameType &&
            searchScopeType.findRoot(project, parent) == other.searchScopeType.findRoot(other.project, other.parent)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, enumName, project, gameType)
    }
}
