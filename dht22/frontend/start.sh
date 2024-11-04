#!/bin/sh

# Inicie o servidor 'serve' em segundo plano
serve -s build &

# Aguarde um pequeno tempo para garantir que o servidor está rodando
sleep 5

# Mantenha o bash ativo
exec bash
