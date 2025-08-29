package icu.windea.pls.model.constraints

import com.intellij.lang.PsiBuilder
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.resolve.FileContextUtil
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.lexer._ParadoxLocalisationTextLexer
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxGameType.Ck3
import icu.windea.pls.model.ParadoxGameType.Stellaris
import icu.windea.pls.model.ParadoxGameType.Vic3

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
            is _ParadoxLocalisationTextLexer -> target.gameType
            is PsiBuilder -> selectGameType(target.getUserData(FileContextUtil.CONTAINING_FILE_KEY))
            is VirtualFile -> selectGameType(target)
            is PsiFile -> selectGameType(target)
            else -> null
        }
        return gameType == null || gameType in this.gameTypes
    }
}
