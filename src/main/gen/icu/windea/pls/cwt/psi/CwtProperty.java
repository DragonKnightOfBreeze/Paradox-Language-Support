// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface CwtProperty extends CwtNamedElement {

  @NotNull
  CwtPropertyKey getPropertyKey();

  @Nullable
  CwtPropertySeparator getPropertySeparator();

  @Nullable
  CwtValue getValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  void checkRename();

  @NotNull
  String getPropertyName();

  @NotNull
  String getPropertyValue();

  @NotNull
  String getPropertyTruncatedValue();

}
