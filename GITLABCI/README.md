stages:
  - deploy-dev
  - setup-db-schema-dev
  - create-temporal-ns-dev
  - deploy-qa
  - setup-db-schema-qa
  - create-temporal-ns-qa
  - deploy-staging
  - setup-db-schema-staging
  - create-temporal-ns-staging
  - deploy-pt
  - setup-db-schema-pt
  - create-temporal-ns-pt

variables:
  GCP_PROJECT_DEV: prj-dev-lfsbd8ca4
  K8S_CLUSTER_DEV: dev-lfs-app-k8s
  TEMPORAL_DB_HOST_DEV: 10.101.164.13
  GCP_PROJECT_QA: prj-qa-lfs2215d1
  K8S_CLUSTER_QA: qa-lfs-app-k8s
  TEMPORAL_DB_HOST_QA: 10.101.164.58
  GCP_PROJECT_STG: prj-stg-lfs4b6c5b
  K8S_CLUSTER_STG: stg-lfs-app-k8s
  TEMPORAL_DB_HOST_STAGING: 10.101.164.62
  GCP_PROJECT_PT: prj-pt-lfs3ff847
  K8S_CLUSTER_PT: pt-lfs-app-k8s
  TEMPORAL_DB_HOST_PT: 10.101.164.198
  TEMPORAL_NAMESPACE: kyc
  NAMESPACE: banking
  REPLICA: 1

deploy-to-dev-gcp:
  stage: deploy-dev
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: dev
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_DEV --region asia-southeast2 --project $GCP_PROJECT_DEV
    - echo "Install Helm charts"
    - helm repo add --username $GCP_NEXUS_USER --password $GCP_NEXUS_PASS temporal_repo $GCP_HELM_REPO_URI
    - helm repo update
    - helm upgrade -i temporal temporal_repo/temporal
      --values values.postgresql.yaml
      --values values.ingress.yaml
      --set admintools.image.repository=${GCP_DOCKER_REGISTRY_DOMAIN}/temporalio/admin-tools
      --set server.image.repository=${GCP_DOCKER_REGISTRY_DOMAIN}/temporalio/server
      --set server.config.persistence.default.sql.host=${TEMPORAL_DB_HOST_DEV}
      --set server.config.persistence.default.sql.database=temporal_db
      --set server.config.persistence.default.sql.user=${TEMPORAL_DB_USERNAME_DEV}
      --set server.config.persistence.default.sql.password=${TEMPORAL_DB_PASSWORD_DEV}
      --set server.config.persistence.visibility.sql.host=${TEMPORAL_DB_HOST_DEV}
      --set server.config.persistence.visibility.sql.database=temporal_visibility_db
      --set server.config.persistence.visibility.sql.user=${TEMPORAL_VISIBILITY_DB_USERNAME_DEV}
      --set server.config.persistence.visibility.sql.password=${TEMPORAL_VISIBILITY_DB_PASSWORD_DEV}
      --set server.replicaCount=$REPLICA
      --set web.ingress.hosts={"${GCP_DEV_SERVICE_INGRESS_HOST}/temporal"}
      --set web.image.repository=${GCP_DOCKER_REGISTRY_DOMAIN}/temporalio/web
      --set web.image.tag=1.13.0-dk
      --set web.image.pullPolicy=Always
      --namespace $NAMESPACE

setup-db-schema-dev:
  stage: setup-db-schema-dev
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: dev
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_DEV --region asia-southeast2 --project $GCP_PROJECT_DEV
    - kubectl exec --namespace=$NAMESPACE services/temporal-admintools -- bash -c
      "
      export SQL_DATABASE=temporal_db &&
      export SQL_PLUGIN=postgres &&
      export SQL_PORT=5432 &&
      export SQL_HOST=${TEMPORAL_DB_HOST_DEV} &&
      export SQL_USER=${TEMPORAL_DB_USERNAME_DEV} &&
      export SQL_PASSWORD=${TEMPORAL_DB_PASSWORD_DEV} &&
      temporal-sql-tool setup-schema -v 0.0 &&
      temporal-sql-tool update -schema-dir schema/postgresql/v96/temporal/versioned &&
      export SQL_DATABASE=temporal_visibility_db &&
      export SQL_USER=${TEMPORAL_VISIBILITY_DB_USERNAME_DEV} &&
      export SQL_PASSWORD=${TEMPORAL_VISIBILITY_DB_PASSWORD_DEV} &&
      temporal-sql-tool setup-schema -v 0.0 &&
      temporal-sql-tool update -schema-dir schema/postgresql/v96/visibility/versioned
      "

