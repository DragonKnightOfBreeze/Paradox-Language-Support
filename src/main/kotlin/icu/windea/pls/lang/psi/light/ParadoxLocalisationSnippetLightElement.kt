package icu.windea.pls.lang.psi.light

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.impl.light.LightElement
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.util.UnaryTemplateInfo
import icu.windea.pls.lang.psi.ParadoxSnippetElement
import icu.windea.pls.model.ParadoxGameType
import java.util.*

/**
 * 本地化引用片段对应的 [LightElement]。
 *
 * 说明：
 * - 这并非完整匹配，因此并不直接解析为对应的本地化，而是解析为这种特殊的相关项。
 * - [name] 为片段文本，[snippetTemplates] 为用于解析完整引用文本的模板参数。
 *
 * @see CwtDataTypes.LocalisationSnippet
 * @since 3.0.4
 */
class ParadoxLocalisationSnippetLightElement(
    parent: PsiElement,
    private val name: String,
    override val snippetTemplates: List<UnaryTemplateInfo>,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent), PsiNameIdentifierOwner, ParadoxSnippetElement {
    override fun getIcon(flags: Int) = ChronicleIcons.Nodes.LocalisationSnippet

    override fun getText() = name

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return other is ParadoxLocalisationSnippetLightElement
            && name == other.name
            && snippetTemplates == other.snippetTemplates
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, snippetTemplates, gameType, project)
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
