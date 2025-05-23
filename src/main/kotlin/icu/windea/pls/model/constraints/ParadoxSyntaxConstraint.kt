package icu.windea.pls.model.constraints

import com.intellij.lang.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.lexer.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxGameType.*

enum class ParadoxSyntaxConstraint(
    vararg val gameTypes: ParadoxGameType
) {
    /**
     * `['{concept_name}']` or `['{concept_name}', {concept_text}]`
     */
    LocalisationConceptCommand(Stellaris),
    /**
     * `#{tag_name} {text}#!`
     */
    LocalisationTextFormat(Ck3, Vic3), // see #137
    /**
     * `@{text_icon_name}!`
     */
    LocalisationTextIcon(Ck3, Vic3),  // see #137
    ;

    fun supports(target: Any): Boolean {
        val gameType = when (target) {
            is PsiBuilder -> target.getUserData(FileContextUtil.CONTAINING_FILE_KEY)?.fileInfo?.rootInfo?.gameType
            is _ParadoxLocalisationTextLexer -> target.gameType
            is VirtualFile -> selectGameType(target)
            is PsiFile -> selectGameType(target)
            else -> null
        }
        return gameType == null || gameType in this.gameTypes
    }
}
