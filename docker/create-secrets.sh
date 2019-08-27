#!/bin/sh
DIR="$( cd "$( dirname "$0" )" && pwd )"
echo "Script executed from:  ${DIR}"

docker secret rm tls_cacert_file || true
docker secret create tls_cacert_file ${DIR}/tls/work/ca/certs/root-ca.cert.pem

docker secret rm tls_cert_file || true
docker secret create tls_cert_file ${DIR}/tls/work/ca/certs/server.cert.pem

docker secret rm tls_key_file || true
docker secret create tls_key_file ${DIR}/tls/work/ca/private/server.decrypted.key.pem

docker secret rm tls_dhparam_file || true
docker secret create tls_dhparam_file ${DIR}/tls/dhparam4096.pem

docker secret rm keystore_file || true
docker secret create keystore_file ${DIR}/tls/work/keystore.p12

docker secret rm truststore_file || true
docker secret create truststore_file ${DIR}/tls/work/keystore.p12

docker secret rm firebase_credential_file || true
docker secret create firebase_credential_file ${DIR}/arch18-216209-firebase-adminsdk-vojrg-dac60a9de8.json

docker secret rm keystore_secret || true
echo "changeit" | docker secret create keystore_secret -

docker secret rm truststore_secret || true
echo "changeit" | docker secret create truststore_secret -

docker secret rm postgres_secret || true
echo "changeit" | docker secret create postgres_secret -

docker secret rm rabbitmq_secret || true
echo "changeit" | docker secret create rabbitmq_secret -

docker secret rm portainer_secret || true
echo "changeit" | docker secret create portainer_secret -
