package icu.windea.pls.core.diff.actions

import com.intellij.diff.*
import com.intellij.diff.actions.impl.*
import com.intellij.diff.chains.*
import com.intellij.diff.contents.*
import com.intellij.diff.requests.*
import com.intellij.diff.util.*
import com.intellij.notification.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import java.awt.*
import java.util.*
import javax.swing.*

/**
 * 将当前本地化与包括当前本地化的只读副本在内的相同名称的本地化进行DIFF。
 *
 * * 当当前文件是模组或游戏文件且是本地化文件时显示。
 * * 当前鼠标位置位于本地化声明中时启用。
 * * 忽略直接位于游戏或模组入口目录下的文件。
 * * 可以用于比较二进制文件。（如DDS图片）
 * * TODO 按照覆盖顺序进行排序。
 */
@Suppress("ComponentNotRegistered")
class ParadoxCompareLocalisationsAction : ParadoxShowDiffAction() {
    private fun findElement(psiFile: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return psiFile.findElementAt(offset)
            ?.parents(withSelf = false)
            ?.find { it is ParadoxLocalisationProperty && it.localisationInfo != null }
            ?.castOrNull()
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = false
        presentation.isEnabled = false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull()
            ?: return
        if(file.isDirectory) return
        if(file.fileType != ParadoxScriptFileType) return
        val fileInfo = file.fileInfo ?: return
        //忽略直接位于游戏或模组入口目录下的文件
        if(fileInfo.entryPath.length <= 1) return
        presentation.isVisible = true
        val project = e.project ?: return
        val offset = e.editor?.caretModel?.offset ?: return
        val psiFile = file.toPsiFile<PsiFile>(project) ?: return
        val localisation = findElement(psiFile, offset)
        presentation.isEnabled = localisation != null
    }
    
