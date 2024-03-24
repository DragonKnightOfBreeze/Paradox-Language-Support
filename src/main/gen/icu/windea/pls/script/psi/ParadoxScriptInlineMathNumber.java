// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.lang.psi.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

public interface ParadoxScriptInlineMathNumber extends ParadoxScriptInlineMathFactor, PsiLiteralValue, ParadoxTypedElement {

  @NotNull
  String getValue();

  @NotNull
  ParadoxType getType();

  @NotNull
  String getExpression();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
