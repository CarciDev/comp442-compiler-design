# Post-Submission Output Format Fixes

## Summary

After submitting Assignment 1, I discovered that my lexer output format did not match the expected format from the grading files. This document explains the formatting differences and the fixes applied.

---

## Issue: Output Format Mismatch

My lexer correctly recognized all tokens, but the output format differed from the expected format.

### My Original Output
```
[EQ, ==, 1]
[PLUS, +, 1]
[LPAREN, (, 1]
[INTEGER, 0, 13]
[FLOAT, 1.23, 20]
[EOF, $, 40]
```

### Expected Output
```
[eq, ==, 1] [plus, +, 1] [openpar, (, 1] [semi, ;, 1]
[intnum, 0, 13]
[floatnum, 1.23, 20]
[blockcmt, /* comment */, 31]
```

---

## Format Differences

| Aspect | My Format | Expected Format |
|--------|-----------|-----------------|
| Token names | UPPERCASE | lowercase |
| Parentheses | `LPAREN`, `RPAREN` | `openpar`, `closepar` |
| Braces | `LBRACE`, `RBRACE` | `opencubr`, `closecubr` |
| Brackets | `LBRACKET`, `RBRACKET` | `opensqbr`, `closesqbr` |
| Not-equal | `NEQ` | `noteq` |
| Semicolon | `SEMICOLON` | `semi` |
| Integer | `INTEGER` | `intnum` |
| Float | `FLOAT` | `floatnum` |
| Grouping | One token per line | Grouped by source line |
| Comments | Not included | Included |
| EOF | Included | Not included |
| Error format | `at line N` | `: line N.` |

---

## Fixes Applied

### 1. Token Name Mapping
Added `getTokenTypeName()` method to map internal enum names to expected lowercase format.

### 2. Output Grouping
Modified driver to group tokens by source line instead of one per line.

### 3. Comment Handling
Changed to include `blockcmt` and `inlinecmt` tokens in output.

### 4. EOF Handling
Removed EOF token from `.outlextokens` output.

### 5. Error Format
Changed from `"lexeme" at line N` to `"lexeme": line N.`

---

## Result

After fixes, output matches the expected grading format. The core lexical analysis logic was unchanged - only the output presentation was modified.
