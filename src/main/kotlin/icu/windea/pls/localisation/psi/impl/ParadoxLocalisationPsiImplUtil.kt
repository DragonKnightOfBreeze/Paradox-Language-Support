package icu.windea.pls.localisation.psi.impl

import com.intellij.navigation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.impl.source.resolve.reference.*
import com.intellij.psi.search.*
import com.intellij.psi.tree.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.navigation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.*
import javax.swing.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationPsiImplUtil {
    //region ParadoxLocalisationPropertyList

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationPropertyList, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.LocalisationLocale
    }

    @JvmStatic
    fun getComponents(element: ParadoxLocalisationPropertyList): List<ParadoxLocalisationProperty> {
        return element.propertyList
    }

    @JvmStatic
    fun getIElementType(element: ParadoxLocalisationPropertyList): IElementType {
        return PROPERTY_LIST
    }

    @JvmStatic
    fun toString(element: ParadoxLocalisationPropertyList): String {
        return "ParadoxLocalisationPropertyList: ${element.locale?.name}"
    }

    //endregion

    //region ParadoxLocalisationLocale

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationLocale): PsiElement {
        val idElement = element.findChild { it.elementType == LOCALE_TOKEN }!!
        return idElement
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationLocale, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.LocalisationLocale
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationLocale): String {
        return element.idElement.text.orEmpty()
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationLocale, name: String): ParadoxLocalisationLocale {
        val newElement = ParadoxLocalisationElementFactory.createLocale(element.project, name)
        return element.replace(newElement).cast()
    }

    //endregion

    //region ParadoxLocalisationProperty

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationProperty, @Iconable.IconFlags flags: Int): Icon {
        if (element.localisationInfo != null) return PlsIcons.Nodes.Localisation
        return PlsIcons.Nodes.LocalisationProperty
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationProperty): String {
        runReadAction { element.stub }?.name?.let { return it }
        return element.propertyKey.idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationProperty, name: String): ParadoxLocalisationProperty {
        val nameElement = element.propertyKey
        val newNameElement = ParadoxLocalisationElementFactory.createPropertyKey(element.project, name).idElement
        nameElement.replace(newNameElement)
        return element
    }

    @JvmStatic
    fun getNameIdentifier(element: ParadoxLocalisationProperty): PsiElement {
        return element.propertyKey.idElement
    }

    @JvmStatic
    fun getTextOffset(element: ParadoxLocalisationProperty): Int {
        return element.propertyKey.textOffset
    }

    @JvmStatic
    fun getType(element: ParadoxLocalisationProperty): ParadoxLocalisationType? {
        runReadAction { element.stub?.type }?.let { return it }
        return element.localisationInfo?.type
    }

    @JvmStatic
    fun getValue(element: ParadoxLocalisationProperty): String? {
        return element.propertyValue?.text?.unquote()
    }

    @JvmStatic
    fun setValue(element: ParadoxLocalisationProperty, value: String): PsiElement {
        val valueElement = element.propertyValue
        if (valueElement == null) {
            val newElement = ParadoxLocalisationElementFactory.createProperty(element.project, element.name, value)
            return element.replace(newElement)
        } else {
            val newValueElement = ParadoxLocalisationElementFactory.createPropertyValue(element.project, value)
            valueElement.replace(newValueElement)
            return element
        }
    }

    @JvmStatic
    fun getIElementType(element: ParadoxLocalisationProperty): IElementType {
        return PROPERTY
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxLocalisationProperty, another: PsiElement): Boolean {
        //name & category (localisation / synced_localisation) & gameType
        return another is ParadoxLocalisationProperty && element.localisationInfo?.equals(another.localisationInfo) == true
    }

    @JvmStatic
    fun toString(element: ParadoxLocalisationProperty): String {
        return "ParadoxLocalisationProperty: ${element.name}"
    }

    //endregion

    //region ParadoxLocalisationPropertyKey

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationPropertyKey): PsiElement {
        val idElement = element.findChild { it.elementType == PROPERTY_KEY_TOKEN }!!
        return idElement
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationPropertyKey, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.LocalisationProperty
    }

    //endregion

    //region ParadoxLocalisationPropertyValue

    @JvmStatic
    fun getTokenElement(element: ParadoxLocalisationPropertyValue): PsiElement? {
        return element.findChild { it.elementType == PROPERTY_VALUE_TOKEN }
    }

    @JvmStatic
    fun getRichTextList(element: ParadoxLocalisationPropertyValue): List<ParadoxLocalisationRichText> {
        return element.tokenElement?.findChildren<_>() ?: emptyList()
    }

    //endregion

    //region ParadoxLocalisationString

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationString): PsiElement {
        val idElement = element.findChild { it.elementType == STRING_TOKEN }!!
        return idElement
    }

    //endregion

    //region ParadoxLocalisationColorfulText

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationColorfulText): PsiElement? {
        val idElement = element.findChild { it.elementType == COLOR_TOKEN }
        return idElement
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationColorfulText): String? {
        return element.idElement?.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationColorfulText, name: String): ParadoxLocalisationColorfulText {
        val idElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createColorfulText(element.project, name).idElement ?: throw IllegalStateException()
        idElement.replace(newIdElement)
        return element
    }

    //endregion

    //region ParadoxLocalisationParameter

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationParameter): PsiElement? {
        val idElement = element.findChild { it.elementType == PARAMETER_TOKEN }
        return idElement
    }

    @JvmStatic
    fun getArgumentElement(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterArgument? {
        return element.findChild<_>(forward = false)
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationParameter): String {
        return element.idElement?.text.orEmpty()
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationParameter, name: String): ParadoxLocalisationParameter {
        val idElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createParameter(element.project, name).idElement ?: throw IllegalStateException()
        idElement.replace(newIdElement)
        return element
    }

    //endregion

    //region ParadoxLocalisationParameterArgument

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationParameterArgument): PsiElement? {
        val idElement = element.findChild(forward = false) { it.elementType == ARGUMENT_TOKEN }
        return idElement
    }

    //endregion

    //region ParadoxLocalisationScriptedVariableReference

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationScriptedVariableReference): PsiElement? {
        val idElement = element.findChild { it.elementType == SCRIPTED_VARIABLE_REFERENCE_TOKEN }
        return idElement
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationScriptedVariableReference, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.ScriptedVariable
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationScriptedVariableReference): String? {
        return element.idElement?.text?.orNull()
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationScriptedVariableReference, name: String): ParadoxLocalisationScriptedVariableReference {
        val idElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createScriptedVariableReference(element.project, name).idElement ?: throw IllegalStateException()
        idElement.replace(newIdElement)
        return element
    }

    //endregion

    //region ParadoxLocalisationCommand

    @JvmStatic
    fun getArgumentElement(element: ParadoxLocalisationCommand): ParadoxLocalisationCommandArgument? {
        return element.findChild<_>(forward = false)
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationCommand, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.LocalisationCommand
    }

    //endregion

    //region ParadoxLocalisationCommandText

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationCommandText): PsiElement? {
        val idElement = element.findChild { it.elementType == COMMAND_TEXT_TOKEN }
        if (!ParadoxLocalisationPsiUtil.isIdElement(idElement)) return null
        return idElement
    }

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

    //endregion

    //region ParadoxLocalisationCommandArgument

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationCommandArgument): PsiElement? {
        val idElement = element.findChild(forward = false) { it.elementType == ARGUMENT_TOKEN }
        if (!ParadoxLocalisationPsiUtil.isIdElement(idElement)) return null
        return idElement
    }

    //endregion

    //region ParadoxLocalisationIcon

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationIcon): PsiElement? {
        val idElement = element.findChild { it.elementType == ICON_TOKEN }
        if (!ParadoxLocalisationPsiUtil.isIdElement(idElement)) return null
        return idElement
    }

    @JvmStatic
    fun getArgumentElement(element: ParadoxLocalisationIcon): ParadoxLocalisationIconArgument? {
        return element.findChild<_>(forward = false)
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationIcon, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.LocalisationIcon
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationIcon): String? {
        val idElement = element.idElement ?: return null
        return idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationIcon, name: String): ParadoxLocalisationIcon {
        val idElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createIcon(element.project, name).idElement ?: throw IllegalStateException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getFrame(element: ParadoxLocalisationIcon): Int {
        //这里的帧数可以用$PARAM$表示，对应某个本地化参数，此时直接返回0
        return element.argumentElement?.idElement?.text?.toIntOrNull() ?: 0
    }

    //endregion

    //region ParadoxLocalisationIconArgument

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationIconArgument): PsiElement? {
        val idElement = element.findChild(forward = false) { it.elementType == ARGUMENT_TOKEN }
        if (!ParadoxLocalisationPsiUtil.isIdElement(idElement)) return null
        return idElement
    }

    //endregion

    //region ParadoxLocalisationConceptCommand

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationConceptCommand, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.LocalisationCommand
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationConceptCommand): String {
        return element.conceptName?.name.orEmpty()
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationConceptCommand, name: String): ParadoxLocalisationConceptCommand {
        val idElement = element.conceptName?.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createConceptName(element.project, name).idElement ?: throw IllegalStateException()
        idElement.replace(newIdElement)
        return element
    }

    //endregion

    //region ParadoxLocalisationConceptName

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationConceptName, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.LocalisationConcept
    }

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationConceptName): PsiElement? {
        val idElement = element.findChild { it.elementType == CONCEPT_NAME_TOKEN }
        if (!ParadoxLocalisationPsiUtil.isIdElement(idElement)) return null
        return idElement
    }

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

    //endregion

    //region ParadoxLocalisationTextFormat

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationTextFormat): PsiElement? {
        val idElement = element.findChild { it.elementType == TEXT_FORMAT_TOKEN }
        if (!ParadoxLocalisationPsiUtil.isIdElement(idElement)) return null
        return idElement
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationTextFormat, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.LocalisationTextFormat
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationTextFormat): String? {
        val idElement = element.idElement ?: return null
        return idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationTextFormat, name: String): ParadoxLocalisationTextFormat {
        val idElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createTextFormat(element.project, name).idElement ?: throw IllegalStateException()
        idElement.replace(newIdElement)
        return element
    }

    //endregion

    //region ParadoxLocalisationTextIcon

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationTextIcon): PsiElement? {
        val idElement = element.findChild { it.elementType == TEXT_ICON_TOKEN }
        if (!ParadoxLocalisationPsiUtil.isIdElement(idElement)) return null
        return idElement
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationTextIcon, @Iconable.IconFlags flags: Int): Icon {
        return PlsIcons.Nodes.LocalisationTextFormat
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationTextIcon): String? {
        val idElement = element.idElement ?: return null
        return idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationTextIcon, name: String): ParadoxLocalisationTextIcon {
        val idElement = element.idElement ?: throw IncorrectOperationException() //不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createTextIcon(element.project, name).idElement ?: throw IllegalStateException()
        idElement.replace(newIdElement)
        return element
    }

    //endregion

    @JvmStatic
    fun getReference(element: PsiElement): PsiReference? {
        return element.references.singleOrNull()
    }

    @JvmStatic
    fun getReferences(element: PsiElement): Array<out PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element)
    }

    @JvmStatic
    fun getPresentation(element: PsiElement): ItemPresentation {
        return ParadoxLocalisationItemPresentation(element)
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
