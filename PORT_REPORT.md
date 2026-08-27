=== Deep Analysis: tmp/tonic-prost/src (rust) -> src/commonMain/kotlin/io/github/kotlinmania/tonicprost (kotlin) ===

Scanning source codebase (rust)...
Codebase: tmp/tonic-prost/src (rust)
  Files: 2
  Total imports: 16

Scanning target codebase (kotlin)...
Codebase: src/commonMain/kotlin/io/github/kotlinmania/tonicprost (kotlin)
  Files: 3
  Total imports: 4

Comparing codebases...
Computing AST similarities...

=== Codebase Comparison Report ===

Source: tmp/tonic-prost/src (2 files)
Target: src/commonMain/kotlin/io/github/kotlinmania/tonicprost (3 files)
Scoring invariant: FnSim is required function body/parameter similarity. Class/type and symbol parity are reported beside it; whole-file shape is diagnostic only.

Matched:   2 files
Unmatched: 0 source, 0 target

=== Matched Files (by porting priority) ===

Source                        Target                        FnSim     Dependents FunctionParityTypeParity  Priority
 --------------------------------------------------------------------------------------------------------------------------
codec                         tonicprost.Codec              0.65      0          13/14         13/13       12703.5   
lib                           tonicprost.Lib                1.00      0          0/0           0/0         0.0       

=== Function and Symbol Details ===

codec -> tonicprost.Codec
  similarity: 0.65, priority: 12703.5, dependents: 0
  functions: 13/14 matched (target total: 57, required body score: 0.65)
  missing functions: poll_frame
  types: 13/13 matched (target total: 26)
  missing types: none
  tests: 3/4 matched

lib -> tonicprost.Lib
  similarity: 1.00, priority: 0.0, dependents: 0
  functions: 0/0 matched (target total: 0, required body score: 1.00)
  missing functions: none
  types: 0/0 matched (target total: 1)
  missing types: none


=== Porting Quality Summary ===

Matched by exact header:          2 / 2
Matched by provenance fallback:   0 / 2
Matched by name:                  0 / 2
Total TODOs in target: 0
Total lint errors:    0
Stub files:           0

=== Big Picture ===

- Missing files: 0
- Incomplete ports (similarity < 60%): 0
- Stub files: 0
- Files missing functions: 1 (total deficit: 1 functions)
- Type definitions missing: 0
- Files missing tests: 1 (total deficit: 1 unported `#[test]` functions)
- Documentation coverage: 92 / 46 lines (200%)

Primary focus: port missing functions/tests to reach per-file parity (1 functions, 1 tests)

=== Files with Issues ===

File                          Similarity LineRatio  FunctionParityTests     TODOs Lint  Status
----------------------------------------------------------------------------------------------------
tonicprost.Codec              0.65       0.00       13/14         3/4       0     0     MISSING_FUNCS
  missing functions: `poll_frame`

=== Porting Recommendations ===

Incomplete ports (similarity < 60%): 0
Missing files: 0


=== Documentation Gaps ===

Documentation coverage: 92 / 46 lines (200%)
Files with >20% doc gap: 1

File                          Src Docs    Tgt Docs    Gap %     DocSim    DocAmt    DocEq     
----------------------------------------------------------------------------------------------
lib                           24          19          20%       0.81      0.79      0.80      

=== Generating Reports ===

✅ Generated: port_status_report.md
✅ Generated: high_priority_ports.md
✅ Generated: NEXT_ACTIONS.md
✅ Generated: port_lint_proposed_changes.md

📁 All reports generated successfully!
