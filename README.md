# 商品管理アプリ

## 説明

兵庫県立明石北高等学校の文化祭運営に使用予定のアプリです。明石北高校の同窓会組織である朔風会の役員が開発しています。

ホームページ: [朔風会公式ホームページ](https://www.sakufukai.com/)

## 使用方法

1. src/main/resources/application.yml を作成し、以下の内容を入力する。`<client-id>`と`<client-secret>`には、Google OAuthのクライアントIDとクライアントシークレットを入力して下さい。また、`admin-emails`には、管理者として登録したいメールアドレスを入れて下さい。

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sales202606?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_general_ci
    username: root
    password: rootpassword
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: <client-id>
            client-secret: <client-secret>
            scope:
              - openid     # OidcUser を取得するには openid が必須
              - email
              - profile
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub

app:
  admin-emails:
    - "example1@gmail.com"
    - "example2@gmail.com"
```

2. 以下のコマンドを実行して、データベースとして MySQL コンテナを立てます。

```
docker run --name sales-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=sales202606 \
  -p 3306:3306 \
  -v $(pwd)/my.cnf:/etc/mysql/conf.d/my.cnf \
  -d mysql:8.1
```

3. リポジトリルートをカレントディレクトリとして、以下のコマンドを実行して下さい。

```
./gradlew bootRun
```

4. [ローカルホストの8080番ポート](http://localhost:8080/)にアクセスして、アプリを使用して下さい。