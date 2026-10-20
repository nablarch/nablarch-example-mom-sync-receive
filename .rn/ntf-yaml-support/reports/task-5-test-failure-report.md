# Task #5 テスト失敗レポート: `${systemTime}` が解釈されず testNormalEnd が失敗

- 対象プロジェクト: `nablarch-example-mom-sync-receive`（ブランチ `ntf-yaml-support`）
- 対象コミット: `6691e47`（task-4 完了記録の時点。Excel 削除済み・YAML 5件あり）
- 実行手順: `LANG=ja_JP.UTF-8 LC_ALL=ja_JP.UTF-8` を設定し、`mvn clean generate-resources` → `mvn test`（steering.md の Build / Test に従う）
- 状態: 運用ルールに従い、**修正は行わず停止中**（ユーザー判断待ち）

---

## 1. 事象

`mvn test` が **BUILD FAILURE** になった。4件中1件が失敗。

| テストクラス | 結果 |
| --- | --- |
| `com.nablarch.example.form.ProjectInsertMessageFormTest`（`@EntityTest`） | 2件パス |
| `com.nablarch.example.ProjectInsertMessageRequestTest`（`@MessagingRequestTest`） | 2件中1件失敗（`testNormalEnd`）、`testValidationError` はパス |

### 1.1 Maven 実行結果（抜粋）

```
[INFO] Running com.nablarch.example.form.ProjectInsertMessageFormTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.177 s -- in com.nablarch.example.form.ProjectInsertMessageFormTest
[INFO] Running com.nablarch.example.ProjectInsertMessageRequestTest
[ERROR] Tests run: 2, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 9.970 s <<< FAILURE! -- in com.nablarch.example.ProjectInsertMessageRequestTest
[ERROR] com.nablarch.example.ProjectInsertMessageRequestTest.testNormalEnd -- Time elapsed: 0.327 s <<< FAILURE!
...
[ERROR] Failures: 
[ERROR]   ProjectInsertMessageRequestTest.testNormalEnd:18 no=[1] case=[データレコードの値が正しくデータベースに登録されること。]  table=PROJECT_TEMP line=1 column=INSERT_DATE expected:<[${systemTime}]> but was:<[2014-09-13 12:34:56.0]>
[INFO] 
[ERROR] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD FAILURE
```

### 1.2 スタックトレース（`target/surefire-reports/com.nablarch.example.ProjectInsertMessageRequestTest.txt`）

```
org.junit.ComparisonFailure: no=[1] case=[データレコードの値が正しくデータベースに登録されること。]  table=PROJECT_TEMP line=1 column=INSERT_DATE expected:<[${systemTime}]> but was:<[2014-09-13 12:34:56.0]>
	at org.junit.Assert.assertEquals(Assert.java:117)
	at nablarch.test.Assertion.assertEqualsAsString(Assertion.java:542)
	at nablarch.test.Assertion.assertTableEquals(Assertion.java:302)
	at nablarch.test.Assertion.assertTableEquals(Assertion.java:82)
	at nablarch.test.Assertion.assertTableEquals(Assertion.java:56)
	at nablarch.test.core.db.DbAccessTestSupport.assertTableEquals(DbAccessTestSupport.java:370)
	at nablarch.test.core.db.DbAccessTestSupport.assertTableEquals(DbAccessTestSupport.java:343)
	at nablarch.test.core.standalone.TestShot.assertTables(TestShot.java:210)
	at nablarch.test.core.standalone.TestShot.assertAll(TestShot.java:132)
	at nablarch.test.core.standalone.TestShot.executeTestShot(TestShot.java:87)
	at nablarch.test.core.standalone.StandaloneTestSupportTemplate.executeTestShot(StandaloneTestSupportTemplate.java:166)
	at nablarch.test.core.standalone.StandaloneTestSupportTemplate.executeTest(StandaloneTestSupportTemplate.java:118)
	at nablarch.test.core.standalone.StandaloneTestSupportTemplate.execute(StandaloneTestSupportTemplate.java:96)
	at nablarch.test.core.standalone.StandaloneTestSupportTemplate.execute(StandaloneTestSupportTemplate.java:58)
	at com.nablarch.example.ProjectInsertMessageRequestTest.testNormalEnd(ProjectInsertMessageRequestTest.java:18)
```

