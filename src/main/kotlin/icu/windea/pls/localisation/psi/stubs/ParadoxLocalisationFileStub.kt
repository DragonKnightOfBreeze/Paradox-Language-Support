package icu.windea.pls.localisation.psi.stubs

import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.psi.stubs.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

@Suppress("UnstableApiUsage")
interface ParadoxLocalisationFileStub : PsiFileStub<ParadoxLocalisationFile>, ParadoxStub<ParadoxLocalisationFile> {
    val localisationType: ParadoxLocalisationType

    class Impl(
        file: ParadoxLocalisationFile?,
        override val localisationType: ParadoxLocalisationType,
        override val gameType: ParadoxGameType
    ) : PsiFileStubImpl<ParadoxLocalisationFile>(file), ParadoxLocalisationFileStub {
        override fun getFileElementType(): IElementType {
            return ParadoxLocalisationFile.ELEMENT_TYPE
        }

        override fun toString(): String {
            return "ParadoxLocalisationFileStub(localisationType=$localisationType, gameType=$gameType)"
        }
    }
}
