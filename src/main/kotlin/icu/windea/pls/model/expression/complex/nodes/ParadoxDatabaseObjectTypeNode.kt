package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxDatabaseObjectTypeNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val config: CwtDatabaseObjectTypeConfig?
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return when(language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_TYPE_KEY
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT_TYPE_KEY
        }
    }
    
    override fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxComplexExpressionError? {
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        val reference = getReference(element)
        if(reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrors.unresolvedDatabaseObjectType(rangeInExpression, text)
    }
    
    override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(CwtConfigHandler.getExpressionOffset(element))
        return Reference(element, rangeInElement, config?.pointer?.element)
    }
    
    class Reference(
        element: PsiElement,
        rangeInElement: TextRange,
        val resolved: CwtProperty?
    ): PsiReferenceBase<PsiElement>(element, rangeInElement) {
        override fun handleElementRename(newElementName: String): PsiElement {
            throw IncorrectOperationException() //cannot rename cwt config
        }
        
        override fun resolve() = resolved
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDatabaseObjectTypeNode {
            val config = configGroup.databaseObjectTypes.get(text)
            return ParadoxDatabaseObjectTypeNode(text, textRange, config)
        }
    }
}
