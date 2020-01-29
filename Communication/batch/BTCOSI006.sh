#!/bin/sh
################################################
### バッチID: BTCOSI006
### 機能名：  オーダーメール送信
################################################

{
echo "HELO jp.ricoh.com"
echo "MAIL FROM: bounce@cotos.ricoh.co.jp"
echo "RCPT TO: fumihiko.komura@g.softbank.co.jp"
echo "RCPT TO: masatoshi_kubo@jp.ricoh.com"
echo "DATA"
echo "From: bounce@cotos.ricoh.co.jp"
echo "To: fumihiko.komura@g.softbank.co.jp"
echo "CC: masatoshi_kubo@jp.ricoh.com"
echo "Subject: =?UTF-8?B?POaWsOimjz7jgJDjg6rjgrPjg7zjg6Ljg5DjgqTjg6soU0lNKeOAkQ==?=`date "+%Y/%m/%d"`"
echo "Content-Type: multipart/mixed; boundary=\"1234\""
echo "Content-Transfer-Encoding: 7bit"
echo "MIME-Version: 1.0"
echo ""
echo "--1234"
echo "Content-Type: text/plain; charset=UTF-8"
echo ""
echo ""
echo "--1234"
echo "Content-Type: text/plain; name=`date "+%Y%m%d"`(SIM)新規.csv"
echo "Content-Disposition: attachment; filename=`date "+%Y%m%d"`(SIM)新規.csv"
echo ""
cat /sharestorage/COTOS2SIM.csv
echo ""
echo "--1234--"
echo "."
echo "QUIT"
} | python -m telnetlib 10.237.225.216 25

