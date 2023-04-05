package icu.windea.pls.core.search.scope.type

import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import icu.windea.pls.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

object ParadoxSearchScopeTypes {
    private val map = mutableMapOf<String, ParadoxSearchScopeType>()
    
    fun get(id: String) = map.get(id) ?: All
    
    fun getScopeTypes(project: Project, context: PsiElement?): List<ParadoxSearchScopeType> {
        return doGetScopeTypes(project, context) ?: listOf(All)
    }
    
    private fun doGetScopeTypes(project: Project, context: PsiElement?): List<ParadoxSearchScopeType>? {
        if(context == null) return null
        val result = mutableListOf<ParadoxSearchScopeType>()
        val file = context.containingFile
        val fileInfo = file.fileInfo ?: return null
        result.add(File)
        val isInProject = ProjectFileIndex.getInstance(project).isInContent(file.virtualFile)
        val rootInfo = fileInfo.rootInfo
        when(rootInfo) {
            is ParadoxModRootInfo -> {
                result.add(Mod)
                if(isInProject) {
                    result.add(Game)
                    result.add(ModAndGame)
                    result.add(ModWithDependencies)
                }
            }
            is ParadoxGameRootInfo -> {
                result.add(Game)
                if(isInProject) {
                    result.add(GameWithDependencies)
                }
            }
        }
        result.add(All)
        return result
    }
    
    //scope types
    
    val Definition = object : ParadoxSearchScopeType("definition", PlsBundle.message("search.scope.type.name.definition")) {
        override fun findRoot(project: Project, context: PsiElement): PsiElement? {
            return context.findParentDefinition()
        }
        
        override fun getGlobalSearchScope(project: Project, context: PsiElement): GlobalSearchScope {
            return GlobalSearchScope.fileScope(context.containingFile) //限定在当前文件作用域
        }
    }.also { map.put(it.id, it) }
    
    val File = object : ParadoxSearchScopeType("file", PlsBundle.message("search.scope.type.name.file")) {
        override fun getGlobalSearchScope(project: Project, context: PsiElement): GlobalSearchScope {
            return GlobalSearchScope.fileScope(context.containingFile) //限定在当前文件作用域
        }
    }.also { map.put(it.id, it) }
    
    val Mod = object : ParadoxSearchScopeType("mod", PlsBundle.message("search.scope.type.name.mod")) {
        override fun getGlobalSearchScope(project: Project, context: PsiElement): GlobalSearchScope {
            return ParadoxSearchScope.modScope(project, context)
        }
    }.also { map.put(it.id, it) }
    
    val Game = object : ParadoxSearchScopeType("game", PlsBundle.message("search.scope.type.name.game")) {
        override fun getGlobalSearchScope(project: Project, context: PsiElement): GlobalSearchScope {
            return ParadoxSearchScope.gameScope(project, context)
        }
    }.also { map.put(it.id, it) }
    
    val ModAndGame = object : ParadoxSearchScopeType("mod_and_game", PlsBundle.message("search.scope.type.name.modAndGame")) {
        override fun getGlobalSearchScope(project: Project, context: PsiElement): GlobalSearchScope {
            return ParadoxSearchScope.modAndGameScope(project, context)
        }
    }.also { map.put(it.id, it) }
    
    val ModWithDependencies = object : ParadoxSearchScopeType("mod_with_dependencies", PlsBundle.message("search.scope.type.name.mod.withDependencies")) {
        override fun getGlobalSearchScope(project: Project, context: PsiElement): GlobalSearchScope {
            return ParadoxSearchScope.modWithDependenciesScope(project, context)
        }
    }.also { map.put(it.id, it) }
    
    val GameWithDependencies = object : ParadoxSearchScopeType("game_with_dependencies", PlsBundle.message("search.scope.type.name.game.withDependencies")) {
        override fun getGlobalSearchScope(project: Project, context: PsiElement): GlobalSearchScope {
            return ParadoxSearchScope.gameWithDependenciesScope(project, context)
        }
    }.also { map.put(it.id, it) }
    
    val All = object : ParadoxSearchScopeType("all", PlsBundle.message("search.scope.type.name.all")) {
        
    }.also { map.put(it.id, it) }
}