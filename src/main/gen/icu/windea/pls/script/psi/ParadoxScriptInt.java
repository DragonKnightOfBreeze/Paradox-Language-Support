// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.lang.model.*;
import org.jetbrains.annotations.*;

public interface ParadoxScriptInt extends ParadoxScriptValue, PsiLiteralValue, ContributedReferenceHost {

  @NotNull
  String getName();

  @NotNull
  String getValue();

  int getIntValue();

  @NotNull
  ParadoxType getType();

  @Nullable
  PsiReference getReference();

  @NotNull
  PsiReference[] getReferences();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
