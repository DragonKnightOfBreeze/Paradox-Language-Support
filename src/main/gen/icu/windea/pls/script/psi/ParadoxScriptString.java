// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.script.reference.ParadoxScriptStringPropertyPsiReference;

public interface ParadoxScriptString extends ParadoxScriptStringValue {

  @Nullable
  PsiElement getQuotedStringToken();

  @Nullable
  PsiElement getStringToken();

  @NotNull
  String getValue();

  @NotNull
  PsiElement setValue(@NotNull String name);

  @NotNull
  ParadoxScriptStringPropertyPsiReference getReference();

  //WARNING: getReferneceName(...) is skipped
  //matching getReferneceName(ParadoxScriptString, ...)
  //methods are not found in ParadoxScriptPsiImplUtil

}
