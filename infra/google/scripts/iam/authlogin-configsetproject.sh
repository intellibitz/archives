while getopts l:p: flag; do
  case "${flag}" in
  l) login=${OPTARG} ;;
  p) project=${OPTARG} ;;
  *) echo "usage:$0 -l login -p project" ;;
  esac
done
if [ -z "${login}" ] || [ -z "${project}" ]; then
  echo "usage:$0 -l login -p project"
  exit 1
fi

gcloud auth login "${login}"
gcloud config set project "${project}"
