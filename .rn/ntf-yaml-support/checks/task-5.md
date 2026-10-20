# Task 5 check: YAMLテストデータで全テストをパスさせる

## Completion Criteria
| Criterion | Self-check | Evidence |
| --- | --- | --- |
| `mvn test` が BUILD SUCCESS で終了する（全テストパス、Excel ファイルなし） | NG | Excel 残存0件の状態で `LANG=ja_JP.UTF-8 mvn clean generate-resources`（BUILD SUCCESS）→ `mvn test` が **BUILD FAILURE**。`Tests run: 4, Failures: 1, Errors: 0, Skipped: 0`。ProjectInsertMessageFormTest 2件はパス、`ProjectInsertMessageRequestTest.testNormalEnd` が失敗（PROJECT_TEMP.INSERT_DATE `expected:<[${systemTime}]> but was:<[2014-09-13 12:34:56.0]>`） |
| テストログで YamlTestDataParser が実際に使われていることが確認できる | OK | ログに `override ... component name = testDataParser, defined ... BasicTestDataParser, override ... YamlTestDataParser` と `YamlTestDataParser does not use TestDataReader; the injected value is ignored.` を確認 |

詳細（事象・直接原因・根本原因・対応案）は `reports/task-5-test-failure-report.md` を参照。
対応案A（`yamlInterpreters` を `dateTimeInterpreter` → `compositeInterpreter` の順に変更）は、スクラッチにコピーしたツリーで BUILD SUCCESS（4件全パス）を事前検証済み。ブランチには未適用。

## QA Expert Review
### Meaningful tests/verification
- Verdict: 未実施
- テストが BUILD FAILURE となったため、運用ルールに従い作業を停止しユーザー判断待ち（QA レビューは対応方針決定・再テスト後に実施する）
### Edge case coverage
- Verdict: 未実施
- 同上

## Expert Reviews (code changes only)
N/A — no code changes in this task.（テスト実行と確認のみ。.java・設定の変更なし。対応案適用時は task-2 成果物（unit-test.xml）の修正となるため、その際に Language / Software-engineering レビューを実施する）

## Overall Verdict
- Self-check: NG（`mvn test` BUILD FAILURE。testNormalEnd の `${systemTime}` が解釈されず失敗）
- QA: 未実施（停止中）
- Language expert / Software-engineering expert: N/A（本タスク時点でコード変更なし）
- 状態: ユーザーへ報告し、対応方針の判断待ち
