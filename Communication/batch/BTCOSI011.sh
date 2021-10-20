#!/bin/sh
################################################
### バッチID: BTCOSI011
### 機能名：  リプライCSV未配置通知メール送信
################################################
{
echo "HELO jp.ricoh.com"
echo "MAIL FROM: zjc_rmobile_sb_order@jp.ricoh.com"
echo "RCPT TO: zjc_simgyomu@jp.ricoh.com"
echo "RCPT TO: zjc_rmobile_sb_order@jp.ricoh.com"
echo "RCPT TO: zjp_cotos_apl_maintenance@jp.ricoh.com"
echo "DATA"
echo "From: zjc_rmobile_sb_order@jp.ricoh.com"
echo "TO: zjc_simgyomu@jp.ricoh.com"
echo "CC: zjc_rmobile_sb_order@jp.ricoh.com"
echo "BCC: zjp_cotos_apl_maintenance@jp.ricoh.com"
echo "Subject: =?UTF-8?B?5paw6KaP5aWR57SE44Oq44OX44Op44KkQ1NW5pyq6YWN572u6YCa55+l==?=`date "+%Y/%m/%d"`"
echo "Content-Type: text/plain; charset=UTF-8"
echo "Content-Transfer-Encoding: base64"
echo "MIME-Version: 1.0"
echo ""
echo "5paw6KaP5aWR57SE44Gu44Oq44OX44Op44KkQ1NW44GM5omA5a6a44Gu44OV44Kp44Or44OA44Gr6YWN572u44GV44KM44Gm44GE44G+44Gb44KT44CC44Oq44OX44Op44KkQ1NW5Y+W6L6844Gu44OQ44OD44OB5a6f6KGM5pmC5Yi7KDE3OjIwKeOBvuOBp+OBq+mFjee9ruOBl+OBpuOBj+OBoOOBleOBhOOAgg0K44G+44Gf44CB5pys5pel5YiG44Gu5paw6KaP5aWR57SE44Gu44Oq44OX44Op44KkQ1NW44GM54Sh44GE5aC05ZCI44Gv44CB44OA44Of44O844OV44Kh44Kk44OrKOaXpeS7mF9TSU1fc2lua2lfcmVwbHlfZHVtbXkuY3N2KeOCkumFjee9ruOBl+OBpuOBj+OBoOOBleOBhOOAgg=="
echo ""
echo "."
echo "QUIT"
} | python -m telnetlib 10.236.225.216 25

