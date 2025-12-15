#!/bin/bash
##########################################################
### build.gradleに書かれたCommonのバージョンを変更します。
##########################################################

### 変更後バージョン
AFTER_COMMON=$1

echo "build.gradleに書かれたCommonLibsのversionを変更します。"
echo "変更後バージョン：${AFTER_COMMON}"

if [[ "${AFTER_COMMON}" =~ ^0\.0\.[0-9]{3,4}$ ]]; then
  echo "OK: 引数は期待どおりの形式です -> ${AFTER_COMMON}"
else
  echo "NG: 引数は '0.0.xxx'（xxxは3〜4桁の数字）ではありません -> '${AFTER_COMMON}'" >&2
  exit 1
fi

echo ${AFTER_COMMON}
for file in `\find . -name '*build.gradle'`; do
    echo $file
    sed -i -E s@jp.co.ricoh.cotos:common-libraries:0.0.[0-9]{3}@jp.co.ricoh.cotos:common-libraries:${AFTER_COMMON}@ $file
done