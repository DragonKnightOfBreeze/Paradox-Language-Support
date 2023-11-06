// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface CwtProperty extends CwtNamedElement {

  @NotNull
  CwtPropertyKey getPropertyKey();

  @Nullable
  CwtValue getPropertyValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  CwtProperty setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @Nullable
  String getValue();

  @NotNull
  CwtSeparatorType getSeparatorType();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  SearchScope getUseScope();

}
