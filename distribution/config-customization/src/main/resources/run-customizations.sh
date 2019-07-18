SCRIPTDIR=$(dirname $0)
HOME_DIR=$(cd "${SCRIPTDIR}/.."; pwd -P)
java -Dddf.home=${HOME_DIR} -cp ${HOME_DIR}/lib/config-customization-${project.version}.jar org.codice.ddf.distribution.customization.Main $*