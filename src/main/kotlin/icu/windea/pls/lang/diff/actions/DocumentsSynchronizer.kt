// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package icu.windea.pls.lang.diff.actions

import com.intellij.diff.impl.AssignmentTracker
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diff.DiffBundle
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.concurrency.annotations.RequiresEdt
import java.beans.PropertyChangeListener

// com.intellij.diff.actions.DocumentsSynchronizer

abstract class DocumentsSynchronizer(
    protected val project: Project?,
    protected val document1: Document,
    protected val document2: Document
) {
    protected var duringModification = false

    private var listenerDisposable: Disposable? = null

    private val listener1 = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            if (duringModification) return
            onDocumentChanged1(event)
        }
    }

    private val listener2 = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            if (duringModification) return
            onDocumentChanged2(event)
        }
    }

    private val readOnlyListener = PropertyChangeListener { event ->
        if (Document.PROP_WRITABLE == event.propertyName) document2.setReadOnly(!document1.isWritable)
    }

    protected abstract fun onDocumentChanged1(event: DocumentEvent)

    protected abstract fun onDocumentChanged2(event: DocumentEvent)

    @RequiresEdt
    protected fun replaceString(document: Document, startOffset: Int, endOffset: Int, newText: CharSequence) {
        try {
            duringModification = true
            CommandProcessor.getInstance().executeCommand(project, {
                ApplicationManager.getApplication().runWriteAction { document.replaceString(startOffset, endOffset, newText) }
            }, DiffBundle.message("synchronize.document.and.its.fragment"), document)
        } finally {
            duringModification = false
        }
    }

    open fun startListen() {
        val disposable = Disposer.newDisposable("DocumentsSynchronizer")
        listenerDisposable?.let { Disposer.dispose(it) }
        listenerDisposable = disposable
        document1.addDocumentListener(listener1, disposable)
        document2.addDocumentListener(listener2, disposable)
        document1.addPropertyChangeListener(readOnlyListener)
    }

    open fun stopListen() {
        listenerDisposable?.let { Disposer.dispose(it) }
        listenerDisposable = null
        document1.removePropertyChangeListener(readOnlyListener)
    }

    class DocumentSynchronizerAssignmentTracker(
        private val synchronizer: DocumentsSynchronizer
    ) : AssignmentTracker() {
        override fun onFirstAssignment() {
            synchronizer.startListen()
        }

        override fun onLastUnassignment() {
            synchronizer.stopListen()
        }
    }
}
