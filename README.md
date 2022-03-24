- `temporal` directory for deploy Temporal with Helm Chart.
- `app/k8s` for deploy simple spring boot app that use Workflows and Activities.

Temporal Setup and Deployment
This guide is for setting up and deploying the Temporal server to GKE and using Google Cloud SQL for its database store.
Resources

GitLab CI for deploying the Temporal server into GKE
https://github.com/alaksmana/temporal/tree/main/GITLABCI

Helm Chart template for Temporal deployment
https://github.com/alaksmana/temporal/tree/main/HELM-CHART

DEV Deployment Explanation
The temporal server required two databases to run. We ask Sniper-team to create on the Google Cloud SQL, temporal_db, and tempo
ral_visibility_db.

We use direct connect for database connection instead of proxy sidecar because we don’t have permission to create Service Account
Key and don’t have permission to enable Workload Identity.


Helm Chart template is the modified version from https://github.com/temporalio/helm-charts and hosted on https://admin-coms-nexus.
bankartos.io/#browse/browse:digibank-helm-hosted-nexus:temporal
Because the Temporal docker image file cannot be pulled from the internet, so we also hosted it on https://admin-coms-nexus.bankartos.
io/#browse/browse:digibank-docker-hosted-nexus:v2%2Ftemporalio

Deploy Temporal server using Helm Chart will not have benefit from Auto-Setup feature likes using Docker compose. We need to create
database schema and Temporal namespace manually. We have to use temporal-sql-tool CLI provided by Temporal admin-tools to
create and update the schema. And use tctl to create a Temporal namespace.

For more information about the Auto-Setup feature, https://docs.temporal.io/blog/auto-setup/
