# Use uma versão específica do Node.js para maior controle
FROM node:20.18.0-alpine

# Instale o xsel usando o apk
RUN apk update && apk add --no-cache xsel

# Defina o diretório de trabalho no container
WORKDIR /app

# Copie os arquivos de dependências e o package-lock.json para o container
COPY package*.json ./

# Instale as dependências com compatibilidade para conflitos de peer dependencies
RUN npm install --legacy-peer-deps

# Copie todo o código da aplicação para o container
COPY . .

# Execute o build da aplicação React com TypeScript
RUN npm run build

# Instale o pacote 'serve' globalmente para servir a aplicação
RUN npm install -g serve

# Exponha a porta 3000 para servir a aplicação
EXPOSE 3000

# Comando para servir o conteúdo da pasta 'build'
CMD ["serve", "-s", "build"]
