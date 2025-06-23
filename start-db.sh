docker run \
  --name ping_pong_db \
  --rm \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=root \
  -e POSTGRES_DB=ping_pong \
  -p 5432:5432 \
  -v ping_pong_db_volume:/var/lib/postgresql/data \
  postgres:17.2