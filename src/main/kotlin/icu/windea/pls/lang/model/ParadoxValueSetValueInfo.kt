package icu.windea.pls.lang.model

import com.intellij.codeInsight.highlighting.*
import com.intellij.psi.*

data class ParadoxValueSetValueInfo(
    val name: String,
    val valueSetNames: Set<String>,
    val readWriteAccess: ReadWriteAccessDetector.Access,
    override val elementOffset: Int,
    override val gameType: ParadoxGameType
): ParadoxElementInfo {
    @Volatile override var file: PsiFile? = null
}
