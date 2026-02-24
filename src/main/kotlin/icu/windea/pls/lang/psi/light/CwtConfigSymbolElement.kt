package icu.windea.pls.lang.psi.light

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

class CwtConfigSymbolElement(
    parent: CwtStringExpressionElement,
    private val name: String,
    val configType: CwtConfigType,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
    private val project: Project
) : CwtConfigMockPsiElement(parent) {
    override fun getIcon(): Icon? {
        return configType.icon
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String): PsiElement? {
        throw IncorrectOperationException() // cannot rename
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

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtConfigSymbolElement
            && name == other.name
            && configType == other.configType
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, configType, gameType, project)
    }
}

