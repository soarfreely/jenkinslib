def call(Closure body) {
     paramsMap = body
     body()

     def tool = new org.devOps.Tools()
     def getCode = new org.devOps.GetCode()
     def build = new org.devOps.Build()

     tool.printMsg("my lib", 'green')
     // tool.getProjectName(body.repository)
     tool.printMsg(paramsMap, 'green')

     tool.printMsg(body.run_composer, 'green')
     tool.printMsg(body.php_project_path, 'green')

     tool.printMsg("${params}", 'green')
     tool.printMsg('environment', 'green')
     tool.printMsg(body.repository, 'green')

    // jenkins 工作目录
    String workspace = "/home/soar/app/nginx-php-fpm/www/jenkins/workspace"

    pipeline {
    	agent {
    		node {
    			label "master" // 指定运行节点的标签或者名称
    			customWorkspace "projectName/${workspace}" // 指定运行工作目录（可选）
    		}
    	}

        // 指定运行选项（可选）
    	options {
    		timestamps() // 日志会有日志
    		skipDefaultCheckout() // 删除隐藏checkout scm 语句
    		disableConcurrentBuilds() // 禁止并行（根据实际情况）
    		timeout(time:1, unit:"HOURS") // 流水线超时设置
    		// 表示保留6次构建历史
    		buildDiscarder(logRotator(daysToKeepStr:'1', numToKeepStr:'6', artifactDaysToKeepStr:'2', artifactNumToKeepStr:'5'))
    	}

        // 参数
        parameters {
            string(name: 'branch', defaultValue: 'develop', description: 'Please enter the code branch to be built')
            string(name: 'version', defaultValue: '', description: 'Please enter the version number to be published')
            choice(name: 'mode', choices: ['deploy', 'rollback'], description: '选择方向！')
        }

    	stages {
    		// 下载代码
    		stage("GetCode") { // 阶段名称
    			steps {
    				timeout(time:5, unit:"MINUTES") {  // 步骤超时时间
    					script { // 脚本式
    						println('fetch code')

    						//Git,拉取代码
    						getCode.GetCode(body.repository, body.jenkins2repository, "${branch}")
    						println('get code ok')
    				 	}
    				}
    			}
    		}

    		// 构建
    		stage("Build") {
    			steps {
    				timeout(time:20, unit:"MINUTES") {
    					script { // 脚本式
    						println('Build tar')
                            build.tar('project-name', body.targetIp, body.jenkins2server)
                            println('sshagent应用打包')
    				 	}
    				}
    			}
    		}

    		// 代码扫描
    		stage("CodeScan") {
    			steps {
    				timeout(time:30, unit:"MINUTES") {
    					script { // 脚本式
    						println('代码扫描')
    						tool.printMsg("我的共享库", 'green')
    				 	}
    				}
    			}
    		}
    	}

    	post { // 构建后的操作
    		always {
    			script {
    				println("always")
    			}
    		}

    		// currentBuild 全局变量，description 构建描述
    		success {
    			script {
    				currentBuild.description += "\n 构建成功!"
    			}
    		}

    		failure {
    			script {
    				currentBuild.description += "\n 构建失败!"
    			}
    		}

    		aborted {
    			script {
    				currentBuild.description += "\n 构建取消!"
    			}
    		}
    	}
    }
}