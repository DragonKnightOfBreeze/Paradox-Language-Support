// Generated by JFlex 1.9.1 http://jflex.de/  (tweaked for IntelliJ platform)
// source: CwtLexer.flex

package icu.windea.pls.cwt.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;


public class _CwtLexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int IN_PROPERTY_KEY = 2;
  public static final int IN_PROPERTY_SEPARATOR = 4;
  public static final int IN_PROPERTY_VALUE = 6;
  public static final int EXPECT_NEXT = 8;
  public static final int IN_DOCUMENTATION = 10;
  public static final int IN_OPTION = 12;
  public static final int IN_OPTION_KEY = 14;
  public static final int IN_OPTION_SEPARATOR = 16;
  public static final int IN_OPTION_VALUE = 18;
  public static final int IN_OPTION_VALUE_TOP_STRING = 20;
  public static final int EXPECT_NEXT_OPTION = 22;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5,  5,  6,  6,  7,  7, 
     8,  8,  6,  6,  9,  9, 10, 10
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\25\u0100\1\u0200\11\u0100\1\u0300\17\u0100\1\u0400\u10cf\u0100";

  private static int [] zzUnpackcmap_top() {
    int [] result = new int[4352];
    int offset = 0;
    offset = zzUnpackcmap_top(ZZ_CMAP_TOP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_top(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Second-level tables for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_BLOCKS = zzUnpackcmap_blocks();

  private static final String ZZ_CMAP_BLOCKS_PACKED_0 =
    "\11\0\1\1\1\2\2\1\1\2\22\0\1\1\1\3"+
    "\1\4\1\5\7\0\1\6\1\0\1\6\1\7\1\0"+
    "\12\10\2\0\1\11\1\12\1\13\35\0\1\14\10\0"+
    "\1\15\10\0\1\16\1\17\3\0\1\20\5\0\1\21"+
    "\1\0\1\22\1\0\1\23\7\0\1\1\32\0\1\1"+
    "\u01df\0\1\1\177\0\13\1\35\0\2\1\5\0\1\1"+
    "\57\0\1\1\240\0\1\1\377\0";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[1280];
    int offset = 0;
    offset = zzUnpackcmap_blocks(ZZ_CMAP_BLOCKS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_blocks(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /**
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\13\0\1\1\1\2\1\1\1\3\2\1\1\4\1\1"+
    "\1\5\2\1\1\6\1\7\1\10\1\11\1\10\1\3"+
    "\3\12\1\13\1\11\1\3\1\14\1\15\1\11\3\15"+
    "\1\16\1\15\1\17\2\15\1\20\1\21\2\22\2\12"+
    "\1\23\2\24\1\0\1\1\2\0\1\1\1\5\1\0"+
    "\1\25\1\26\1\5\1\27\1\1\1\10\1\0\1\30"+
    "\1\13\1\0\1\15\2\0\1\15\1\17\1\0\1\31"+
    "\1\17\1\32\1\15\1\22\1\0\1\33\1\23\1\0"+
    "\1\24\1\0\1\34";

  private static int [] zzUnpackAction() {
    int [] result = new int[89];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\24\0\50\0\74\0\120\0\144\0\170\0\214"+
    "\0\240\0\264\0\310\0\334\0\360\0\u0104\0\u0118\0\u012c"+
    "\0\u0140\0\u012c\0\u0154\0\u0168\0\u017c\0\u0190\0\u0168\0\u0168"+
    "\0\u01a4\0\u01b8\0\u01cc\0\u01e0\0\u0168\0\u01f4\0\u0208\0\u021c"+
    "\0\u0230\0\u0244\0\u0258\0\u026c\0\u0280\0\u0294\0\u02a8\0\u02bc"+
    "\0\u02a8\0\u02d0\0\u0168\0\u02e4\0\u02f8\0\u0168\0\u0168\0\u030c"+
    "\0\u0320\0\u0334\0\u0348\0\u035c\0\u0370\0\u0384\0\u0398\0\u0398"+
    "\0\u03ac\0\u03c0\0\u03d4\0\u0104\0\u03e8\0\u03fc\0\u0140\0\334"+
    "\0\334\0\u0410\0\u0168\0\u0424\0\u0168\0\u0168\0\u0438\0\u0438"+
    "\0\u044c\0\u0460\0\u0474\0\u0294\0\u0488\0\u02bc\0\u026c\0\u026c"+
    "\0\u049c\0\u0168\0\u04b0\0\u0168\0\u0168\0\u04c4\0\u0168\0\u04d8"+
    "\0\u0168";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[89];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length() - 1;
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpacktrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\14\2\15\1\14\1\16\1\17\1\20\1\21\1\22"+
    "\1\23\1\24\3\14\1\25\2\14\1\26\1\27\1\30"+
    "\1\31\2\32\1\31\1\33\1\34\4\31\1\35\7\31"+
    "\1\27\1\30\1\35\2\32\1\36\1\35\1\34\3\35"+
    "\1\37\1\40\7\35\1\27\1\30\1\14\2\41\1\14"+
    "\1\16\1\17\1\20\1\21\1\22\1\23\1\24\3\14"+
    "\1\25\2\14\1\26\1\27\1\30\1\35\2\32\2\35"+
    "\1\42\14\35\1\27\1\30\1\43\2\32\21\43\1\44"+
    "\2\45\1\44\1\46\1\42\1\47\1\50\1\51\1\52"+
    "\1\53\3\44\1\54\2\44\1\55\1\56\1\57\1\60"+
    "\2\32\1\60\1\61\1\42\4\60\1\35\7\60\1\56"+
    "\1\57\1\35\2\32\1\62\1\35\1\42\3\35\1\63"+
    "\1\64\7\35\1\56\1\57\1\65\2\32\1\65\1\66"+
    "\1\42\4\65\1\35\7\65\1\56\1\57\1\35\2\32"+
    "\2\35\1\42\14\35\1\56\1\57\1\14\2\67\1\14"+
    "\1\70\1\0\3\14\1\23\1\24\7\14\3\0\2\15"+
    "\1\71\5\0\1\72\1\24\11\0\2\16\1\67\1\16"+
    "\1\70\4\16\1\73\1\74\1\16\1\75\7\16\2\42"+
    "\1\0\2\42\1\76\16\42\1\14\2\67\1\14\1\70"+
    "\1\0\1\14\1\21\1\22\1\23\1\24\7\14\2\0"+
    "\1\14\2\67\1\14\1\70\1\0\2\14\1\77\1\23"+
    "\1\24\7\14\2\0\1\14\2\67\1\14\1\70\1\0"+
    "\3\14\1\23\1\24\1\100\6\14\26\0\1\14\2\67"+
    "\1\14\1\70\1\0\3\14\1\23\1\24\4\14\1\101"+
    "\2\14\2\0\1\14\2\67\1\14\1\70\1\0\3\14"+
    "\1\23\1\24\2\14\1\102\4\14\2\0\1\31\2\0"+
    "\1\31\1\103\1\0\4\31\1\0\7\31\3\0\2\32"+
    "\21\0\2\33\1\0\1\33\1\103\7\33\1\104\7\33"+
    "\2\42\1\0\2\42\1\0\16\42\12\0\1\105\24\0"+
    "\1\105\22\0\1\106\12\0\2\41\1\71\5\0\1\72"+
    "\1\24\11\0\2\42\1\0\21\42\2\43\1\0\21\43"+
    "\1\44\2\107\1\44\1\110\1\0\3\44\1\52\1\53"+
    "\7\44\3\0\2\45\1\111\5\0\1\112\1\53\11\0"+
    "\2\46\1\107\1\46\1\110\4\46\1\113\1\114\1\46"+
    "\1\115\7\46\1\44\2\107\1\44\1\110\1\0\1\44"+
    "\1\50\1\51\1\52\1\53\7\44\2\0\1\44\2\107"+
    "\1\44\1\110\1\0\2\44\1\116\1\52\1\53\7\44"+
    "\2\0\1\44\2\107\1\44\1\110\1\0\3\44\1\52"+
    "\1\53\1\117\6\44\2\0\1\44\2\107\1\44\1\110"+
    "\1\0\3\44\1\52\1\53\4\44\1\120\2\44\2\0"+
    "\1\44\2\107\1\44\1\110\1\0\3\44\1\52\1\53"+
    "\2\44\1\121\4\44\2\0\1\60\2\0\1\60\1\122"+
    "\1\0\4\60\1\0\7\60\2\0\2\61\1\0\1\61"+
    "\1\122\7\61\1\123\7\61\12\0\1\124\24\0\1\124"+
    "\22\0\1\125\11\0\1\65\1\126\1\0\1\65\1\127"+
    "\1\0\4\65\1\0\7\65\2\0\2\66\1\0\1\66"+
    "\1\127\7\66\1\130\7\66\1\0\2\67\1\71\5\0"+
    "\1\72\1\24\23\0\1\24\24\0\1\24\10\0\2\16"+
    "\1\67\1\16\1\70\4\16\1\73\2\74\1\75\33\16"+
    "\5\0\1\131\16\0\1\14\2\67\1\14\1\70\1\0"+
    "\3\14\1\23\1\24\5\14\1\101\1\14\2\0\24\33"+
    "\1\0\2\107\1\111\5\0\1\112\1\53\23\0\1\53"+
    "\24\0\1\53\10\0\2\46\1\107\1\46\1\110\4\46"+
    "\1\113\2\114\1\115\33\46\1\44\2\107\1\44\1\110"+
    "\1\0\3\44\1\52\1\53\5\44\1\120\1\44\2\0"+
    "\24\61\1\65\1\126\1\0\1\65\2\0\4\65\1\0"+
    "\7\65\2\0\24\66";

  private static int [] zzUnpacktrans() {
    int [] result = new int[1260];
    int offset = 0;
    offset = zzUnpacktrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpacktrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\13\0\10\1\1\11\2\1\2\11\4\1\1\11\15\1"+
    "\1\11\2\1\2\11\7\1\1\0\1\1\2\0\2\1"+
    "\1\0\5\1\1\11\1\0\2\11\1\0\1\1\2\0"+
    "\2\1\1\0\4\1\1\11\1\0\2\11\1\0\1\11"+
    "\1\0\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[89];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** Number of newlines encountered up to the start of the matched text. */
  @SuppressWarnings("unused")
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  @SuppressWarnings("unused")
  protected int yycolumn;

  /** Number of characters up to the start of the matched text. */
  @SuppressWarnings("unused")
  private long yychar;

  /** Whether the scanner is currently at the beginning of a line. */
  @SuppressWarnings("unused")
  private boolean zzAtBOL = true;

  /** Whether the user-EOF-code has already been executed. */
  @SuppressWarnings("unused")
  private boolean zzEOFDone;

  /* user code: */
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final Deque<Integer> optionStack = new ArrayDeque<>();

    public _CwtLexer() {
        this((java.io.Reader)null);
    }

    private void enterState(Deque<Integer> stack, int state) {
        stack.offerLast(state);
        yybegin(state);
    }

    private void exitState(Deque<Integer> stack, int defaultState) {
        Integer state = stack.pollLast();
        if(state != null) {
            yybegin(state);
        } else {
            yybegin(defaultState);
        }
    }

    private void processBlank() {
        boolean lineBreak = false;
        for (int i = 0; i < yylength(); i++) {
            char c = yycharat(i);
            if(c == '\r' || c == '\n') {
                lineBreak = true;
                break;
            }
        }
        if(lineBreak) {
            yybegin(YYINITIAL);
            optionStack.clear();
        } else {
            if(yystate() == EXPECT_NEXT) {
                yybegin(YYINITIAL);
                optionStack.clear();
            } else if(yystate() == EXPECT_NEXT_OPTION) {
                yybegin(IN_OPTION);
            }
        }
    }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public _CwtLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** Returns the maximum size of the scanner buffer, which limits the size of tokens. */
  private int zzMaxBufferLen() {
    return Integer.MAX_VALUE;
  }

  /**  Whether the scanner buffer can grow to accommodate a larger token. */
  private boolean zzCanGrow() {
    return true;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      {@code false}, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position {@code pos} from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException
  {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMap(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
            { yybegin(EXPECT_NEXT); return STRING_TOKEN;
            }
          // fall through
          case 29: break;
          case 2:
            { return WHITE_SPACE;
            }
          // fall through
          case 30: break;
          case 3:
            { return COMMENT;
            }
          // fall through
          case 31: break;
          case 4:
            { yybegin(EXPECT_NEXT); return INT_TOKEN;
            }
          // fall through
          case 32: break;
          case 5:
            { yypushback(yylength()); yybegin(IN_PROPERTY_KEY);
            }
          // fall through
          case 33: break;
          case 6:
            { enterState(stack, YYINITIAL); return LEFT_BRACE;
            }
          // fall through
          case 34: break;
          case 7:
            { exitState(stack, YYINITIAL); return RIGHT_BRACE;
            }
          // fall through
          case 35: break;
          case 8:
            { yybegin(IN_PROPERTY_SEPARATOR); return PROPERTY_KEY_TOKEN;
            }
          // fall through
          case 36: break;
          case 9:
            { processBlank(); return WHITE_SPACE;
            }
          // fall through
          case 37: break;
          case 10:
            { return BAD_CHARACTER;
            }
          // fall through
          case 38: break;
          case 11:
            { yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN;
            }
          // fall through
          case 39: break;
          case 12:
            { yybegin(YYINITIAL); return DOCUMENTATION_TOKEN;
            }
          // fall through
          case 40: break;
          case 13:
            { if(optionStack.isEmpty()){
              yypushback(yylength()); yybegin(IN_OPTION_VALUE_TOP_STRING);
         } else {
              yybegin(EXPECT_NEXT_OPTION); return STRING_TOKEN;
        }
            }
          // fall through
          case 41: break;
          case 14:
            { yybegin(EXPECT_NEXT_OPTION); return INT_TOKEN;
            }
          // fall through
          case 42: break;
          case 15:
            { yypushback(yylength()); yybegin(IN_OPTION_KEY);
            }
          // fall through
          case 43: break;
          case 16:
            { enterState(optionStack, IN_OPTION); return LEFT_BRACE;
            }
          // fall through
          case 44: break;
          case 17:
            { exitState(optionStack, IN_OPTION); return RIGHT_BRACE;
            }
          // fall through
          case 45: break;
          case 18:
            { yybegin(IN_OPTION_SEPARATOR); return OPTION_KEY_TOKEN;
            }
          // fall through
          case 46: break;
          case 19:
            { yybegin(IN_OPTION_VALUE); return EQUAL_SIGN;
            }
          // fall through
          case 47: break;
          case 20:
            { yybegin(EXPECT_NEXT_OPTION); return STRING_TOKEN;
            }
          // fall through
          case 48: break;
          case 21:
            { yybegin(IN_OPTION); return OPTION_START;
            }
          // fall through
          case 49: break;
          case 22:
            { yybegin(EXPECT_NEXT); return FLOAT_TOKEN;
            }
          // fall through
          case 50: break;
          case 23:
            { yybegin(EXPECT_NEXT); return BOOLEAN_TOKEN;
            }
          // fall through
          case 51: break;
          case 24:
            { yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN;
            }
          // fall through
          case 52: break;
          case 25:
            { yybegin(EXPECT_NEXT_OPTION); return FLOAT_TOKEN;
            }
          // fall through
          case 53: break;
          case 26:
            { yybegin(EXPECT_NEXT_OPTION); return BOOLEAN_TOKEN;
            }
          // fall through
          case 54: break;
          case 27:
            { yybegin(IN_OPTION_VALUE); return NOT_EQUAL_SIGN;
            }
          // fall through
          case 55: break;
          case 28:
            { yybegin(IN_DOCUMENTATION); return DOCUMENTATION_START;
            }
          // fall through
          case 56: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
