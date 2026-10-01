USAGE="usage:$0 -n serviceaccountname -p project"

while getopts n:p: flag; do
  case "${flag}" in
  n) name=${OPTARG} ;;
  p) project=${OPTARG} ;;
  *) echo "${USAGE}" ;;
  esac
done
if [ -z "${name}" ] || [ -z "${project}" ]; then
  echo "${USAGE}"
  exit 1
fi

gcloud iam service-accounts delete "${name}";
gcloud iam service-accounts create "${name}" \
  --project="${project}" \
  --description="${project} Project Service Account" \
  --display-name="${project} Project  Service Account"

gcloud projects add-iam-policy-binding "${project}" \
  --member=serviceAccount:"${name}"@"${project}".iam.gserviceaccount.com \
  --role=roles/compute.instanceAdmin.v1

gcloud projects add-iam-policy-binding "${project}" \
  --member=serviceAccount:"${name}"@"${project}".iam.gserviceaccount.com \
  --role=roles/iam.serviceAccountUser

gcloud iam service-accounts list
