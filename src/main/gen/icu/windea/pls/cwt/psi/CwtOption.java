// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.psi.*;
import icu.windea.pls.lang.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface CwtOption extends CwtNamedElement {

  @NotNull
  CwtOptionKey getOptionKey();

  @Nullable
  CwtValue getOptionValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  CwtOption setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @Nullable
  String getValue();

  @NotNull
  CwtSeparatorType getSeparatorType();

}
