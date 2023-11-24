以下、やったことを確認しています。
 - [ ] 自動テストは追記され、完璧です。
 - [ ] Pull Request名にはチケット番号を入れてあります。
 - [ ] N+1問題は発生させていません。
 - [ ] DBへの変更はFlywayに反映させています。
 - [ ] ソースコードフォーマットはEclipseでバッチリです。
 - [ ] Javaバッチ追加時は起動用シェルもセットで用意しています。
 - [ ] [構成ファイル種類毎のルール](https://mygithub.ritscm.xyz/cotos/BatchLightTemplate/tree/develop#batchlighttemplate)通りにファイルを修正しています。
 - [ ] 例外を握りつぶす場合はLog.Warnでのログ出力およびその理由をコメントで記載しています。
 - [ ] Date型の比較で時分秒の考慮が不要な場合は、切り捨てた上で比較しています。
 - [ ] 新規コードの場合、既存コードの部分的な修正以外の場合は、日付/日時の型としてLocalDate/LocalDateTimeを使用し、計算/比較/変換処理は[サンプルコード](https://mygithub.ritscm.xyz/cotos/SampleCode/blob/master/src/main/java/dateAndTimeSample/Main.java)を確認して実装しています。
 - [ ] 外部に連携する項目が将来的に桁数オーバーしないことを考慮しています。
 
