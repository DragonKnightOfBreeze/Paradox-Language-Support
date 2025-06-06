package icu.windea.pls.lang.search.scope.type

import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*

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
                result.add(Game)
                if (isInProject) {
                    result.add(GameWithDependencies)
                }
            }
            is ParadoxRootInfo.Mod -> {
                result.add(Mod)
                if (isInProject) {
                    result.add(Game)
                    result.add(ModAndGame)
                    result.add(ModWithDependencies)
                }
            }
        }
        return result
    }

    //scope types

    val Definition = object : ParadoxSearchScopeType("definition", PlsBundle.message("search.scope.type.name.definition")) {
        override fun findRoot(project: Project, context: Any?): PsiElement? {
            return when {
                context is PsiElement -> context.findParentDefinition()
                context is ParadoxIndexInfo -> context.virtualFile?.toPsiFile(project)?.findElementAt(context.elementOffset)?.findParentDefinition()
                else -> null
            }
        }

        override fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return GlobalSearchScope.EMPTY_SCOPE
            return GlobalSearchScope.fileScope(project, file) //限定在当前文件作用域
        }

        override fun distinctInFile(): Boolean {
            return false
        }
    }.also { map.put(it.id, it) }

    val File = object : ParadoxSearchScopeType("file", PlsBundle.message("search.scope.type.name.file")) {
        override fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope {
            val file = selectFile(context) ?: return GlobalSearchScope.EMPTY_SCOPE
            return GlobalSearchScope.fileScope(project, file) //限定在当前文件作用域
        }
    }.also { map.put(it.id, it) }

    val Mod = object : ParadoxSearchScopeType("mod", PlsBundle.message("search.scope.type.name.mod")) {
        override fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope {
            return ParadoxSearchScope.modScope(project, context)
        }
    }.also { map.put(it.id, it) }

    val Game = object : ParadoxSearchScopeType("game", PlsBundle.message("search.scope.type.name.game")) {
        override fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope {
            return ParadoxSearchScope.gameScope(project, context)
        }
    }.also { map.put(it.id, it) }

    val ModAndGame = object : ParadoxSearchScopeType("mod_and_game", PlsBundle.message("search.scope.type.name.modAndGame")) {
        override fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope {
            return ParadoxSearchScope.modAndGameScope(project, context)
        }
    }.also { map.put(it.id, it) }

    val ModWithDependencies = object : ParadoxSearchScopeType("mod_with_dependencies", PlsBundle.message("search.scope.type.name.mod.withDependencies")) {
        override fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope {
            return ParadoxSearchScope.modWithDependenciesScope(project, context)
        }
    }.also { map.put(it.id, it) }

    val GameWithDependencies = object : ParadoxSearchScopeType("game_with_dependencies", PlsBundle.message("search.scope.type.name.game.withDependencies")) {
        override fun getGlobalSearchScope(project: Project, context: Any?): GlobalSearchScope {
            return ParadoxSearchScope.gameWithDependenciesScope(project, context)
        }
    }.also { map.put(it.id, it) }

    val All = object : ParadoxSearchScopeType("all", PlsBundle.message("search.scope.type.name.all")) {

    }.also { map.put(it.id, it) }
}
