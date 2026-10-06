# Goal

NTF（Nablarch Testing Framework）のAI対応として、AIが読み書きできないバイナリ形式のExcelテストデータをYAML形式に移行する。
参照: https://github.com/Fintan-contents/nablarch-system-development-guide/pull/211

対象は `nablarch-example-mom-sync-receive` リポジトリに存在するNTFのExcelテストデータ全件（現時点で2件）：
- `src/test/java/com/nablarch/example/ProjectInsertMessageRequestTest.xlsx`（メッセージングリクエスト単体テスト）
- `src/test/java/com/nablarch/example/form/ProjectInsertMessageFormTest.xls`（エンティティ精査テスト）

# Acceptance criteria

- `develop` ブランチから作成した `ntf-yaml-support` ブランチで全作業が行われている
- 作業前に既存の全テストがパスすることを確認済みである
- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープの依存として追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグインとして追加されている（test依存ではない）
- `testDataParser` が参照される全フェーズで YAML が有効になるよう、`YamlTestDataParser` と `yamlInterpreters` が適切な設定ファイルに追加されている
- 変換対象の全てのExcelファイルが対応するYAMLに変換され、サンプリングで内容一致を確認済みである
- 変換後、変換対象だったExcelファイルが全て削除されている
- YAMLテストデータを使った状態で全テストがパスしている

# Assumptions

- `nablarch-testing-yaml:1.0.0-SNAPSHOT` と `nablarch-testing-converter:1.0.0-SNAPSHOT` はローカルMavenリポジトリ（`~/.m2`）に存在する（確認済み）
- テスト実行にはH2インメモリDBを使用し、DBセットアップ済みである
- JDK / Maven / H2 などビルド・テスト実行環境は整備済みである（Java 21 / Maven 3.9.16）
- YAMLファイルの出力先は `src/test/java` 配下（Excelと同じ場所）とし、`nablarch.test.resource-root` の設定変更は不要
- 変換ツール（`nablarch-testing-converter`）の実行手順は `/home/k10949/workspace/nablarch-testing-converter/README.md` に従う

# Build / Test commands（README準拠）

README（`実行手順`）に基づき、ビルド・テストは以下の手順で実行する。

- エンティティ生成（DBセットアップ含む）: `mvn generate-resources`
  - gspプラグイン（`gsp-dba-maven-plugin`）が `generate-sources` フェーズで `generate-ddl` / `execute-ddl` / `generate-entity` / `load-data` を実行する。
  - Java 17+ 実行のためのJVMオプションは `.mvn/jvm.config`（`--add-opens java.base/java.lang=ALL-UNNAMED`）で指定済み。
- テスト実行: `mvn test`
- 文字化け・メッセージ解決失敗を避けるため、テストは `LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8` 環境で実行する（`C.UTF-8` だとメッセージ解決に失敗しテストが落ちる）。
- 変換ツールはビルドプラグインとしてCLIで実行する（Task #3）:
  `mvn com.nablarch.framework:nablarch-testing-converter:1.0.0-SNAPSHOT:convert`（パラメータは `-Dnablarch-testing-converter.*` またはplugin `<configuration>`）。

# Rules

- commit every change; one completion marker per task
- push は行わない（コミットのみ実施し、リモートへの push は最後にユーザーが行う）
- テストクラス（`.java`）は変更しない
- `develop` から新規作成した `ntf-yaml-support` ブランチで作業する
- steering.md は各タスクで更新し、当該タスクのコミットに含める
- テストはExcelファイルが存在しない状態で最終パスを確認する
- YAML変換後はサンプリングでExcelとYAMLの内容を比較してから削除する
- サンプリング比較で差異が見つかった場合は修正せずユーザーに報告して停止する

# YAML設定の配置方針（判定）

Rules「`testDataParser` が参照される全フェーズで YAML リーダーが有効になるよう配置」に従い、本プロジェクトの構成を判定した。

- 構成: `src/test/resources/unit-test.xml` が `messaging-sync-receive-boot.xml`（diConfig）を import している。
- テスト種別:
  - `ProjectInsertMessageFormTest` … `@EntityTest`（エンティティ精査）。通常のNTFテスト。
  - `ProjectInsertMessageRequestTest` … `@MessagingRequestTest`（`MessagingRequestTestSupport` = `StandaloneTestSupportTemplate` ベースのメッセージングリクエスト単体テスト）。
