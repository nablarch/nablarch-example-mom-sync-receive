# Task 3 check: ExcelテストデータをYAMLに変換

## Completion Criteria
| Criterion | Self-check | Evidence |
| --- | --- | --- |
| 変換対象の全 Excel に対応する YAML が生成されている | OK | converter `convert` ゴールで Converted 2 file(s)。生成 YAML 5件: RequestTest（setUpDb/testNormalEnd/testValidationError.yaml）、FormTest（testCharsetAndLength/testSingleValidation.yaml） |
| サンプリング比較で Excel と YAML が一致（各最低3件、3件未満は全件） | OK | 下記 Sampling Comparison 参照。全5 YAML で差異なし |
| 変換後の YAML がコミットされている | OK | commit ece341e（5 files, 1351 insertions）。Excel は未削除（Task #4 で削除） |

## QA Expert Review
### Meaningful tests/verification
- Verdict: OK
- 変換元 Excel 2件を ExcelDump で全シート・全行ダンプし、生成 YAML 5件と**全件照合**（サンプリングではなく全データ）。list_maps（testShots normal=1/validation=17、charsetAndLength=6、singleValidation=130）が Excel 行数と完全一致、欠落・重複なし。messages は fields(name/type/length)・rows 全値一致、no列はメタとして除外、directives(MS932/CRLF)保存、requestId/userId は fw_header へ分離。setup_tables/expected_tables の group_id・列・行一致。文字列"null"→ネイティブ null、NTFコメント（行/セル内/末尾注釈）除外、空セル→""、日付・${systemTime}・${半角数字,9} 保存を確認。
- 補足: messages.directives に `file-type: "Fixed"` が付与されるのは誤記でなく、NTF が MESSAGE 本体を FixedLengthFile として読む本来の解釈を converter が反映したもの（往復で挙動保存）。
### Edge case coverage
- Verdict: OK
- サンプリング最低3件（元3件未満は全件）を全ファイルで充足（charsetAndLength 3/6、singleValidation 3/130、testNormalEnd 3、testValidationError 3/17、setUpDb 全件0件）。setUpDb.yaml が空なのは元シートが空のため忠実。.xls/.xlsx 併存なしの判定も正しい。

## Expert Reviews (code changes only)
N/A — no code changes in this task.（Excel→YAML 変換データの追加のみ。プロダクション/テストコードの変更なし）

## Overall Verdict
- OK（変換5件は変換元 Excel を忠実・漏れなく表現。全件照合で差異なし。QA 総合 Verdict も OK。directives/fw_header/file-type の変換は QA が別途照合し正当性確認済み）

## Sampling Comparison（task-3 のみ）

Excel は NTF と同じ Apache POI（`scripts/ExcelDump.java`）でダンプし、各 YAML と突き合わせた。
NTF コメント列（`// ...`）・コメント行は converter が除外（データではないため）。空セルは `""`、
Excel の文字列 `null` は YAML ネイティブ `null`（YamlTestDataParser がそのまま null として解釈）。

| YAML ファイル | 比較件数 | レコードキー/行 | Excel 値 | YAML 値 | 一致 |
| --- | --- | --- | --- | --- | --- |
| form/.../testCharsetAndLength.yaml | 3/6 | charsetAndLength[propertyName=projectName] | allowEmpty=x,max=128,全角英字=o,中国語=x | 同左 | OK |
|  |  | [propertyName=projectType] | allowEmpty=x,半角英字=o,全角英字=x | 同左 | OK |
|  |  | [propertyName=note] | allowEmpty=o,半角記号=o,全角漢字=o | 同左 | OK |
| form/.../testSingleValidation.yaml | 3 | singleValidation[1] clientId（空） | input1="",messageId="" | 同左 | OK |
|  |  | [2] clientId | input1=${半角数字,9},messageId="" | 同左 | OK |
|  |  | [3] clientId | input1=1${半角数字,9},messageId={...Digits.integer.message},integer/9 | 同左 | OK |
| RequestTest/setUpDb.yaml | 全件（0件） | setUpDb シート | 空（r0 empty） | 空ファイル | OK |
| RequestTest/testNormalEnd.yaml | 3 | testShots[no=1] | expectedStatusCode=null,setUpTable=1,diConfig=messaging-sync-receive-boot.xml | 同左 | OK |
|  |  | setUpMessages 受信電文 row | プロジェクト００１/development/s/20140203/.../3000/"" | 同左 | OK |
|  |  | expected_tables PROJECT_TEMP | PROJECT_START_DATE=2014-02-03,STATUS=0,INSERT_DATE=${systemTime} | 同左 | OK |
| RequestTest/testValidationError.yaml | 3/17 | testShots[no=1] | 必須プロジェクト名空,setUpTable=com,expectedStatusCode=null | 同左 | OK |
|  |  | setUpMessages row[1]（プロジェクト名空） | ["","development","s",...,"3000",""] | 同左 | OK |
|  |  | expectedMessages row[1] | ["error.validation",""] | 同左 | OK |

（差異なし。差異があれば原因を調査し、修正せずユーザーへ報告して停止する）

## .xls/.xlsx 併存チェック
- 併存の有無: **なし**（`ProjectInsertMessageRequestTest.xlsx` と `form/ProjectInsertMessageFormTest.xls`
  はファイル名・配置ディレクトリとも別。同名 basename の重複なし）
- 差分確認結果の保存先: 該当なし（併存なしのため個別変換比較は不要）
- 未使用側（通常 `.xlsx`）の削除: 該当なし
