# Task 2 check: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加

## Completion Criteria
| Criterion | Self-check | Evidence |
| --- | --- | --- |
| `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープで追加 | OK | pom.xml dependencies に groupId=com.nablarch.framework / artifactId=nablarch-testing-yaml / version=1.0.0-SNAPSHOT / scope=test を追加 |
| `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグインとして追加（plugin直下 configuration に変換パラメータ、CLIで convert） | OK | build/plugins に converter プラグイン追加。configuration: from=xls / to=yaml / input=output=${project.basedir}/src/test/java / overwrite=true。executions 未定義（CLI専用、通常 test では誤起動しない） |
| `yamlInterpreters` と `testDataParser`（YamlTestDataParser）が全参照フェーズで有効な設定ファイルに追加。list は compositeInterpreter と dateTimeInterpreter の2つのみ | OK | unit-test.xml に `<list name="yamlInterpreters">`（component-ref: compositeInterpreter / dateTimeInterpreter の2つのみ）と `<component name="testDataParser" class="...YamlTestDataParser">`（dbInfo・interpreters を注入）を追加。定義順は test-data.xml import・dbInfo 定義より後。受信側（sendSync 不使用）=非トリガーのため unit-test.xml 単一配置で全参照フェーズをカバー。本番 boot は無改変 |
| `mvn dependency:resolve` 成功、converter `convert` ゴールが `mvn help:describe` で解決 | OK | dependency:resolve → nablarch-testing-yaml:1.0.0-SNAPSHOT:test 解決、BUILD SUCCESS（exit 0）。help:describe -Dgoal=convert → Mojo 'nablarch-testing-converter:convert' 解決、from/to/input/output/overwrite 提供確認（exit 0） |

## QA Expert Review
### Meaningful tests/verification
- Verdict: OK
- 依存解決＋convert ゴール記述の検証は Task #2 の範囲として妥当でいずれも成功。component-ref（compositeInterpreter/dateTimeInterpreter）が framework の test-data-interpreter.xml に実在し unit-test.xml の import 経由で解決可能なことを実体確認。testDataParser 上書きの定義順（dbInfo→test-data.xml import→yamlInterpreters→testDataParser）が framework 上書きの標準パターンで妥当。
### Edge case coverage
- Verdict: OK
- interpreter 2点絞り込みの設計根拠を実データで裏付け: null はネイティブ（expectedStatusCode: null）→ nullInterpreter 不要、文字列はYAMLダブルクォートで loader が除去 → quotationTrimmer 不要、`${systemTime}` 実在 → dateTimeInterpreter 必須で正しく残置、複数行 block scalar 無し → lineSeparatorInterpreter 削除も妥当。本段階は Excel 未変換のため mvn test 失敗が想定内。

## Expert Reviews (code changes only)
### Language Expert
- Best practices: OK — scope=test の妥当性、build/plugins への converter 配置（executions 無しでCLI専用）、component-ref 参照・property 注入は Nablarch DI 正規記法。
- Codebase style consistency: OK — インデント（pom 6/8/10、XML 2/4）・CRLF・日本語コメントスタイルが既存および隣接 batch/web と一致。
- GWT test format: N/A — テストクラス（.java）の変更なし（git show 43ff0c8 --stat で確認）。
### Software-engineering Expert
- Separation of concerns: OK — converter をビルドプラグイン化しテストランタイム classpath への混入を回避。テスト専用設定は unit-test.xml に限定、本番 boot 無改変。
- System integrity: OK — converter は executions 無しで誤起動なし。YamlTestDataParser は dbInfo/interpreters 注入で override 充足、setTestDataReader は no-op で reader 未設定でも問題なし。yamlInterpreters は既存 interpreters と別名で衝突なし。Excel 未変換による mvn test 失敗は設計意図どおりの中間状態。
- Maintainability: OK — 2インタープリタ構成の選定根拠が XML コメントに明記され追跡可能。input/output パスは実 XLS 配置と一致。（SNAPSHOT版の2箇所重複は非ブロッカーの将来改善余地）

## Overall Verdict
- OK（QA / Language / Software-engineering すべて OK。指示書 v1.5 の3要件＝interpreter 2つのみ・converter はビルドプラグイン・単一配置で全参照フェーズ有効 を充足）
