// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.config.cwt.config.CwtSeparatorType;
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
  CwtOption setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  String getOptionName();

  @NotNull
  String getOptionValue();

  @NotNull
  String getOptionTruncatedValue();

  @NotNull
  CwtSeparatorType getSeparatorType();

}
