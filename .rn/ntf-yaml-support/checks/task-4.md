# Task 4 check: Excelを削除する

## Completion Criteria
| Criterion | Self-check | Evidence |
| --- | --- | --- |
| 変換対象だった全 Excel ファイルが削除されている | OK | `git rm` で2件削除（ProjectInsertMessageRequestTest.xlsx / form/ProjectInsertMessageFormTest.xls）。リポジトリ全体（target/.git 除く）を `-iname '*.xl*'` で検索し0件（残存Excelなし） |
| 削除がコミットされている | OK | commit 97df53a（2 files deleted）。Excel なしでの `mvn clean test` 実行と結果確認は Task #5 で行う |

## QA Expert Review
### Meaningful tests/verification
- Verdict: OK
- 97df53a は変換対象2件の削除（D）のみで他ファイル変更なし。HEAD ツリー・ワーキングツリー（target 除く）とも Excel 残存0件。移行前に存在した Excel はこの2件のみで変換対象と一致。ece341e で追加した YAML 5件は HEAD に全件残存、`.java` の変更なし。
### Edge case coverage
- Verdict: OK
- 大文字小文字を区別せず `.xl*`（.XLS / .xlsm / .xlsb / .xltx 等）で全ディレクトリ（src/test/resources 含む）を検索し0件。YAML 側の欠落なし（xlsx→3件、xls→2件で ece341e の変換記録と一致）。

## Expert Reviews (code changes only)
N/A — no code changes in this task.（Excel ファイルの削除のみ。プロダクション/テストコードの変更なし）

## Overall Verdict
- Self-check: OK
- QA: OK