    override fun getDiffRequestChain(e: AnActionEvent): DiffRequestChain? {
        val project = e.project ?: return null
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        if(file.fileType != ParadoxLocalisationFileType) return null
        val offset = e.editor?.caretModel?.offset ?: return null
        val psiFile = file.toPsiFile<PsiFile>(project) ?: return null
        val localisation = findElement(psiFile, offset) ?: return null
        val localisationName = localisation.name
        val localisations = Collections.synchronizedList(mutableListOf<ParadoxLocalisationProperty>())
        ProgressManager.getInstance().runProcessWithProgressSynchronously({
            runReadAction {
                val selector = localisationSelector().gameTypeFrom(file)
                val result = ParadoxLocalisationSearch.search(localisationName, project, selector = selector).findAll()
                localisations.addAll(result)
            }
        }, PlsBundle.message("diff.compare.localisations.collect.title"), true, project)
        if(localisations.size <= 1) {
            NotificationGroupManager.getInstance().getNotificationGroup("pls").createNotification(
                PlsBundle.message("diff.compare.localisations.content.title.info.1"),
                NotificationType.INFORMATION
            ).notify(project)
            return null
        }
        
        val contentFactory = DiffContentFactory.getInstance()
        
        val windowTitle = getWindowsTitle(localisation) ?: return null
        val contentTitle = getContentTitle(localisation) ?: return null
        val documentContent = contentFactory.createDocument(project, file) ?: return null
        val content = createContent(contentFactory, project, documentContent, localisation)
        
        var index = 0
        var defaultIndex = 0
        val producers = runReadAction {
            localisations.mapNotNull { otherLocalisation ->
                val otherPsiFile = otherLocalisation.containingFile ?: return@mapNotNull null
                val locale = otherPsiFile.localeConfig ?: return@mapNotNull null
                val otherFile = otherPsiFile.virtualFile ?: return@mapNotNull null
                val isSamePosition = localisation isSamePosition otherLocalisation
                val otherContentTitle = when {
                    isSamePosition -> getContentTitle(otherLocalisation, true)
                    else -> getContentTitle(otherLocalisation)
                } ?: return@mapNotNull null
                var isCurrent = false
                var readonly = false
                val otherContent = when {
                    isSamePosition -> {
                        isCurrent = true
                        readonly = true
                        val otherDocument = EditorFactory.getInstance().createDocument(documentContent.document.text)
                        val otherDocumentContent = contentFactory.create(project, otherDocument, content.highlightFile)
                        createContent(contentFactory, project, otherDocumentContent, localisation)
                    }
                    else -> {
                        val otherDocumentContent = contentFactory.createDocument(project, otherFile) ?: return@mapNotNull null
                        createContent(contentFactory, project, otherDocumentContent, otherLocalisation)
                    }
                }
                if(isCurrent) defaultIndex = index
                if(readonly) otherContent.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true)
                index++
                val icon = otherLocalisation.icon
                val request = SimpleDiffRequest(windowTitle, content, otherContent, contentTitle, otherContentTitle)
                MyRequestProducer(request, otherLocalisation.name, locale, otherFile, icon, isCurrent)
            }
        }
        val chain = MyDiffRequestChain(producers, defaultIndex)
        //如果打开了编辑器，窗口定位到当前光标位置
        val editor = e.editor
        if(editor != null) {
            val currentLine = editor.caretModel.logicalPosition.line
            chain.putUserData(DiffUserDataKeys.SCROLL_TO_LINE, Pair.create(Side.RIGHT, currentLine))
        }
        return chain
    }
    
    private fun createContent(contentFactory: DiffContentFactory, project: Project, documentContent: DocumentContent, localisation: ParadoxLocalisationProperty): DocumentContent {
        return contentFactory.createFragment(project, documentContent, localisation.textRange)
    }
    
    private fun getWindowsTitle(localisation: ParadoxLocalisationProperty): String? {
        val name = localisation.name
        val file = localisation.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        return PlsBundle.message("diff.compare.localisations.dialog.title", name, fileInfo.path, fileInfo.rootInfo.gameRootPath)
    }
    
    private fun getContentTitle(localisation: ParadoxLocalisationProperty, original: Boolean = false): String? {
        val name = localisation.name
        val file = localisation.containingFile ?: return null
        val fileInfo = file.fileInfo ?: return null
        return when {
            original -> PlsBundle.message("diff.compare.localisations.originalContent.title", name, fileInfo.path, fileInfo.rootInfo.gameRootPath)
            else -> PlsBundle.message("diff.compare.localisations.content.title", name, fileInfo.path, fileInfo.rootInfo.gameRootPath)
        }
    }
    
    class MyDiffRequestChain(
        producers: List<DiffRequestProducer>,
        defaultIndex: Int = 0
    ) : UserDataHolderBase(), DiffRequestSelectionChain, GoToChangePopupBuilder.Chain {
        private val listSelection = ListSelection.createAt(producers, defaultIndex)
        
        override fun getListSelection() = listSelection
        
        override fun createGoToChangeAction(onSelected: Consumer<in Int>, defaultSelection: Int): AnAction {
            return MyGotoChangePopupAction(this, onSelected, defaultSelection)
        }
    }
    
    class MyRequestProducer(
        request: DiffRequest,
        val otherLocalisationName: String,
        val locale: CwtLocalisationLocaleConfig,
        val otherFile: VirtualFile,
        val icon: Icon,
        val isCurrent: Boolean
    ) : SimpleDiffRequestChain.DiffRequestProducerWrapper(request) {
        override fun getName(): String {
            val fileInfo = otherFile.fileInfo ?: return super.getName()
            return PlsBundle.message("diff.compare.localisations.popup.name", otherLocalisationName, locale, fileInfo.path, fileInfo.rootInfo.gameRootPath)
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
        ) : BaseListPopupStep<DiffRequestProducer>(PlsBundle.message("diff.compare.localisations.popup.title"), chain.requests) {
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
                val selectedIndex = chain.requests.indexOf(selectedValue)
                onSelected.consume(selectedIndex)
            }
        }
    }
}
