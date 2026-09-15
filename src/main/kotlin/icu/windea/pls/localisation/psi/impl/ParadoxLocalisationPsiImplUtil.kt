package icu.windea.pls.localisation.psi.impl

import com.intellij.openapi.util.Iconable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.ResolveScopeManager
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.settings.ChronicleInternalSettings
import icu.windea.pls.core.children
import icu.windea.pls.core.orNull
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.core.select.listBy
import icu.windea.pls.core.select.oneBy
import icu.windea.pls.core.text.QuotePattern
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.core.transformAndKeepQuotes
import icu.windea.pls.core.truncate
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.values.or
import icu.windea.pls.core.util.values.unresolved
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationColorfulText
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandArgument
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommandText
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
import icu.windea.pls.localisation.psi.ParadoxLocalisationContextTag
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementFactory
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementManipulationService
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementPresentation
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon
import icu.windea.pls.localisation.psi.ParadoxLocalisationIconArgument
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameterArgument
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyKey
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiService
import icu.windea.pls.localisation.psi.ParadoxLocalisationRichText
import icu.windea.pls.localisation.psi.ParadoxLocalisationScriptedVariableReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationTag
import icu.windea.pls.localisation.psi.ParadoxLocalisationTaggedParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.localisation.text.ParadoxLocalisation
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constants.ChronicleStrings
import javax.swing.Icon

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationPsiImplUtil {
    // region ParadoxLocalisationFile

    @JvmStatic
    fun getPropertyLists(element: ParadoxLocalisationFile): List<ParadoxLocalisationPropertyList> {
        return element.children().listBy()
    }

    @JvmStatic
    fun getPropertyList(element: ParadoxLocalisationFile): ParadoxLocalisationPropertyList? {
        return getPropertyLists(element).singleOrNull() // single, not first
    }

    @JvmStatic
    fun getProperties(element: ParadoxLocalisationFile): List<ParadoxLocalisationProperty> {
        return getPropertyList(element)?.propertyList.orEmpty()
    }

    @JvmStatic
    fun isEquivalentTo(element: ParadoxLocalisationFile, another: PsiElement?): Boolean {
        if (element === another) return true
        if (another !is ParadoxLocalisationFile) return false
        return ParadoxFileManager.isEquivalentFile(element, another)
    }

    // endregion

    // region ParadoxLocalisationPropertyList

    @JvmStatic
    fun getComponents(element: ParadoxLocalisationPropertyList): List<PsiElement> {
        return element.children().listBy<ParadoxLocalisationProperty>()
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationPropertyList, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.LocalisationLocale
    }

    @JvmStatic
    fun getIElementType(element: ParadoxLocalisationPropertyList): IElementType {
        return PROPERTY_LIST
    }

    // endregion

    // region ParadoxLocalisationLocale

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationLocale): PsiElement {
        return element.firstChild?.takeIf { it.elementType === LOCALE_TOKEN }!!
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationLocale, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.LocalisationLocale
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationLocale): String {
        element.stub?.locale?.let { return it }
        return element.idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationLocale, name: String): ParadoxLocalisationLocale {
        val idElement = element.idElement
        val newIdElement = ParadoxLocalisationElementFactory.createLocale(element.project, name).idElement
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getIElementType(element: ParadoxLocalisationLocale): IElementType {
        return LOCALE
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationLocale): String {
        val name = element.name
        return name.or.unresolved()
    }

    // endregion

    // region ParadoxLocalisationProperty

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationProperty, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.LocalisationProperty
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationProperty): String {
        element.stub?.name?.let { return it }
        return element.propertyKey.idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationProperty, name: String): ParadoxLocalisationProperty {
        val nameElement = element.propertyKey
        val idElement = nameElement.idElement
        val newIdElement = ParadoxLocalisationElementFactory.createPropertyKey(element.project, name).idElement
        idElement.replace(newIdElement)
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
        element.stub?.type?.let { return it }
        return ParadoxLocalisationType.resolve(element)
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
        // name & type & gameType
        if (another !is ParadoxLocalisationProperty) return false
        if (element.name.let { it.isEmpty() || it != another.name }) return false
        if (element.type.let { it == null || it != another.type }) return false
        if (selectGameType(element) != selectGameType(another)) return false
        return true
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationProperty): String {
        val name = element.name
        return ChronicleStrings.localisationPropertyFolder(name.or.unresolved())
    }

    // endregion

    // region ParadoxLocalisationPropertyKey

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationPropertyKey): PsiElement {
        return element.firstChild?.takeIf { it.elementType === PROPERTY_KEY_TOKEN }!!
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationPropertyKey, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.LocalisationProperty
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationPropertyKey): String {
        return element.idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationPropertyKey, name: String): ParadoxLocalisationPropertyKey {
        val idElement = element.idElement
        val newIdElement = ParadoxLocalisationElementFactory.createPropertyKey(element.project, name).idElement
        idElement.replace(newIdElement)
        return element
    }

    // endregion

    // region ParadoxLocalisationPropertyValue

    @JvmStatic
    fun getTokenElement(element: ParadoxLocalisationPropertyValue): PsiElement? {
        return element.children().oneBy(PROPERTY_VALUE_TOKEN)
    }

    @JvmStatic
    fun getRichTextList(element: ParadoxLocalisationPropertyValue): List<ParadoxLocalisationRichText> {
        return element.tokenElement.children().listBy()
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationPropertyValue): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.transformAndKeepQuotes { it.truncate(limit) }
    }

    // endregion

    // region ParadoxLocalisationColorfulText

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationColorfulText): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType === COLOR_TOKEN }
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationColorfulText): String? {
        return element.idElement?.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationColorfulText, name: String): ParadoxLocalisationColorfulText {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createColorfulText(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationColorfulText): String {
        val name = element.name
        return ChronicleStrings.localisationColorfulTextFolder(name.or.unresolved())
    }

    // endregion

    // region ParadoxLocalisationParameter

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationParameter): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType === PARAMETER_TOKEN }?.takeIf { ParadoxLocalisationPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun getArgumentElement(element: ParadoxLocalisationParameter): ParadoxLocalisationParameterArgument? {
        return element.children(forward = false).oneBy()
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationParameter): String {
        return element.idElement?.text.orEmpty()
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationParameter, name: String): ParadoxLocalisationParameter {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createParameter(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationParameter): String {
        val name = element.name
        return ChronicleStrings.localisationParameterFolder(name.or.unresolved())
    }

    // endregion

    // region ParadoxLocalisationParameterArgument

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationParameterArgument): PsiElement? {
        return element.firstChild?.takeIf { it.elementType === ARGUMENT_TOKEN }
    }

    // endregion

    // region ParadoxLocalisationScriptedVariableReference

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationScriptedVariableReference): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType == SCRIPTED_VARIABLE_REFERENCE_TOKEN }
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationScriptedVariableReference, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.ScriptedVariable
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationScriptedVariableReference): String? {
        return element.idElement?.text?.orNull()
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationScriptedVariableReference, name: String): ParadoxLocalisationScriptedVariableReference {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createScriptedVariableReference(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    // endregion

    // region ParadoxLocalisationCommand

    @JvmStatic
    fun getArgumentElement(element: ParadoxLocalisationCommand): ParadoxLocalisationCommandArgument? {
        return element.children(forward = false).oneBy()
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationCommand, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.LocalisationCommand
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationCommand): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        val expression = element.commandText?.presentableText
        return ChronicleStrings.localisationCommandFolder(expression.orEmpty().truncate(limit))
    }

    // endregion

    // region ParadoxLocalisationCommandText

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationCommandText): PsiElement? {
        return element.children().oneBy(COMMAND_TEXT_TOKEN)?.takeIf { ParadoxLocalisationPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun setValue(element: ParadoxLocalisationCommandText, value: String): ParadoxLocalisationCommandText {
        return ParadoxLocalisationElementManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: ParadoxLocalisationCommandText, content: String, range: TextRange): ParadoxLocalisationCommandText {
        return ParadoxLocalisationElementManipulationService.changeContent(element, content, range)
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationCommandText): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.truncate(limit)
    }

    // endregion

    // region ParadoxLocalisationCommandArgument

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationCommandArgument): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == ARGUMENT_TOKEN }?.takeIf { ParadoxLocalisationPsiService.isIdElement(it) }
    }

    // endregion

    // region ParadoxLocalisationConceptCommand

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationConceptCommand, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.LocalisationConceptCommand
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationConceptCommand): String {
        return element.conceptName?.name.orEmpty()
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationConceptCommand, name: String): ParadoxLocalisationConceptCommand {
        val idElement = element.conceptName?.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createConceptName(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationConceptCommand): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        val expression = element.conceptName?.presentableText
        val withText = element.conceptString != null
        return if (withText) {
            ChronicleStrings.localisationConceptCommandFolder(expression.orEmpty().truncate(limit))
        } else {
            ChronicleStrings.localisationConceptCommandFolderWithText(expression.orEmpty().truncate(limit))
        }
    }

    // endregion

    // region ParadoxLocalisationConceptName

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationConceptName): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType == CONCEPT_NAME_TOKEN }?.takeIf { ParadoxLocalisationPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun setValue(element: ParadoxLocalisationConceptName, value: String): ParadoxLocalisationConceptName {
        return ParadoxLocalisationElementManipulationService.changeContent(element, value)
    }

    @JvmStatic
    fun setContent(element: ParadoxLocalisationConceptName, content: String, range: TextRange): ParadoxLocalisationConceptName {
        return ParadoxLocalisationElementManipulationService.changeContent(element, content, range)
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationConceptName): String {
        val limit = ChronicleInternalSettings.getInstance().presentableTextLimit
        return element.text.truncate(limit)
    }

    // endregion

    // region ParadoxLocalisationIcon

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationIcon): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType == ICON_TOKEN }?.takeIf { ParadoxLocalisationPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun getArgumentElement(element: ParadoxLocalisationIcon): ParadoxLocalisationIconArgument? {
        return element.children(forward = false).oneBy()
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationIcon, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.LocalisationIcon
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationIcon): String? {
        val idElement = element.idElement ?: return null
        return idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationIcon, name: String): ParadoxLocalisationIcon {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createIcon(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationIcon): String {
        val name = element.name
        return ChronicleStrings.localisationIconFolder(name.or.unresolved())
    }

    // endregion

    // region ParadoxLocalisationIconArgument

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationIconArgument): PsiElement? {
        return element.firstChild?.takeIf { it.elementType == ARGUMENT_TOKEN }?.takeIf { ParadoxLocalisationPsiService.isIdElement(it) }
    }

    // endregion

    // region ParadoxLocalisationTextIcon

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationTextIcon): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType == TEXT_ICON_TOKEN }?.takeIf { ParadoxLocalisationPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationTextIcon, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.LocalisationTextFormat
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationTextIcon): String? {
        val idElement = element.idElement ?: return null
        return idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationTextIcon, name: String): ParadoxLocalisationTextIcon {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createTextIcon(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationTextIcon): String {
        val name = element.name
        return ChronicleStrings.localisationTextIconFolder(name.or.unresolved())
    }

    // endregion

    // region ParadoxLocalisationTextFormat

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationTextFormat): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType == TEXT_FORMAT_TOKEN }?.takeIf { ParadoxLocalisationPsiService.isIdElement(it) }
    }

    @JvmStatic
    fun getIcon(element: ParadoxLocalisationTextFormat, @Iconable.IconFlags flags: Int): Icon {
        return ChronicleIcons.Nodes.LocalisationTextFormat
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationTextFormat): String? {
        val idElement = element.idElement ?: return null
        return idElement.text
    }

    @JvmStatic
    fun setName(element: ParadoxLocalisationTextFormat, name: String): ParadoxLocalisationTextFormat {
        val idElement = element.idElement ?: throw IncorrectOperationException() // 不支持重命名
        val newIdElement = ParadoxLocalisationElementFactory.createTextFormat(element.project, name).idElement ?: throw IncorrectOperationException()
        idElement.replace(newIdElement)
        return element
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationTextFormat): String {
        val name = element.name
        return ChronicleStrings.localisationTextFormatFolder(name.or.unresolved())
    }

    // endregion

    // region ParadoxLocalisationTag

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationTag): PsiElement {
        return element.firstChild?.takeIf { it.elementType == TAG_TOKEN }!!
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationTag): String {
        val idElement = element.idElement
        return idElement.text
    }

    // endregion

    // region ParadoxLocalisationContextTag

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationContextTag): PsiElement {
        return element.firstChild?.takeIf { it.elementType == CONTEXT_TAG_TOKEN }!!
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationContextTag): String {
        val idElement = element.idElement
        return idElement.text
    }

    // endregion

    // region ParadoxLocalisationTaggedParameter

    @JvmStatic
    fun getIdElement(element: ParadoxLocalisationTaggedParameter): PsiElement? {
        return element.firstChild?.nextSibling?.takeIf { it.elementType === PARAMETER_TOKEN }
    }

    @JvmStatic
    fun getName(element: ParadoxLocalisationTaggedParameter): String? {
        val idElement = element.idElement ?: return null
        return idElement.text
    }

    // endregion

    // region ParadoxLocalisationExpressionElement

    @JvmStatic
    fun getName(element: ParadoxLocalisationExpressionElement): String {
        return element.value
    }

    @JvmStatic
    fun getValue(element: ParadoxLocalisationExpressionElement): String {
        return element.text
    }

    @JvmStatic
    fun setValue(element: ParadoxLocalisationExpressionElement, value: String): ParadoxLocalisationExpressionElement {
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun setContent(element: ParadoxLocalisationExpressionElement, content: String, range: TextRange): ParadoxLocalisationExpressionElement {
        throw IncorrectOperationException()
    }

    @JvmStatic
    fun getPresentableText(element: ParadoxLocalisationExpressionElement): String {
        return element.value
    }

    // endregion

    // region Common Methods

    @JvmStatic
    fun getResolveScope(element: PsiElement): GlobalSearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementResolveScope(element)
    }

    @JvmStatic
    fun getUseScope(element: PsiElement): SearchScope {
        return ParadoxSearchScope.fromElement(element) ?: ResolveScopeManager.getElementUseScope(element)
    }

    @JvmStatic
    fun toString(element: PsiElement): String {
        return PsiService.toPresentableString(element)
    }

    @JvmStatic
    fun getPresentation(element: NavigatablePsiElement): ParadoxLocalisationElementPresentation {
        return ParadoxLocalisationElementPresentation(element)
    }

    @JvmStatic
    fun getQuotePattern(element: PsiQuoteAwareElement): QuotePattern {
        return QuotePatterns.ParadoxLocalisation
    }

    @JvmStatic
    fun getReference(element: PsiElement): PsiReference? {
        return element.references.singleOrNull()
    }

    @JvmStatic
    fun getReferences(element: PsiElement): Array<out PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element)
    }

    @JvmStatic
    fun getReferences(element: ParadoxLocalisationExpressionElement): Array<out PsiReference> {
        return ParadoxExpressionManager.getReferences(element)
    }

    // endregion
}
