#!/bin/bash
################################################
### バッチID: BTCOSI005
### 機能名：  [RUCCS]IFSその他機器情報CSV作成
################################################

# 共通設定シェルが2階層上のディレクトリに存在することを前提とする
BATCH_BASE_PATH=$(cd $(dirname $0);cd ../..;pwd)
source ${BATCH_BASE_PATH}/config.sh

# ログファイルパス
LOG_FILE_PATH="${LOG_DIR}/BTCOSI005.log"
PROCESS_LOG_FILE_PATH="${LOG_DIR}/CreateCsvIFS.log"

Log.Info "BTCOSI005:[RUCCS]IFSその他機器情報CSV作成を開始します。" >> ${LOG_FILE_PATH}

################################################
### パラメーター設定
################################################
if [ $# -ne 3 ]; then
  Log.Error "実行するには3個の引数が必要です。処理を終了します。" >> ${LOG_FILE_PATH}
  exit 1
fi

### ファイル名
FILE_NAME=$1
### 出力先ディレクトリパス
DIR_PATH=$2
### 商品種類区分
PRODUCT_CLASS_DIV=$3

Log.Info "ファイル名：${FILE_NAME}" >> ${LOG_FILE_PATH}
Log.Info "出力先ディレクトリパス：${DIR_PATH}" >> ${LOG_FILE_PATH}
Log.Info "商品種類区分：${PRODUCT_CLASS_DIV}" >> ${LOG_FILE_PATH}

################################################
### ディレクトリ作成
################################################
mkdir -p ${DIR_PATH}

################################################
### 処理実行
################################################
SPRING_PROFILES_ACTIVE=${ENVIRONMENT_NAME} /usr/bin/java -Dlogging.file.name=${PROCESS_LOG_FILE_PATH} -jar ${CONTRACT_JAR_PATH}/${BATCH_PG_BTCOSI005} "${FILE_NAME}" "${DIR_PATH}" "${PRODUCT_CLASS_DIV}" > /dev/null 2>&1
BATCH_RET=$?
if [  ${BATCH_RET} != 0 ]; then
  Log.Error "BTCOSI005:[RUCCS]IFSその他機器情報CSV作成に失敗しました。処理を終了します。" >> ${LOG_FILE_PATH};
  exit 1
fi

Log.Info "BTCOSI005:[RUCCS]IFSその他機器情報CSV作成が完了しました。" >> ${LOG_FILE_PATH} 

