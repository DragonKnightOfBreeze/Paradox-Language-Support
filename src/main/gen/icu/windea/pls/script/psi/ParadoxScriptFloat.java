// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

public interface ParadoxScriptFloat extends ParadoxScriptValue, PsiLiteralValue, ContributedReferenceHost {

  @NotNull
  String getName();

  @NotNull
  String getValue();

  float getFloatValue();

  @NotNull
  ParadoxType getType();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
