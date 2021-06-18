// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.model.SeparatorType;
import javax.swing.Icon;

public interface CwtOption extends CwtNamedElement {

  @NotNull
  CwtOptionKey getOptionKey();

  @Nullable
  CwtValue getValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  String getOptionName();

  @NotNull
  String getOptionValue();

  @NotNull
  String getOptionTruncatedValue();

  @NotNull
  SeparatorType getSeparatorType();

}
