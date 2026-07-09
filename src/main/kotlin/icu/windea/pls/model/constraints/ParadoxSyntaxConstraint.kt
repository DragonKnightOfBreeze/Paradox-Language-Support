package icu.windea.pls.model.constraints

import com.intellij.lang.PsiBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.resolve.FileContextUtil
import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.lexer._ParadoxLocalisationTextLexer
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxGameType.*

/**
 * 语法约束。
 *
 * 用于测试特定的语法是否受指定的游戏类型和游戏版本支持。
 */
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
            is VirtualFile -> test(selectGameType(target))
            is PsiFile -> test(selectGameType(target))
            else -> false // unsupported
        }
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
