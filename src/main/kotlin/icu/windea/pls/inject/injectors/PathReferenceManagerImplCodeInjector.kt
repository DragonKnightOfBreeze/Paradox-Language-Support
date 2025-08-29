@file:Suppress("UnstableApiUsage", "unused")

package icu.windea.pls.inject.injectors

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.paths.PathReferenceProvider
import com.intellij.openapi.paths.PsiDynaReference
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectTarget
import icu.windea.pls.lang.references.paths.ParadoxPathReference

/**
 * @see com.intellij.openapi.paths.PathReferenceManagerImpl
 * @see com.intellij.openapi.paths.PathReferenceManagerImpl.createReferences
 */
@InjectTarget("com.intellij.openapi.paths.PathReferenceManagerImpl")
class PathReferenceManagerImplCodeInjector : CodeInjectorBase() {
    //如果解析得到的路径引用中包含 ParadoxPathReference，则仅保留它

    @InjectMethod(pointer = InjectMethod.Pointer.AFTER)
    fun createReferences(
        element: PsiElement,
        soft: Boolean,
        endingSlashNotAllowed: Boolean,
        relativePathsAllowed: Boolean,
        suitableFileTypes: Array<out FileType>?,
        vararg additionalProviders: PathReferenceProvider,
        returnValue: Array<out PsiReference>
    ): Array<out PsiReference> {
        val reference = findReference(returnValue)
        if (reference != null) return arrayOf(reference)
        return returnValue
    }

    private fun findReference(returnValue: Array<out PsiReference>): PsiReference? {
        for (reference in returnValue) {
            if (reference is ParadoxPathReference) return reference
            if (reference is PsiDynaReference<*>) {
                val mergedReference = reference.references.find { it is ParadoxPathReference }
                if (mergedReference != null) return mergedReference
            }
        }
        return null
    }
}
