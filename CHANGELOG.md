# Changelog

## v7.2.0 (07/09/2023)
- [**closed**] 20053 CommonLibsバージョン修正 [#305](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/305)
- [**closed**] #19680 月額計上データ作成バッチ振替情報取得SQL修正 [#304](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/304)

---

## v6.13.0 (23/05/2023)
- [**closed**] #18243免税対応 5/23リリース暫定対応 [#301](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/301)

---

## v6.11.1 (02/05/2023)
- [**closed**] #18519 oracle19cへ移行に伴うクエリ修正 [#299](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/299)

---

## v6.8.0 (09/03/2023)
- [**closed**] #18064 データSIMリプライ未配置ご連絡メールのfrom修正 [#293](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/293)

---

## v6.6.0 (07/02/2023)
- [**closed**] #17450 実装納品書出力要否の区分をNULLにする [#290](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/290)

---

## v6.2.0 (08/12/2022)
- [**closed**] #16618 SQL更新時更新者、更新日時設定 追加修正 [#287](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/287)
- [**closed**] #16618 SQL更新時更新者、更新日時設定 [#286](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/286)

---

## v5.7.0 (06/09/2022)
- [**closed**] #15721_CommonLibsのバージョンアップ [#284](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/284)

---

## v5.3.0 (23/06/2022)
- [**closed**] #15386 [本番障害]計上データ作成で振替が作成されない [#280](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/280)

---

## v5.2.0 (09/06/2022)
- [**closed**] #14542 リプライCSV取込バッチ例外出力修正 [#274](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/274)

---

## v5.0.0 (20/05/2022)
- [**closed**] #14877 APIを呼んでいるバッチのCommonLibsバージョンアップ対応 [#275](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/275)

---

## v4.13.0 (21/02/2022)
- [**closed**] #13763 デバイス空欄警告メール送信で送信エラー時に未送信メールもエラーのステータスになる [#271](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/271)

---

## v4.5.0 (04/11/2021)
- [**closed**] #12779 [本番改修]SIMのCC送信メールアドレス追加　追加修正 [#269](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/269)
- [**closed**] #12779 [本番改修]SIMのCC送信メールアドレス追加 [#268](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/268)
- [**closed**] #12558 軽量バッチのbean.xml, orm.xmlの設定不正 [#266](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/266)
- [**closed**] #12350 ExportCSV.csvの機種コードを修正 [#264](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/264)
- [**closed**] #12589 解約手配CSV作成バッチ 音声SIM分が出力されないよう修正 [#263](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/263)
- [**closed**] #12589 [改善]バッチ異常終了検知対応 デバイス空欄メール送信、解約手配CSV作成 [#259](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/259)
- [**closed**] #12589 [改善]バッチ異常終了検知対応 オーダーCSV作成、解約手配CSV作成 [#260](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/260)
- [**closed**] #12350 SIM IFS（Cforce）連携への連携情報変更 [#262](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/262)

---

## v4.4.0 (04/11/2021)
- [**closed**] #12387 [改善]解約リプライCSVで存在しない契約を指定しても正常終了して処理される [#251](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/251)
- [**closed**] #12387[改善]解約リプライCSVで存在しない契約を指定しても正常終了して処理される [#248](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/248)
- [**closed**] #12468 [改善]バッチ異常終了検知対応 計上データ作成バッチ [#246](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/246)

---

## v4.3.0 (21/09/2021)
- [**closed**] #12150 手配情報更新処理に失敗した場合、異常終了する処理の追加 [#243](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/243)
- [**closed**] #12150[改善]解約リプライCSVで一部異常終了しても正常終了して処理される [#240](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/240)
- [**closed**] #12151 [改善]リプライCSVで存在しない契約を指定しても正常終了して処理される(ログには表示される) [#241](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/241)

---

## v4.1.0 (24/08/2021)
- [**closed**] #12079 [本番障害]オーダーCSV送信時に手入力された郵便番号と住所が連携されていない [#233](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/233)
- [**closed**] #11494 全解約時に抽出するデータ条件の追加 [#232](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/232)
- [**closed**] #7974 FFM出力コメント１の内容を修正 [#231](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/231)

---

## v4.0.0 (30/07/2021)
- [**closed**] #10819 案件番号項目にRJ管理番号を入れる [#220](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/220)

---

## v3.22.0 (10/06/2021)
- [**closed**] #11413 [改善]SIMメール(新規・解約)送信方法修正 [#226](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/226)

---

## v3.21.0 (24/05/2021)
- [**closed**] #11296 [本番障害]SIMメールサーバーIP変更 [#221](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/221)

---

## v3.20.0 (10/05/2021)
- [**closed**] #10010 [改善]SIMメール送信処理でテスト環境実行時にCOTOS開発メンバー以外にメールが飛ばないようにする [#215](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/215)

---

## v3.19.1 (19/04/2021)
- [**closed**] #11004 CommonLibs最新化 [#217](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/217)

---

## v3.19.0 (14/04/2021)
- [**closed**] #10737 [改善]SIMオーダーメール送信BCC変更 [#213](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/213)

---

## v3.16.0 (08/03/2021)
- [**closed**] #9976 SIMランニングSQLのパフォーマンス改善 [#210](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/210)
- [**closed**] #9868 課金開始日を考慮するようにSQL修正 [#209](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/209)

---

## v3.15.0 (04/02/2021)
- [**closed**] #8276 郵便番号に応じて最短納期日数を修正CSV読み取り [#203](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/203)
- [**closed**] #9697 SIMメール失敗時ログ出力 追加修正 [#201](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/201)
- [**closed**] #9697 SIMメール失敗時ログ出力 [#200](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/200)
- [**closed**] #9312 計上データ作成バッチ(SIM)で作成データパターンが振替の場合に設定する値を追加 [#195](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/195)

---

## v3.12.0 (17/12/2020)
- [**closed**] #8494 メールヘッダーに設定する項目を変更 [#191](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/191)
- [**closed**] #8494 バウンスメール対応 [#181](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/181)

---

## v3.11.0 (03/12/2020)
- [**closed**] #9382 [本番障害]リプライ取込バッチ_納品予定日フォーマットエラー エラーメッセージ修正 [#188](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/188)
- [**closed**] #9382 [本番障害]リプライ取込バッチ_納品予定日フォーマットエラー [#187](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/187)

---

## v3.10.0 (19/11/2020)
- [**closed**] #9149 [本番改善]SIMランニング計上バッチ対象契約条件修正 [#183](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/183)
- [**closed**] #9128 APIのエラーを検知するように修正 [#182](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/182)

---

## v3.9.0 (05/11/2020)
- [**closed**] #8965 バッチ処理エラーをslackに通知するように修正 [#178](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/178)

---

## v3.8.0 (15/10/2020)
- [**closed**] #8469 営業日判定バッチ シェル名称変更 [#172](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/172)
- [**closed**] #8469 営業日判定バッチ シェル名称変更 [#171](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/171)
- [**closed**] #8469 営業日判定バッチ実装 [#170](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/170)
- [**closed**] #6065 オーダーCSV作成 業務区登記簿コピー手配の取得方法を修正 [#163](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/163)
- [**closed**] #6065 オーダーCSV作成 業務区登記簿コピー添付の手配以外にのみ担当者登録APIと手配業務受付APIを実施するよう修正 [#161](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/161)
- [**closed**] #6065 リプライCSV取込 手配情報更新が既存の「リコー モバイル 通信サービス」手配のみ更新するように修正 [#160](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/160)
- [**closed**] #6065 オーダーCSV作成 新規契約の場合、業務区登記簿コピー添付が作業完了になっているもののみを対象とする [#159](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/159)

---

## v3.7.0 (01/10/2020)
- [**closed**] #8266 標準出力を行わないよう修正,SQLログを本番以外でのみ出すよう修正 [#158](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/158)
- [**closed**] #7955 新規オーダーCSV作成 ベンダーの非営業日マスタを使った制御を行う対応 [#153](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/153)

---

## v3.6.0 (17/09/2020)
- [**closed**] #8137 同一のログが出力される箇所修正 [#152](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/152)
- [**closed**] #8137 APIをコールするバッチのログ表示内容改修 [#151](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/151)

---

## v3.5.0 (03/09/2020)
- [**closed**] #7933 デバイス空欄メール送信履歴登録処理追加 [#148](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/148)

---

## v3.4.0 (20/08/2020)
- [**closed**] #7946 締結中のみ取得するようにSQLを修正 [#145](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/145)
- [**closed**] #7676 リプライCSV未配置通知メール送信シェル作成 [#143](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/143)
- [**closed**] #7780 IFSその他機器情報CSV作成 全解約分もCSV作成 SQLの不備を修正 [#141](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/141)
- [**closed**] #7780 IFSその他機器情報CSV作成 全解約分もCSV作成 [#139](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/139)

---

## v3.3.0 (21/07/2020)
- [**closed**] #7583 リプライCSV取込 CSVに納入予定日(サービス開始日)が入っていない状態でリプライCSV取込を実施した際にエラーログを出力する [#137](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/137)
- [**closed**] #7519 [本番障害]デバイス空欄メールが送信されない [#136](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/136)
- [**closed**] #7583 リプライCSV取込 各契約一番上の行に納入予定日が無い場合、該当契約を取込対象外にする [#135](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/135)
- [**closed**] #7639 オーダーCSV作成 容量変更オーダーCSV作成を月末営業日-2営業日のみ実行にする [#131](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/131)
- [**closed**]  #7519 解約手配CSV作成/デバイス空欄警告メール 合計数量16を超える契約も対象データとして検知可能にする修正 [#130](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/130)
- [**closed**] #7539 [本番障害]解約オーダーCSVメール送信時にzip圧縮しない [#129](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/129)

---

## v3.2.2 (13/07/2020)
- [**closed**] #7531 IFSその他機器情報CSV作成 対象データ取得SQL 拡張項目繰返の文字列長が長い場合も判定可能にする修正 [#127](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/127)
- [**closed**] #7517 オーダーCSV作成 対象データ取得SQL 拡張項目繰返の文字列長が長い場合も判定可能にする修正 [#126](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/126)
- [**closed**] #7538 [本番障害]オーダーCSVメール送信時にzip圧縮しない [#125](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/125)
- [**closed**] #7487 オーダーメール送信 BCCに開発メンバアドレスを追加 [#124](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/124)
- [**closed**] #7481 解約手配CSV作成シェル 処理実行対象日以外の場合、戻り値として異常終了を返すよう修正 [#123](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/123)

---

## v3.2.0 (02/07/2020)
- [**closed**] #7091 logback追加 [#121](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/121)
- [**closed**] #7091 バッチのログが上書きされずにローテーションするように修正 [#120](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/120)
- [**closed**] #7315 容量変更、有償交換を新規として取り込まないよう修正 [#118](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/118)
- [**closed**] #7316 オーダーメール送信シェル 件名を引数で受け取るように修正 [#119](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/119)
- [**closed**] #7303 オーダーメール送信シェル 添付ファイルの拡張子を修正 [#117](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/117)
- [**closed**] #7303 オーダーメール送信処理 引数の順番を修正 [#116](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/116)
- [**closed**] #7303 メール送信シェル 添付ファイル内容の指定を引数指定に変更 [#115](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/115)
- [**closed**] #7239 リプライCSV取込デバイス空欄対応 [#114](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/114)
- [**closed**] #7239  マッピングに商材固有項目デバイス追加 [#109](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/109)
- [**closed**] #7234 オーダーCSVメール送信　添付ファイルを修正 [#108](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/108)
- [**closed**]  #7234 SIM解約手配メール送信バッチ 添付ファイルの指定を修正 [#107](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/107)
- [**closed**] #6636 デバイス空欄メール メール内容修正 [#106](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/106)
- [**closed**] #7182 SIM解約手配CSV作成 数量減の場合に明細が無いケースが存在するため、拡張項目繰返を基にCSV作成するよう修正 [#104](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/104)
- [**closed**] #7177 SIMリプライCSV取込解約 部分解約の場合サービス利用希望日の翌月1日をサービス開始日に設定する [#102](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/102)
- [**closed**] #7167 [IT1]IFSその他機器情報CSV作成バッチ（BTCOSI005.sh）条件間違い [#101](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/101)
- [**closed**] #7067 SIMリプライCSV取込解約 解約対象契約取得SQLを修正 [#100](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/100)
- [**closed**] #7067 SIMリプライCSV取込解約 数量減の場合に、手配情報更新→契約情報更新の順にAPIを呼ぶように変更 [#99](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/99)
- [**closed**] #7150 [IT1][SB]リプライCSV取込解約バッチ（BTCOSI008.sh）一部解約時手配が進まない [#97](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/97)
- [**closed**] #7068 [IT1]リプライCSV取込で回線番号が反映されない [#96](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/96)
- [**closed**] #7051 [IT1]全解約のリプライ取込で手配のステータスが完了にならない [#95](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/95)
- [**closed**] #7068 [IT1]容量変更、有償交換のリプライ取込で商材固有項目から「新規」の情報が消える [#94](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/94)
- [**closed**] #7051 [IT1]全解約のリプライ取込で手配のステータスが完了にならない [#92](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/92)
- [**closed**] #7126 [IT1]デバイス空欄警告メール（BTCOSI009）パラメータがログに出ない [#93](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/93)
- [**closed**] #7051 [IT1]全解約のリプライ取込で手配のステータスが完了にならない [#90](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/90)
- [**closed**] #7068 [IT1]容量変更、有償交換のリプライ取込で商材固有項目から「新規」の情報が消える [#91](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/91)
- [**closed**] #7067 [IT1][SB]リプライCSV取込解約バッチ（BTCOSI008.sh）一部解約時エラー [#88](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/88)
- [**closed**] #7084 [IT1]デバイス空欄警告メールパラメータ未設定時初期値修正 [#86](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/86)
- [**closed**] #7085 [IT1]デバイス空欄警告メール（BTCOSI009）パラメータ無しでエラーが発生してしまう [#89](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/89)
- [**closed**] #7068 SIMリプライCSV取込 リプライCSVに記載されていない拡張項目繰返について保持するように修正 [#87](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/87)
- [**closed**] #7051 SIMリプライCSV取込解約 全解約データの抽出条件を修正 [#85](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/85)
- [**closed**] #7062 SIMオーダーCSV作成 オーダーCSV行作成処理修正 [#84](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/84)
- [**closed**] #7019 SIMオーダーCSV作成 CSV行データ作成時にnullチェックを追加 [#83](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/83)
- [**closed**] #7051 SIMリプライCSV取込解約バッチ 全解約データの取得条件をライフサイクル状態=解約手続き中 ⇒ ライフライクル状態=解約予定… [#82](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/82)
- [**closed**] #7019 SIMオーダーCSV作成 容量変更・有償交換の場合に拡張項目繰返の情報をオーダーCSVに載せる [#81](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/81)
- [**closed**] #6727 [バッチ実装]オーダーCSV作成で恒久契約識別番号を利用している [#80](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/80)
- [**closed**] #6728 [バッチ実装]解約手配CSV作成に契約番号枝番を追加する [#79](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/79)
- [**closed**] #6977 解約手配CSV作成 全解約分の取得条件を解約手続き中 ⇒ 解約予定日待ちに修正 [#78](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/78)
- [**closed**]  #6976 SIMオーダーCSV作成 容量変更の場合、処理日当月最終営業日 - 2営業日にオーダーCSV出力するように修正 [#77](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/77)
- [**closed**] #6977 解約手配CSV作成 解約申込日、申込日によるデータ取得条件を削除 [#76](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/76)
- [**closed**] #6991 SIMオーダーCSV作成バッチ データ取得条件にライフサイクル状態を追加 [#75](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/75)
- [**closed**] #6977 解約手配CSV作成 全解約分の取得条件修正 [#74](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/74)
- [**closed**] #6976 オーダーCSV作成 容量変更の場合のデータ取得条件から更新日時を削除 [#73](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/73)
- [**closed**] #6964 解約手配CSV作成バッチ 業務カレンダーマスタ参照を非営業日カレンダーマスタ参照に修正 [#72](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/72)
- [**closed**] #6962 オーダーCSV作成バッチ 業務カレンダーマスタ参照を非営業日カレンダーマスタに修正 [#71](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/71)
- [**closed**] #6951 [IT0]リプライCSV取込のバッチ実行でエラー発生 [#70](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/70)
- [**closed**] #6910 [IT0]リプライCSV取込のバッチ実行でエラー発生 [#69](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/69)
- [**closed**] #6910 [IT0]リプライCSV取込のバッチ実行でエラー発生 [#68](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/68)
- [**closed**] #6905 [IT0]オーダーCSV作成のバッチ実行でエラー発生 [#67](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/67)
- [**closed**] #6896 [IT0]オーダーCSV作成のバッチ実行で引数エラーになる [#65](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/65)
- [**closed**] #6602 オーダーCSV作成 容量変更・有償交換、CSVデバイス追加 [#56](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/56)
- [**closed**] #6622 オーダー解約CSVメール送信バッチ作成 [#61](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/61)
- [**closed**] #6712 リプライCSV取込容量変更・有償交換追加 [#60](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/60)
- [**closed**] #6673 リプライCSV取込解約 シェル修正 [#59](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/59)
- [**closed**] #6673 [バッチ実装]リプライCSV取込解約 [#58](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/58)
- [**closed**] #6674 IFSその他機器情報CSV作成バッチ 容量変更、有償交換追加 [#57](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/57)
- [**closed**] #6589 解約手配CSV作成バッチ 実装 [#55](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/55)
- [**closed**]  #6536 デバイス空欄警告メール作成 [#54](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/54)

---

## v2.0.20 (21/05/2020)
- [**closed**] #6792 SIMランニング振替を回線単位にする [#62](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/62)

---

## v2.0.10 (06/03/2020)
- [**closed**] #5945 オーダーメール送信シェル修正 [#51](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/51)

---

## v2.0.9 (03/03/2020)
- [**closed**] #5373 [SIMベンダー]オーダーCSV作成 メール送信シェル追加 [#23](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/23)
- [**closed**] 本番環境設定漏れ修正 [#49](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/49)
- [**closed**] 値がセットされるカラムを変更 [#47](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/47)
- [**closed**] #5755 オーダーメール送信 本番用アドレス設定 [#45](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/45)
- [**closed**] 祝日対応、手配担当作業者変更 [#43](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/43)
- [**closed**] #5524 計上データ作成（SIMランニング分）バッチ CSP修正分取込 [#39](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/39)
- [**closed**] #5422 リプライCSV取込 拡張項目繰返をID昇順にソートして更新 [#37](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/37)
- [**closed**] #5507 オーダーメール送信 送信元アドレス変更 [#36](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/36)
- [**closed**] #5359 [RUCCS]IFSその他機器情報CSV作成 起動シェル実装 [#33](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/33)
- [**closed**] #5355 計上データ作成シェル修正 [#32](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/32)
- [**closed**] st-3 ymlファイル手配API URL追加 [#31](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/31)
- [**closed**] オーダーCSV作成バッチ（出力CSVのシリアル番号の文言修正） [#29](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/29)
- [**closed**]  日付障害対応、担当者ワークフロー状態対応 [#27](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/27)
- [**closed**] オーダーCSV作成障害対応 [#25](https://mygithub.ritscm.xyz/cotos/Ext_SIM_Batches/pull/25)
