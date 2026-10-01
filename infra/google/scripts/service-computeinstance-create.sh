USAGE="usage:$0 -n instancename -p project -s serviceaccountname"

while getopts "n:p:s:" flag; do
  case "${flag}" in
  n) name=${OPTARG} ;;
  p) project=${OPTARG} ;;
  s) serviceaccount=${OPTARG} ;;
  *) echo "${USAGE}" ;;
  esac
done
if [ -z "${name}" ] || [ -z "${project}" ] || [ -z "${serviceaccount}" ]; then
  echo "${USAGE}"
  exit 1
fi

iam-serviceaccounts-create.sh -n "${serviceaccount}" -p "${project}"
compute-instances-create.sh -n "${name}" -p "${project}" -s "${serviceaccount}"
