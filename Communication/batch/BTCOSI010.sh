#!/bin/sh
################################################
### バッチID: BTCOSI010
### 機能名：  オーダー解約メール送信
################################################
{
echo "HELO jp.ricoh.com"
echo "MAIL FROM: zjc_rmobile_sb_order@jp.ricoh.com"
echo "RCPT TO: SBBGRP-RJMobile@g.softbank.co.jp"
echo "RCPT TO: sbcs-kitting@fw.softbank.co.jp"
echo "RCPT TO: SP_Biz-kanri@g.softbank.co.jp"
echo "RCPT TO: shuu_nishikawa@jp.ricoh.com"
echo "RCPT TO: masatoshi_kubo@jp.ricoh.com"
echo "DATA"
echo "From: zjc_rmobile_sb_order@jp.ricoh.com"
echo "To: SBBGRP-RJMobile@g.softbank.co.jp"
echo "CC: sbcs-kitting@fw.softbank.co.jp;SP_Biz-kanri@g.softbank.co.jp;shuu_nishikawa@jp.ricoh.com;masatoshi_kubo@jp.ricoh.com;"
echo "Subject: =?UTF-8?B?POaWsOimjz7jgJDjg6rjgrPjg7zjg6Ljg5DjgqTjg6soU0lNKeOAkQ==?=`date "+%Y/%m/%d"`"
echo "Content-Type: multipart/mixed; boundary=\"1234\""
echo "Content-Transfer-Encoding: base64"
echo "MIME-Version: 1.0"
echo ""
echo "--1234"
echo "Content-Type: text/plain; charset=UTF-8"
echo ""
echo ""
echo "--1234"
echo "Content-Type: text/plain; name=`date "+%Y%m%d"`_SIM_kaiyaku.zip"
echo "Content-Transfer-Encoding: base64"
echo "Content-Disposition: attachment; filename=`date "+%Y%m%d"`_SIM_kaiyaku.zip"
echo ""
cat $1 | base64
echo ""
echo "--1234--"
echo "."
echo "QUIT"
} | python -m telnetlib 10.237.225.216 25