失敗箇所は検証フェーズ（`TestShot.assertTables` → `assertTableEquals`）。アクションの実行は正常に終わっている。

### 1.3 アプリケーションログ（テスト実行時の出力）

**(a) テスト設定の読み込み。`systemTimeProvider` が固定時刻の実装に、`testDataParser` が `YamlTestDataParser` に上書きされている**

```
2026-10-20 10:42:16.566 -DEBUG- nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader ... load component config file. file = classpath:nablarch/core/date_test.xml
2026-10-20 10:42:16.588 -WARN- nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader ... override component classname was not matched.  replace all component configuration.  component name = systemTimeProvider, defined component definition classname = nablarch.core.date.BasicSystemTimeProvider, override component definition classname = nablarch.test.FixedSystemTimeProvider
2026-10-20 10:42:16.595 -DEBUG- nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader ... load component config file. file = classpath:nablarch/test/test-data.xml
2026-10-20 10:42:16.599 -DEBUG- nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader ... load component config file. file = classpath:nablarch/test/test-data-interpreter.xml
2026-10-20 10:42:16.656 -WARN- nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader ... override component classname was not matched.  replace all component configuration.  component name = testDataParser, defined component definition classname = nablarch.test.core.reader.BasicTestDataParser, override component definition classname = nablarch.test.core.reader.YamlTestDataParser
...
2026-10-20 10:42:18.678 -INFO- nablarch.test.core.reader.YamlTestDataParser ... YamlTestDataParser does not use TestDataReader; the injected value is ignored.
```

**(b) アクション実行フェーズの SQL トレースログ（TRACE）。アクションは固定のシステム時刻 `2014-09-13 12:34:56.0` で INSERT_DATE と UPDATE_DATE を登録し、正常にコミットしている**

```
2026-10-20 10:42:31.075 -DEBUG- SQL [202610201042309210036] ... req_id = [ProjectInsertMessage] usr_id = [batch_user] nablarch.core.db.statement.BasicSqlPStatement#executeUpdate
	SQL = [INSERT INTO PUBLIC.PROJECT_TEMP(ALLOCATION_OF_CORP_EXPENSES,CLIENT_ID,COST_OF_GOODS_SOLD,INSERT_DATE,NOTE,PROJECT_CLASS,PROJECT_END_DATE,PROJECT_LEADER,PROJECT_MANAGER,PROJECT_NAME,PROJECT_START_DATE,PROJECT_TYPE,SALES,SGA,STATUS,UPDATE_DATE,USER_ID)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)]
2026-10-20 10:42:31.075 -TRACE- SQL [202610201042309210036] ... nablarch.core.db.statement.BasicSqlPStatement#Parameters
	01 = [3000]
	02 = [1]
	03 = [1000]
	04 = [2014-09-13 12:34:56.0]      ← INSERT_DATE
	05 = [備考欄]
	...
	15 = [0]
	16 = [2014-09-13 12:34:56.0]      ← UPDATE_DATE
	17 = [100]
2026-10-20 10:42:31.079 -DEBUG- SQL ... BasicSqlPStatement#executeUpdate
	execute_time(ms) = [4] update_count = [1]
2026-10-20 10:42:31.081 -DEBUG- SQL ... transaction commit. resource=[transaction]
```

応答電文も正常（`message_body = [ProjectInsertMessage ... success ...]`）。

**(c) 検証フェーズ。期待値テーブルとの比較用の SELECT で1件取得している**

```
2026-10-20 10:42:31.098 -DEBUG- SQL [null] ... nablarch.core.db.statement.BasicSqlPStatement#retrieve
	SQL = [select PROJECT_NAME,PROJECT_TYPE,PROJECT_CLASS,PROJECT_START_DATE,PROJECT_END_DATE,CLIENT_ID,PROJECT_MANAGER,PROJECT_LEADER,USER_ID,NOTE,SALES,COST_OF_GOODS_SOLD,SGA,ALLOCATION_OF_CORP_EXPENSES,STATUS,INSERT_DATE,UPDATE_DATE from PROJECT_TEMP order by PROJECT_TEMP_ID]
2026-10-20 10:42:31.100 -DEBUG- SQL ... BasicSqlPStatement#retrieve
	execute_time(ms) = [1] retrieve_time(ms) = [1] count = [1]
```

