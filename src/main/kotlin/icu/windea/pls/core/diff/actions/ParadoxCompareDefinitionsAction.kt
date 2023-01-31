package icu.windea.pls.core.diff.actions

import com.intellij.diff.*
import com.intellij.diff.actions.impl.*
import com.intellij.diff.chains.*
import com.intellij.diff.requests.*
import com.intellij.diff.tools.util.base.*
import com.intellij.diff.util.*
import com.intellij.notification.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.awt.*
import java.util.*
import javax.swing.*

/**
 * 将当前定义与包括当前本地化的只读副本在内的相同名称且相同主要类型的定义进行DIFF。
 *
 * TODO 按照覆盖顺序进行排序。
 */
@Suppress("ComponentNotRegistered")
class ParadoxCompareDefinitionsAction : ParadoxShowDiffAction() {
    override fun isAvailable(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
        if(file.fileType != ParadoxScriptFileType) return false
        val offset = e.editor?.caretModel?.offset ?: return false
        val psiFile = file.toPsiFile<PsiFile>(project) ?: return false
        val element = psiFile.findElementAt(offset) ?: return false
        val definition = element.parentOfType<ParadoxScriptDefinitionElement>(withSelf = false) ?: return false
        //要求能够获取定义信息
        return definition.definitionInfo != null
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain? {
        val project = e.project ?: return null
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        if(file.fileType != ParadoxScriptFileType) return null
        val offset = e.editor?.caretModel?.offset ?: return null
        val psiFile = file.toPsiFile<PsiFile>(project) ?: return null
        val element = psiFile.findElementAt(offset) ?: return null
        val definition = element.parentOfType<ParadoxScriptDefinitionElement>(withSelf = false) ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        val definitions = Collections.synchronizedList(mutableListOf<ParadoxScriptDefinitionElement>())
        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            val selector = definitionSelector().gameTypeFrom(file)
            //pass main type only
            val result = ParadoxDefinitionSearch.search(definitionInfo.name, definitionInfo.type, project, selector = selector).findAll()
            definitions.addAll(result)
        }, PlsBundle.message("diff.compare.definitions.collect.title"), true, project)
        if(definitions.size <= 1) {
            NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
                PlsBundle.message("diff.compare.definitions.content.title.info.1"),
                NotificationType.INFORMATION
            ).notify(project)
            return null
        }
        
        val contentFactory = DiffContentFactory.getInstance()
        
        val windowTitle = getWindowsTitle(definition, definitionInfo) ?: return null
        val contentTitle = getContentTitle(definition, definitionInfo) ?: return null
        val documentContent = contentFactory.createDocument(project, file) ?: return null
        val textRange = definition.textRange
        val content = contentFactory.createFragment(project, documentContent, textRange)
        
        val producers = definitions.mapNotNull { otherDefinition ->
            val otherDefinitionInfo = otherDefinition.definitionInfo ?: return@mapNotNull null
            val otherPsiFile = otherDefinition.containingFile ?: return@mapNotNull null
            val otherFile = otherPsiFile.virtualFile ?: return@mapNotNull null
            val isSamePosition = definition isSamePosition otherDefinition
            val otherContentTitle = when {
                isSamePosition -> getContentTitle(otherDefinition, otherDefinitionInfo, true)
                else -> getContentTitle(otherDefinition, otherDefinitionInfo)
            } ?: return@mapNotNull null
            var isCurrent = false
            val otherContent = when {
                isSamePosition -> {
                    isCurrent = true
                    val otherDocument = EditorFactory.getInstance().createDocument(documentContent.document.text)
                    val otherDocumentContent = contentFactory.create(project, otherDocument, content.highlightFile)
                    contentFactory.createFragment(project, otherDocumentContent, textRange)
                }
                else -> {
                    val otherDocumentContent = contentFactory.createDocument(project, otherFile) ?: return null
                    contentFactory.createFragment(project, otherDocumentContent, otherDefinition.textRange)
                }
            }
            if(isCurrent) otherContent.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)
            val icon = otherDefinition.icon
            val request = SimpleDiffRequest(windowTitle, content, otherContent, contentTitle, otherContentTitle)
            MyRequestProducer(request, otherDefinitionInfo, otherFile, icon, isCurrent)
        }
        val chain = MyDiffRequestChain(producers)
        //如果打开了编辑器，左窗口定位到当前光标位置
        val editor = e.editor
        if(editor != null) {
            val currentLine = editor.caretModel.logicalPosition.line
            chain.putUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.LEFT, currentLine))
        }
        return chain
    }
    
    private fun getWindowsTitle(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): String? {
        val file = definition.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        return PlsBundle.message("diff.compare.definitions.dialog.title", definitionInfo.name, definitionInfo.typesText, fileInfo.path, fileInfo.rootPath)
    }
    
    private fun getContentTitle(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, original: Boolean = false): String? {
        val file = definition.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        return when {
            original -> PlsBundle.message("diff.compare.definitions.originalContent.title", definitionInfo.name, definitionInfo.typesText, fileInfo.path, fileInfo.rootPath)
            else -> PlsBundle.message("diff.compare.definitions.content.title", definitionInfo.name, definitionInfo.typesText, fileInfo.path, fileInfo.rootPath)
        }
    }
    
    class MyDiffRequestChain(
        producers: List<DiffRequestProducer>,
        var currentIndex: Int = 0
    ) : UserDataHolderBase(), DiffRequestSelectionChain, GoToChangePopupBuilder.Chain {
        private val listSelection = ListSelection.createAt(producers, currentIndex)
        
        override fun getListSelection() = listSelection
        
        override fun createGoToChangeAction(onSelected: Consumer<in Int>, defaultSelection: Int): AnAction {
            return MyGotoChangePopupAction(this, onSelected, defaultSelection)
        }
        
        fun syncEditorsCaretPosition(selectedIndex: Int) {
            if(currentIndex == selectedIndex) return
            val request = (requests[currentIndex] as MyRequestProducer).request
            val selectedRequest = (requests[selectedIndex] as MyRequestProducer).request
            val positions = DiffUserDataKeysEx.EDITORS_CARET_POSITION.get(request)
            val vPositions = InitialScrollPositionSupport.EditorsVisiblePositions.KEY.get(request)
            if(positions != null) DiffUserDataKeysEx.EDITORS_CARET_POSITION.set(selectedRequest, positions)
            if(vPositions != null) InitialScrollPositionSupport.EditorsVisiblePositions.KEY.set(selectedRequest, vPositions)
            currentIndex = selectedIndex
        }
    }
    
    class MyRequestProducer(
        request: DiffRequest,
        val otherDefinitionInfo: ParadoxDefinitionInfo,
        val otherFile: VirtualFile,
        val icon: Icon,
        val isCurrent: Boolean
    ) : SimpleDiffRequestChain.DiffRequestProducerWrapper(request) {
        override fun getName(): String {
            val fileInfo = otherFile.fileInfo ?: return super.getName()
            return PlsBundle.message("diff.compare.definitions.popup.name", otherDefinitionInfo.name, otherDefinitionInfo.typesText, fileInfo.path, fileInfo.rootPath)
        }
    }
    
    class MyGotoChangePopupAction(
        val chain: MyDiffRequestChain,
        val onSelected: Consumer<in Int>,
        val defaultSelection: Int
    ) : GoToChangePopupBuilder.BaseGoToChangePopupAction() {
        override fun canNavigate(): Boolean {
            return chain.requests.size > 1
        }
        
        override fun createPopup(e: AnActionEvent): JBPopup {
            return JBPopupFactory.getInstance().createListPopup(Popup(e))
        }
        
        private inner class Popup(
            val e: AnActionEvent
        ) : BaseListPopupStep<DiffRequestProducer>(PlsBundle.message("diff.compare.definitions.popup.title"), chain.requests) {
            init {
                defaultOptionIndex = defaultSelection
            }
            
            override fun getIconFor(value: DiffRequestProducer) = (value as MyRequestProducer).icon
            
            override fun getTextFor(value: DiffRequestProducer) = value.name
            
            //com.intellij.find.actions.ShowUsagesTableCellRenderer.getTableCellRendererComponent L205
            override fun getBackgroundFor(value: DiffRequestProducer) =
                if((value as MyRequestProducer).isCurrent) Color(0x808080) else null
            
            override fun isSpeedSearchEnabled() = true
            
            override fun onChosen(selectedValue: DiffRequestProducer, finalChoice: Boolean) = doFinalStep {
                //如果打开了编辑器，左窗口重新定位到当前光标位置
                val selectedIndex = chain.requests.indexOf(selectedValue)
                chain.syncEditorsCaretPosition(selectedIndex)
                onSelected.consume(selectedIndex)
            }
        }
    }
}
