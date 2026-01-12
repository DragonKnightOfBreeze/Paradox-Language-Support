package icu.windea.pls.model

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.PsiElement
import icu.windea.pls.core.getDefaultProject
import icu.windea.pls.core.util.copy
import icu.windea.pls.ep.resolve.parameter.ParadoxParameterSupport
import icu.windea.pls.ep.resolve.parameter.support
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import javax.swing.Icon

/**
 * 参数信息。
 */
data class ParadoxParameterInfo(
    val name: String,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    val gameType: ParadoxGameType,
    val project: Project,
) : UserDataHolderBase() {
    val modificationTracker by lazy { support?.getModificationTracker(this) }

    companion object {
        val EMPTY by lazy { ParadoxParameterInfo("", "", null, "", ParadoxGameType.Core, getDefaultProject()) }
    }
}

@Suppress("unused")
fun ParadoxParameterInfo.toPsiElement(parent: PsiElement, readWriteAccess: ReadWriteAccessDetector.Access): ParadoxParameterElement {
    return ParadoxParameterElement(parent, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        .also { ParadoxParameterSupport.Keys.sync(this, it) }
}

fun ParadoxParameterElement.toInfo(): ParadoxParameterInfo {
    return ParadoxParameterInfo(name, contextName, contextIcon, contextKey, gameType, project)
        .also { ParadoxParameterSupport.Keys.sync(this, it) }
}
