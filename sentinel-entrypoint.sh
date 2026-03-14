#!/bin/sh

# aguarda o redis-master estar disponível
until redis-cli -h redis-master -p 6379 ping; do
  echo "Aguardando redis-master..."
  sleep 2
done

echo "redis-master disponível, iniciando sentinel..."
exec redis-sentinel /etc/sentinel.conf