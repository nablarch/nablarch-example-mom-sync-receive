# nablarch-example-mom-sync-receive NTF YAML 対応 steering

## Overview
- 目的・背景: NTF（Nablarch Testing Framework）のAI対応として、AIが読み書きできないバイナリ形式の Excel テストデータを YAML 形式へ移行する（背景資料: https://github.com/Fintan-contents/nablarch-system-development-guide/pull/211）。
- Acceptance criteria の要約:
  - 既存 `ntf-yaml-support` ブランチ上で履歴を維持したまま、先端（過去の移行コミットを含む）の上に develop 同一ツリーのベースラインコミットを積み、task-1〜5 を再実施する（スキップ禁止）。
  - develop 同一状態で全テストがパスすることを確認する。
  - `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test 依存）と `nablarch-testing-converter:1.0.0-SNAPSHOT`（`<build><plugins>` のビルドプラグイン）を追加する。
  - `testDataParser` が参照される全フェーズで `YamlTestDataParser` が有効になるよう配置する。`interpreters` はプロパティ内 `<list>` に `dateTimeInterpreter` → `compositeInterpreter` の2つのみを直接記載する（testdata.html の設定例どおり）。
  - 全 Excel を YAML へ変換（サンプリングで一致確認）し、Excel を削除した状態で全テストがパスする。
- 対象プロジェクト名: `nablarch-example-mom-sync-receive`（MOM同期応答メッセージングの受信側 Example）
- 指示書: `.rn/ntf-yaml-support/ntf-yaml-support-instructions_mom-sync-receive_V1.6.md`（原本: `/home/k10949/instructions/ntf-yaml-support-instructions_mom-sync-receive_V1.6.md`）

## Build / Test
README（「実行手順」）で確認した手順。
- 前提: 動作確認用モジュール `nablarch-example-mom-testing-common` がローカル Maven リポジトリに install 済みであること（README 3.1。`~/.m2/repository/com/nablarch/example/nablarch-example-mom-testing-common` に存在することを確認済み）。
- プロファイル指定: 不要（README に `-P` 指定の記載なし）。
- generate-resources: 必要。`mvn clean generate-resources` で DB セットアップ（gsp-dba-maven-plugin の DDL 生成・実行、データロード）とエンティティクラス生成を行う（README「データベースのセットアップ及びエンティティクラスの作成について」。Java 17 用 JVM オプションは `.mvn/jvm.config` で指定済み）。
- テストコマンド: `mvn test`
- ロケール: メッセージ解決・文字化けを避けるため `LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8` を設定して実行する。
- 実行例: `LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8 mvn clean generate-resources && LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8 mvn test`
- 変換（Task #3）: `/home/k10949/workspace/nablarch-testing-converter/README.md` に従い、ビルドプラグインの `convert` ゴールを CLI 実行する。

## 対象 Excel テストデータ（全2件）
- `src/test/java/com/nablarch/example/ProjectInsertMessageRequestTest.xlsx`（`@MessagingRequestTest`）
- `src/test/java/com/nablarch/example/form/ProjectInsertMessageFormTest.xls`（`@EntityTest`）

## YAML 設定の配置方針（判定）
- `src/test/resources/unit-test.xml` はテスト用 diConfig `messaging-sync-receive-boot.xml`（`src/test/resources`）と `nablarch/test/test-data.xml` を import している。
- `ProjectInsertMessageRequestTest` の diConfig は `messaging-sync-receive-boot.xml`。メッセージングプロバイダは `EmbeddedMessagingProvider`（テストデータを読まない）。
- アクション `ProjectInsertMessageAction` は受信側で `sendSync` を呼ばず、テストデータに sendSync 応答電文（`response_*`）はない。要求電文（`setUpMessages`）・期待応答電文（`expectedMessages`）は、アクション実行の前後に `MQSupport` が `TestSupport.getTestDataParser()`（＝`unit-test.xml` のリポジトリ）経由で読む。
- 判定: **非該当 → `unit-test.xml` に配置する**（boot 側への配置は不要）。本番用 boot（`src/main/resources`）には入れない。

## scripts
- `scripts/ExcelDump.java`: Task #3 のサンプリング比較用。Excel（.xls/.xlsx）の各シート・各行・各セルを Apache POI（test クラスパス）でテキスト出力し、converter 生成 YAML と突き合わせる。

## Tasks

### #1: ベースライン化（developと同一状態）し、全PASSを確認する
**Purpose**: 既存 `ntf-yaml-support` ブランチ（履歴維持）の先端の上に、ワーキングツリー全体を `develop` と同一内容へ揃えるコミットを1つ新規に積み、その状態で全テストがパスすることを確認する（過去の移行コミットがあっても必ず再実施する）。
**Prerequisites**: none
**Steps**:
- [x] 既存の `ntf-yaml-support` ブランチで作業する（新規作成しない。履歴維持）
- [x] 過去の移行コミット（先端 `461e4d4`）を巻き戻さず、その上に処理を積む（rebase / reset --hard / amend は不使用）
- [x] `develop` の存在を確認し、develop 同一ツリーの新規コミット `8724d49` を積む（`git read-tree -u --reset develop` は auto mode で拒否されたため、`git commit-tree develop^{tree} -p HEAD` → `git merge --ff-only` で同等のコミットを作成。`HEAD^{tree}` = `develop^{tree}` = `ff07be4` を確認）
- [x] README を確認し、ビルド・テスト手順を把握する（本ファイル「Build / Test」）
- [x] steering.md と scripts の初版を作成する
- [x] 本指示書を `.rn/ntf-yaml-support/` 配下へ同名でコピーし、`cmp` で原本と同一であることを確認する
- [x] `mvn clean generate-resources` でエンティティクラスを生成する（BUILD SUCCESS）
- [x] `mvn test` で全テストパスを確認する（Tests run: 4, Failures: 0, Errors: 0 / BUILD SUCCESS）
- [x] 結果を `checks/task-1.md` に記録する
- [x] 変更ファイルをコミットする
- [x] self-check
- [ ] QA expert review (Agent: general-purpose / Opus)
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- 既存の `ntf-yaml-support` ブランチ上で作業しており、コミット履歴が維持されている（過去の移行コミットが残っている）
- ブランチの既存先端の上に、ワーキングツリー全体が `develop` と同一内容になる新規コミットが1つ積まれている
- `mvn test` が全テストパスで終了している（BUILD SUCCESS）
- 本指示書のコピーが `.rn/ntf-yaml-support/` 配下に原本と同一内容で配置され、コミットされている

### #2: 設定変更 — pom.xml と unit-test.xml にYAML対応を追加
**Purpose**: `pom.xml` に `nablarch-testing-yaml`（test 依存）と `nablarch-testing-converter`（ビルドプラグイン）を追加し、`unit-test.xml` に `YamlTestDataParser`（`testDataParser` 上書き）を testdata.html の設定例どおりに追加する。
**Prerequisites**: #1
**Steps**:
- [ ] `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT`（test scope）を追加する
- [ ] `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` を追加する（`<configuration>` に from=xls / to=yaml / input / output / overwrite）
- [ ] 構成判定（応答電文 `response_*` / sendSync の有無）→ 非該当（「YAML 設定の配置方針」参照）
- [ ] `unit-test.xml` に `YamlTestDataParser` を配置する（`interpreters` 内 `<list>` に `dateTimeInterpreter` → `compositeInterpreter` の2つのみ。`test-data.xml` import・`dbInfo` 定義より後）
- [ ] `mvn dependency:resolve` と `mvn help:describe` で yaml 依存と converter `convert` ゴールの解決を確認する
- [ ] 変更したファイルをコミットする
- [ ] self-check
- [ ] QA / language / software-engineering expert review (Agent: general-purpose / Opus)
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- `pom.xml` に `nablarch-testing-yaml:1.0.0-SNAPSHOT` が test スコープで追加されている
- `pom.xml` の `<build><plugins>` に `nablarch-testing-converter:1.0.0-SNAPSHOT` がビルドプラグインとして追加されている
- `YamlTestDataParser` が `testDataParser` の参照される全フェーズで有効な設定ファイルに追加され、`interpreters` は `dateTimeInterpreter` → `compositeInterpreter` の2つのみ
- `mvn dependency:resolve` が成功し、converter の `convert` ゴールが `mvn help:describe` で解決できる

### #3: ExcelテストデータをYAMLに変換
**Purpose**: converter で Excel を YAML へ変換してソースツリー内に配置し、サンプリングで内容一致を確認する。
**Prerequisites**: #2
**Steps**:
- [ ] 対象 Excel の所在を確認する
- [ ] 同名 `.xls` / `.xlsx` の併存を確認する
- [ ] converter README に従い全 Excel を YAML 変換する
- [ ] 各 YAML を Excel と最低3件（元データが3件未満は全件）サンプリング比較する
- [ ] 比較結果を `checks/task-3.md` の Sampling Comparison に記録する
- [ ] 変換 YAML をコミットする（Excel はまだ削除しない）
- [ ] self-check
- [ ] QA expert review (Agent: general-purpose / Opus)
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- 変換対象の全 Excel に対応する YAML が生成されている
- サンプリング比較で Excel と YAML の内容が一致している
- 変換後の YAML がコミットされている

### #4: Excelを削除する
**Purpose**: 変換対象だった Excel を削除してコミットする。
**Prerequisites**: #3
**Steps**:
- [ ] 変換対象 Excel を削除する
- [ ] 残存 Excel が0件であることを `checks/task-4.md` に記録する
- [ ] Excel 削除をコミットする
- [ ] self-check
- [ ] QA expert review (Agent: general-purpose / Opus)
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- 変換対象だった全 Excel が削除されている
- 削除がコミットされている

### #5: YAMLテストデータで全テストをパスさせる
**Purpose**: Excel なしで全テストがパスすることを確認する。
**Prerequisites**: #4
**Steps**:
- [ ] `mvn test` が BUILD SUCCESS になることを確認する
- [ ] テストログで YamlTestDataParser が使われていることを確認する
- [ ] self-check
- [ ] QA / language / software-engineering expert review (Agent: general-purpose / Opus)
- [ ] user review（全 task 完了後にまとめて実施）
**Completion criteria**:
- `mvn test` が BUILD SUCCESS で終了する（全テストパス、Excel ファイルなし）
- テストログで YamlTestDataParser が実際に使われていることが確認できる
