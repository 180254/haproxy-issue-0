#!/bin/bash

# https://superuser.com/a/226229
openssl req \
  -new \
  -newkey rsa:2048 \
  -days 365 \
  -nodes \
  -x509 \
  -subj "/C=US/ST=Denial/L=Springfield/O=Dis/CN=www.example.com" \
  -keyout cert.key \
  -out cert.crt

cat cert.key cert.crt > repro-haproxy/cert.pem
