package icu.windea.pls.lang.diff

import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.contents.DocumentContentBase
import com.intellij.diff.contents.FileContent
import com.intellij.diff.util.DiffUserDataKeysEx
import com.intellij.diff.util.DiffUtil
import com.intellij.diff.util.LineCol
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.diff.DiffBundle
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.util.LineSeparator
import com.intellij.util.ObjectUtils
import icu.windea.pls.lang.diff.actions.DocumentsSynchronizer
import java.nio.charset.Charset
import java.util.function.IntUnaryOperator

//com.intellij.diff.contents.FileDocumentContentImpl
//com.intellij.diff.actions.DocumentFragmentContent

class FileDocumentFragmentContent(
    project: Project?,
    original: DocumentContent,
    range: TextRange,
    private val file: VirtualFile,
    private val highlightFile: VirtualFile? = null
) : DocumentContentBase(project, original.document), FileContent {
    private val document = runReadAction { FileDocumentManager.getInstance().getDocument(file) }!!

    private val original = original
    private val rangeMarker: RangeMarker
    private val mySynchronizer: MyDocumentsSynchronizer

    private var assignments = 0

    init {
        rangeMarker = createRangeMarker(original.document, range)
        val document1: Document = original.document

        //val document2 = EditorFactory.getInstance().createDocument("")
        //document2.putUserData(UndoManager.ORIGINAL_DOCUMENT, document1)

        val document2 = document
        document2.putUserData(UndoManager.ORIGINAL_DOCUMENT, document1)

        mySynchronizer = MyDocumentsSynchronizer(project, rangeMarker, document1, document2)

        val originalLineConvertor = original.getUserData(DiffUserDataKeysEx.LINE_NUMBER_CONVERTOR)
        putUserData(DiffUserDataKeysEx.LINE_NUMBER_CONVERTOR, IntUnaryOperator block@{ value: Int ->
            if (!rangeMarker.isValid) return@block -1
            val line = value + document1.getLineNumber(rangeMarker.startOffset)
            originalLineConvertor?.applyAsInt(line) ?: line
        })
    }

    private fun createRangeMarker(document: Document, range: TextRange): RangeMarker {
        val rangeMarker = document.createRangeMarker(range.startOffset, range.endOffset, true)
        rangeMarker.isGreedyToLeft = true
        rangeMarker.isGreedyToRight = true
        return rangeMarker
    }

    override fun getDocument(): Document {
        return document
    }

    override fun getHighlightFile(): VirtualFile? {
        return ObjectUtils.chooseNotNull(highlightFile, file)
    }

    override fun getNavigatable(position: LineCol): Navigatable? {
        if (!rangeMarker.isValid) return null
        val offset = position.toOffset(document)
        val originalOffset: Int = offset + rangeMarker.startOffset
        val originalPosition = LineCol.fromOffset(original.document, originalOffset)
        return original.getNavigatable(originalPosition)
    }

    override fun getNavigatable(): Navigatable? {
        return getNavigatable(LineCol(0))
    }

    override fun getLineSeparator(): LineSeparator? {
        val s = LoadTextUtil.detectLineSeparator(file, true) ?: return null
        return LineSeparator.fromString(s)
    }

    override fun getCharset(): Charset {
        return file.charset
    }

    override fun hasBom(): Boolean {
        return file.bom != null
    }

    override fun getFile(): VirtualFile {
        return file
    }

    override fun getContentType(): FileType {
        return file.fileType
    }

    override fun onAssigned(isAssigned: Boolean) {
        if (isAssigned) DiffUtil.refreshOnFrameActivation(file)

        if (isAssigned) {
            if (assignments == 0) mySynchronizer.startListen()
            assignments++
        } else {
            assignments--
            if (assignments == 0) mySynchronizer.stopListen()
        }
        assert(assignments >= 0)
    }

    private class MyDocumentsSynchronizer(
        project: Project?,
        private val myRangeMarker: RangeMarker,
        document1: Document,
        document2: Document
    ) : DocumentsSynchronizer(project, document1, document2) {
        override fun onDocumentChanged1(event: DocumentEvent) {
            if (!myRangeMarker.isValid) {
                myDocument2.setReadOnly(false)
                replaceString(myDocument2, 0, myDocument2.textLength, DiffBundle.message("synchronize.document.and.its.fragment.range.error"))
                myDocument2.setReadOnly(true)
                return
            }
            val newText = myDocument1.charsSequence.subSequence(myRangeMarker.startOffset, myRangeMarker.endOffset)
            replaceString(myDocument2, 0, myDocument2.textLength, newText)
        }

        override fun onDocumentChanged2(event: DocumentEvent) {
            if (!myRangeMarker.isValid) return
            if (!myDocument1.isWritable) return
            val newText = event.newFragment
            val originalOffset = event.offset + myRangeMarker.startOffset
            val originalEnd = originalOffset + event.oldLength
            replaceString(myDocument1, originalOffset, originalEnd, newText)
        }

        override fun startListen() {
            if (myRangeMarker.isValid) {
                myDocument2.setReadOnly(false)
                val nexText = myDocument1.charsSequence.subSequence(myRangeMarker.startOffset, myRangeMarker.endOffset)
                replaceString(myDocument2, 0, myDocument2.textLength, nexText)
                myDocument2.setReadOnly(!myDocument1.isWritable)
            } else {
                myDocument2.setReadOnly(false)
                replaceString(myDocument2, 0, myDocument2.textLength, DiffBundle.message("synchronize.document.and.its.fragment.range.error"))
                myDocument2.setReadOnly(true)
            }
            super.startListen()
        }
    }
}
