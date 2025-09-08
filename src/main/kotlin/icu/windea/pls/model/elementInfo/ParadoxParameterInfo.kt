package icu.windea.pls.model.elementInfo

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.ep.parameter.ParadoxParameterSupport
import icu.windea.pls.ep.parameter.support
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.model.ParadoxGameType
import javax.swing.Icon

data class ParadoxParameterInfo(
    override val name: String,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    override val gameType: ParadoxGameType,
    override val project: Project,
) : UserDataHolderBase(), ParadoxElementInfo {
    val modificationTracker by lazy { support?.getModificationTracker(this) }

    companion object {
        val EMPTY by lazy { ParadoxParameterInfo("", "", null, "", ParadoxGameType.Core, getDefaultProject()) }
    }
}

fun ParadoxParameterInfo.toPsiElement(parent: PsiElement, readWriteAccess: ReadWriteAccessDetector.Access): ParadoxParameterElement {
    return ParadoxParameterElement(parent, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        .also { ParadoxParameterSupport.Keys.syncUserData(this, it) }
}

fun ParadoxParameterElement.toInfo(): ParadoxParameterInfo {
    return ParadoxParameterInfo(name, contextName, contextIcon, contextKey, gameType, project)
        .also { ParadoxParameterSupport.Keys.syncUserData(this, it) }
}
