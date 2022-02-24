#!/bin/bash
POSTGRES_SCHEMA="$(dirname "$0")/postgres_schema.sql"
psql -h localhost -U postgres -f $POSTGRES_SCHEMA
