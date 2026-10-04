package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.core.psi.PsiReadWriteAccessAwareElement
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.lang.search.scope.ParadoxSearchScopeType
import icu.windea.pls.lang.search.scope.ParadoxSearchScopeTypes
import icu.windea.pls.model.ParadoxGameType
import java.util.*

/**
 * @see CwtDataTypes.EnumValue
 */
class ParadoxComplexEnumValueLightElement(
    parent: PsiElement,
    private val name: String,
    val enumName: String,
    override val readWriteAccess: ReadWriteAccess,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner, PsiReadWriteAccessAwareElement {
    val config: CwtComplexEnumConfig?
        get() = ChronicleFacade.getConfigGroup(project, gameType).complexEnums.get(enumName)
    val caseInsensitive: Boolean
        get() = config?.caseInsensitive ?: false
    val searchScopeType: ParadoxSearchScopeType
        get() = when {
            config?.perDefinition == true -> ParadoxSearchScopeTypes.Definition
            else -> ParadoxSearchScopeTypes.All
        }

    override fun getIcon(flags: Int) = ChronicleIcons.Nodes.ComplexEnumValue(enumName)

    override fun getText() = name

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return other is ParadoxComplexEnumValueLightElement
            && name.equals(other.name, caseInsensitive) // # 261
            && enumName == other.enumName
            && gameType == other.gameType
            && project == other.project
            && searchScopeType.findRoot(project, parent) == other.searchScopeType.findRoot(other.project, other.parent)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, enumName, gameType, project)
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String): PsiElement {
        return this // do nothing
    }

    override fun getNameIdentifier(): PsiElement {
        return this // use self
    }
}
