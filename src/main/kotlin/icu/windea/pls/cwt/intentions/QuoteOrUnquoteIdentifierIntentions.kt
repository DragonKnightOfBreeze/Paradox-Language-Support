package icu.windea.pls.cwt.intentions

import com.intellij.codeInsight.intention.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.cwt.psi.*

class QuoteIdentifierIntention : IntentionAction, DumbAware {
    override fun getFamilyName() = text
    
    override fun getText() = PlsBundle.message("cwt.intention.quoteIdentifier")
    
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if(editor == null || file == null) return false
        val offset = editor.caretModel.offset
        val element = findElement(file, offset)
        return element != null
    }
    
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if(editor == null || file == null) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        ElementManipulators.handleContentChange(element, element.text.quote())
    }
    
    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        //can also be applied to number value tokens 
        return file.findElementAt(offset) {
            val identifier = it.parent
            val result = when(identifier) {
                is CwtOptionKey -> canQuote(identifier)
                is CwtPropertyKey -> canQuote(identifier)
                is CwtString -> canQuote(identifier)
                is CwtInt -> true
                is CwtFloat -> true
                else -> false
            }
            if(result) identifier else null
        }
    }
    
    fun canQuote(element: PsiElement): Boolean {
        val text = element.text
        return !text.isQuoted()
    }
    
    override fun startInWriteAction() = true
}

class UnquoteIdentifierIntention : IntentionAction, DumbAware {
    override fun getFamilyName() = text
    
    override fun getText() = PlsBundle.message("cwt.intention.unquoteIdentifier")
    
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if(editor == null || file == null) return false
        val offset = editor.caretModel.offset
        val element = findElement(file, offset)
        return element != null
    }
    
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if(editor == null || file == null) return
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        ElementManipulators.handleContentChange(element, element.text.unquote())
    }
    
    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return file.findElementAt(offset) {
            val identifier = it.parent
            val result = when(identifier) {
                is CwtOptionKey -> canUnquote(identifier)
                is CwtPropertyKey -> canUnquote(identifier)
                is CwtString -> canUnquote(identifier)
                else -> false
            }
            if(result) identifier else null
        }
    }
    
    fun canUnquote(element: PsiElement): Boolean {
        val text = element.text
        return text.isQuoted() && !text.containsBlank()
    }
    
    override fun startInWriteAction() = true
}