# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/2 (100.0%)
- **Function parity:** 13/14 matched (target 66) — 92.9%
- **Class/type parity:** 13/13 matched (target 29) — 100.0%
- **Combined symbol parity:** 26/27 matched (target 95) — 96.3%
- **Average inline-code cosine:** 0.50 (function body across 2 matched files)
- **Average documentation cosine:** 0.74 (doc text across 2 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 1 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. codec

- **Target:** `tonicprost.Codec [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 12710.0
- **Functions:** 13/14 matched (target 66)
- **Missing functions:** `poll_frame`
- **Types:** 13/13 matched (target 28)
- **Missing types:** _none_
- **Tests:** 3/4 matched

### 2. lib

- **Target:** `tonicprost.Lib`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

