# task-3 Completion Check

## Completion Criteria

| Criterion | Self-check | Evidence | QA | QA Evidence |
|---|---|---|---|---|
| 変換対象の全 Excel に対応する YAML が生成されている | OK | 対象 Excel 2件（`ProjectInsertMessageRequestTest.xlsx`、`form/ProjectInsertMessageFormTest.xls`）。`mvn com.nablarch.framework:nablarch-testing-converter:1.0.0-SNAPSHOT:convert`（pom の `<configuration>` 使用）で `Converted 2 file(s).` / BUILD SUCCESS。シート数どおり YAML 5件（RequestTest: setUpDb / testNormalEnd / testValidationError、FormTest: testCharsetAndLength / testSingleValidation）を生成 | | |
| サンプリング比較（各ファイル最低3件、元データが3件未満は全件）で Excel と YAML の内容が一致している | OK | `scripts/ExcelDump.java` で Excel をダンプし YAML と突き合わせ。5ファイルとも差異なし（「Sampling Comparison」参照）。行数も一致（testShots 1 / 17、charsetAndLength 6、singleValidation 130、setUpDb は Excel 側が空シートで YAML も空） | | |
| 変換後の YAML ファイルがコミットされている | OK | `236daa6` で YAML 5件をコミット（Excel は未削除） | | |

## QA Expert Review

| Aspect | Verdict | Evidence / Improvement |
|---|---|---|
| Meaningful tests/verification | | |
| Edge case coverage | | |

## Expert Reviews (code changes only)

N/A — no code changes in this task.

## Sampling Comparison（task-3 のみ）

| YAML ファイル | 比較件数 | レコードキー/行 | Excel 値 | YAML 値 | 一致 |
| --- | --- | --- | --- | --- | --- |
| `ProjectInsertMessageRequestTest/setUpDb.yaml` | 0（元データ0件のため全件） | シート全体 | 空シート（r0 のみ・データなし） | 空ファイル（0 byte） | OK |
| `ProjectInsertMessageRequestTest/testNormalEnd.yaml` | 3 | testShots no=1 | `1 / データレコードの値が正しくデータベースに登録されること。 / null / 1 / 1 / messaging-sync-receive-boot.xml / RECEIVEAPP / batch_user` | 同値（`expectedStatusCode: null`） | OK |
| 〃 | | setUpMessages 1行目 | `プロジェクト００１ / development / s / 20140203 / 20180304 / 1 / 鈴木 / 佐藤 / 100 / 備考欄 / 10000 / 1000 / 2000 / 3000 / (空)`、fw_header requestId=ProjectInsertMessage・userId=hoge、MS932 / CRLF | 同値 | OK |
| 〃 | | EXPECTED_TABLE[1]=PROJECT_TEMP 1行目 | `… STATUS=0 / INSERT_DATE=${systemTime} / UPDATE_DATE=${systemTime}`（PROJECT_START_DATE=2014-02-03 ほか全17列） | 同値 | OK |
| `ProjectInsertMessageRequestTest/testValidationError.yaml` | 3 | setUpMessages no=1 | プロジェクト名が空、他列は正常値 | `["", "development", "s", "20140203", …, "3000", ""]` | OK |
| 〃 | | setUpMessages no=8 | プロジェクト開始日付 `20149999` | `"20149999"` | OK |
| 〃 | | setUpMessages no=17 / expectedMessages / テーブル | 本社配賦 `d`、応答 `error.validation` ×17、SETUP_TABLE[com] / EXPECTED_TABLE[com]=PROJECT_TEMP（データ行なし） | `"d"`、`["error.validation", ""]` ×17、group_id `com` で `rows: []` | OK |
| `form/ProjectInsertMessageFormTest/testCharsetAndLength.yaml` | 3 | propertyName=projectName | `x / (空) / 128 / {…Length.max.message} / max / 128 / {…SystemChar.message} / x x x x o o o o o o o x x x` | 同値 | OK |
| 〃 | | propertyName=projectLeader | `o / (空) / 128 / … / x x x x o o o o o o o x x x` | 同値 | OK |
| 〃 | | propertyName=note | `o / (空) / 128 / … / o o o o o o o o o o o x x x` | 同値 | OK |
| `form/ProjectInsertMessageFormTest/testSingleValidation.yaml` | 3 | 1行目 clientId（emptyNG） | input1・messageId 等すべて空（ヘッダ外のコメント列 `// emptyNG` は対象外） | すべて `""` | OK |
| 〃 | | projectStartDate `20230817` 行 | `projectStartDate / 20230817 / (空)…` | 同値 | OK |
| 〃 | | 最終行 allocationOfCorpExpenses | `${改行, 1} / {…Digits.integer.message} / min / 0 / max / 999999999` | 同値 | OK |

- 補足: 本再実施で生成した YAML 5件は、過去の移行コミット（`461e4d4` 時点）の YAML とバイト単位で同一（`cmp`）。

## .xls/.xlsx 併存チェック（該当時のみ）
- 併存の有無: なし（`ProjectInsertMessageRequestTest.xlsx` と `form/ProjectInsertMessageFormTest.xls` は名前・ディレクトリとも別。拡張子を除いたパスの重複 0件）
- 差分確認結果の保存先: 該当なし
- 未使用側（通常 `.xlsx`）の削除: 該当なし

## Overall Verdict

- Self-check: OK
- QA: 
- Language expert: N/A
- Software-engineering expert: N/A
- Ready for user review: 
