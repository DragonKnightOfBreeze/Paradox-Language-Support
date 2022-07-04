package icu.windea.pls.util.selector

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

@Suppress("UNCHECKED_CAST")
fun <T> nopSelector() = NopParadoxSelector as ParadoxSelector<T>

fun <T> selector() = ChainedParadoxSelector<T>()

fun fileSelector() = ChainedParadoxSelector<VirtualFile>()

fun scriptedVariableSelector() = ChainedParadoxSelector<ParadoxScriptVariable>()

fun definitionSelector() = ChainedParadoxSelector<ParadoxDefinitionProperty>()


fun <T> ChainedParadoxSelector<T>.gameType(gameType: ParadoxGameType?) = apply { if(gameType != null) selectors += ParadoxGameTypeSelector(gameType) }

/**
 * @param from [VirtualFile] | [PsiFile] | [PsiElement]
 */
fun <T> ChainedParadoxSelector<T>.gameTypeFrom(from: Any?) = apply { if(from != null) selectors += ParadoxGameTypeSelector(from = from) }

fun <T> ChainedParadoxSelector<T>.root(rootFile: VirtualFile?) = apply { if(rootFile != null) selectors += ParadoxRootFileSelector(rootFile) }

/**
 * @param from [VirtualFile] | [PsiFile] | [PsiElement]
 */
fun <T> ChainedParadoxSelector<T>.rootFrom(from: Any?) = apply { if(from != null) selectors += ParadoxRootFileSelector(from = from) }

fun <T> ChainedParadoxSelector<T>.preferRoot(rootFile: VirtualFile?) = apply { if(rootFile != null) selectors += ParadoxPreferRootFileSelector(rootFile) }

/**
 * @param from [VirtualFile] | [PsiFile] | [PsiElement]
 */
fun <T> ChainedParadoxSelector<T>.preferRootFrom(from: Any?) = apply { if(from != null) selectors += ParadoxPreferRootFileSelector(from = from) }