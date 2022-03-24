variables:
  GIT_USER_NAME: gitlabrunner
  GIT_EMAIL: gitlabrunner@dkatalis.com
  MS_NAME: temporal-hello-world-worker

stages:
  - build
  - deploy-dev

build:
  stage: build
  image:
    name: digibank-docker-group-nexus.bankartos.io/kaniko-executor:beta
  allow_failure: true
  when: manual
  script:
    - git config --global user.email $GIT_EMAIL
    - git config --global user.name $GIT_USER_NAME
    - git checkout -B "$CI_COMMIT_REF_NAME" "$CI_COMMIT_SHA"
    - git pull origin "$CI_COMMIT_REF_NAME"
    - git remote set-url origin http://gitlab:${CI_ACCESS_TOKEN}@${CI_SERVER_HOST}/${CI_PROJECT_PATH}
    - mkdir -p /kaniko/.docker
    - echo "{\"auths\":{\"https://$GCP_DOCKER_REGISTRY_DOMAIN\":{\"username\":\"$GCP_NEXUS_USER\",\"password\":\"$GCP_NEXUS_PASS\"},\"https://$GCP_DOCKER_GROUP_REGISTRY_DOMAIN\":{\"username\":\"$GCP_NEXUS_USER\",\"password\":\"$GCP_NEXUS_PASS\"}}}" > /kaniko/.docker/config.json
    - /kaniko/executor --context $CI_PROJECT_DIR --dockerfile $CI_PROJECT_DIR/Dockerfile --destination $GCP_DOCKER_REGISTRY_DOMAIN/$MS_NAME:latest
  tags:
    - nonprod-jago-app-deployment

deploy-to-dev:
  stage: deploy-dev
  image:
    name: digibank-docker-group-nexus.bankartos.io/helm-kubectl-gcloud:latest
  allow_failure: true
  when: manual
  environment: dev
  script:
    - echo "Install Helm charts"
    - SERVICE_ROUTE_PATH="'/${MS_NAME/_/-}(/|$)(.*)'"
    - gcloud container clusters get-credentials dev-lfs-app-k8s --region asia-southeast2 --project prj-dev-lfsbd8ca4
    - helm repo add --username $GCP_NEXUS_USER --password $GCP_NEXUS_PASS banking_repo $GCP_HELM_REPO_URI
    - helm repo update
    - helm upgrade -i ${MS_NAME/_/-} banking_repo/application
      --set ingress.enabled=false
      --set image.repository=$GCP_DOCKER_REGISTRY_DOMAIN/$MS_NAME
      --set image.tag=latest
      --set image.pullPolicy=Always
      --set imagePullSecrets="nexus"
      --set hpa.enabled=false
      --set service.port=8080
      --set deployment.livenessProbe.path=/actuator/health/liveness
      --set deployment.readinessProbe.path=/actuator/health/readiness
      --namespace banking
  tags:
    - nonprod-jago-app-deployment
