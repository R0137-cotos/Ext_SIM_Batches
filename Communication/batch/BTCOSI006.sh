#!/bin/bash
################################################
### バッチID: BTCOSI006
### 機能名：  オーダーメール送信
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI006.log"

Log.Info "BTCOSI006:[SB]オーダーメール送信を開始します。" >> ${LOG_FILE_PATH}

################################################
### パラメーター設定
################################################
if [ $# -ne 4 ]; then
  Log.Error "実行するには4個の引数が必要です。処理を終了します。" >> ${LOG_FILE_PATH}
  exit 1
fi

### ディレクトリパス
DIR_PATH=$1
### ファイル名
FILE_NAME=$2
### 種別
TYPE=$3
### 件名
SUBJECT=$4

Log.Info "ディレクトリパス：${DIR_PATH}" >> ${LOG_FILE_PATH}
Log.Info "ファイル名：${FILE_NAME}" >> ${LOG_FILE_PATH}
Log.Info "種別：${TYPE}" >> ${LOG_FILE_PATH}
Log.Info "件名：${SUBJECT}" >> ${LOG_FILE_PATH}

################################################
### メール送信
################################################
FROM=zjc_rmobile_sb_order@jp.ricoh.com
TO=Tatsuya.Kamada1@jp.ricoh.com
CC=Tatsuya.Homma@jp.ricoh.com
BCC=tatsuya.kamada@jp.ricoh.com

Log.Info "  メール送信を開始します。" >> ${LOG_FILE_PATH}
${COMMON}/executeSendCsvMailCcBcc.sh ${FROM} ${TO} ${CC} ${BCC} ${SUBJECT} ${DIR_PATH} ${FILE_NAME}

Log.Info "BTCOSI006:[SB]オーダーメール送信が完了しました。" >> ${LOG_FILE_PATH}
