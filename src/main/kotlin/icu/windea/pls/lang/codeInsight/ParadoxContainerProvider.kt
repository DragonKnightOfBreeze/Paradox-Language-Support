package icu.windea.pls.lang.codeInsight

import com.intellij.codeInsight.*
import com.intellij.psi.*
import icu.windea.pls.lang.*

class ParadoxContainerProvider : ContainerProvider {
    override fun getContainer(item: PsiElement): PsiElement? {
        if (item.language is ParadoxBaseLanguage) {
            //no implementation, only containingFile
            return item.containingFile
        }
        return null
    }
}
