package icu.windea.pls.core.inspections

import com.intellij.codeInspection.lang.*
import com.intellij.codeInspection.reference.*
import com.intellij.lang.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*
import org.jdom.*

class ParadoxRefManager(
    private val manager: RefManager
): RefManagerExtension<ParadoxRefManager> {
    companion object {
        private val _id = Key.create<ParadoxRefManager>("ParadoxRefManager")
        private val _languages = setOf(ParadoxScriptLanguage, ParadoxLocalisationLanguage)
    }
    
    override fun getID(): Key<ParadoxRefManager> {
        return _id
    }
    
    @Suppress("OVERRIDE_DEPRECATION")
    override fun getLanguage(): Language {
        return ParadoxScriptLanguage
    }
    
    override fun getLanguages(): Collection<Language> {
        return _languages
    }
    
    override fun iterate(visitor: RefVisitor) {
        return manager.iterate(visitor)
    }
    
    override fun cleanup() {
        
    }
    
    override fun removeReference(refElement: RefElement) {
        
    }
    
    override fun createRefElement(psiElement: PsiElement): RefElement? {
        return null
    }
    
    override fun getReference(type: String?, fqName: String?): RefEntity? {
        return null
    }
    
    override fun getType(entity: RefEntity): String? {
        return null
    }
    
    override fun getRefinedElement(ref: RefEntity): RefEntity {
        return ref
    }
    
    override fun visitElement(element: PsiElement) {
        
    }
    
    override fun getGroupName(entity: RefEntity): String? {
        if(entity is RefFile) {
            //按目录分组时显示相对于游戏或模组根目录的路径
            val fileInfo = entity.psiElement.containingFile?.fileInfo ?: return null
            return fileInfo.path.parent
        }
        return null
    }
    
    override fun belongsToScope(psiElement: PsiElement): Boolean {
        return true
    }
    
    override fun export(refEntity: RefEntity, element: Element) {
        
    }
    
    override fun onEntityInitialized(refEntity: RefElement, psiElement: PsiElement) {
        
    }
}