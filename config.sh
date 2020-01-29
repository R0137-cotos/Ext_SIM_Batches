#!/bin/bash
################################################
### 共通設定
################################################
# メインブランチと別個に同時稼働したい場合にのみサフィックスを指定する
BATCH_BASE_SUFFIX=

# バッチジョブの共通設定シェル読み込み
BATCHE_JOBS_BASE_PATH=$(cd $(dirname $0);cd ../../../BatcheJobs${BATCH_BASE_SUFFIX};pwd)
source ${BATCHE_JOBS_BASE_PATH}/environment.sh
source ${BATCHE_JOBS_BASE_PATH}/config.sh
source ${BATCHE_JOBS_BASE_PATH}/common/logger.sh

export LOG_DIR=/var/log/cotos/Ext_SIM_Batches${BATCH_BASE_SUFFIX}

################################################
### バッチJava実行モジュール パス
################################################
export BATCH_BASE_PATH=${BASE_DIR_PATH}/Ext_SIM_Batches${BATCH_BASE_SUFFIX}
export COMMUNICATION_JAR_PATH=${BATCH_BASE_PATH}/Communication
export ORDER_JAR_PATH=${BATCH_BASE_PATH}/Order
export ACCOUNTING_JAR_PATH=${BATCH_BASE_PATH}/Accounting
export CONTRACT_JAR_PATH=${BATCH_BASE_PATH}/Contract

################################################
### バッチJava実行モジュール名
################################################
# BTCOSI001 [SB]オーダーCSV作成
export BATCH_PG_BTCOSI001=CreateOrderCsv-0.0.1-SNAPSHOT.jar

# BTCOSI002 [SB]オーダーメール送信
export BATCH_PG_BTCOSI002=SendOrderMail-0.0.1-SNAPSHOT.jar

# BTCOSI003 [SB]リプライCSV取込
export BATCH_PG_BTCOSI003=ImportReplyCsv-0.0.1-SNAPSHOT.jar

# BTCOSI004 計上データ作成(SIMランニング分)
export BATCH_PG_BTCOSI004=AccountingCreateSimRunning-0.0.1-SNAPSHOT.jar

# BTCOSI005 [RUCCS]IFSその他機器情報CSV作成
export BATCH_PG_BTCOSI005=CreateCsvIFS-0.0.1-SNAPSHOT.jar

