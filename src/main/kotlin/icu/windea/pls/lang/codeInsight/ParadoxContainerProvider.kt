package icu.windea.pls.lang.codeInsight

import com.intellij.codeInsight.ContainerProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.ParadoxBaseLanguage

class ParadoxContainerProvider : ContainerProvider {
    override fun getContainer(item: PsiElement): PsiElement? {
        if (item.language is ParadoxBaseLanguage) {
            //no implementation, only containingFile
            return item.containingFile
        }
        return null
    }
}
