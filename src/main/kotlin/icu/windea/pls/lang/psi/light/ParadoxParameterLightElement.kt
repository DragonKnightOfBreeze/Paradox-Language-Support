package icu.windea.pls.lang.psi.light

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsIcons
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.model.ParadoxGameType
import java.util.*
import javax.swing.Icon

/**
 * @see ParadoxParameterSupport
 */
class ParadoxParameterLightElement(
    parent: PsiElement,
    private val name: String,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val gameType: ParadoxGameType,
    private val project: Project,
) : ParadoxLightElementBase(parent) {
    override fun getIcon(flags: Int) = PlsIcons.Nodes.Parameter

    override fun getName() = name

    override fun getText() = name

    override fun getProject() = project

    override fun equals(other: Any?): Boolean {
        return other is ParadoxParameterLightElement
            && name == other.name
            && contextKey == other.contextKey
            && gameType == other.gameType
            && project == other.project
    }

    override fun hashCode(): Int {
        return Objects.hash(name, contextKey, project, gameType)
    }
}

