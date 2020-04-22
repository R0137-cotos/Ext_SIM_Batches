# BatcheTemplateについて
新たにバッチを作成する場合は、当PRJをコピーしてバッチを作成してください。  

# テンプレート構成概要
JobComponent->BatchComponent->BatchStepComponentの各メソッドを呼び出すよう構成されている。

商材個別処理については、BatchStepComponentをDynamicProductConfig経由でBatchComponentで切り替えを実施する。

各メソッドの引数、戻り値については、実装するバッチに応じて適宜変更すること。

また、商材切替が存在しない場合は定数の削除、BatchStepComponentXXXの削除、DynamicProductConfig内の切替メソッドの内容を適宜変更すること。

# テンプレート構成詳細
## JobComponent
１つのBatchComponentを呼び出す。

BatchComponent以下で発生する例外はすべてここでキャッチするようにし、上位に例外をスローしないこと。

単体テスト：正常系、異常系の２パターン

## BatchComponent
商材切替処理後にBatchStepComponentのcheck, afterProcess, process, beforeProcessを呼び出す。

単体テスト：C0パターン × 切替可能商材数

## DynamicProductConfig
BatchComponent毎に商材切替メソッドを実装する。

商材切替を判断するパラメーター（例：商品マスタの商品種類区分）はバッチ毎に異なる想定なので、適宜メソッドの処理内容を変更すること。

## BatchStepComponent
標準：IBatchStepComponentをimplementsし、各メソッドを実装する。

商材個別：BatchStepComponentをextendsし、商材個別処理が必要なメソッドのみOverrideする。

### check
パラメーターチェックを実施する。

単体テスト：C0ベースでテスト

### afterProcess
INPUTファイルの読み込み、DBから処理に必要なデータを取得する。

単体テスト：C0ベースでテスト

### process
読み込んだデータを加工する。

単体テスト：C0ベースでテスト

### beforeProcess
ファイルの出力、DBに処理データを書き込み（ステータス更新など）を行う。

単体テスト：C0ベースでテスト