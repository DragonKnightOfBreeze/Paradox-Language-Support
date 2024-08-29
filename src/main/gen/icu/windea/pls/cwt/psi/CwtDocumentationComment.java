// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiComment;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;

public interface CwtDocumentationComment extends PsiComment {

  @Nullable
  CwtDocumentationText getDocumentationText();

  @NotNull
  IElementType getTokenType();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  SearchScope getUseScope();

}
