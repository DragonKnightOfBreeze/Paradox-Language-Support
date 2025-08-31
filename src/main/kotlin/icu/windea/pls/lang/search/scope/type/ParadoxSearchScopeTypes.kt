package icu.windea.pls.lang.search.scope.type

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.ParadoxBaseFileType
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.indexInfo.ParadoxIndexInfo
import icu.windea.pls.script.psi.findParentDefinition

object ParadoxSearchScopeTypes {
    private val map = mutableMapOf<String, ParadoxSearchScopeType>()

    fun get(id: String?) = if (id != null) map.get(id) ?: All else All

    fun getScopeTypes(project: Project, context: PsiElement?): List<ParadoxSearchScopeType> {
        return doGetScopeTypes(project, context) ?: listOf(All)
    }

    private fun doGetScopeTypes(project: Project, context: PsiElement?): List<ParadoxSearchScopeType>? {
        //context: PsiDirectory | PsiFile | PsiElement | null
        if (context == null) return null
        val file = selectFile(context) ?: return null
        val fileInfo = file.fileInfo ?: return null
        val result = mutableListOf<ParadoxSearchScopeType>()
        result.add(All)
        if (file.fileType is ParadoxBaseFileType) {
            result.add(File)
        }
        val isInProject = ProjectFileIndex.getInstance(project).isInContent(file)
        val rootInfo = fileInfo.rootInfo
        when (rootInfo) {
            is ParadoxRootInfo.Game -> {
                result.add(RootFile)
                result.add(Game)
                if (isInProject) {
                    result.add(GameWithDependencies)
                }
            }
            is ParadoxRootInfo.Mod -> {
                result.add(RootFile)
                result.add(Mod)
                if (isInProject) {
                    result.add(Game)
                    result.add(ModAndGame)
                    result.add(ModWithDependencies)
                }
            }
            else -> {}
        }
        return result
    }

    //scope types

    val Definition = ParadoxSearchScopeType.InFile("definition", PlsBundle.message("search.scope.type.name.definition")) { project, context ->
        val contextElement = when {
            context is PsiElement -> context
            context is ParadoxIndexInfo -> context.virtualFile?.toPsiFile(project)?.findElementAt(context.elementOffset)
            else -> null
        }
        contextElement?.findParentDefinition()
    }.also { map.put(it.id, it) }

    val File = ParadoxSearchScopeType.FromFiles("file", PlsBundle.message("search.scope.type.name.file")) { project, context ->
        ParadoxSearchScope.fileScope(project, context)
    }.also { map.put(it.id, it) }

    val RootFile = ParadoxSearchScopeType.FromFiles("rootFile", PlsBundle.message("search.scope.type.name.rootFile")) { project, context ->
        ParadoxSearchScope.rootFileScope(project, context)
    }.also { map.put(it.id, it) }

    val Mod = ParadoxSearchScopeType.FromFiles("mod", PlsBundle.message("search.scope.type.name.mod")) { project, context ->
        ParadoxSearchScope.modScope(project, context)
    }.also { map.put(it.id, it) }

    val Game = ParadoxSearchScopeType.FromFiles("game", PlsBundle.message("search.scope.type.name.game")) { project, context ->
        ParadoxSearchScope.gameScope(project, context)
    }.also { map.put(it.id, it) }

    val ModAndGame = ParadoxSearchScopeType.FromFiles("mod_and_game", PlsBundle.message("search.scope.type.name.modAndGame")) { project, context ->
        ParadoxSearchScope.modAndGameScope(project, context)
    }.also { map.put(it.id, it) }

    val ModWithDependencies = ParadoxSearchScopeType.FromFiles("mod_with_dependencies", PlsBundle.message("search.scope.type.name.mod.withDependencies")) { project, context ->
        ParadoxSearchScope.modWithDependenciesScope(project, context)
    }.also { map.put(it.id, it) }

    val GameWithDependencies = ParadoxSearchScopeType.FromFiles("game_with_dependencies", PlsBundle.message("search.scope.type.name.game.withDependencies")) { project, context ->
        ParadoxSearchScope.gameWithDependenciesScope(project, context)
    }.also { map.put(it.id, it) }

    val All = ParadoxSearchScopeType.FromFiles("all", PlsBundle.message("search.scope.type.name.all")) { project, context ->
        ParadoxSearchScope.allScope(project, context)
    }.also { map.put(it.id, it) }
}
