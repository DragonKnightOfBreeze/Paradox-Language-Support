// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public interface ParadoxScriptConditionalExpression extends PsiPresentableTextAwareElement {

  @NotNull
  ParadoxScriptConditionalParameter getConditionalParameter();

  @NotNull String getPresentableText();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
