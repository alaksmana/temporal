variables:
  HELM_CHART_STORE: "https://admin-coms-nexus.bankartos.io/repository/digibank-helm-hosted-nexus"
  CHART_NAME: "temporal"
  VERSION: "0.14.0"

helm-chart-upload:
  image: alpine/helm:3.3.0
  environment: dev
  when: manual
  tags:
    - nonprod-jago-app-deployment
  script:
    - apk add curl
    - helm package .
    - curl -v -u ${GCP_NEXUS_USER}:${GCP_NEXUS_PASS} ${HELM_CHART_STORE}/${CHART_NAME}-${VERSION}.tgz  --upload-file ${CHART_NAME}-${VERSION}.tgz
