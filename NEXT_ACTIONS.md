# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/2 (100.0%)
- **Function parity:** 13/14 matched (target 62) — 92.9%
- **Class/type parity:** 13/13 matched (target 28) — 100.0%
- **Combined symbol parity:** 26/27 matched (target 90) — 96.3%
- **Average inline-code cosine:** 0.84 (function body across 2 matched files)
- **Average documentation cosine:** 0.75 (doc text across 2 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 0 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. codec

- **Target:** `tonicprost.Codec [PROVENANCE-FALLBACK]`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 12703.3
- **Functions:** 13/14 matched (target 62)
- **Missing functions:** `poll_frame`
- **Types:** 13/13 matched (target 27)
- **Missing types:** _none_
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `codec.rs` vs expected `codec.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:codec.rs` vs expected `codec.rs`
- **Proposed provenance header:** `// port-lint: source codec.rs` (current: `// port-lint: source codec.rs`)
- **Proposed provenance header:** `// port-lint: tests codec.rs` (current: `// port-lint: tests codec.rs`)
- **Lint issues:** 2

### 2. lib

- **Target:** `tonicprost.Lib [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source lib.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

