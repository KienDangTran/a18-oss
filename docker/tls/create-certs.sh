#!/bin/sh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
CA_DIR=work/ca
KEYSTORE_FILE=work/keystore.p12

if [[ -d work/ca ]] ; then
    rm -Rf ${CA_DIR}
fi

if [[ -f ${KEYSTORE_FILE} ]] ; then
    rm -Rf ${KEYSTORE_FILE}
fi

if [ ! -x "$(which openssl)" ] ; then
   echo "[ERROR] No openssl in PATH"
   exit 1
fi

KEYTOOL=keytool

if [  ! -x "${KEYTOOL}" ] ; then
   KEYTOOL=${JAVA_HOME}/bin/keytool
fi

if [  ! -x "${KEYTOOL}" ] ; then
   echo "[ERROR] No keytool in PATH/JAVA_HOME"
   exit 1
fi

mkdir -p ${CA_DIR}/private ${CA_DIR}/certs ${CA_DIR}/crl ${CA_DIR}/csr ${CA_DIR}/newcerts

echo "[INFO] Generating Root CA private key"
# Less bits = less secure = faster to generate
openssl genrsa \
        -passout pass:changeit \
        -aes256 -out ${CA_DIR}/private/root-ca.key.pem 4096

chmod 400 ${CA_DIR}/private/root-ca.key.pem

echo "[INFO] Generating Root CA certificate"
openssl req \
        -config ${DIR}/openssl.cnf \
        -key ${CA_DIR}/private/root-ca.key.pem \
        -new -x509 -days 7300 -sha256 -extensions v3_ca \
        -out ${CA_DIR}/certs/root-ca.cert.pem \
        -passin pass:changeit \
        -subj "/C=NN/ST=Unknown/L=Unknown/O=a18/CN=Local Root CA Certificate"

echo "[INFO] Prepare CA database"
echo 1000 > ${CA_DIR}/serial
touch ${CA_DIR}/index.txt

echo "[INFO] Generating server private key"
openssl genrsa \
        -aes256 \
        -passout pass:changeit \
        -out ${CA_DIR}/private/server.key.pem 2048

openssl rsa \
        -in ${CA_DIR}/private/server.key.pem \
        -out ${CA_DIR}/private/server.decrypted.key.pem \
        -passin pass:changeit

chmod 400 ${CA_DIR}/private/server.key.pem
chmod 400 ${CA_DIR}/private/server.decrypted.key.pem

echo "[INFO] Generating server certificate request"
openssl req \
        -config ${DIR}/openssl.cnf \
        -key ${CA_DIR}/private/server.key.pem \
        -passin pass:changeit \
        -new -sha256 \
        -out ${CA_DIR}/csr/server.csr.pem \
        -subj "/C=NN/ST=Unknown/L=Unknown/O=a18/CN=localhost"

echo "[INFO] Signing server certificate request"
openssl ca \
        -config ${DIR}/openssl.cnf \
        -extensions server_cert \
        -days 3650 \
        -notext \
        -md sha256 \
        -passin pass:changeit \
        -batch \
        -in ${CA_DIR}/csr/server.csr.pem \
        -out ${CA_DIR}/certs/server.cert.pem


echo "[INFO] Generating client auth private key"
openssl genrsa \
        -aes256 \
        -passout pass:changeit \
        -out ${CA_DIR}/private/client.key.pem 2048

openssl rsa \
        -in ${CA_DIR}/private/client.key.pem \
        -out ${CA_DIR}/private/client.decrypted.key.pem \
        -passin pass:changeit

chmod 400 ${CA_DIR}/private/client.key.pem

echo "[INFO] Generating client certificate request"
openssl req \
        -config ${DIR}/openssl.cnf \
        -key ${CA_DIR}/private/client.key.pem \
        -passin pass:changeit \
        -new -sha256 -out ${CA_DIR}/csr/client.csr.pem \
        -subj "/C=NN/ST=Unknown/L=Unknown/O=a18/CN=client"

echo "[INFO] Signing client certificate request"
openssl ca \
        -config ${DIR}/openssl.cnf \
        -extensions usr_cert -days 375 -notext -md sha256 \
        -passin pass:changeit \
        -batch \
        -in ${CA_DIR}/csr/client.csr.pem \
        -out ${CA_DIR}/certs/client.cert.pem

echo "[INFO] Creating  PKCS12 file with Root CA certificate"
${KEYTOOL} -importcert \
           -keystore ${KEYSTORE_FILE} \
           -file ${CA_DIR}/certs/root-ca.cert.pem \
           -storepass changeit \
           -alias root-CA \
           -noprompt

echo "[INFO] Creating  PKCS12 file with client certificate"
openssl pkcs12 \
        -export \
        -clcerts \
        -CAfile ${CA_DIR}/certs/root-ca.cert.pem \
        -caname root-ca \
        -chain \
        -in ${CA_DIR}/certs/client.cert.pem \
        -inkey ${CA_DIR}/private/client.decrypted.key.pem \
        -passout pass:changeit \
        -name localhost \
        -out ${DIR}/work/client.p12

${KEYTOOL} -importkeystore \
           -srckeystore ${DIR}/work/client.p12 \
           -srcstoretype PKCS12 \
           -srcstorepass changeit\
           -destkeystore ${KEYSTORE_FILE} \
           -deststoretype PKCS12 \
           -noprompt -storepass changeit

echo "[INFO] Creating  PKCS12 file with server certificate"
openssl pkcs12 \
        -export \
        -clcerts \
        -CAfile ${CA_DIR}/certs/root-ca.cert.pem \
        -caname root-ca \
        -chain \
        -in ${CA_DIR}/certs/server.cert.pem \
        -inkey ${CA_DIR}/private/server.decrypted.key.pem \
        -passout pass:changeit \
        -name server \
        -out ${DIR}/work/server.p12

${KEYTOOL} -importkeystore \
           -srckeystore ${DIR}/work/server.p12 \
           -srcstoretype PKCS12 \
           -srcstorepass changeit\
           -destkeystore ${KEYSTORE_FILE} \
           -deststoretype PKCS12 \
           -noprompt -storepass changeit

rm -f ${DIR}/work/client.p12 ${DIR}/work/server.p12 ${CA_DIR}/*
rm -rf ${CA_DIR}/crl ${CA_DIR}/csr ${CA_DIR}/newcerts