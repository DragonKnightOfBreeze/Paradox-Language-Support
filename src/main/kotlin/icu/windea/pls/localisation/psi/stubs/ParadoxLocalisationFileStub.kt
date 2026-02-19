package icu.windea.pls.localisation.psi.stubs

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.PsiFileStubImpl
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 本地化文件的存根。
 *
 * @property localisationType 本地化的类型。
 */
@Suppress("UnstableApiUsage")
sealed interface ParadoxLocalisationFileStub : PsiFileStub<ParadoxLocalisationFile>, ParadoxStub<ParadoxLocalisationFile> {
    val localisationType: ParadoxLocalisationType

    private class Impl(
        file: ParadoxLocalisationFile?,
        override val localisationType: ParadoxLocalisationType,
        override val gameType: ParadoxGameType,
    ) : PsiFileStubImpl<ParadoxLocalisationFile>(file), ParadoxLocalisationFileStub {
        override fun getParentStub() = null

        override fun getFileElementType() = ParadoxLocalisationFile.ELEMENT_TYPE

        override fun toString(): String {
            return "ParadoxLocalisationFileStub(localisationType=$localisationType, gameType=$gameType)"
        }
    }

    companion object {
        fun create(file: ParadoxLocalisationFile?, localisationType: ParadoxLocalisationType, gameType: ParadoxGameType): ParadoxLocalisationFileStub {
            return Impl(file, localisationType, gameType)
        }
    }
}
