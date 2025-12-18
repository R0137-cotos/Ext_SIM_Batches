#!/bin/bash
##########################################################
### build.gradleに書かれたCommonのブランチを変更します。
##########################################################

### 変更前ブランチ
BEFORE_BRANCH=$1

### 変更後ブランチ
AFTER_BRANCH=$2

echo "build.gradleに書かれたCommonLibsのブランチを変更します。"
echo "変更前ブランチ：${BEFORE_BRANCH}"
echo "変更後ブランチ：${AFTER_BRANCH}"

echo ${AFTER_BRANCH}
for file in `\find . -name '*build.gradle'`; do
    echo $file
    sed -i -E s@https://mygithub.ritscm.xyz/raw/cotos/LibsRepo/${BEFORE_BRANCH}/repository/CommonLibs@https://mygithub.ritscm.xyz/raw/cotos/LibsRepo/${AFTER_BRANCH}/repository/CommonLibs@ $file
done