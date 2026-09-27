#https://www.nginx.com/blog/our-roadmap-quic-http-3-support-nginx/

# Builds NGINX from the QUIC+HTTP/3 development branch
# - Based on the official NGINX docker image, including all modules built by default
# - OpenSSL replaced with LibreSSL to support QUIC's TLS requirements (statically linked)
#
# docker build --no-cache -t nginx:quic .
# docker run -d -p 443:443 -p 443:443/udp nginx:quic
#
# Note that a suitable configuration file and TLS certificates are required for testing!
# See <https://quic.nginx.org/readme.html> for more info

FROM nginx AS build

WORKDIR /src
RUN apt-get update && apt-get install -y git gcc make autoconf libtool perl
RUN git clone -b v3.6.1 https://github.com/libressl-portable/portable.git libressl && \
    cd libressl && \
    ./autogen.sh && \
    ./configure && \
    make check && \
    make install

RUN apt-get install -y mercurial libperl-dev libpcre3-dev zlib1g-dev libxslt1-dev libgd-ocaml-dev libgeoip-dev
RUN hg clone -b quic https://hg.nginx.org/nginx-quic && \
    cd nginx-quic && \
    auto/configure `nginx -V 2>&1 | sed "s/ \-\-/ \\\ \n\t--/g" | grep "\-\-" | grep -ve opt= -e param= -e build=` \
      --with-http_v3_module --with-stream_quic_module \
      --with-debug --build=nginx-quic \
      --with-cc-opt="-I/src/libressl/build/include" --with-ld-opt="-L/src/libressl/build/lib" --with-ld-opt="-static" && \
    make

FROM nginx
COPY --from=build /src/nginx-quic/objs/nginx /usr/sbin
RUN /usr/sbin/nginx
EXPOSE 80 443 443/udp

#