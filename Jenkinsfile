pipeline {
  agent any
  stages {
    stage("build") {
      when {
        expression {
          def isJobNameMatch = "${env.JOB_NAME}".contains("PullRequestBuild")
          return isJobNameMatch
        }
      }
      steps {
        withCredentials([string(credentialsId: 'github-statuses-token', variable: 'GITHUB_TOKEN')]){
          script {
            notifyStatus('pending', 'starting gradle test.', "${GITHUB_TOKEN}")
            echo "buildを実行します"
            echo ">> PullRequestの情報を表示します。"
            echo "PR作成者： ${env.CHANGE_AUTHOR}"
            echo "Forkリポジトリ： ${env.CHANGE_FORK}"
            echo "PRブランチ： ${env.CHANGE_BRANCH}"
            echo "ターゲットブランチ： ${env.CHANGE_TARGET}" 
            def testCsv = readFile "autoTestList.csv"
            def lines = testCsv.readLines()
            def hasFailure = false
            lines.each { line ->
              def parts = line.split(',')
              def runTest = parts[0].trim()
              if ("${runTest}" == "run-test") {
                echo "ヘッダー行のため処理しません。"
              } else if ("${runTest}" == "1"){
                def targetPath = parts[1].trim()
                dir("${targetPath}"){
                  try {
                    sh "gradle clean"
                    def springProfileActive = "${env.CHANGE_TARGET}" == 'master' ? 'ci_master' : 'ci'
                    def gradleTestOption = "${env.GRADLE_TEST_OPTION}"
                    withEnv(["SPRING_PROFILES_ACTIVE=${springProfileActive}"]) {
                      sh "gradle ${gradleTestOption} test"
                    }
                  } catch (e) {
                    echo "テスト失敗: ${targetPath}"
                    hasFailure = true
                  } finally {
                    junit "**/build/test-results/test/*.xml"
                    archiveArtifacts "**/build/test-results/test/*.xml"
                  }
                }
              }
            }
            if (hasFailure) {
              notifyStatus('failure', 'Some tests failed.', "${GITHUB_TOKEN}")
              error("failed")
            } else {
              notifyStatus('success', 'All tests passed.', "${GITHUB_TOKEN}")
            }
          }
        }
      }
    }
  }
}
def notifyStatus(state, description, token) {
  // Jsonペイロード
  def payload = groovy.json.JsonOutput.toJson([
    state: state,
    context: 'gradle test',
    description: description,
    target_url: env.BUILD_URL,
  ])
  def revision = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
  // curl で POST
  sh """
    curl -s -X POST https://mygithub.ritscm.xyz/api/v3/repos/cotos/Ext_SIM_Batches/statuses/${revision} \
      -H "Authorization: token ${GITHUB_TOKEN}" \
      -H "Content-Type: application/json" \
      -d '${payload}'
  """
}
