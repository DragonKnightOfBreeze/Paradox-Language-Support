package icu.windea.pls.extension.diagram

import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

abstract class ParadoxDiagramElementManager(
    open val provider: ParadoxDiagramProvider
) : DiagramElementManagerEx<PsiElement>() {
    override fun findInDataContext(context: DataContext): PsiElement? {
        //rootFile
        val file = context.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val project = context.getData(CommonDataKeys.PROJECT) ?: return null
        val rootInfo = file.fileInfo?.rootInfo ?: return null
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        if (rootInfo.gameType != provider.gameType) return null //获取当前上下文的游戏类型，以确定可以提供哪些图表
        return file.toPsiFileSystemItem(project)
    }

    override fun isAcceptableAsNode(o: Any?): Boolean {
        return o is PsiDirectory || (o is PsiFile && o.language is ParadoxBaseLanguage)
    }

    override fun getEditorTitle(element: PsiElement?, additionalElements: MutableCollection<PsiElement>): String {
        return provider.presentableName
    }

    override fun getElementTitle(element: PsiElement): String? {
        if (element is PsiFileSystemItem) return provider.presentableName
        return null
    }

    // underlying implementation of diagram plugin use DocumentationProvider, rather than DocumentationTarget
    // so this method is meaningless
    // override fun getElementDocOwner(element: Any?, builder: DiagramBuilder): Any? {
    //     return super.getElementDocOwner(element, builder)
    // }

    // default to null
    // property -> No documentation found -> ok
    // override fun getItemDocOwner(element: Any?, builder: DiagramBuilder): PsiElement? {
    //     return super.getItemDocOwner(element, builder)
    // }

    override fun getNodeTooltip(element: PsiElement?): String? {
        return null
    }
}
