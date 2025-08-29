package icu.windea.pls.lang.quickfix

import com.intellij.codeInsight.daemon.impl.actions.IntentionActionWithFixAllOption
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.encoding.ChangeFileEncodingAction
import com.intellij.openapi.vfs.encoding.EncodingUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.addBom
import icu.windea.pls.core.hasBom
import icu.windea.pls.core.removeBom
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.model.constants.PlsConstants
import java.nio.charset.Charset

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
