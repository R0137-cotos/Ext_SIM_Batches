#!/bin/bash
################################################
### バッチID: BTCOSI008
### 機能名：  [SB]リプライCSV取込解約
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI008.log"
PROCESS_LOG_FILE_PATH="${LOG_DIR}/ImportCancelReplyCsv.log"

Log.Info "BTCOSI008:[SB]リプライCSV取込解約を開始します。" >> ${LOG_FILE_PATH}

################################################
### パラメーター設定
################################################
if [ $# -ne 2 ]; then
  Log.Error "実行するには2個の引数が必要です。処理を終了します。" >> ${LOG_FILE_PATH}
  exit 1
fi

### ディレクトリパス
DIR_PATH=$1
### ファイル名
FILE_NAME=$2

Log.Info "ディレクトリパス：${DIR_PATH}" >> ${LOG_FILE_PATH}
Log.Info "ファイル名：${FILE_NAME}" >> ${LOG_FILE_PATH}

################################################
### 処理実行
################################################
SPRING_PROFILES_ACTIVE=${ENVIRONMENT_NAME} /usr/bin/java -Dlogging.file=${PROCESS_LOG_FILE_PATH} -jar ${ORDER_JAR_PATH}/${BATCH_PG_BTCOSI008} "${DIR_PATH}" "${FILE_NAME}" > /dev/null 2>&1
BATCH_RET=$?
if [  ${BATCH_RET} != 0 ]; then
  Log.Error "BTCOSI008:[SB]リプライCSV取込解約に失敗しました。処理を終了します。" >> ${LOG_FILE_PATH};
  exit 1
fi

Log.Info "BTCOSI008:[SB]リプライCSV取込解約が完了しました。" >> ${LOG_FILE_PATH}