package icu.windea.pls.localisation.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.Iconable.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.references.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.navigation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.script.psi.*
import javax.swing.*

//getName 确定进行重构和导航时显示的PsiElement的名字
//setName 确定进行重命名时的逻辑
//getTextOffset 确定选中一个PsiElement时，哪一部分会高亮显示
//getReference 确定选中一个PsiElement时，哪些其他的PsiElement会同时高亮显示

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationPsiImplUtil {
    //region ParadoxLocalisationPropertyList
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationPropertyList, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationLocale
    }
    
    @JvmStatic
    fun getComponents(element: ParadoxLocalisationPropertyList): List<ParadoxLocalisationProperty> {
        return element.propertyList
    }
    //endregion
    
    //region ParadoxLocalisationLocale
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationLocale, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationLocale
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationLocale): String {
        return element.localeId.text.orEmpty()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationLocale, name: String): ParadoxLocalisationLocale {
        val nameElement = element.localeId
        val newNameElement = ParadoxLocalisationElementFactory.createLocale(element.project, name).localeId
        nameElement.replace(newNameElement)
        return element
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
        if(element.localisationInfo != null) return PlsIcons.Localisation
        return PlsIcons.LocalisationProperty
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationProperty): String {
        element.greenStub?.name?.let { return it }
        return element.propertyKey.propertyKeyId.text
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationProperty, name: String): ParadoxLocalisationProperty {
        val nameElement = element.propertyKey.propertyKeyId
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
        element.greenStub?.category?.let { return it }
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
        return PlsIcons.LocalisationProperty
    }
    //endregion
    
    //region ParadoxLocalisationPropertyReference
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationPropertyReference, @IconFlags flags: Int): Icon {
        val resolved = element.reference?.resolve()
        return when {
            resolved is ParadoxLocalisationProperty -> PlsIcons.Localisation
            resolved is CwtProperty -> PlsIcons.PredefinedParameter
            else -> PlsIcons.LocalisationProperty
        }
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationPropertyReference): String {
        return element.propertyReferenceId?.text.orEmpty()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationPropertyReference, name: String): ParadoxLocalisationPropertyReference {
        val nameElement = element.propertyReferenceId ?: throw IncorrectOperationException() //不支持重命名
        val newNameElement = ParadoxLocalisationElementFactory.createPropertyReference(element.project, name).propertyReferenceId!!
        nameElement.replace(newNameElement)
        return element
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
        return PlsIcons.ScriptedVariable
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationScriptedVariableReference): String? {
        return element.text.removePrefix("@").takeIfNotEmpty()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationScriptedVariableReference, name: String): ParadoxLocalisationScriptedVariableReference {
        // 不包含作为前缀的"@"
        val nameElement = element.idElement ?: throw IncorrectOperationException()
        val newNameElement = ParadoxLocalisationElementFactory.createScriptedVariableReference(element.project, name).idElement!!
        nameElement.replace(newNameElement)
        return element
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
        return PlsIcons.LocalisationIcon
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationIcon): String? {
        //string / command / property reference
        val iconIdElement = element.iconId
        if(iconIdElement != null) return iconIdElement.text
        val iconIdReferenceElement = element.iconIdReference ?: return null
        return iconIdReferenceElement.reference?.resolve()?.castOrNull<ParadoxLocalisationProperty>()?.value
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationIcon, name: String): ParadoxLocalisationIcon {
        val nameElement = element.iconId ?: throw IncorrectOperationException() //不支持重命名
        val newNameElement = ParadoxLocalisationElementFactory.createIcon(element.project, name).iconId!!
        nameElement.replace(newNameElement)
        return element
    }
    
    @JvmStatic
    fun getFrame(element: ParadoxLocalisationIcon): Int {
        //NOTE 这里的帧数可能用propertyReference表示，对应脚本中的参数，这时帧数传0
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
    
    //region ParadoxLocalisationColorfulText
    @JvmStatic
    fun getName(element: ParadoxLocalisationColorfulText): String? {
        return element.idElement?.text
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationColorfulText, name: String): ParadoxLocalisationColorfulText {
        val nameElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newNameElement = ParadoxLocalisationElementFactory.createColorfulText(element.project, name).idElement!!
        nameElement.replace(newNameElement)
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
    
    //region ParadoxLocalisationCommand
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationCommand, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationCommand
    }
    //endregion
    
    //region ParadoxLocalisationCommandIdentifier
    @JvmStatic
    fun getPrevIdentifier(element: ParadoxLocalisationCommandIdentifier): ParadoxLocalisationCommandScope? {
        var separator = element.prevSibling ?: return null
        if(separator.elementType == TokenType.WHITE_SPACE) separator = separator.prevSibling ?: return null
        if(separator.elementType != DOT) return null
        var prev = separator.prevSibling ?: return null
        if(prev.elementType == TokenType.WHITE_SPACE) prev = prev.prevSibling ?: return null
        if(prev !is ParadoxLocalisationCommandScope) return null
        return prev
    }
    
    @JvmStatic
    fun getNextIdentifier(element: ParadoxLocalisationCommandIdentifier): ParadoxLocalisationCommandIdentifier? {
        var separator = element.nextSibling ?: return null
        if(separator.elementType == TokenType.WHITE_SPACE) separator = separator.nextSibling ?: return null
        if(separator.elementType != DOT) return null
        var next = separator.nextSibling ?: return null
        if(next.elementType == TokenType.WHITE_SPACE) next = next.nextSibling ?: return null
        if(next !is ParadoxLocalisationCommandIdentifier) return null
        return next
    }
    //endregion
    
    //region ParadoxLocalisationCommandScope
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationCommandScope, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationCommandScope
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationCommandScope): String {
        return element.idElement.text.trim()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationCommandScope, name: String): ParadoxLocalisationCommandScope {
        val nameElement = element.idElement
        val newNameElement = ParadoxLocalisationElementFactory.createCommandScope(element.project, name).idElement
        nameElement.replace(newNameElement)
        return element
    }
    
    @JvmStatic
    fun getReference(element: ParadoxLocalisationCommandScope): ParadoxLocalisationCommandScopePsiReference {
        return CachedValuesManager.getCachedValue(element) {
            val value = run {
                val idElement = element.idElement
                val rangeInElement = idElement.textRangeInParent
                //兼容"event_target:"前缀
                val text = idElement.text
                val prefix = ParadoxValueSetValueHandler.EVENT_TARGET_PREFIX
                if(text.startsWith(prefix)) {
                    ParadoxLocalisationCommandScopePsiReference(element, rangeInElement.let { TextRange.create(it.startOffset + prefix.length, it.endOffset) }, prefix)
                } else {
                    ParadoxLocalisationCommandScopePsiReference(element, rangeInElement, null)
                }
            }
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    @JvmStatic
    fun getExpression(element: ParadoxLocalisationCommandScope): String {
        return element.name
    }
    
    @JvmStatic
    fun getConfigExpression(element: ParadoxLocalisationCommandScope): String? {
        val resolved = element.reference.resolve()
        val config = resolved?.getUserData(PlsKeys.cwtConfigKey)
        return when {
            config is CwtLocalisationLinkConfig -> "localisation scope"
            config is CwtSystemLinkConfig -> "system link"
            resolved is ParadoxValueSetValueElement -> {
                val valueSetName = resolved.valueSetName
                when {
                    valueSetName == "event_target" -> "value[event_target]"
                    valueSetName == "global_event_target" -> "value[global_event_target]"
                    else -> null
                }
            }
            else -> null
        }
    }
    //endregion
    
    //region ParadoxLocalisationCommandField
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationCommandField, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationCommandField
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationCommandField): String {
        return element.idElement?.text?.trim().orEmpty()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationCommandField, name: String): ParadoxLocalisationCommandField {
        val nameElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newNameElement = ParadoxLocalisationElementFactory.createCommandField(element.project, name).idElement!!
        nameElement.replace(newNameElement)
        return element
    }
    
    @JvmStatic
    fun getReference(element: ParadoxLocalisationCommandField): ParadoxLocalisationCommandFieldPsiReference? {
        return CachedValuesManager.getCachedValue(element) {
            val value = run {
                val rangeInElement = element.idElement?.textRangeInParent ?: return@run null
                ParadoxLocalisationCommandFieldPsiReference(element, rangeInElement)
            }
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    @JvmStatic
    fun getExpression(element: ParadoxLocalisationCommandField): String {
        return element.name
    }
    
    @JvmStatic
    fun getConfigExpression(element: ParadoxLocalisationCommandField): String? {
        val resolved = element.reference?.resolve()
        val config = resolved?.getUserData(PlsKeys.cwtConfigKey)
        return when {
            config is CwtLocalisationCommandConfig -> "localisation command"
            resolved is ParadoxScriptProperty -> "<scripted_loc>"
            resolved is ParadoxValueSetValueElement -> "value[variable]"
            else -> null
        }
    }
    //endregion
    
    //region ParadoxLocalisationConceptName
    @JvmStatic
    fun getIcon(element: ParadoxLocalisationConceptName, @IconFlags flags: Int): Icon {
        return PlsIcons.LocalisationConceptName
    }
    
    @JvmStatic
    fun getName(element: ParadoxLocalisationConceptName): String {
        return element.idElement?.text?.trim().orEmpty()
    }
    
    @JvmStatic
    fun setName(element: ParadoxLocalisationConceptName, name: String): ParadoxLocalisationConceptName {
        val nameElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newNameElement = ParadoxLocalisationElementFactory.createConceptName(element.project, name).idElement!!
        nameElement.replace(newNameElement)
        return element
    }
    
    @JvmStatic
    fun getReference(element: ParadoxLocalisationConceptName): ParadoxLocalisationConceptNamePsiReference? {
        return CachedValuesManager.getCachedValue(element) {
            val value = run {
                val rangeInElement = element.idElement?.textRangeInParent ?: return@run null
                ParadoxLocalisationConceptNamePsiReference(element, rangeInElement)
            }
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    @JvmStatic
    fun getExpression(element: ParadoxLocalisationConceptName): String {
        return element.name
    }
    //endregion
    
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
