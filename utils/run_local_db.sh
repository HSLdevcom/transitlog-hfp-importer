#!/bin/bash
docker run --rm -p 5432:5432 -v $PWD/postgres_data:/var/lib/postgresql/data --env POSTGRES_PASSWORD=password --name transitlog-hfp-importer-db postgres postgres