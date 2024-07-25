package icu.windea.pls.localisation.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.util.Iconable.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.localisation.navigation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.model.*
import javax.swing.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationPsiImplUtil {
    //region ParadoxLocalisationPropertyList
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationPropertyList, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationNodes.Locale
    }
    
    @JvmStatic
    fun getComponents(element: ParadoxLocalisationPropertyList): List<ParadoxLocalisationProperty> {
        return element.propertyList
    }
    //endregion
    
    //region ParadoxLocalisationLocale
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationLocale, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationNodes.Locale
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationLocale): String {
        return element.localeId.text.orEmpty()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationLocale, name: String): ParadoxLocalisationLocale {
        val newElement = ParadoxLocalisationElementFactory.createLocale(element.project, name)
        return element.replace(newElement).cast()
    }
    
    @JvmStatic
    fun getReference(element: ParadoxLocalisationLocale): ParadoxLocalisationLocalePsiReference {
        val rangeInElement = element.localeId.textRangeInParent
        return ParadoxLocalisationLocalePsiReference(element, rangeInElement)
    }
    //endregion
    
    //region ParadoxLocalisationProperty
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationProperty, @IconFlags flags: Int): Icon {
        if(element.localisationInfo != null) return PlsIcons.Nodes.Localisation
        return PlsIcons.LocalisationNodes.Property
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationProperty): String {
        element.stub?.name?.let { return it }
        return element.propertyKey.propertyKeyId.text
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationProperty, name: String): ParadoxLocalisationProperty {
        val nameElement = element.propertyKey
        val newNameElement = ParadoxLocalisationElementFactory.createPropertyKey(element.project, name).propertyKeyId
        nameElement.replace(newNameElement)
        return element
    }
    
    @JvmStatic
    fun getNameIdentifier(element: ParadoxLocalisationProperty): PsiElement {
        return element.propertyKey.propertyKeyId
    }
    
    @JvmStatic
    fun getTextOffset(element: ParadoxLocalisationProperty): Int {
        return element.propertyKey.textOffset
    }
    
    @JvmStatic
    fun getCategory(element: ParadoxLocalisationProperty): ParadoxLocalisationCategory? {
        element.stub?.category?.let { return it }
        return element.localisationInfo?.category
    }
    
    @JvmStatic
    fun getValue(element: ParadoxLocalisationProperty): String? {
        return element.propertyValue?.text?.unquote()
    }
    
    @JvmStatic
    fun setValue(element: ParadoxLocalisationProperty, value: String): PsiElement {
        val valueElement = element.propertyValue
        if(valueElement == null) {
            val newElement = ParadoxLocalisationElementFactory.createProperty(element.project, element.name, value)
            return element.replace(newElement)
        } else {
            val newValueElement = ParadoxLocalisationElementFactory.createPropertyValue(element.project, value)
            valueElement.replace(newValueElement)
            return element
        }
    }
    
    @JvmStatic
    fun getPresentation(element: ParadoxLocalisationProperty): ItemPresentation {
        val localisationInfo = element.localisationInfo
        if(localisationInfo != null) return ParadoxLocalisationPresentation(element)
        return BaseParadoxItemPresentation(element)
    }
    
    @JvmStatic
    fun isEquivalentTo(element: ParadoxLocalisationProperty, another: PsiElement): Boolean {
        //name & category (localisation / synced_localisation) & gameType
        return another is ParadoxLocalisationProperty
            && element.localisationInfo?.equals(another.localisationInfo) == true
    }
    
    @JvmStatic
    fun toString(element: ParadoxLocalisationProperty): String {
        return "ParadoxLocalisationProperty(name=${element.name})"
    }
    //endregion
    
    //region ParadoxLocalisationPropertyKey
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationPropertyKey, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationNodes.Property
    }
    //endregion
    
    //region ParadoxLocalisationPropertyReference
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationPropertyReference, @IconFlags flags: Int): Icon {
        val resolved = element.reference?.resolve()
        return when {
            resolved is ParadoxLocalisationProperty -> PlsIcons.Nodes.Localisation
            resolved is CwtProperty -> PlsIcons.Nodes.PredefinedParameter
            resolved is ParadoxParameterElement -> PlsIcons.Nodes.Parameter
            else -> PlsIcons.LocalisationNodes.Property
        }
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationPropertyReference): String {
        return element.propertyReferenceId?.text.orEmpty()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationPropertyReference, name: String): ParadoxLocalisationPropertyReference {
        if(element.propertyReferenceId == null) throw IncorrectOperationException() //不支持重命名
        val newElement = ParadoxLocalisationElementFactory.createPropertyReference(element.project, name)
        return element.replace(newElement).cast()
    }
    
    @JvmStatic
    fun getReference(element: ParadoxLocalisationPropertyReference): ParadoxLocalisationPropertyPsiReference? {
        return CachedValuesManager.getCachedValue(element) {
            val value = run {
                val rangeInElement = element.propertyReferenceId?.textRangeInParent ?: return@run null
                ParadoxLocalisationPropertyPsiReference(element, rangeInElement)
            }
            CachedValueProvider.Result.create(value, element)
        }
    }
    //endregion
    
    //region ParadoxLocalisationScriptedVariableReference
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationScriptedVariableReference, @IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.ScriptedVariable
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationScriptedVariableReference): String? {
        return element.text.removePrefix("@").orNull()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationScriptedVariableReference, name: String): ParadoxLocalisationScriptedVariableReference {
        if(element.idElement == null) throw IncorrectOperationException() //不支持重命名
        val newElement = ParadoxLocalisationElementFactory.createScriptedVariableReference(element.project, name)
        return element.replace(newElement).cast()
    }
    
    @JvmStatic
    fun getReference(element: ParadoxLocalisationScriptedVariableReference): ParadoxScriptedVariablePsiReference? {
        return CachedValuesManager.getCachedValue(element) {
            val value = run {
                val rangeInElement = element.idElement?.textRangeInParent ?: return@run null
                ParadoxScriptedVariablePsiReference(element, rangeInElement)
            }
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    @JvmStatic
    fun getType(element: ParadoxLocalisationScriptedVariableReference): ParadoxType {
        return element.reference?.resolve()?.type ?: ParadoxType.Unknown
    }
    
    @JvmStatic
    fun getExpression(element: ParadoxLocalisationScriptedVariableReference): String {
        return element.text
    }
    //endregion
    
    //region ParadoxLocalisationIcon	
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationIcon, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationNodes.Icon
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationIcon): String? {
        //string / command / property reference
        val iconIdElement = element.iconId
        if(iconIdElement != null) return iconIdElement.text
        val iconIdReferenceElement = element.iconIdReference
        val resolved = iconIdReferenceElement?.reference?.resolveLocalisation() //直接解析为本地化以优化性能
        return resolved?.value
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationIcon, name: String): ParadoxLocalisationIcon {
        if(element.iconId == null) throw IncorrectOperationException() //不支持重命名
        val newElement = ParadoxLocalisationElementFactory.createIcon(element.project, name)
        return element.replace(newElement).cast()
    }
    
    @JvmStatic
    fun getFrame(element: ParadoxLocalisationIcon): Int {
        //这里的帧数可以用$PARAM$表示，对应某个本地化参数，此时直接返回0
        val iconFrameElement = element.iconFrame //默认为0（不切分）
        if(iconFrameElement != null) return iconFrameElement.text.toIntOrNull() ?: 0
        //这里的propertyReference是一个来自脚本文件的参数，不解析
        //val iconFrameReferenceElement = element.iconFrameReference ?: return 0
        //return iconFrameReferenceElement.reference?.resolve()?.value?.toIntOrDefault(0) ?: 0
        return 0
    }
    
    @JvmStatic
    fun getReference(element: ParadoxLocalisationIcon): ParadoxLocalisationIconPsiReference? {
        return CachedValuesManager.getCachedValue(element) {
            val value = run {
                val rangeInElement = element.iconId?.textRangeInParent ?: return@run null
                ParadoxLocalisationIconPsiReference(element, rangeInElement)
            }
            CachedValueProvider.Result.create(value, element)
        }
    }
    //endregion
    
    //region ParadoxLocalisationCommand
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationCommand, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationNodes.Command
    }
    //endregion
    
    //region ParadoxLocalisationCommandText
    @JvmStatic
    fun getName(element: ParadoxLocalisationCommandText): String {
        return element.text
    }
    
    @JvmStatic
    fun getValue(element: ParadoxLocalisationCommandText): String {
        return element.text
    }
    
    @JvmStatic
    fun setValue(element: ParadoxLocalisationCommandText, value: String): ParadoxLocalisationCommandText {
        val newElement = ParadoxLocalisationElementFactory.createCommandText(element.project, value)
        return element.replace(newElement).cast()
    }
    
    @JvmStatic
    fun getType(element: ParadoxLocalisationCommandText): ParadoxType? {
        if(element.isCommandExpression()) return ParadoxType.CommandExpression
        return null
    }
    
    @JvmStatic
    fun getExpression(element: ParadoxLocalisationCommandText): String {
        return element.name
    }
    //endregion
    
    //region ParadoxLocalisationConcept
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationConcept, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationNodes.Concept
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationConcept): String {
        return element.conceptName?.idElement?.text?.trim().orEmpty()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationConcept, name: String): ParadoxLocalisationConcept {
        val nameElement = element.conceptName
        if(nameElement?.idElement == null) throw IncorrectOperationException() //不支持重命名
        val newNameElement = ParadoxLocalisationElementFactory.createConceptName(element.project, name)
        nameElement.replace(newNameElement)
        return element
    }
    
    @JvmStatic
    fun getReference(element: ParadoxLocalisationConcept): ParadoxLocalisationConceptPsiReference? {
        val nameElement = element.conceptName ?: return null
        
        //作为复杂表达式的场合，另行处理（参见：ParadoxLocalisationReferenceContributor）
        if(nameElement.isComplexExpression()) return null
        
        return CachedValuesManager.getCachedValue(element) {
            val rangeInElement = nameElement.textRangeInParent
            val value = ParadoxLocalisationConceptPsiReference(element, rangeInElement)
            CachedValueProvider.Result.create(value, element)
        }
    }
    //endregion
    
    //region ParadoxLocalisationConceptName
    @JvmStatic
    fun getName(element: ParadoxLocalisationConceptName): String {
        return element.text
    }
    
    @JvmStatic
    fun getValue(element: ParadoxLocalisationConceptName): String {
        return element.text
    }
    
    @JvmStatic
    fun setValue(element: ParadoxLocalisationConceptName, value: String): ParadoxLocalisationConceptName {
        val newElement = ParadoxLocalisationElementFactory.createConceptName(element.project, value)
        return element.replace(newElement).cast()
    }
    
    @JvmStatic
    fun getType(element: ParadoxLocalisationConceptName): ParadoxType? {
        if(element.isDatabaseObjectExpression()) return ParadoxType.DatabaseObjectExpression
        return null
    }
    
    @JvmStatic
    fun getExpression(element: ParadoxLocalisationConceptName): String {
        return element.name
    }
    //endregion
    
    //region ParadoxLocalisationColorfulText
    @JvmStatic
    fun getName(element: ParadoxLocalisationColorfulText): String? {
        return element.idElement?.text
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationColorfulText, name: String): ParadoxLocalisationColorfulText {
        val idElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createColorfulText(element.project, name).idElement!!
        idElement.replace(newIdElement)
        return element
    }
    
    @JvmStatic
    fun getReference(element: ParadoxLocalisationColorfulText): ParadoxLocalisationColorPsiReference? {
        return CachedValuesManager.getCachedValue(element) {
            val value = run {
                val rangeInElement = element.idElement?.textRangeInParent ?: return@run null
                ParadoxLocalisationColorPsiReference(element, rangeInElement)
            }
            CachedValueProvider.Result.create(value, element)
        }
    }
    //endregion
    
    @JvmStatic
    fun getReference(element: PsiElement): PsiReference? {
        return element.references.singleOrNull()
    }
    
    @JvmStatic
    fun getReferences(element: PsiElement): Array<out PsiReference> {
        //这里不需要进行缓存
        return PsiReferenceService.getService().getContributedReferences(element)
    }
    
    @JvmStatic
    fun getPresentation(element: PsiElement): ItemPresentation {
        return BaseParadoxItemPresentation(element)
    }
    
    @JvmStatic
    fun getResolveScope(element: PsiElement): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementResolveScope(element)
    }
    
    @JvmStatic
    fun getUseScope(element: PsiElement): SearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementUseScope(element)
    }
}
