#!/bin/bash
################################################
### バッチID: BTCOSI003
### 機能名：  [SB]リプライCSV取込
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI003.log"
PROCESS_LOG_FILE_PATH="${LOG_DIR}/ImportReplyCsv.log"

Log.Info "BTCOSI003:[SB]リプライCSV取込を開始します。" >> ${LOG_FILE_PATH}

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
SPRING_PROFILES_ACTIVE=${ENVIRONMENT_NAME} /usr/bin/java -jar ${IFS_JAR_PATH}/${BATCH_PG_BTCOSI005} "${DIR_PATH}" "${FILE_NAME}" > ${PROCESS_LOG_FILE_PATH}

if [  $? != 0 ]; then
  Log.Error "BTCOSI003:[SB]リプライCSV取込に失敗しました。処理を終了します。" >> ${LOG_FILE_PATH};
  exit 1
fi

Log.Info "BTCOSI003:[SB]リプライCSV取込が完了しました。" >> ${LOG_FILE_PATH}