create-temporal-ns-dev:
  stage: create-temporal-ns-dev
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: dev
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_DEV --region asia-southeast2 --project $GCP_PROJECT_DEV
    - kubectl exec --namespace=$NAMESPACE services/temporal-admintools -- bash -c
      "
      tctl --namespace $TEMPORAL_NAMESPACE namespace register
      "

deploy-to-qa-gcp:
  stage: deploy-qa
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: qa
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_QA --region asia-southeast2 --project $GCP_PROJECT_QA
    - echo "Install Helm charts"
    - helm repo add --username $GCP_NEXUS_USER --password $GCP_NEXUS_PASS temporal_repo $GCP_HELM_REPO_URI
    - helm repo update
    - helm upgrade -i temporal temporal_repo/temporal
      --values values.postgresql.yaml
      --values values.ingress.yaml
      --set admintools.image.repository=${GCP_DOCKER_GROUP_REGISTRY_DOMAIN}/temporalio/admin-tools
      --set server.image.repository=${GCP_DOCKER_GROUP_REGISTRY_DOMAIN}/temporalio/server
      --set server.config.persistence.default.sql.host=${TEMPORAL_DB_HOST_QA}
      --set server.config.persistence.default.sql.database=temporal_db
      --set server.config.persistence.default.sql.user=${TEMPORAL_DB_USERNAME_QA}
      --set server.config.persistence.default.sql.password=${TEMPORAL_DB_PASSWORD_QA}
      --set server.config.persistence.visibility.sql.host=${TEMPORAL_DB_HOST_QA}
      --set server.config.persistence.visibility.sql.database=temporal_visibility_db
      --set server.config.persistence.visibility.sql.user=${TEMPORAL_VISIBILITY_DB_USERNAME_QA}
      --set server.config.persistence.visibility.sql.password=${TEMPORAL_VISIBILITY_DB_PASSWORD_QA}
      --set server.replicaCount=$REPLICA
      --set web.ingress.hosts={"${GCP_QA_SERVICE_INGRESS_HOST}/temporal"}
      --set web.image.repository=${GCP_DOCKER_GROUP_REGISTRY_DOMAIN}/temporalio/web
      --set web.image.tag=1.13.0-dk
      --set web.image.pullPolicy=Always
      --namespace $NAMESPACE

setup-db-schema-qa:
  stage: setup-db-schema-qa
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: qa
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_QA --region asia-southeast2 --project $GCP_PROJECT_QA
    - kubectl exec --namespace=$NAMESPACE services/temporal-admintools -- bash -c
      "
      export SQL_DATABASE=temporal_db &&
      export SQL_PLUGIN=postgres &&
      export SQL_PORT=5432 &&
      export SQL_HOST=${TEMPORAL_DB_HOST_QA} &&
      export SQL_USER=${TEMPORAL_DB_USERNAME_QA} &&
      export SQL_PASSWORD=${TEMPORAL_DB_PASSWORD_QA} &&
      temporal-sql-tool setup-schema -v 0.0 &&
      temporal-sql-tool update -schema-dir schema/postgresql/v96/temporal/versioned &&
      export SQL_DATABASE=temporal_visibility_db &&
      export SQL_USER=${TEMPORAL_VISIBILITY_DB_USERNAME_QA} &&
      export SQL_PASSWORD=${TEMPORAL_VISIBILITY_DB_PASSWORD_QA} &&
      temporal-sql-tool setup-schema -v 0.0 &&
      temporal-sql-tool update -schema-dir schema/postgresql/v96/visibility/versioned
      "

