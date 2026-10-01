#!/usr/bin/env bash
# https://hub.docker.com/_/httpd
# https://github.com/docker-library/httpd
# https://httpd.apache.org/docs/2.4/ssl/ssl_faq.html

set -euo pipefail

# Pull and run the official Apache httpd image
docker pull httpd:2.4
docker run -dit --name httpd -p 65430:80 -v "$PWD/htdocs":/usr/local/apache2/htdocs httpd:2.4

# ========================================================
# Reference Guides & Customizations
# ========================================================
#
# To build a custom image with your HTML files:
#
#   FROM httpd:2.4
#   COPY ./public-html/ /usr/local/apache2/htdocs/
#
# Commands to build and run:
#   docker build -t my-apache2 .
#   docker run -dit --name my-running-app -p 8080:80 my-apache2
#
# To extract and customize httpd.conf:
#   docker run --rm httpd:2.4 cat /usr/local/apache2/conf/httpd.conf > my-httpd.conf
#
# In a custom Dockerfile:
#   FROM httpd:2.4
#   COPY ./my-httpd.conf /usr/local/apache2/conf/httpd.conf
#
# Enable SSL modules:
#   LoadModule socache_shmcb_module modules/mod_socache_shmcb.so
#   LoadModule ssl_module modules/mod_ssl.so
#   Include conf/extra/httpd-ssl.conf