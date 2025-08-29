package icu.windea.pls.lang.psi.mock

import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.orNull
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import java.util.*
import javax.swing.Icon

/**
 * 用于在 *随处搜索* 中查找定义。
 */
class ParadoxDefinitionNavigationElement(
    val parent: ParadoxScriptDefinitionElement,
    private val definitionInfo: ParadoxDefinitionInfo
) : ParadoxMockPsiElement(parent) {
    override fun getIcon(): Icon {
        return PlsIcons.Nodes.Definition(definitionInfo.type)
    }

    override fun getName(): String? {
        return definitionInfo.name.orNull()
    }

    override fun getTypeName(): String {
        return PlsBundle.message("script.description.definition")
    }

    override fun getText(): String? {
        return parent.text
    }

    override fun getNameIdentifier(): PsiElement? {
        return parent.nameIdentifier
    }

    override fun getNavigationElement(): PsiElement {
        return parent
    }

    override val gameType: ParadoxGameType get() = definitionInfo.gameType

    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionNavigationElement
            && definitionInfo == other.definitionInfo
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(definitionInfo, project)
    }
}
