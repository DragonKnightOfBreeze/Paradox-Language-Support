package icu.windea.pls.model.constraints

import com.intellij.lang.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.lexer.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxGameType.*
import icu.windea.pls.script.lexer.*

enum class ParadoxSyntaxConstraint(
    vararg val gameTypes: ParadoxGameType
) {
    /**
     * `['{concept_name}']` or `['{concept_name}', {concept_text}]`
     */
    LocalisationConceptQuoted(Stellaris),
    /**
     * `#{tag_name} {text}#!`
     */
    LocalisationFormatting(Ck3, Vic3), // see #137
    /**
     * `@{text_icon_name}!`
     */
    LocalisationTextIcon(Ck3, Vic3),  // see #137
    ;

    fun supports(target: Any): Boolean {
        val gameType = when (target) {
            is PsiBuilder -> target.getUserData(FileContextUtil.CONTAINING_FILE_KEY)?.fileInfo?.rootInfo?.gameType
            is _ParadoxScriptLexer -> target.gameType
            is _ParadoxLocalisationLexer -> target.gameType
            else -> null
        }
        return gameType == null || gameType in this.gameTypes
    }
}
