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
import icu.windea.pls.cwt.psi.util.CwtPsiImplUtil;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;

public class CwtOptionCommentImpl extends ASTWrapperPsiElement implements CwtOptionComment {

  public CwtOptionCommentImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable CwtOption getOption() {
    return CwtPsiImplUtil.getOption(this);
  }

  @Override
  public @Nullable CwtValue getOptionValue() {
    return CwtPsiImplUtil.getOptionValue(this);
  }

  @Override
  public @NotNull IElementType getTokenType() {
    return CwtPsiImplUtil.getTokenType(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return CwtPsiImplUtil.getPresentation(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return CwtPsiImplUtil.getUseScope(this);
  }

}
