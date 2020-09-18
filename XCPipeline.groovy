pipeline {

agent any
environment {
    M2 = "/opt/maven/apache-maven-3.6.2/bin"
}
stages {
	stage ("Cleaning Workspace") {
		steps {
			cleanWs()
		}
	
	}
	
	stage ("Git Checkout") {
		steps {

		    	checkout(
              			[$class: 'GitSCM',
               			branches: [[name: "${env.GIT_COMMIT}"]],
               			userRemoteConfigs: [[credentialsId: "${env.GIT_CREDENTIALS}", url: "${env.PROJECT_GIT_URI}"]]])
            		sh "mv ./Demo-SpringApplication/* ./; rm -r Demo-SpringApplication"

		}	
	}
	
	stage ("Cleaning Stage") {
		steps {
			sh "mvn clean"
		}	
	}	
	
	stage ("Testing Stage") {
		steps {
			sh "mvn test"
			junit **/target/surefire-reports/TEST-*.xml
		}	
	}
	
	stage ("Building Stage") {
		steps {
			sh "mvn package"
		    	script {
				def IMAGE_NAME = ${env.IMAGE} + ":0-$BUILD_NUMBER"
		        	dockerImage = docker.build "${env.IMAGE_NAME}"
		        	docker.withRegistry("${env.REGISTRY}"){
		            		dockerImage.push()
		        	}
		        sh "docker rmi ${IMAGE_NAME}
			sh "sed -i 's/IMAGE_NAME/${IMAGE_NAME}/' ./k8s/test.yaml"
                        sh "sed -i 's/IMAGE_NAME/${IMAGE_NAME}/' ./k8s/prod.yaml"
		    }
		}	
	}	
	
	stage ("Deploy to Test Environment") {
                steps {
                        sh "/usr/local/bin/kubectl --context=${env.TEST_K8S_CONTEXT} apply -f ./k8s_files/test.yaml"
                }	
	}	

	stage ("Manual Approval for Deployment to Prod Environment") {
        	input{
		    message "Do you want to delete this test env and deploy to prod?"
		}
		steps {
                	echo '/usr/local/bin/kubectl --context=${env.TEST_K8S_CONTEXT} delete -f ./k8s_files/test.yaml'
        	}
	}	

	stage ("Deployment to Prod Environment") {
		steps {
			sh "/usr/local/bin/kubectl --context=${env.PROD_K8S_CONTEXT} apply -f ./k8s_files/prod.yaml"
		}	
	}	

}

}

