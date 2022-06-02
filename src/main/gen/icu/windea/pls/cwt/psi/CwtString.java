// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface CwtString extends CwtValue, CwtNamedElement {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  @NotNull
  String getStringValue();

  @NotNull
  String getName();

  @NotNull
  CwtString setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

}
