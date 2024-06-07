以下、やったことを確認しています。
 - [ ] Pull Request名にはチケット番号を入れてあります。
 - [ ] 自動テストは追記され、完璧です。
 - [ ] マイルストーンを設定しました。（topic or releaseブランチでの初回対応時のみ必要、CommonLibs verupのみだけの場合は不要）
 - [ ] ソースコードフォーマットはEclipseでバッチリです。
 - [ ] [プルリクレビュー依頼前のチェック観点](https://ex-redmine-1.cotos.ricoh.co.jp/projects/cotos_root/wiki/%E3%83%97%E3%83%AB%E3%83%AA%E3%82%AF%E3%83%AC%E3%83%93%E3%83%A5%E3%83%BC%E4%BE%9D%E9%A0%BC%E5%89%8D%E3%81%AE%E3%83%81%E3%82%A7%E3%83%83%E3%82%AF%E8%A6%B3%E7%82%B9)、および[コーディングルール](https://ex-redmine-1.cotos.ricoh.co.jp/projects/cotos_root/wiki/%E3%82%B3%E3%83%BC%E3%83%87%E3%82%A3%E3%83%B3%E3%82%B0%E3%83%AB%E3%83%BC%E3%83%AB)を確認の上、実装しました。
 - [ ] [影響範囲調査wiki](https://ex-redmine-1.cotos.ricoh.co.jp/projects/cotos_root/wiki/%E5%BD%B1%E9%9F%BF%E7%AF%84%E5%9B%B2%E8%AA%BF%E6%9F%BB%E6%96%B9%E6%B3%95)を参照の上、子チケットを作成して親チケットに紐づけました。（調査不要の場合も紐づけて調査不要な理由を記載した上でチェックを付けること）

 以下は該当する場合のみ確認しています。
 - [ ] 再構成後のバッチの場合、[構成ファイル種類毎のルール](https://mygithub.ritscm.xyz/cotos/BatchLightTemplate/tree/develop#batchlighttemplate)通りにファイルを修正しています。
 - [ ] 新規でJavaバッチ追加時は、起動用シェルもセットで用意しています。
 - [ ] N+1問題は発生させていません。
 - [ ] DBへの変更はFlywayに反映させています。
 - [ ] 例外を握りつぶす場合はLog.Warnでのログ出力およびその理由をコメントで記載しています。
 - [ ] Date型の比較で時分秒の考慮が不要な場合は、切り捨てた上で比較しています。
 - [ ] 新規コードの場合、既存コードの部分的な修正以外の場合は、日付/日時の型としてLocalDate/LocalDateTimeを使用し、計算/比較/変換処理は[サンプルコード](https://mygithub.ritscm.xyz/cotos/SampleCode/blob/master/src/main/java/dateAndTimeSample/Main.java)を確認して実装しています。
 - [ ] メール送信処理を新規に追加する場合は、[CommonLibsから直接メール送信処理を呼ぶ場合のメールの実装について](http://ex-redmine-1.cotos.ricoh.co.jp/projects/cotos_root/wiki/%E3%83%A1%E3%83%BC%E3%83%AB%E9%96%A2%E9%80%A3%E3%81%AE%E5%AE%9F%E8%A3%85%E3%81%AB%E3%81%A4%E3%81%84%E3%81%A6#CommonLibs%E3%81%8B%E3%82%89%E7%9B%B4%E6%8E%A5%E3%83%A1%E3%83%BC%E3%83%AB%E9%80%81%E4%BF%A1%E5%87%A6%E7%90%86%E3%82%92%E5%91%BC%E3%81%B6%E5%A0%B4%E5%90%88%E3%81%AE%E3%83%A1%E3%83%BC%E3%83%AB%E3%81%AE%E5%AE%9F%E8%A3%85%E3%81%AB%E3%81%A4%E3%81%84%E3%81%A6)を確認の上、本番ymlを設定しました。
 - [ ] メール送信処理について、jp.ricoh.comアドレス使用に関する[ルール](http://ex-redmine-1.cotos.ricoh.co.jp/projects/cotos_root/wiki/%E3%83%97%E3%83%AB%E3%83%AA%E3%82%AF%E3%83%AC%E3%83%93%E3%83%A5%E3%83%BC%E4%BE%9D%E9%A0%BC%E5%89%8D%E3%81%AE%E3%83%81%E3%82%A7%E3%83%83%E3%82%AF%E8%A6%B3%E7%82%B9#%E5%85%B1%E9%80%9A%E3%83%A1%E3%83%BC%E3%83%AB%E9%96%A2%E9%80%A3)に従っていることを確認しました。
 - [ ] 外部に連携する項目が将来的に桁数オーバーしないことを考慮しています。