create-temporal-ns-qa:
  stage: create-temporal-ns-qa
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: qa
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_QA --region asia-southeast2 --project $GCP_PROJECT_QA
    - kubectl exec --namespace=$NAMESPACE services/temporal-admintools -- bash -c
      "
      tctl --namespace $TEMPORAL_NAMESPACE namespace register
      "

deploy-to-staging-gcp:
  stage: deploy-staging
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: staging
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_STG --region asia-southeast2 --project $GCP_PROJECT_STG
    - echo "Install Helm charts"
    - helm repo add --username $GCP_NEXUS_USER --password $GCP_NEXUS_PASS temporal_repo $GCP_HELM_REPO_URI
    - helm repo update
    - helm upgrade -i temporal temporal_repo/temporal
      --values values.postgresql.yaml
      --values values.ingress.yaml
      --set admintools.image.repository=${GCP_RC_DOCKER_REGISTRY_DOMAIN}/temporalio/admin-tools
      --set server.image.repository=${GCP_RC_DOCKER_REGISTRY_DOMAIN}/temporalio/server
      --set server.config.persistence.default.sql.host=${TEMPORAL_DB_HOST_STAGING}
      --set server.config.persistence.default.sql.database=temporal_db
      --set server.config.persistence.default.sql.user=${TEMPORAL_DB_USERNAME_STAGING}
      --set server.config.persistence.default.sql.password=${TEMPORAL_DB_PASSWORD_STAGING}
      --set server.config.persistence.visibility.sql.host=${TEMPORAL_DB_HOST_STAGING}
      --set server.config.persistence.visibility.sql.database=temporal_visibility_db
      --set server.config.persistence.visibility.sql.user=${TEMPORAL_VISIBILITY_DB_USERNAME_STAGING}
      --set server.config.persistence.visibility.sql.password=${TEMPORAL_VISIBILITY_DB_PASSWORD_STAGING}
      --set server.replicaCount=$REPLICA
      --set web.ingress.hosts={"${GCP_STAGING_SERVICE_INGRESS_HOST}/temporal"}
      --set web.image.repository=${GCP_RC_DOCKER_REGISTRY_DOMAIN}/temporalio/web
      --set web.image.tag=1.13.0-dk
      --set web.image.pullPolicy=Always
      --namespace $NAMESPACE

setup-db-schema-staging:
  stage: setup-db-schema-staging
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: staging
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_STG --region asia-southeast2 --project $GCP_PROJECT_STG
    - kubectl exec --namespace=$NAMESPACE services/temporal-admintools -- bash -c
      "
      export SQL_DATABASE=temporal_db &&
      export SQL_PLUGIN=postgres &&
      export SQL_PORT=5432 &&
      export SQL_HOST=${TEMPORAL_DB_HOST_STAGING} &&
      export SQL_USER=${TEMPORAL_DB_USERNAME_STAGING} &&
      export SQL_PASSWORD=${TEMPORAL_DB_PASSWORD_STAGING} &&
      temporal-sql-tool setup-schema -v 0.0 &&
      temporal-sql-tool update -schema-dir schema/postgresql/v96/temporal/versioned &&
      export SQL_DATABASE=temporal_visibility_db &&
      export SQL_USER=${TEMPORAL_VISIBILITY_DB_USERNAME_STAGING} &&
      export SQL_PASSWORD=${TEMPORAL_VISIBILITY_DB_PASSWORD_STAGING} &&
      temporal-sql-tool setup-schema -v 0.0 &&
      temporal-sql-tool update -schema-dir schema/postgresql/v96/visibility/versioned
      "

create-temporal-ns-staging:
  stage: create-temporal-ns-staging
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: staging
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_STG --region asia-southeast2 --project $GCP_PROJECT_STG
    - kubectl exec --namespace=$NAMESPACE services/temporal-admintools -- bash -c
      "
      tctl --namespace $TEMPORAL_NAMESPACE namespace register
      "

