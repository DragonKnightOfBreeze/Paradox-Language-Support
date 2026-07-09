package icu.windea.pls.model.constraints

import com.intellij.lang.PsiBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.resolve.FileContextUtil
import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.lexer._ParadoxLocalisationTextLexer
import icu.windea.pls.model.ParadoxGameType

/**
 * 语法约束。
 *
 * 用于测试特定的语法是否受指定的游戏类型和游戏版本支持。
 */
enum class ParadoxSyntaxConstraint {
    // #86
    // for ck3, vic3, eu5
    // `?=` in `k ?= v`
    SafeAssignOperator {
        override fun test(gameType: ParadoxGameType?): Boolean {
            return super.test(gameType) || gameType matchesBy ParadoxGameTypeConstraint.JominiBased
        }
    },

    // #331
    // for stellaris 4.4+
    // `? =` in `k? = v`
    SafeCallAssignOperator {
        override fun test(gameType: ParadoxGameType?): Boolean {
            return super.test(gameType) || gameType == ParadoxGameType.Stellaris
        }

        override fun getTestResult(gameType: ParadoxGameType?, gameVersion: String?): TestResult {
            if (gameType == ParadoxGameType.Stellaris) return sinceGameVersion(gameVersion, "4.4")
            return super.getTestResult(gameType, gameVersion)
        }
    },

    // for stellaris
    // `['{concept_name}']` or `['{concept_name}', {concept_text}]`
    LocalisationConceptCommand {
        override fun test(gameType: ParadoxGameType?): Boolean {
            return super.test(gameType) || gameType == ParadoxGameType.Stellaris
        }
    },

    // #137
    // for ck3, vic3, eu5
    // `#{tag_name} {text}#!`
    LocalisationTextFormat {
        override fun test(gameType: ParadoxGameType?): Boolean {
            return super.test(gameType) || gameType matchesBy ParadoxGameTypeConstraint.JominiBased
        }
    },

    // #137
    // for ck3, vic3, eu5
    // `@{text_icon_name}!`
    LocalisationTextIcon {
        override fun test(gameType: ParadoxGameType?): Boolean {
            return super.test(gameType) || gameType matchesBy ParadoxGameTypeConstraint.JominiBased
        }
    },

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

    open fun test(gameType: ParadoxGameType?): Boolean = gameType == null || gameType == ParadoxGameType.Core

    open fun getTestResult(gameType: ParadoxGameType?, gameVersion: String? = null): TestResult = TestResult(test(gameType))

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
