#!/bin/sh
sudo certbot certonly \
  --preferred-challenges dns\
  --dns-digitalocean \
  --dns-digitalocean-credentials /run/secrets/digitalocean.ini \
  -d "staging.3bwins.com" \
  -d "*.staging.3bwins.com" \
  --dry-run