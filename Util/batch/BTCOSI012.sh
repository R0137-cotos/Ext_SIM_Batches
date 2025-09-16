#!/bin/bash
################################################
### バッチID: BTCOSI012
### 機能名：  [SB]営業日判定
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI012.log"
PROCESS_LOG_FILE_PATH="${LOG_DIR}/IsBusinessDay.log"

Log.Info "BTCOSI012:[SB]営業日判定を開始します。" >> ${LOG_FILE_PATH}

################################################
### パラメーター設定
################################################
if [ $# -ne 1 ]; then
  Log.Error "実行するには1個の引数が必要です。処理を終了します。" >> ${LOG_FILE_PATH}
  exit 1
fi

### 処理日
PROCESS_DATE=$1

Log.Info "処理日：${PROCESS_DATE}" >> ${LOG_FILE_PATH}

################################################
### 処理実行
################################################
SPRING_PROFILES_ACTIVE=${ENVIRONMENT_NAME} /usr/bin/java -Dlogging.file.name=${PROCESS_LOG_FILE_PATH} -jar ${UTIL_JAR_PATH}/${BATCH_PG_BTCOSI012} "${PROCESS_DATE}"
BATCH_RET=$?
if [  ${BATCH_RET} == 1 ]; then
  Log.Error "BTCOSI012:営業日判定に失敗しました。処理を終了します。" >> ${LOG_FILE_PATH};
  exit 1
elif [  ${BATCH_RET} == 2 ]; then
  Log.Info "BTCOSI012:営業日ではありませんでした。" >> ${LOG_FILE_PATH};
  exit 2
fi

Log.Info "BTCOSI012:[SB]営業日判定が完了しました。" >> ${LOG_FILE_PATH}