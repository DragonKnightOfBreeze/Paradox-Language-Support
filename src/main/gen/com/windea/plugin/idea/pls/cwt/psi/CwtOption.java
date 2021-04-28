// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
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

  void checkRename();

  @NotNull
  String getOptionName();

  @NotNull
  String getOptionValue();

  @NotNull
  String getOptionTruncatedValue();

}
