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
    // `? =` in `k? = v`
    SafeCallAssignOperator(Stellaris) {
        override fun testResult(gameType: ParadoxGameType?, gameVersion: String?): TestResult {
            if (gameType == Stellaris) return sinceGameVersion(gameVersion, "4.4")
            return super.testResult(gameType, gameVersion)
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
            is ParadoxGameType -> test(target)
            is _ParadoxLocalisationTextLexer -> test(target.gameType)
            is PsiBuilder -> test(selectGameType(target.getUserData(FileContextUtil.CONTAINING_FILE_KEY)))
            is VirtualFile -> testFrom(target)
            is PsiFile -> testFrom(target)
            else -> false // unsupported
        }
    }

    fun testFrom(from: Any): Boolean {
        val selectedFile = selectFile(from)
        val rootInfo = selectedFile?.fileInfo?.rootInfo
        val gameType = rootInfo?.gameType
        return gameType == null || gameType == Core || gameType in gameTypes
    }

    fun test(gameType: ParadoxGameType?): Boolean {
        return gameType == null || gameType == Core || gameType in gameTypes
    }

    open fun testResult(gameType: ParadoxGameType?, gameVersion: String? = null): TestResult {
        return TestResult(test(gameType))
    }

    @Suppress("SameParameterValue")
    protected fun sinceGameVersion(gameVersion: String?, since: String): TestResult {
        val strictValue = gameVersion == null || ParadoxGameManager.compareGameVersion(gameVersion, since) >= 0
        return TestResult(true, strictValue, since)
    }

    data class TestResult(
        val value: Boolean,
        val strictValue: Boolean = value,
        val sinceGameVersion: String? = null,
    )
}