- 判定結果: **非トリガー → `unit-test.xml` への配置で十分**。
  - 本プロジェクトのアクション `ProjectInsertMessageAction` は **`sendSync` を呼ばない受信側アクション**（`src/main/java` に `sendSync`/`MessageSender` の使用なし）。
  - `MessagingRequestTestSupport.beforeExecute` が `MQSupport.prepareRequestMessage` / `prepareForAssertion` を呼び、`MQSupport` は `TestSupport.getTestDataParser()`（= 現リポジトリ `unit-test.xml` の `testDataParser`）でリクエスト電文・期待応答電文を**アクション実行（リポジトリ再初期化）の前**に読み込む。
  - 実行中にモックが `testDataParser`/`messagingTestDataParser` 経由でテストデータを読む経路（sendSyncモック `RequestTestingMessagingClient`/`SendSyncSupport`）は、本アクションが sendSync を使わないため**非該当**。
  - よって全テストデータ読み込みは `unit-test.xml` がアクティブなフェーズで完結する。batch の request test（同じ `StandaloneTestSupportTemplate` ベース）が `unit-test.xml` 配置のみで全パスした前例とも一致。
- 配置: `unit-test.xml` に `yamlInterpreters` リスト（`quotationTrimmer` を除く）と `YamlTestDataParser`（`testDataParser` 上書き）を追加する。定義順は `test-data.xml` import より後・`dbInfo` 定義より後に置き参照解決を担保する。
- 本番用 boot（`src/main/resources` 配下）には入れない。

# Tasks

### #1: 現状のビルド・テストで全PASSを確認する

**Purpose**: `develop` から `ntf-yaml-support` ブランチを作成し、変更前の状態で全テストがパスすることを確認する。

**Prerequisites**: none

**Steps**:

- [x] `develop` ブランチの存在を確認し、そこから `ntf-yaml-support` ブランチを新規作成する
- [x] 対象プロジェクトの README を確認し、ビルド・テストの実行手順を把握する
- [x] 隣接プロジェクトの `.rn/` 配下MDを参照し、steering.md と scripts の初版を作成する（README で確認したビルド・テスト手順を明記）
- [x] README で確認した手順に従いエンティティクラスを生成する（`mvn generate-resources`）
- [x] README で確認した手順でテストを実行し、全テストがパスすることを確認する（`mvn test`）
- [x] 結果を `checks/task-1.md` に記録する（self-check列のみ）
- [x] 変更ファイル（check ファイルおよび steering.md）をコミットする
- [x] self-check (OK/NG per completion criterion, record in checks/task-1.md)
- [ ] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- `mvn test` が全テストパスで終了している（BUILD SUCCESS）

### #2: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加

**Purpose**: `pom.xml` に `nablarch-testing-yaml`（test 依存）を追加し、`nablarch-testing-converter` を `<build><plugins>` のビルドプラグインとして追加し、`unit-test.xml` に `yamlInterpreters` リストと `YamlTestDataParser` コンポーネントを追加する。

**Prerequisites**: #1

**Steps**:

- [x] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）を追加する
- [x] `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` をビルドプラグインとして追加する（`<configuration>` に from=xls / to=yaml / input / output / overwrite を設定）
- [x] 本テストの構成を判定する（応答電文 `response_*` / sendSync の有無で判断）→ 非トリガーと判定済み（steering 参照）
- [x] `unit-test.xml` に `yamlInterpreters` リストと `YamlTestDataParser`（`testDataParser` 上書き）を配置する（`test-data.xml` import・`dbInfo` 定義より後）
- [x] `mvn dependency:resolve` などで pom とyaml依存が解決できること、`mvn help:describe` で converter プラグインの `convert` ゴールが解決できることを確認する
- [x] 変更したファイルをコミットする
- [x] self-check (OK/NG per completion criterion, record in checks/task-2.md)
- [x] QA expert review (Agent: general-purpose / Opus 4.8)
- [x] language expert review (Agent: general-purpose / Opus 4.8)
- [x] software-engineering expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープで追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグインとして追加されている
- `yamlInterpreters` リストと `testDataParser` コンポーネント（`YamlTestDataParser`）が、`testDataParser` が参照される全フェーズで有効になる設定ファイルに追加されている
- `mvn dependency:resolve` が成功し、converter プラグインの `convert` ゴールが `mvn help:describe` で解決できる

