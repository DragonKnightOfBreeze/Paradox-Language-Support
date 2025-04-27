package icu.windea.pls.model.constraints

import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxGameType.*

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

        return gameType != null && gameType in this.gameTypes
    }
}
