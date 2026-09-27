FROM postgres:latest

RUN apt-get update && \
    apt-get install -y sudo procps vim file less

RUN echo "postgres ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers
RUN usermod -aG sudo postgres 

# passwd postgres #sets password to user postgres
# RUN mkdir -p /data/tablespaces      \
#  && mkdir -p /data/tablespaces/ts_a \
#  && mkdir -p /data/tablespaces/ts_b \
#  && mkdir -p /data/tablespaces/ts_c \
#  && chown -R postgres:postgres /data 


# RUN echo '[ ! -z "$TERM" -a -r /etc/motd ] && cat /etc/motd' >> /etc/bash.bashrc
# RUN echo 'export PATH=$PATH:/usr/lib/postgresql/16/bin/' >> /etc/bash.bashrc
# RUN echo 'export EDITOR=vim' >> /etc/bash.bashrc
# COPY ./motd /etc/motd

# RUN echo en_US.UTF-8 UTF-8 > /etc/locale.gen
# RUN locale-gen en_US.UTF-8
