package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.daemon.impl.actions.*
import com.intellij.codeInsight.intention.preview.*
import com.intellij.codeInspection.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.encoding.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import java.nio.charset.*

class ChangeFileEncodingFix(
    element: PsiElement,
    private val charset: Charset,
    private val addBom: Boolean?
) : LocalQuickFixAndIntentionActionOnPsiElement(element), IntentionActionWithFixAllOption {
    override fun getText() = PlsBundle.message("fix.changeFileEncoding")

    override fun getFamilyName() = text

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
        val virtualFile = file.virtualFile
        val changeCharset = virtualFile.charset != charset
        val hasBom = virtualFile.hasBom(PlsConstants.utf8Bom)
        if (addBom == true && !hasBom) {
            runCatchingCancelable {
                virtualFile.addBom(PlsConstants.utf8Bom)
            }.onFailure { e -> thisLogger().warn("Unexpected exception occurred on attempt to add BOM from file $this", e) }
        } else if (addBom == false && hasBom) {
            runCatchingCancelable {
                virtualFile.removeBom(PlsConstants.utf8Bom)
            }.onFailure { e -> thisLogger().warn("Unexpected exception occurred on attempt to remove BOM from file $this", e) }
        }
        if (changeCharset) virtualFile.charset = charset
        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = fileDocumentManager.getDocument(virtualFile)
        if (document != null) {
            if (changeCharset) {
                ChangeFileEncodingAction.changeTo(project, document, editor, virtualFile, charset, EncodingUtil.Magic8.ABSOLUTELY, EncodingUtil.Magic8.ABSOLUTELY)
            }
            fileDocumentManager.saveDocument(document) //保存文件
        }
    }

    override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor) = IntentionPreviewInfo.EMPTY

    override fun generatePreview(project: Project, editor: Editor, file: PsiFile) = IntentionPreviewInfo.EMPTY

    override fun startInWriteAction() = false
}
