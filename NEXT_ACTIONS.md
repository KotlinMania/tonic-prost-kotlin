# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/2 (100.0%)
- **Function parity:** 10/14 matched (target 33) — 71.4%
- **Class/type parity:** 5/13 matched (target 13) — 38.5%
- **Combined symbol parity:** 15/27 matched (target 46) — 55.6%
- **Average inline-code cosine:** 0.69 (function body across 2 matched files)
- **Average documentation cosine:** 0.57 (doc text across 2 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 1 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. codec

- **Target:** `tonicprost.Codec`
- **Similarity:** 0.38
- **Dependents:** 0
- **Priority Score:** 122706.2
- **Functions:** 10/14 matched (target 33)
- **Missing functions:** `decode_max_message_size_exceeded`, `encode_max_message_size_exceeded`, `encode_too_big`, `poll_frame`
- **Types:** 5/13 matched (target 12)
- **Missing types:** `Encode`, `Decode`, `Item`, `Error`, `MockEncoder`, `MockDecoder`, `MockBody`, `Data`
- **Tests:** 0/4 matched

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

