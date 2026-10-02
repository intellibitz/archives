USAGE="usage:$0  -n name -p project [-s serviceaccount]"

while getopts n:p:s: flag; do
  case "${flag}" in
  n) name=${OPTARG} ;;
  p) project=${OPTARG} ;;
  s) serviceaccount=${OPTARG} ;;
  *) echo "${USAGE}" ;;
  esac
done
if [ -z "${name}" ] || [ -z "${project}" ] ]; then
  echo "${USAGE}"
  exit 1
fi
if [ -z "${serviceaccount}" ]; then
  serviceaccount="default"
else
  serviceaccount="${serviceaccount}"@"${project}".iam.gserviceaccount.com
fi
gcloud compute instances delete "${name}"
gcloud compute instances create "${name}" \
  --project="${project}" \
  --machine-type=e2-micro \
  --boot-disk-size=10GB \
  --boot-disk-type=pd-standard \
  --image-family=ubuntu-minimal-2004-lts \
  --image-project=ubuntu-os-cloud \
  --network=default \
  --zone=us-central1-a \
  --scopes=default,cloud-platform,compute-rw \
  --service-account="${serviceaccount}"
gcloud compute instances list
