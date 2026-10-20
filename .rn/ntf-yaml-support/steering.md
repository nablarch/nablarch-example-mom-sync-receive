# nablarch-example-mom-sync-receive NTF YAML 対応 steering

## Overview

- **目的**: NTF（Nablarch Testing Framework）のAI対応として、AIが読み書きできないバイナリ形式の
  Excelテストデータを YAML 形式へ移行する。
- **背景資料**: https://github.com/Fintan-contents/nablarch-system-development-guide/pull/211
- **対象プロジェクト**: `nablarch-example-mom-sync-receive`（MOM同期応答メッセージングの受信側 Example）
- **Acceptance criteria の要約**:
  - 既存 `ntf-yaml-support` ブランチ上で（履歴維持のまま）作業し、先端に develop 同一ツリーの
    ベースラインコミットを積んだ上で task-1〜5 を再実施する。
  - `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test 依存）、
    `nablarch-testing-converter:1.0.0-SNAPSHOT`（ビルドプラグイン）を追加。
  - `testDataParser` が参照される全フェーズで YAML が有効になるよう `YamlTestDataParser` と
    `yamlInterpreters`（`compositeInterpreter` と `dateTimeInterpreter` の2つのみ）を配置。
  - 全 Excel を YAML へ変換（サンプリング一致確認）し、変換後に Excel を削除、全テストパス。

## 対象 Excel テストデータ（全2件）

- `src/test/java/com/nablarch/example/ProjectInsertMessageRequestTest.xlsx`
  （`@MessagingRequestTest` メッセージングリクエスト単体テスト）
- `src/test/java/com/nablarch/example/form/ProjectInsertMessageFormTest.xls`
  （`@EntityTest` エンティティ精査テスト）
- 同名 `.xls`/`.xlsx` の併存はなし（ファイル名・配置ディレクトリとも別）。

## Build / Test

README（`実行手順`）に基づき、ビルド・テストは以下の手順で実行する。

- 前提: 動作確認用モジュール `nablarch-example-mom-testing-common` がローカル Maven リポジトリに
  install 済みであること（README 3.1。本環境では `~/.m2` に導入済みを確認）。
- エンティティ生成（DBセットアップ含む）: `mvn generate-resources`
  - `gsp-dba-maven-plugin` が `generate-ddl` / `execute-ddl` / `generate-entity` / `load-data` を実行。
  - Java 17 実行用の JVM オプションは `.mvn/jvm.config` で指定済み。
- テスト実行: `mvn test`
- 文字化け・メッセージ解決失敗を避けるため、テストは `LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8` で実行する
  （`C.UTF-8` だとメッセージ解決に失敗しテストが落ちる）。
- 変換ツール（Task #3）はビルドプラグインの `convert` ゴールを CLI 実行する。
  実行手順は `/home/k10949/workspace/nablarch-testing-converter/README.md` に従う。

## YAML設定の配置方針（判定）

Rules「`testDataParser` が参照される全フェーズで YAML リーダーが有効になるよう配置」に従い判定した。

- 構成: `src/test/resources/unit-test.xml` が `messaging-sync-receive-boot.xml`（diConfig）と
  `nablarch/test/test-data.xml`（→ `test-data-interpreter.xml`）を import している。
- テスト種別:
  - `ProjectInsertMessageFormTest` … `@EntityTest`（エンティティ精査）。通常のNTFテスト。
  - `ProjectInsertMessageRequestTest` … `@MessagingRequestTest`
    （`StandaloneTestSupportTemplate` ベースのメッセージングリクエスト単体テスト）。
- 判定結果: **非トリガー → `unit-test.xml` への配置で十分**。
  - 本プロジェクトのアクション `ProjectInsertMessageAction` は **`sendSync` を呼ばない受信側アクション**。
  - 全テストデータ（リクエスト電文・期待応答電文）は、アクション実行（リポジトリ再初期化）の前に
    `TestSupport.getTestDataParser()`（＝ `unit-test.xml` の `testDataParser`）経由で読み込まれる。
  - 実行中にモックが `testDataParser` 経由でテストデータを読む経路（sendSync モック）は非該当。
  - batch の request test（同じ `StandaloneTestSupportTemplate` ベース）が `unit-test.xml` 配置のみで
    全パスした前例とも一致。
- 配置: `unit-test.xml` に `yamlInterpreters` リストと `YamlTestDataParser`（`testDataParser` 上書き）を
  追加する。定義順は `test-data.xml` import より後・`dbInfo` 定義より後に置き参照解決を担保する。
- `yamlInterpreters` には **`dateTimeInterpreter` → `compositeInterpreter` の順で2つのみ**を定義する
  （YAML はネイティブに null / 改行 / 引用符を扱うため、NTF 固有記法のうち文字増幅＝compositeInterpreter と
  システム時刻記法＝dateTimeInterpreter のみが必要。他の interpreter は追加しない）。
- 本番用 boot（`src/main/resources` 配下）には入れない。

## reports

- `reports/task-5-test-failure-report.md`: Task #5 のテスト失敗（`${systemTime}` 未解釈）についての事象・直接原因・根本原因・対応案。

## scripts

- `scripts/ExcelDump.java`: Task #3 のサンプリング比較用。Excel（.xls/.xlsx）の各シート・各行・各セルを
  テキスト出力し、converter 生成 YAML と内容を突き合わせる。NTF と同じ Apache POI（test クラスパス）で読む。

## Tasks

### #1: ベースライン化（developと同一状態）し、全PASSを確認する
**Purpose**: 既存 `ntf-yaml-support` ブランチ（履歴維持）の先端の上に、ワーキングツリー全体を develop と
同一内容へ揃えるコミットを1つ新規に積み、develop 同等状態で全テストがパスすることを確認する。
**Prerequisites**: none
**Steps**:
- [x] 既存 `ntf-yaml-support` ブランチへ切替（新規作成しない／履歴維持）
- [x] 過去移行コミットを巻き戻さず、先端の上に処理を積む（rebase/reset --hard/amend を使わない）
- [x] `develop` 存在確認 → `git read-tree -u --reset develop` → commit でベースライン化（ツリーが develop と完全一致）
- [x] README を確認しビルド・テスト手順を把握
- [x] steering.md と scripts の初版を作成
- [x] `mvn generate-resources` でエンティティ生成
- [x] `mvn test` で全テストパスを確認
- [x] 結果を `checks/task-1.md` に記録（self-check列）
- [x] 変更ファイル（checks・steering.md・scripts）をコミット
- [x] self-check / QA expert review（Verdict OK）/ user review（全task完了後にまとめて実施）
**Completion criteria**:
- 既存 `ntf-yaml-support` ブランチ上・履歴維持（過去移行コミット残存、rebase/amend/hard reset なし）
- 先端の上に develop 同一ツリーの新規コミットが1つ積まれている
- `mvn test` が全テストパス（BUILD SUCCESS）

### #2: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加
**Purpose**: `pom.xml` に `nablarch-testing-yaml`（test 依存）、`nablarch-testing-converter`（ビルドプラグイン）を
追加し、`unit-test.xml` に `yamlInterpreters`（2つのみ）と `YamlTestDataParser` を追加する。
**Prerequisites**: #1
**Steps**:
- [x] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）追加
- [x] `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` をビルドプラグイン追加
      （`<configuration>` に from=xls/to=yaml/input/output/overwrite を設定）
- [x] 構成判定（応答電文 response_* / sendSync の有無）→ 非トリガー（steering 参照）
- [x] `unit-test.xml` に `yamlInterpreters`（compositeInterpreter・dateTimeInterpreter のみ）と
      `YamlTestDataParser`（testDataParser 上書き）を配置（test-data.xml import・dbInfo 定義より後）
- [x] （Task #5 失敗を受けた修正）`yamlInterpreters` を dateTimeInterpreter → compositeInterpreter の順に変更（49e385c。
      compositeInterpreter は `${...}` を解釈すると後続へ委譲しないため。task-2 の QA / language / software-engineering 再レビュー OK）
- [x] `mvn dependency:resolve` と `mvn help:describe` で converter `convert` ゴール解決を確認
- [x] 変更をコミット
- [x] self-check / QA / language / software-engineering（すべて Verdict OK）/ user review（全task完了後まとめて）
**Completion criteria**:
- `pom.xml` に yaml（test）と converter（ビルドプラグイン）が追加されている
- `yamlInterpreters` に compositeInterpreter と dateTimeInterpreter の2つのみ、`testDataParser` が
  `YamlTestDataParser` に上書きされている
- `mvn dependency:resolve` 成功、converter `convert` ゴールが `mvn help:describe` で解決できる

### #3: ExcelテストデータをYAMLに変換
**Purpose**: converter で Excel を YAML へ変換し、ソースツリー内に配置してサンプリングで内容一致を確認する。
**Prerequisites**: #2
**Steps**:
- [x] 対象 Excel の所在確認（2件）
- [x] 同名 .xls/.xlsx 併存チェック（本プロジェクトは異名のため非該当）
- [x] converter README に従い全 Excel を YAML 変換（Converted 2 file(s)、YAML 5件生成）
- [x] 各 YAML を Excel と最低3件（元データが3件未満は全件）サンプリング比較
- [x] 比較結果を `checks/task-3.md` の Sampling Comparison に記録（差異なし）
- [x] 変換 YAML をコミット（Excel はまだ削除しない）
- [x] self-check / QA（Verdict OK）/ user review（全task完了後まとめて）
**Completion criteria**:
- 全 Excel に対応する YAML が生成されている
- サンプリング比較で Excel と YAML が一致
- 変換 YAML がコミットされている

### #4: Excelを削除する
**Purpose**: 変換対象だった Excel を削除してコミットする。
**Prerequisites**: #3
**Steps**:
- [x] 変換対象 Excel を削除
- [x] 残存 Excel が0件であることを `checks/task-4.md` に記録（Excelなしテストは Task #5）
- [x] Excel 削除をコミット
- [x] self-check / QA（Verdict OK）/ user review（全task完了後まとめて）
**Completion criteria**:
- 変換対象だった全 Excel が削除されている
- 削除がコミットされている

### #5: YAMLテストデータで全テストをパスさせる
**Purpose**: Excelなしで全テストがパスすることを確認する。
**Prerequisites**: #4
**Steps**:
- [x] `mvn test` が BUILD SUCCESS（1回目: BUILD FAILURE。testNormalEnd の `${systemTime}` が未解釈。
      原因は `yamlInterpreters` の順番（composite → dateTime）。`reports/task-5-test-failure-report.md` 参照。
      ユーザー承認のうえ案A を task-2 修正コミット 49e385c で適用し、再実行で 4件全パス・BUILD SUCCESS）
- [x] テストログで YamlTestDataParser が使われていることを確認
- [x] self-check / QA（Verdict OK）/ language・software-engineering（コード変更なしのため N/A。修正 49e385c は task-2 側でレビュー OK）/ user review（全task完了後まとめて）
**Completion criteria**:
- `mvn test` が BUILD SUCCESS（全テストパス、Excel なし）
- テストログで YamlTestDataParser が使われていることが確認できる
