#!/bin/bash
################################################
### バッチID: BTCOSI009
### 機能名：  デバイス空欄警告メール送信
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI009.log"
PROCESS_LOG_FILE_PATH="${LOG_DIR}/SendDeviceBlankAlertMail.log"

Log.Info "BTCOSI009:デバイス空欄警告メール送信を開始します。" >> ${LOG_FILE_PATH}

################################################
### パラメーター設定
################################################

### 処理日
SERVICE_TERM_START=$1

Log.Info "処理日：${SERVICE_TERM_START}" >> ${LOG_FILE_PATH}

################################################
### 処理実行
################################################
SPRING_PROFILES_ACTIVE=${ENVIRONMENT_NAME} /usr/bin/java -Dlogging.file=${PROCESS_LOG_FILE_PATH} -jar ${COMMUNICATION_JAR_PATH}/${BATCH_PG_BTCOSI009} "${SERVICE_TERM_START}"
BATCH_RET=$?
if [  ${BATCH_RET} != 0 ]; then
  Log.Error "BTCOSI009:デバイス空欄警告メール送信に失敗しました。処理を終了します。" >> ${LOG_FILE_PATH};
  exit 1
fi

Log.Info "BTCOSI009:デバイス空欄警告メール送信が完了しました。" >> ${LOG_FILE_PATH}