deploy-to-pt-gcp:
  stage: deploy-pt
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: pt
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_PT --region asia-southeast2 --project $GCP_PROJECT_PT
    - echo "Install Helm charts"
    - helm repo add --username $GCP_NEXUS_USER --password $GCP_NEXUS_PASS temporal_repo $GCP_HELM_REPO_URI
    - helm repo update
    - helm upgrade -i temporal temporal_repo/temporal
      --values values.postgresql.yaml
      --values values.ingress.yaml
      --set admintools.image.repository=${GCP_RC_DOCKER_REGISTRY_DOMAIN}/temporalio/admin-tools
      --set server.image.repository=${GCP_RC_DOCKER_REGISTRY_DOMAIN}/temporalio/server
      --set server.config.persistence.default.sql.host=${TEMPORAL_DB_HOST_PT}
      --set server.config.persistence.default.sql.database=temporal_db
      --set server.config.persistence.default.sql.user=${TEMPORAL_DB_USERNAME_PT}
      --set server.config.persistence.default.sql.password=${TEMPORAL_DB_PASSWORD_PT}
      --set server.config.persistence.visibility.sql.host=${TEMPORAL_DB_HOST_PT}
      --set server.config.persistence.visibility.sql.database=temporal_visibility_db
      --set server.config.persistence.visibility.sql.user=${TEMPORAL_VISIBILITY_DB_USERNAME_PT}
      --set server.config.persistence.visibility.sql.password=${TEMPORAL_VISIBILITY_DB_PASSWORD_PT}
      --set server.replicaCount=$REPLICA
      --set web.ingress.hosts={"${GCP_PT_SERVICE_INGRESS_HOST}/temporal"}
      --set web.image.repository=${GCP_RC_DOCKER_REGISTRY_DOMAIN}/temporalio/web
      --set web.image.tag=1.13.0-dk
      --set web.image.pullPolicy=Always
      --namespace $NAMESPACE

setup-db-schema-pt:
  stage: setup-db-schema-pt
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: pt
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_PT --region asia-southeast2 --project $GCP_PROJECT_PT
    - kubectl exec --namespace=$NAMESPACE services/temporal-admintools -- bash -c
      "
      export SQL_DATABASE=temporal_db &&
      export SQL_PLUGIN=postgres &&
      export SQL_PORT=5432 &&
      export SQL_HOST=${TEMPORAL_DB_HOST_PT} &&
      export SQL_USER=${TEMPORAL_DB_USERNAME_PT} &&
      export SQL_PASSWORD=${TEMPORAL_DB_PASSWORD_PT} &&
      temporal-sql-tool setup-schema -v 0.0 &&
      temporal-sql-tool update -schema-dir schema/postgresql/v96/temporal/versioned &&
      export SQL_DATABASE=temporal_visibility_db &&
      export SQL_USER=${TEMPORAL_VISIBILITY_DB_USERNAME_PT} &&
      export SQL_PASSWORD=${TEMPORAL_VISIBILITY_DB_PASSWORD_PT} &&
      temporal-sql-tool setup-schema -v 0.0 &&
      temporal-sql-tool update -schema-dir schema/postgresql/v96/visibility/versioned
      "

create-temporal-ns-pt:
  stage: create-temporal-ns-pt
  tags:
    - nonprod-jago-app-deployment
  image: asia.gcr.io/prj-nonprod-admin/helm-kubectl-gcloud
  when: manual
  environment: pt
  script:
    - echo $GCP_CRED > ${HOME}/gcloud-service-key.json
    - gcloud auth activate-service-account --key-file=${HOME}/gcloud-service-key.json
    - gcloud container clusters get-credentials $K8S_CLUSTER_PT --region asia-southeast2 --project $GCP_PROJECT_PT
    - kubectl exec --namespace=$NAMESPACE services/temporal-admintools -- bash -c
      "
      tctl --namespace $TEMPORAL_NAMESPACE namespace register
      "