この直後に 1.2 の `ComparisonFailure` が発生する。

### 1.4 テストデータと設定

`src/test/java/com/nablarch/example/ProjectInsertMessageRequestTest/testNormalEnd.yaml`（expected_tables）:

```yaml
expected_tables:
  - group_id: "1"
    table: "PROJECT_TEMP"
    rows:
      - PROJECT_NAME: "プロジェクト００１"
        ...
        STATUS: "0"
        INSERT_DATE: "${systemTime}"
        UPDATE_DATE: "${systemTime}"
```

`src/test/resources/unit-test.xml`（task-2 / commit 43ff0c8 で追加）:

```xml
  <list name="yamlInterpreters">
    <component-ref name="compositeInterpreter"/>
    <component-ref name="dateTimeInterpreter"/>
  </list>
```

---

## 2. 直接原因

期待値の `${systemTime}` がシステム時刻に置き換えられず、文字列 `"${systemTime}"` のまま実際の値 `2014-09-13 12:34:56.0` と比較された。置き換えられなかったのは、**`yamlInterpreters` の先頭にある `compositeInterpreter` が `${systemTime}` を受け取り、後ろの `dateTimeInterpreter` に渡さないまま処理を終えた**ためである。

nablarch-testing（6-NEXT-SNAPSHOT）のソースで確認した動作は次のとおり。

- **`InterpretationContext#invokeNext`**: インタープリタをリストの順に1つずつ呼び出す。各インタープリタが `invokeNext()` を呼んだときだけ、次のインタープリタに値が渡る。
- **`CompositeInterpreter#interpret`**:
  - 値に `\$\{[^\}]+\}` が含まれる場合は、その部分を内側のインタープリタ（`BasicJapaneseCharacterInterpreter` のみ）で処理した結果を返す。この場合、`invokeNext()` は呼ばない。
  - `invokeNext()` を呼ぶのは、パターンに一致しなかった場合だけである。
  ```java
  while (m.find()) { ... m.appendReplacement(result, interpreted); }
  if (result.length() > 0) { m.appendTail(result); return result.toString(); }   // ← ここで終了
  return context.invokeNext();
  ```
- **`BasicJapaneseCharacterInterpreter`**:
  - 対象とするのは `${文字種,文字数}`（`\$\{(\W+)\s*,\s*([0-9]+)\}`）の形式だけである。
  - `${systemTime}` はこの形式に一致しないため、内側のチェーンの末尾まで進み、元の値 `"${systemTime}"` がそのまま返る。
- **`DateTimeInterpreter#interpret`**: `${systemTime}` などの記法をシステム時刻に置き換える。しかし今回のリストの順番では、ここまで値が届かない。

まとめると、`"${systemTime}"` は `compositeInterpreter` に「処理済み」と扱われ、変換されないまま返された。その結果、期待値が `"${systemTime}"` という文字列になった。

参考: NTF 標準の `nablarch/test/test-data-interpreter.xml`（nablarch-testing-default-configuration）の `interpreters` は次の順番で、`dateTimeInterpreter` が `compositeInterpreter` より先にある。

```xml
<list name="interpreters">
  <component-ref name="nullInterpreter"/>
  <component-ref name="quotationTrimmer"/>
  <component-ref name="dateTimeInterpreter"/>
  <component-ref name="lineSeparatorInterpreter"/>
  <component-ref name="compositeInterpreter"/>
</list>
```

### 他のテストが失敗しなかった理由

- `ProjectInsertMessageFormTest`: `${半角数字,9}` などの文字列増幅記法だけを使っている。`compositeInterpreter` が先頭にあっても正しく処理される。
- `testValidationError`: 期待値テーブルに `${systemTime}` を使っていない。
- `${systemTime}` を期待値に持つのは `testNormalEnd` の `INSERT_DATE` と `UPDATE_DATE` だけ。最初に比較される列 `INSERT_DATE` で失敗した。

---

## 3. 根本原因

