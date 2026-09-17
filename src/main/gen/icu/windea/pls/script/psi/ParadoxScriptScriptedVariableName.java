// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParadoxScriptScriptedVariableName extends ParadoxScriptInterpolationContainer, PsiPresentableTextAwareElement, NavigatablePsiElement {

  @Nullable PsiElement getIdElement();

  @NotNull String getName();

  @NotNull String getPresentableText();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
