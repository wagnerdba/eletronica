# eletronica


# Restaurar banco para o docker

## Copiar o dump para o container
docker cp C:\Users\wagne\Desktop\dump_dbsensor_2025-09-02_00-00-01.dump postgresql_17.6:/dump.dump

## Executar o restore
docker exec -i postgresql_17.6 pg_restore --clean -h localhost -p 5432 -U postgres -d sensordb -v /dump.dump 2>&1 | Tee-Object -FilePath C:\Users\wagne\Desktop\restore_log.txt

## NPM (frontend)

# Apaga node_modules e package-lock.json
Remove-Item -Recurse -Force node_modules

Remove-Item -Force package-lock.json

# Instala tudo ignorando peerDependencies
npm install --legacy-peer-deps

npm install ipaddr.js --legacy-peer-deps

npm install ajv@8.12.0 ajv-keywords@5.0.0 --legacy-peer-deps

# Inicia o projeto
npm start