#!/bin/bash
################################################
### バッチID: BTCOSI007
### 機能名：  [SB]解約手配CSV作成
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI007.log"
PROCESS_LOG_FILE_PATH="${LOG_DIR}/CreateCancelOrderCsv.log"

Log.Info "BTCOSI007:[SB]解約手配CSV作成を開始します。" >> ${LOG_FILE_PATH}

################################################
### パラメーター設定
################################################
if [ $# -ne 3 ]; then
  Log.Error "実行するには3個の引数が必要です。処理を終了します。" >> ${LOG_FILE_PATH}
  exit 1
fi

### 処理日(yyyyMMdd)
OPERATION_DATE=$1
### ディレクトリパス
DIR_PATH=$2
### ファイル名(拡張子まで含む 例:test.csv)
FILE_NAME=$3

Log.Info "処理日：${OPERATION_DATE}" >> ${LOG_FILE_PATH}
Log.Info "ディレクトリパス：${DIR_PATH}" >> ${LOG_FILE_PATH}
Log.Info "ファイル名：${FILE_NAME}" >> ${LOG_FILE_PATH}

################################################
### 処理実行
################################################
SPRING_PROFILES_ACTIVE=${ENVIRONMENT_NAME} /usr/bin/java -Dlogging.file=${PROCESS_LOG_FILE_PATH} -jar ${ORDER_JAR_PATH}/${BATCH_PG_BTCOSI007} "${OPERATION_DATE}" "${DIR_PATH}" "${FILE_NAME}"
BATCH_RET=$?
if [  ${BATCH_RET} == 1 ]; then
  Log.Error "BTCOSI007:[SB]解約手配CSV作成に失敗しました。処理を終了します。" >> ${LOG_FILE_PATH};
  exit 1
elif [  ${BATCH_RET} == 2 ]; then
  Log.Info "BTCOSI007:処理対象日付ではありませんでした。解約手配CSV作成処理は「月末営業日-2営業日」のみ処理を実行します。" >> ${LOG_FILE_PATH};
fi

Log.Info "BTCOSI007:[SB]解約手配CSV作成が完了しました。" >> ${LOG_FILE_PATH}