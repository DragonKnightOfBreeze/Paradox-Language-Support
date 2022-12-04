// This is a generated file. Not intended for manual editing.
package icu.windea.pls.gameTypes.stellaris.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.gameTypes.stellaris.references.StellarisFormatPsiReference;

public interface StellarisFormatReference extends PsiElement {

  @Nullable
  PsiElement getFormatReferenceToken();

  @Nullable
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  @Nullable
  StellarisFormatPsiReference getReference();

}
