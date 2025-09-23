package icu.windea.pls.localisation.psi.stubs

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IElementType
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType

@Suppress("UnstableApiUsage")
interface ParadoxLocalisationFileStub : PsiFileStub<ParadoxLocalisationFile>, ParadoxStub<ParadoxLocalisationFile> {
    val localisationType: ParadoxLocalisationType

    class Impl(
        file: ParadoxLocalisationFile?,
        override val localisationType: ParadoxLocalisationType,
        override val gameType: ParadoxGameType,
    ) : PsiFileStubImpl<ParadoxLocalisationFile>(file), ParadoxLocalisationFileStub {
        override fun getParentStub(): ParadoxStub<*>? {
            return null
        }

        override fun getFileElementType(): IElementType {
            return ParadoxLocalisationFile.ELEMENT_TYPE
        }

        override fun toString(): String {
            return "ParadoxLocalisationFileStub(localisationType=$localisationType, gameType=$gameType)"
        }
    }
}
