#!/bin/bash
################################################
### バッチID: BTCOSI002
### 機能名：  [SB]オーダーメール送信
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI002.log"
PROCESS_LOG_FILE_PATH="${LOG_DIR}/SendOrderMail.log"

Log.Info "BTCOSI002:[SB]オーダーメール送信を開始します。" >> ${LOG_FILE_PATH}

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
### 商品グループマスタID
PRODUCT_GRP_MASTER_ID=$3
### 宛先メールアドレスリスト
MAIL_ADDRESS_LIST=$4

Log.Info "ディレクトリパス：${DIR_PATH}" >> ${LOG_FILE_PATH}
Log.Info "ファイル名：${FILE_NAME}" >> ${LOG_FILE_PATH}
Log.Info "商品グループマスタID：${PRODUCT_GRP_MASTER_ID}" >> ${LOG_FILE_PATH}
Log.Info "宛先メールアドレスリスト：${MAIL_ADDRESS_LIST}" >> ${LOG_FILE_PATH}

################################################
### 処理実行
################################################
SPRING_PROFILES_ACTIVE=${ENVIRONMENT_NAME} /usr/bin/java -jar ${COMMUNICATION_JAR_PATH}/${BATCH_PG_BTCOSI002} "${DIR_PATH}" "${FILE_NAME}" "${PRODUCT_GRP_MASTER_ID}" "${MAIL_ADDRESS_LIST}" > ${PROCESS_LOG_FILE_PATH}
BATCH_RET=$?
if [  ${BATCH_RET} != 0 ]; then
  Log.Error "BTCOSI002:[SB]オーダーメール送信に失敗しました。処理を終了します。" >> ${LOG_FILE_PATH};
  exit 1
fi

Log.Info "BTCOSI002:[SB]オーダーメール送信が完了しました。" >> ${LOG_FILE_PATH}