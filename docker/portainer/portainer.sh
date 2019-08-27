#!/bin/sh
DIR="$( cd "$( dirname "$0" )" && pwd )"
echo "Script executed from:  ${DIR}"

docker service create \
    --name portainer \
    --constraint 'node.role == manager' \
    --network proxy \
    --publish 9000:9000 \
    --secret source=tls_cacert_file,target=ca.pem \
    --secret source=tls_cert_file,target=cert.pem \
    --secret source=tls_key_file,target=key.pem \
    --replicas=1 \
    --mount type=bind,src=${DIR}/data,dst=/data \
    --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
  portainer/portainer \
    --ssl \
    --sslcert '/run/secrets/cert.pem' \
    --sslkey '/run/secrets/key.pem' \
    --tlscacert '/run/secrets/ca.pem' \
    --tlscert '/run/secrets/cert.pem' \
    --tlskey '/run/secrets/key.pem' \
    -H unix:///var/run/docker.sock
