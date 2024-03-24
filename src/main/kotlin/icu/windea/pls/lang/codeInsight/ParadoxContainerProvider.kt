package icu.windea.pls.lang.codeInsight

import com.intellij.codeInsight.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

class ParadoxContainerProvider : ContainerProvider {
    override fun getContainer(item: PsiElement): PsiElement? {
        if(item.language.isParadoxLanguage()) {
            //no implementation, only containingFile
            return item.containingFile
        }
        return null
    }
}