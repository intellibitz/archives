USAGE="usage:$0 [-k sshkey]  [-u username] -h hostname -s source [-d destination] [-c command]"

while getopts u:h:s:k:c:d: flag; do
  case "${flag}" in
  k) key=${OPTARG} ;;
  u) user=${OPTARG} ;;
  h) host=${OPTARG} ;;
  s) src=${OPTARG} ;;
  d) dst=${OPTARG} ;;
  c) cmd=${OPTARG} ;;
  *) echo "${USAGE}" ;;
  esac
done
if [ -z "${host}" ] || [ -z "${src}" ]; then
  echo "${USAGE}"
  exit 1
fi
if [ -z "${key}" ]; then
  key="$HOME/.ssh/id_rsa"
else
  [ -f "${key}" ] || key="$HOME/.ssh/${key}"
fi
if [ -z "${user}" ]; then
  user="$USER"
fi
if [ -z "${dst}" ]; then
  dst="$HOME"
fi
if [ -z "${cmd}" ]; then
  cmd="${dst}/`basename ${src}`"
fi

echo "running:$0 -k ${key} -u ${user} -h ${host} -s ${src} -d ${dst} -c ${cmd}"
scp-Cprv.sh -k "${key}" -u "${user}" -h "${host}" -s "${src}" -d "${dst}" &&
  ssh-cmd.sh -k "${key}" -h "${host}" -c "${cmd}"
