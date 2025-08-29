#/bin/sh

set -x
set -e

# Location of appengine-java-standard project.
APPENGINE_JAVA_STANDARD="../appengine-java-standard"
if [ ! -d "$APPENGINE_JAVA_STANDARD" ]; then
  echo "Please modify APPENGINE_JAVA_STANDARD in this script to point to your local appengine-java-standard repository."
  exit 1
fi

APPENGINE_STANDARD_DEMO="$(cd "$(dirname "$0")" && pwd)"
APP_LOCATION="$APPENGINE_STANDARD_DEMO/target/appengine-staging"

# Build the appengine-standard-demo project.
cd $APPENGINE_STANDARD_DEMO
mvn clean install -DskipTests
rm -rf target/appengine-staging
$APPENGINE_JAVA_STANDARD/sdk_assembly/target/appengine-java-sdk/bin/appcfg.sh stage target/appengine-spring-boot target/appengine-staging

# Extract the runtime-deployment jars into a tmp directory.
RUNTIME_DEPLOYMENT_ZIP=$(  find $APPENGINE_JAVA_STANDARD/runtime/deployment/target/ -name "runtime-deployment-*.zip" )
RUNTIME_DEPLOYMENT="/tmp/runtime-deployment"
mkdir -p $RUNTIME_DEPLOYMENT
rm -rf ${RUNTIME_DEPLOYMENT:?}/*
unzip $RUNTIME_DEPLOYMENT_ZIP -d $RUNTIME_DEPLOYMENT

# To start API Server you run this from root of appengine-java-standard repository.
# mvn exec:java -pl :appengine-apis-dev -Dexec.mainClass="com.google.appengine.tools.development.HttpApiServer"

"$JAVA_HOME"/bin/java \
 --add-opens java.base/java.lang=ALL-UNNAMED \
 --add-opens java.base/java.nio.charset=ALL-UNNAMED \
 --add-opens java.logging/java.util.logging=ALL-UNNAMED \
 --add-opens java.base/java.util.concurrent=ALL-UNNAMED \
 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 \
 -showversion -XX:+PrintCommandLineFlags \
 -Djava.class.path=$RUNTIME_DEPLOYMENT/runtime-main.jar \
 -Dclasspath.runtimebase=$RUNTIME_DEPLOYMENT: \
 com/google/apphosting/runtime/JavaRuntimeMainWithDefaults \
 --fixed_application_path=$APP_LOCATION  \
 $RUNTIME_DEPLOYMENT
