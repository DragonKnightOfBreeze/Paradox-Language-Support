// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.cwt.config.CwtSeparatorType;
import javax.swing.Icon;

public interface CwtProperty extends CwtNamedElement {

  @NotNull
  CwtPropertyKey getPropertyKey();

  @Nullable
  CwtValue getValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  CwtProperty setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  String getPropertyName();

  @NotNull
  String getPropertyValue();

  @NotNull
  String getPropertyTruncatedValue();

  @NotNull
  CwtSeparatorType getSeparatorType();

}
