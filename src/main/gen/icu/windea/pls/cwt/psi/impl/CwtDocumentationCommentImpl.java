// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.cwt.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;

public class CwtDocumentationCommentImpl extends ASTWrapperPsiElement implements CwtDocumentationComment {

  public CwtDocumentationCommentImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  @NotNull
  public IElementType getTokenType() {
    return CwtPsiImplUtil.getTokenType(this);
  }

  @Override
  @NotNull
  public ItemPresentation getPresentation() {
    return CwtPsiImplUtil.getPresentation(this);
  }

  @Override
  @NotNull
  public SearchScope getUseScope() {
    return CwtPsiImplUtil.getUseScope(this);
  }

}
