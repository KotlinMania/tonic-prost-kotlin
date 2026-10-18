# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 1/2 (50.0%)
- **Function parity:** 13/14 matched (target 35) — 92.9%
- **Class/type parity:** 12/13 matched (target 16) — 92.3%
- **Combined symbol parity:** 25/27 matched (target 51) — 92.6%
- **Average inline-code cosine:** 0.65 (function body across 1 matched files)
- **Average documentation cosine:** 0.79 (doc text across 1 matched files)
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
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 22703.5
- **Functions:** 13/14 matched (target 35)
- **Missing functions:** `poll_frame`
- **Types:** 12/13 matched (target 16)
- **Missing types:** `Data`
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `codec.rs` vs expected `codec.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:codec.rs` vs expected `codec.rs`
- **Proposed provenance header:** `// port-lint: source codec.rs` (current: `// port-lint: source codec.rs`)
- **Proposed provenance header:** `// port-lint: tests codec.rs` (current: `// port-lint: tests codec.rs`)
- **Lint issues:** 2

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `src/lib.rs` | `Lib.kt` |

