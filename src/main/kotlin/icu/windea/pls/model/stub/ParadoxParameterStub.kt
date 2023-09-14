package icu.windea.pls.model.stub

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.model.*
import javax.swing.*

data class ParadoxParameterStub(
    val name: String,
    val contextName: String,
    val contextIcon: Icon?,
    val contextKey: String,
    val gameType: ParadoxGameType,
    val project: Project,
) : UserDataHolderBase() {
    val modificationTracker by lazy { support?.getModificationTracker(this) }
    
    companion object {
        val EMPTY = ParadoxParameterStub("", "", null, "", ParadoxGameType.placeholder(), getDefaultProject())
    }
}

fun ParadoxParameterStub.toPsiElement(parent: PsiElement, rangeInParent: TextRange?, readWriteAccess: ReadWriteAccessDetector.Access): ParadoxParameterElement {
    return ParadoxParameterElement(parent, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
}

fun ParadoxParameterElement.toStub(): ParadoxParameterStub {
    return ParadoxParameterStub(name, contextName, contextIcon, contextKey, gameType, project)
}