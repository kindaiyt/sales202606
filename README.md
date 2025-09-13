```
docker run --name sales-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=sales202606 \
  -p 3306:3306 \
  -d mysql:8.1
```