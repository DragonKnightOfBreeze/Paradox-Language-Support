// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParadoxScriptNormalParameterArgument extends ParadoxScriptArgument, ParadoxLanguageInjectionHost {

  @Nullable PsiElement getIdElement();

  @NotNull ParadoxScriptElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
