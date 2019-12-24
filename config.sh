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
export ACCOUNTING_JAR_PATH=${BATCH_BASE_PATH}/Communication

################################################
### バッチJava実行モジュール名
################################################
# BTCOSI001 [FFM]イニシャル受注データファイル取込(SIM)
export BATCH_PG_BTCOSI001=FFMOrderDataFileImport-0.0.1-SNAPSHOT.jar

# BTCOSI002 [FFM]検収データファイル作成(SIM)
export BATCH_PG_BTCOSI002=CreateAcceptanceDataFile-0.0.1-SNAPSHOT.jar

# BTCOSI003 計上データ作成(SIMランニング分)
export BATCH_PG_BTCOSI003=AccountingCreateSimRunnging-0.0.1-SNAPSHOT.jar

# BTCOSI004 [SB]オーダーCSV作成
export BATCH_PG_BTCOSI004=CreateOrderCsvFile-0.0.1-SNAPSHOT.jar

# BTCOSI005 [SB]オーダーメール送信
export BATCH_PG_BTCOSI005=SendOrderMail-0.0.1-SNAPSHOT.jar

# BTCOSI006 [SB]リプライCSV取込
export BATCH_PG_BTCOSI006=ImportReplyCsv-0.0.1-SNAPSHOT.jar

# BTCOSI007 [RUCCUS]IFSその他機器情報CSV作成
export BATCH_PG_BTCOSI007=CreateCsvIFS-0.0.1-SNAPSHOT.jar
