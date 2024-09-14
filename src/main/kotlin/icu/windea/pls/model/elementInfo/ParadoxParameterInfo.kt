package icu.windea.pls.model.elementInfo

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.model.*
import javax.swing.*

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
        val EMPTY = ParadoxParameterInfo("", "", null, "", ParadoxGameType.placeholder(), getDefaultProject())
    }
}

fun ParadoxParameterInfo.toPsiElement(parent: PsiElement, rangeInParent: TextRange?, readWriteAccess: ReadWriteAccessDetector.Access): ParadoxParameterElement {
    return ParadoxParameterElement(parent, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project).also { syncUserData(this, it) }
}

fun ParadoxParameterElement.toInfo(): ParadoxParameterInfo {
    return ParadoxParameterInfo(name, contextName, contextIcon, contextKey, gameType, project).also { syncUserData(this, it) }
}

//use optimized method rather than UserDataHolderBase.copyUserDataTo to reduce memory usage
private fun syncUserData(from: UserDataHolder, to : UserDataHolder) {
    ParadoxParameterSupport.Keys.keysToSync.forEach { key ->
        @Suppress("UNCHECKED_CAST")
        key as Key<Any>
        to.putUserData(key, from.getUserData(key))
    }
}