1. **インタープリタは「どれを使うか」だけでなく「どの順番か」でも動作が変わるが、その要件がどこにも書かれていなかった。**
   - 指示書の Acceptance criteria と Rules には「`yamlInterpreters` には `compositeInterpreter` と `dateTimeInterpreter` の2つのみを定義する」とだけある。順番の指定はない。
   - task-2 では、この記載の並び（composite → dateTime）をそのまま定義の順番にした。
   - `CompositeInterpreter` は `${...}` を見つけると後続に渡さないため、`${...}` 形式の記法同士では先に置いたものが優先される。この制約が考慮されていなかった。
2. **前回の構成から変えた影響を評価していなかった。**
   - 前回パスした構成（02b2b61）と、隣接プロジェクト nablarch-example-batch の構成は `nullInterpreter → dateTimeInterpreter → lineSeparatorInterpreter → compositeInterpreter` の順で、NTF 標準と同じだった。
   - task-2 で「2つのみ」に絞ったとき、標準の順番を保たずに並べ替えてしまった。
3. **task-2 の検証とレビューで、実行時の動作を確認していなかった。**
   - task-2 の完了条件は、依存の解決（`dependency:resolve`）と `convert` ゴールの解決（`help:describe`）だけだった。設定を変えた後にテストを実行する手順がなかった。
   - QA、Language、Software-engineering のレビューも、コンポーネントが実在するか、定義の順番（import と dbInfo との前後関係）が正しいかを確認しただけだった。インタープリタチェーンの順番の意味までは確認していない。
   - その結果、問題は Excel を削除した後の task-5 で初めて見つかった。

---

## 4. 対応案

### 案A（推奨）: `yamlInterpreters` の順番を `dateTimeInterpreter` → `compositeInterpreter` に変える

```xml
  <list name="yamlInterpreters">
    <component-ref name="dateTimeInterpreter"/>
    <component-ref name="compositeInterpreter"/>
  </list>
```

- 「`compositeInterpreter` と `dateTimeInterpreter` の2つのみ」というルールは守ったまま、NTF 標準と同じ前後関係になる。
- `DateTimeInterpreter` は `${systemTime}` などの記法と完全に一致する値しか変換しない。一致しない値は `invokeNext()` で次に渡すので、文字列増幅記法（`${半角数字,9}` など）の処理には影響しない。
- **事前検証の結果**（ブランチには触れず、スクラッチにコピーした HEAD のツリーで順番だけを入れ替えて実行）:
  ```
  [INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.156 s -- in com.nablarch.example.form.ProjectInsertMessageFormTest
  [INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.558 s -- in com.nablarch.example.ProjectInsertMessageRequestTest
  [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  ```
- 修正手順の案:
  1. unit-test.xml の順番を入れ替える。コメントには「`dateTimeInterpreter` は `compositeInterpreter` より先に置く。`compositeInterpreter` は `${...}` を処理すると後続に渡さないため」と書く。
  2. task-2 の修正として、新しいコミットを積む（履歴は書き換えない）。
  3. `checks/task-2.md` と steering.md に修正の内容と理由を追記する。
  4. task-5 を再実行する（`mvn clean generate-resources` → `mvn test`）。
  5. task-5 の QA、Language、Software-engineering の各レビューを実施する。

### 案B: テストデータ（YAML）を変える（非推奨）

- `INSERT_DATE` と `UPDATE_DATE` の期待値を固定の文字列 `2014-09-13 12:34:56.0` などに書き換える。
- `FixedSystemTimeProvider` の設定値にテストデータが依存することになり、Excel に書かれていた元の意図（`${systemTime}`）から外れる。変換の忠実性も損なうため、推奨しない。

### 再発防止

- 指示書の Rules と Acceptance criteria に、「`yamlInterpreters` は `dateTimeInterpreter` → `compositeInterpreter` の順番で定義する（`compositeInterpreter` は `${...}` を処理すると後続に渡さないため）」と順番も明記する。
- task-2 の Steps と完了条件に、設定の変更後に Excel がある状態でテストを実行し、パスを確認する手順を加える。また task-3 で YAML が揃った時点でもテストを実行する。こうすれば、parser とインタープリタの設定の誤りを Excel 削除前に見つけられる。
- 並列で作業している他のプロジェクトにも、task-2 で同じ順番（composite → dateTime）にしているものがないか確認する。
