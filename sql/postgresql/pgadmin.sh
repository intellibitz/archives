#https://www.pgadmin.org/download/pgadmin-4-apt/

#
# Setup the repository
#
# Install the public key for the repository (if not done previously):
curl -fsS https://www.pgadmin.org/static/packages_pgadmin_org.pub | sudo gpg --dearmor -o /usr/share/keyrings/packages-pgadmin-org.gpg
# Create the repository configuration file:
sudo sh -c 'echo "deb [signed-by=/usr/share/keyrings/packages-pgadmin-org.gpg] https://ftp.postgresql.org/pub/pgadmin/pgadmin4/apt/$(lsb_release -cs) pgadmin4 main" > /etc/apt/sources.list.d/pgadmin4.list && apt update'
#
# Install pgAdmin
#
# Install for both desktop and web modes:
sudo apt install pgadmin4
# Install for desktop mode only:
sudo apt install pgadmin4-desktop
# Install for web mode only:
sudo apt install pgadmin4-web
# Configure the webserver, if you installed pgadmin4-web:
sudo /usr/pgadmin4/bin/setup-web.sh

#
#https://www.pgadmin.org/docs/pgadmin4/latest/container_deployment.html
#
#Examples¶
#Run a simple container over port 80:
docker pull dpage/pgadmin4
docker run -p 80:80 \
    -e 'PGADMIN_DEFAULT_EMAIL=user@domain.com' \
    -e 'PGADMIN_DEFAULT_PASSWORD=SuperSecret' \
    -d dpage/pgadmin4

#Run a simple container over port 80, setting some configuration options:
docker pull dpage/pgadmin4
docker run -p 80:80 \
    -e 'PGADMIN_DEFAULT_EMAIL=user@domain.com' \
    -e 'PGADMIN_DEFAULT_PASSWORD=SuperSecret' \
    -e 'PGADMIN_CONFIG_ENHANCED_COOKIE_PROTECTION=True' \
    -e 'PGADMIN_CONFIG_LOGIN_BANNER="Authorised users only!"' \
    -e 'PGADMIN_CONFIG_CONSOLE_LOG_LEVEL=10' \
    -d dpage/pgadmin4

#Run a TLS secured container using a shared config/storage directory in /private/var/lib/pgadmin on the host, and servers pre-loaded from /tmp/servers.json on the host:
docker pull dpage/pgadmin4
docker run -p 443:443 \
    -v /private/var/lib/pgadmin:/var/lib/pgadmin \
    -v /path/to/certificate.cert:/certs/server.cert \
    -v /path/to/certificate.key:/certs/server.key \
    -v /tmp/servers.json:/pgadmin4/servers.json \
    -e 'PGADMIN_DEFAULT_EMAIL=user@domain.com' \
    -e 'PGADMIN_DEFAULT_PASSWORD=SuperSecret' \
    -e 'PGADMIN_ENABLE_TLS=True' \
    -d dpage/pgadmin4

#
#https://www.pgadmin.org/download/pgadmin-4-python/
#
#A Python package is available for those wishing to run pgAdmin as a web application in a Python environment. Note that the packages do not include the Desktop Runtime.
#pgAdmin is available on PyPi. To install it, create a virtual environment as required, and then use pip to install. Note that pgAdmin will run in server mode, using system-wide paths so you may need to create them first:

$ sudo mkdir /var/lib/pgadmin
$ sudo mkdir /var/log/pgadmin
$ sudo chown $USER /var/lib/pgadmin
$ sudo chown $USER /var/log/pgadmin
$ python3 -m venv pgadmin4
$ source pgadmin4/bin/activate
$(pgadmin4) $ pip install pgadmin4
...
$(pgadmin4) $ pgadmin4
NOTE: Configuring authentication for SERVER mode.

Enter the email address and password to use for the initial pgAdmin user account:

Email address: user@domain.com
Password:
Retype password:
Starting pgAdmin 4. Please navigate to http://127.0.0.1:5050 in your browser.
# * Serving Flask app "pgadmin" (lazy loading)
 * Environment: production
   WARNING: Do not use the development server in a production environment.
   Use a production WSGI server instead.
 * Debug mode: off

#
#Manual installation with a Python Wheel package
#
#To install, download the wheel and install with a command such as
pip install https://ftp.postgresql.org/pub/pgadmin/pgadmin4/v4.29/pip/pgadmin4-4.29-py3-none-any.whl or
pip install ./pgadmin4-4.29-py3-none-any.whl
#if you've manually downloaded the file. It is strongly recommended that a Python Virtual Environment is used.

#pgAdmin can now be run with a command like
python ~/pgadmin4/lib/python3.8/site-packages/pgadmin4/pgAdmin4.py
#(assuming you use a virtual environment at ~/pgadmin4). Finally, point your browser to
http://127.0.0.1:5050.

