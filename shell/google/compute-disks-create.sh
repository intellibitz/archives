while getopts d: flag; do
  case "${flag}" in
  d) diskname=${OPTARG} ;;
  *) echo "usage:$0 -d diskname" ;;
  esac
done
if [ -z "${diskname}" ]; then
  echo "usage:$0 -d diskname"
  exit 1
fi
gcloud compute disks delete disk_name "${diskname}"
gcloud compute disks create disk_name "${diskname}" \
  --type=pd-standard \
  --image-project=ubuntu-os-cloud \
  --image-family=ubuntu-minimal-2004-lts \
  --size=10gb \
  --zone=us-central1-a
gcloud compute disks list
