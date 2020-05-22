#!/bin/bash
################################################
### バッチID: BTCOSI001
### 機能名：  [SB]オーダーCSV作成
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI001.log"
PROCESS_LOG_FILE_PATH="${LOG_DIR}/CreateOrderCsv.log"

Log.Info "BTCOSI001:[SB]オーダーCSV作成を開始します。" >> ${LOG_FILE_PATH}

################################################
### パラメーター設定
################################################
if [ $# -ne 4 ]; then
  Log.Error "実行するには4個の引数が必要です。処理を終了します。" >> ${LOG_FILE_PATH}
  exit 1
fi

### 処理日
OPERATION_DATE=$1
### ディレクトリパス
DIR_PATH=$2
### ファイル名
FILE_NAME=$3
### 種別
TYPE=$4

Log.Info "処理日：${OPERATION_DATE}" >> ${LOG_FILE_PATH}
Log.Info "ディレクトリパス：${DIR_PATH}" >> ${LOG_FILE_PATH}
Log.Info "ファイル名：${FILE_NAME}" >> ${LOG_FILE_PATH}
Log.Info "種別：${TYPE}" >> ${LOG_FILE_PATH}

################################################
### 処理実行
################################################
SPRING_PROFILES_ACTIVE=${ENVIRONMENT_NAME} /usr/bin/java -jar ${ORDER_JAR_PATH}/${BATCH_PG_BTCOSI001} "${OPERATION_DATE}" "${DIR_PATH}" "${FILE_NAME}" "${TYPE}" > ${PROCESS_LOG_FILE_PATH}

if [  $? != 0 ]; then
  Log.Error "BTCOSI001:[SB]オーダーCSV作成に失敗しました。処理を終了します。" >> ${LOG_FILE_PATH};
  exit 1
fi

Log.Info "BTCOSI001:[SB]オーダーCSV作成が完了しました。" >> ${LOG_FILE_PATH}