#!/bin/bash
################################################
### バッチID: BTCOSI010
### 機能名：  オーダー解約メール送信
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI010.log"

Log.Info "BTCOSI010:[SB]解約オーダーメール送信を開始します。" >> ${LOG_FILE_PATH}

################################################
### パラメーター設定
################################################
if [ $# -ne 2 ]; then
  Log.Error "実行するには2個の引数が必要です。処理を終了します。" >> ${LOG_FILE_PATH}
  exit 1
fi

### ファイルパス
FILE_PATH=$1
### 件名
SUBJECT=$2

Log.Info "ファイルパス：${FILE_PATH}" >> ${LOG_FILE_PATH}
Log.Info "件名：${SUBJECT}" >> ${LOG_FILE_PATH}

################################################
### メール送信
################################################
FROM=zjc_rmobile_sb_order@jp.ricoh.com
TO=SBBGRP-RJMobile@g.softbank.co.jp
CC=sbcs-kitting@fw.softbank.co.jp,SP_Biz-kanri@g.softbank.co.jp,zjc_rmobile_sb_order@jp.ricoh.com
BCC=zjp_cotos_apl_maintenance@jp.ricoh.com

${COMMON}/executeSendCsvMailCcBcc.sh ${FROM} ${TO} ${CC} ${BCC} ${SUBJECT} ${FILE_PATH}

Log.Info "BTCOSI010:[SB]解約オーダーメール送信が完了しました。" >> ${LOG_FILE_PATH}
