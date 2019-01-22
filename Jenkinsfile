echo "Starting Pipeline for ....'${JOB_NAME}'"
 def mvnCmd = "mvn -s configuration/cicd-settings-nexus3.xml"
 
pipeline {
    
 agent { label 'maven' }
    
    stages {
     stage('Build Code') {
        steps {
		 script {
		     checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CheckoutOption', timeout: 240]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '45517441-c819-4700-9a74-e9320a918ade', url: env.SOURCE_CODE_URL]]])
              sh "${mvnCmd} install -DskipTests=true"
			   echo "Maven Build Complete"
					}
                }
            }
     stage('Code Coverage') {
        steps {
		 script {
          sh "${mvnCmd} sonar:sonar -Dsonar.host.url=http://sonarqube:9000 -DskipTests=true"
		   echo "Code Test Coverage Complete"
					}
				}
			}
     stage('Unit Tests') {
        steps {
			script {
            sh "${mvnCmd} test"
			step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
			  echo "Maven Unit test Complete"
					}
				}
			}
     stage('Archive App') {
		steps {
            script {
//			 sh "${mvnCmd} deploy -DskipTests=true -P nexus3"
             echo "Archive app complete"
					}	
				}
			}
     stage('Image Build') {
        steps {
		 script {
			sh "echo Building Image from Jar File"
			 sh """
			  rm -rf oc-build && mkdir -p oc-build/deployments
			    for t in \$(echo "jar;war;ear" | tr ";" "\\n"); do
                 cp -rfv ./target/*.\$t oc-build/deployments/ 2> /dev/null || echo "No \$t files"
			      done
				"""
			 openshift.withCluster('dev') {
              openshift.withProject(env.PROJECT) {
               openshift.selector("bc", env.APPLICATION_NAME).startBuild("--from-dir=oc-build/deployments", "--wait=true")
			    echo "Building Image Complete"
                            }
                        }
                    }
                }
            }
     stage('Deploy DEV') {
        steps {
			script {
			 echo "Starting Dev Deploy"
			 openshift.withCluster('dev') {
               openshift.withProject(env.PROJECT) {
                try { openshift.selector(dc/env.APPLICATION_NAME).rollout().latest() } catch (err) { }
                 //Verify deployment to dev code here
				 echo "Deploy to Dev complete"
							}
						}
					}
				}
			}
	
     stage('Deploy QA') { 
	  agent { label 'skopeo' }
        steps {
            script {
			 echo "Promoting new image to QA registry using Skopeo..."
			  openshift.withProject(env.NAMESPACE) {
			   withCredentials([
				usernamePassword(credentialsId: "c3f49bb8-9c5b-40e3-8576-310980421366", usernameVariable: "QA_USER", passwordVariable: "QA_PWD"),
				usernamePassword(credentialsId: "a0657c24-e4dd-48c3-acc5-f74db3104dbc", usernameVariable: "DEV_USER", passwordVariable: "DEV_PWD")
				 ]) {
                 sh "skopeo copy docker://docker-registry.default.svc:5000/${env.PROJECT}/${env.APPLICATION_NAME}:latest docker://docker-registry-default.ospqa.gcom.grainger.com/${env.PROJECT}/${env.APPLICATION_NAME}:latest --src-creds \"$DEV_USER:$DEV_PWD\" --dest-creds \"$QA_USER:$QA_PWD\" --src-tls-verify=false --dest-tls-verify=false"
                    }
				}
				echo "Skopeo update complete"
            openshift.withCluster('qa') {
              openshift.withProject(env.PROJECT) {
               openshift.selector("dc", env.APPLICATION_NAME).rollout().latest();
					echo "Testing QA deployment"
                  //Verify deploy to QA code here
								}
							}
						}
					}
				}
		
     stage('Deploy PROD LF') {
	  agent { label 'skopeo' }
        steps {
            mail (
            to: env.EMAIL,
            subject: "Job '${JOB_NAME}' (${BUILD_NUMBER}) is waiting for input",
             body: "Please go to ${BUILD_URL} and verify the build");
                 timeout(time:15, unit:'MINUTES') {
                      input message: "Promote to Prod LF?", ok: "Promote"
                 }
                script {
				 withCredentials([
					usernamePassword(credentialsId: "c3f49bb8-9c5b-40e3-8576-310980421366", usernameVariable: "QA_USER", passwordVariable: "QA_PWD"),
					usernamePassword(credentialsId: "43da03dd-ba91-4cf2-b2ae-175383e381f6", usernameVariable: "PROD_USER", passwordVariable: "PROD_PWD")
					]) {
                   sh "skopeo copy docker://docker-registry-default.ospqa.gcom.grainger.com/${env.PROJECT}/${env.APPLICATION_NAME}:latest docker://docker-registry-default.ospprodlf.gcom.grainger.com/${env.PROJECT}/${env.APPLICATION_NAME}:latest  --src-creds \"$QA_USER:$QA_PWD\" --dest-creds \"$PROD_USER:$PROD_PWD\" --src-tls-verify=false --dest-tls-verify=false"
                        }
            openshift.withCluster('prod-lf') {
             echo "Starting Prod LF rollout"
               openshift.withProject(env.PROJECT) {
                openshift.selector("dc", env.APPLICATION_NAME).rollout().latest();
                 //Verify deploy to Prod here
								}
							}
						}
					}
				}
	 
    stage('Deploy PROD T5') {
	 agent { label 'skopeo' }
        steps {
          timeout(time:120, unit:'MINUTES') {
          input message: "Promote to Prod T5?", ok: "Promote"
                 }
			script {
				 withCredentials([
					usernamePassword(credentialsId: "c3f49bb8-9c5b-40e3-8576-310980421366", usernameVariable: "QA_USER", passwordVariable: "QA_PWD"),
					usernamePassword(credentialsId: "8cb3c210-025b-4f8c-a09e-fab0f1063f11", usernameVariable: "PROD_USER", passwordVariable: "PROD_PWD")
					]) {
                   sh "skopeo copy docker://docker-registry-default.ospqa.gcom.grainger.com/${env.PROJECT}/${env.APPLICATION_NAME}:latest docker://docker-registry-default.ospprodt5.gcom.grainger.com/${env.PROJECT}/${env.APPLICATION_NAME}:latest  --src-creds \"$QA_USER:$QA_PWD\" --dest-creds \"$PROD_USER:$PROD_PWD\" --src-tls-verify=false --dest-tls-verify=false"
                        }
            openshift.withCluster('prod-t5') {
			 echo "Staring Prod T5 rollout"
               openshift.withProject(env.PROJECT) {
                openshift.selector("dc", env.APPLICATION_NAME).rollout().latest();
              
                  //verify deploy to prod here
										}
									}
								}
							}      
						}
					}
				}
