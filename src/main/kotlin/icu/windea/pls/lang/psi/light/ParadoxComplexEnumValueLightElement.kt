package icu.windea.pls.lang.psi.light

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeType
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeTypes
import icu.windea.pls.model.ParadoxGameType
import java.util.*

class ParadoxComplexEnumValueLightElement(
    parent: PsiElement,
    private val name: String,
    val enumName: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner {
    val config: CwtComplexEnumConfig?
        get() = PlsFacade.getConfigGroup(project, gameType).complexEnums.get(enumName)
    val caseInsensitive: Boolean
        get() = config?.caseInsensitive ?: false
    val searchScopeType: ParadoxSearchScopeType
        get() = when {
            config?.perDefinition == true -> ParadoxSearchScopeTypes.Definition
            else -> ParadoxSearchScopeTypes.All
        }

    override fun getIcon(flags: Int) = PlsIcons.Nodes.ComplexEnumValue(enumName)

    override fun getName() = name

    override fun getText() = name

    override fun getProject() = project

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement {
        return this
    }

    override fun equals(other: Any?): Boolean {
        return other is ParadoxComplexEnumValueLightElement
            && name.equals(other.name, caseInsensitive) // # 261
            && enumName == other.enumName
            && gameType == other.gameType
            && project == other.project
            && searchScopeType.findRoot(project, parent) == other.searchScopeType.findRoot(other.project, other.parent)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, enumName, project, gameType)
    }
}
