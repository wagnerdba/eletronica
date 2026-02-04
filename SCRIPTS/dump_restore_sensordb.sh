#!/bin/bash

# Variáveis para o dump
DUMP_FILE="./dump_dbsensor_$(date +'%Y-%m-%d_%H-%M-%S').dump"
DUMP_HOST="192.168.1.14"
DUMP_DB_NAME="sensordb"
DUMP_DB_USER="postgres"
DUMP_DB_SCHEMA="public"
DUMP_PORT=5432
DUMP_PASSWORD="123456"  # Senha do dump

# Variáveis para o restore
RESTORE_HOST="autorack.proxy.rlwy.net"
RESTORE_DB_NAME="railway"
RESTORE_DB_USER="postgres"
#RESTORE_DB_SCHEMA="public"
RESTORE_PORT=54009
RESTORE_PASSWORD="@FlakE2028@#$"  # Senha do restore
LOG_FILE="restore.log"

# Dump do banco de dados (sensordb)
echo "Iniciando o dump do banco de dados sensordb..."
export PGPASSWORD="$DUMP_PASSWORD"
pg_dump -h "$DUMP_HOST" -n "$DUMP_DB_SCHEMA" -p "$DUMP_PORT" -U "$DUMP_DB_USER" -F c -b -v -f "$DUMP_FILE" "$DUMP_DB_NAME"
DUMP_STATUS=$?

if [ $DUMP_STATUS -ne 0 ]; then
    echo "Erro durante o processo de dump. Abortando o script."
    exit 1
fi

echo "Dump finalizado com sucesso. Arquivo gerado: $DUMP_FILE"

# Limpar a variável de senha do dump
unset PGPASSWORD

# Restore do banco de dados (railway)
echo "Iniciando o processo de restore no banco de dados railway..."
export PGPASSWORD="$RESTORE_PASSWORD"
pg_restore --clean -h "$RESTORE_HOST" -p "$RESTORE_PORT" -U "$RESTORE_DB_USER" -d "$RESTORE_DB_NAME" -v "$DUMP_FILE" 2>&1 | tee "$LOG_FILE"
RESTORE_STATUS=$?

if [ $RESTORE_STATUS -ne 0 ]; then
    echo "Erro durante o processo de restore. Verifique o arquivo de log: $LOG_FILE"
    exit 1
fi

echo "Restore finalizado com sucesso. Verifique o arquivo de log: $LOG_FILE"

# Compactar o arquivo do dump com tar.gz após o restore
TAR_FILE="${DUMP_FILE}.tar.gz"
echo "Compactando o arquivo de dump..."
tar -czvf "$TAR_FILE" "$DUMP_FILE"
TAR_STATUS=$?

if [ $TAR_STATUS -ne 0 ]; then
    echo "Erro ao compactar o arquivo de dump."
    exit 1
fi

echo "Arquivo de dump compactado: $TAR_FILE"

# Deletar o arquivo .dump original após a compactação
rm "$DUMP_FILE"
if [ $? -eq 0 ]; then
    echo "Arquivo $DUMP_FILE deletado com sucesso."
else
    echo "Erro ao deletar o arquivo $DUMP_FILE."
    exit 1
fi

# Limpar a variável de senha do restore
unset PGPASSWORD
