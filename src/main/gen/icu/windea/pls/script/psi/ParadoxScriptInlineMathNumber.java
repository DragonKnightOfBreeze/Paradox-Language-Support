// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import icu.windea.pls.lang.psi.ParadoxTypedElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.ParadoxType;

public interface ParadoxScriptInlineMathNumber extends ParadoxScriptInlineMathFactor, PsiLiteralValue, ParadoxTypedElement {

  @NotNull String getValue();

  @NotNull ParadoxType getType();

  @NotNull String getExpression();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
