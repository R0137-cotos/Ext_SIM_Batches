#!/bin/bash
################################################
### バッチID: BTCOSI004
### 機能名：  計上データ作成（SIMランニング分）
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI004.log"
PROCESS_LOG_FILE_PATH="${LOG_DIR}/AccountingCreateSimRunnging.log"

Log.Info "BTCOSI004:計上データ作成（SIMランニング分）を開始します。" >> ${LOG_FILE_PATH}

################################################
### パラメーター設定
################################################
if [ $# -ne 1 ]; then
  Log.Error "実行するには1個の引数が必要です。処理を終了します。" >> ${LOG_FILE_PATH}
  exit 1
fi

### 処理年月日[yyyymmdd]
EXECUTE_DATE=$1

Log.Info "処理年月日[yyyymmdd]：${EXECUTE_DATE}" >> ${LOG_FILE_PATH}

################################################
### 処理実行
################################################

SPRING_PROFILES_ACTIVE=${ENVIRONMENT_NAME} /usr/bin/java -jar ${ACCOUNTING_JAR_PATH}/${BATCH_PG_BTCOSI004} "${EXECUTE_DATE}" > ${PROCESS_LOG_FILE_PATH}

if [  $? != 0 ]; then
  Log.Error "BTCOSI004:計上データ作成（SIMランニング分）に失敗しました。処理を終了します。" >> ${LOG_FILE_PATH}
  exit 1
fi


Log.Info "BTCOSI004:計上データ作成（SIMランニング分）が完了しました。" >> ${LOG_FILE_PATH}