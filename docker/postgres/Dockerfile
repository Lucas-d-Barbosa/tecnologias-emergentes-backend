FROM postgres:15

# Copy init scripts to run schema + seeds when the DB initializes
COPY init/ /docker-entrypoint-initdb.d/
