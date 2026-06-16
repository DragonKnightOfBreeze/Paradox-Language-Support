package icu.windea.pls.model.constraints

import com.intellij.lang.PsiBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.resolve.FileContextUtil
import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.lexer._ParadoxLocalisationTextLexer
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxGameType.*

enum class ParadoxSyntaxConstraint(vararg val gameTypes: ParadoxGameType) {
    // #86
    // `?=` in `k ?= v`
    SafeAssignOperator(Ck3, Vic3, Eu5),

    // #331
    // `?=` in `k? = v`
    SafeCallAssignOperator(Stellaris) {
        override fun testStrict(gameType: ParadoxGameType?, gameVersion: String?): Boolean {
            return when (gameType) {
                Stellaris -> sinceGameVersion(gameVersion, "4.4")
                else -> super.testStrict(gameType, gameVersion)
            }
        }
    },

    // `['{concept_name}']` or `['{concept_name}', {concept_text}]`
    LocalisationConceptCommand(Stellaris),

    // #137
    // `#{tag_name} {text}#!`
    LocalisationTextFormat(Ck3, Vic3, Eu5),

    // #137
    // `@{text_icon_name}!`
    LocalisationTextIcon(Ck3, Vic3, Eu5),

    ;

    fun testTarget(target: Any): Boolean {
        return when (target) {
            is ParadoxGameType -> testLenient(target)
            is _ParadoxLocalisationTextLexer -> testLenient(target.gameType)
            is PsiBuilder -> testLenient(selectGameType(target.getUserData(FileContextUtil.CONTAINING_FILE_KEY)))
            is VirtualFile -> testTargetFromFile(target)
            is PsiFile -> testTargetFromFile(target)
            else -> false // unsupported
        }
    }

    private fun testTargetFromFile(from: Any): Boolean {
        val selectedFile = selectFile(from)
        val rootInfo = selectedFile?.fileInfo?.rootInfo
        val gameType = rootInfo?.gameType
        val gameVersion = rootInfo?.gameVersion
        return testStrict(gameType, gameVersion)
    }

    open fun testLenient(gameType: ParadoxGameType?, gameVersion: String? = null): Boolean {
        return gameType == null || gameType == Core || gameType in gameTypes
    }

    open fun testStrict(gameType: ParadoxGameType?, gameVersion: String? = null): Boolean {
        return testLenient(gameType)
    }

    @Suppress("SameParameterValue")
    protected fun sinceGameVersion(gameVersion: String?, since: String): Boolean {
        return gameVersion != null && ParadoxGameManager.compareGameVersion(gameVersion, since) >= 0
    }
}
