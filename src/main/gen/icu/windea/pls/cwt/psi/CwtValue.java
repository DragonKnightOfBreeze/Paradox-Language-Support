// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface CwtValue extends CwtNamedElement {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  @NotNull
  String getTruncatedValue();

  @NotNull
  String getName();

  @NotNull
  CwtValue setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

}
