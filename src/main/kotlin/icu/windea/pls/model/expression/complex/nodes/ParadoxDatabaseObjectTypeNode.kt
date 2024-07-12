package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.script.highlighter.*

class ParadoxDatabaseObjectTypeNode(
    override val text: String,
    override val rangeInExpression: TextRange
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return when(language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_TYPE_KEY
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT_TYPE_KEY
        }
    }
}