### #3: ExcelテストデータをYAMLに変換

**Purpose**: `nablarch-testing-converter` を使って ExcelのテストデータをYAML形式に変換し、各テストのソースツリー内に配置して、変換内容をサンプリングで確認する。

**Prerequisites**: #2

**Steps**:

- [x] 対象 Excel ファイルの所在を確認する
- [x] 同名の `.xls` と `.xlsx` が併存していないか確認する（本プロジェクトは別名のため非該当）
- [x] converter の README 手順に従い、全ての対象ExcelファイルをYAML変換する
- [x] 変換後、各YAMLファイルについてExcelの内容とサンプリングで比較し、一致を確認する（各ファイル最低3件。元データが3件未満の場合は元データ件数分）
- [x] サンプリング比較結果を `checks/task-3.md` に記録する（差異があれば原因を調査しユーザーに報告し、修正せずに止める）
- [x] 変換したYAMLファイルをコミットする（Excelはまだ削除しない）
- [x] self-check (OK/NG per completion criterion, record in checks/task-3.md)
- [x] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- 変換対象の全てのExcelファイルに対応するYAMLファイルが生成されている
- サンプリング比較でExcelとYAMLの内容が一致している
- 変換後のYAMLファイルがコミットされている

### #4: Excelを削除する

**Purpose**: 変換対象だった Excel を削除してコミットする。

**Prerequisites**: #3

**Steps**:

- [ ] 変換対象となった Excel ファイルを削除する
- [ ] 削除結果（残存Excelが0件であること）を `checks/task-4.md` に記録する
- [ ] Excelファイル削除をコミットする
- [ ] self-check (OK/NG per completion criterion, record in checks/task-4.md)
- [ ] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- 変換対象だった全 Excel ファイルが削除されている
- 削除がコミットされている

### #5: YAMLテストデータで全テストをパスさせる

**Purpose**: Excelなしで全テストがパスすることを確認する。

**Prerequisites**: #4

**Steps**:

- [ ] `mvn test` が BUILD SUCCESS になることを確認する
- [ ] テストログで YamlTestDataParser が実際に使われていることを確認する
- [ ] self-check (OK/NG per completion criterion, record in checks/task-5.md)
- [ ] QA expert review (Agent: general-purpose / Opus 4.8)
- [ ] language expert review (Agent: general-purpose / Opus 4.8)
- [ ] software-engineering expert review (Agent: general-purpose / Opus 4.8)
- [ ] user review

**Completion criteria**:

- `mvn test` が BUILD SUCCESS で終了する（全テストパス、Excel ファイルなし）
- テストログで YamlTestDataParser が実際に使われていることが確認できる

# Scripts

- `.rn/ntf-yaml-support/scripts/ExcelDump.java` — Task #3 のサンプリング比較用。NTF と同じ Apache POI（poi/poi-ooxml 3.8、テストクラスパスに同梱）で `.xls`/`.xlsx` の各シート・各行・各セルをプレーンテキストにダンプし、converter が生成した YAML と内容を人手で突合するために使用。実行方法はソース冒頭コメント参照（テストクラスパスは `mvn dependency:build-classpath` で取得、`LANG=ja_JP.UTF-8` で実行）。

# Decisions

- YAML設定の配置は `unit-test.xml` のみとする（本アクションは sendSync を使わない受信側のため、実行中にモックが testDataParser を参照しない。詳細は「YAML設定の配置方針」参照）。
- `nablarch-testing-converter` は test 依存ではなくビルドプラグインとして宣言する（web/batch テンプレート準拠）。

# State

- **Status**: in progress
- **Date**: 2026-10-06
- **Last completed**: #3 Excel→YAML変換（2 Excel→5 YAML）。サンプリング比較で差異なし、self-check/QA ともに OK
- **Next**: #4 Excel を削除する
- **Notes**: ブランチ `ntf-yaml-support` を `develop`(39a4f5c) から作成。変換対象Excelは2件（.xlsx 1 / .xls 1、別名のため併存問題なし）。YAML配置は unit-test.xml 単一（受信側=sendSync不使用=非トリガー）。
