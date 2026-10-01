#!/usr/bin/env sh
#https://github.com/docker-library/docs/blob/master/postgres/README.md

docker run -it --rm --network host localhost psql -h postgres -p 65432 -U postgres
# psql (14.3)
# Type "help" for help.

# postgres=# SELECT 1;
#  ?column? 
# ----------
#         1
# (1 row)