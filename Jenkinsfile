#!/usr/bin/env groovy

pipeline {

  agent {
    label 'kieker-slave-docker'
  } 

  environment {
    DOCKER_IMAGE = 'kieker/kieker-build'
    DOCKER_LABEL = 'openjdk7-small'
    DOCKER_INIT  = 'docker run 
    DOCKER_ARGS  = '--rm -v ${env.WORKSPACE}:/opt/kieker '
    DOCKER_BASH  = ' /bin/bash -c '
  }

 
  //triggers {
  //  cron{}
  //}

  stages {
    stage('Precheck') {
      when {
        expression {
          (env.CHANGE_TARGET != null) && (env.CHANGE_TARGET == 'stable')
        }
      }
      steps {
        echo "BRANCH_NAME: " + env.BRANCH_NAME
        echo "CHANGE_TARGET: " + env.CHANGE_TARGET
        echo "NODE_NAME: " + env.NODE_NAME
        echo "NODE_LABELS: " + env.NODE_LABELS
        error "It is not allowed to create pull requests towards the 'stable' branch. Create a new pull request towards the 'master' branch please."
      }
    }

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Compile') {
      
      // Can't be used due to bug: https://stackoverflow.com/questions/46385542/jenkinsfile-docker-agent-step-dies-after-1-second
      /**
      agent {
        docker {
          reuseNode true 
          image 'kieker/kieker-build:openjdk7-small'
          args ' --rm -u `id -u` -v ${env.WORKSPACE}:/opt/kieker'
          label 'kieker-slave-docker'
        }
      }**/
      steps {
        sh DOCKER_INIT + " -u `id -u` " + DOCKER_ARGS +  DOCKER_IMAGE + ':' + DOCKER_LABEL + DOCKER_BASH + '"cd /opt/kieker; ./gradlew -S compileJava compileTestJava"'
        //sh './kieker/gradlew -S -p kieker compileJava compileTestJava'
        //stash 'everything'
      }
    }
  
/**
    stage('Parallel') {
      steps {
        parallel (
          'Unit Test' : {
            unstash 'everything'
            sh DOCKER_BASE + '"cd /opt/kieker; ./gradlew -S test"'
          },
          'Static Analysis' : {
            unstash 'everything'
            sh DOCKER_BASE + '"cd /opt/kieker; ./gradlew -S check"'
            stash 'everything'
          }
        )
      }
    }

    stage('Unit Test') {
      steps {
        sh DOCKER_BASE + '"cd /opt/kieker; ./gradlew -S test"'
      }
    }

    stage('Static Analysis') {
      steps {
        sh DOCKER_BASE + '"cd /opt/kieker; ./gradlew -S check"'
      }
    }
    
    stage('Release Check Short') {
      steps {
        sh DOCKER_BASE + '"cd /opt/kieker; ./gradlew checkReleaseArchivesShort"'
        archiveArtifacts artifacts: 'build/distributions/*,kieker-documentation/userguide/kieker-userguide.pdf,build/libs/*.jar', fingerprint: true
      }
    }

    stage('Release Check Extended') {
      when {
        branch 'master'
      }
      steps {
        echo "We are in master - executing the extended release archive check."
        sh DOCKER_BASE + '"cd /opt/kieker; ./gradlew checkReleaseArchives -x test -x check "'
      }
    }

    stage('Push Stable') {
      when {
        branch 'master'
      }
      steps {
        echo "We are in master - pushing to stable branch."
        sh 'git push git@github.com:kieker-monitoring/kieker.git $(git rev-parse HEAD):stable'
      }
    }

    //stage('Fix permissions') {
    //  steps {         
    //  }
    //}
    **/
  }

  post {
    always {
      deleteDir()
    }
   
    //changed {
      //mail to: 'ci@kieker-monitoring.net', subject: 'Pipeline outcome has changed.', body: 'no text'
    //}


    //failure {
      //mail to: 'ci@kieker-monitoring.net', subject: 'Pipeline build failed.', body: 'no text'
    //}
  
    //success {
    //}

    //unstable {
    //}
  }
}

