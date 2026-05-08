# 商品管理アプリ

## 説明

2026年兵庫県立明石北高等学校の文化祭運営に使用予定のアプリです。明石北高校の同窓会組織である朔風会の役員のYuto T.が開発しています。このアプリはAWSにてデプロイ済みです。

朔風会ホームページ: [朔風会公式ホームページ](https://www.sakufukai.com/)

このアプリのデプロイ先: [2026年明石北高校文化祭店舗案内](https://www.sales202606.com/)

## 使用方法

1. src/main/resources/application.yml を作成し、以下の内容を入力します。また、環境変数は以下の値をAWSの環境プロパティに設定してください。ローカルの場合は、環境変数をエクスポートしてください。

```
ローカル・AWS共通
SPRING_DATASOURCE_PASSWORD: データベースに接続するためのパスワード
SPRING_DATASOURCE_URL: 接続先データベースの場所（RDS等）を指定するURL（例: jdbc:mysql://localhost:3306/sales202606?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_general_ci&serverTimezone=Asia/Tokyo）
SPRING_DATASOURCE_USERNAME: データベースに接続するためのユーザー名
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID: Googleログインに使用するOAuthクライアントID
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET: Googleログイン認証に使用するクライアント秘密鍵
APP_IMAGE_STORAGE_TYPE: ローカルの場合は 'local'、AWS上では 's3'
APP_IMAGE_LOCAL_DIR: $(pwd)/uploads/store-locations のように、ローカル上で画像を格納する絶対パスを記述。AWS 上では不要
APP_IMAGE_PUBLIC_PATH: '/uploads/store-locations' のように、ローカル上で画像を格納するパスを記述。AWS 上では不要
APP_ADMIN_EMAILS: 初期値の管理者として登録するGoogleアカウント。ymlファイルに直接書く場合は設定不要

AWS上でのみ必要なもの
SERVER_FORWARD_HEADERS_STRATEGY: リバースプロキシ（ALB等）経由でも正しいURL・HTTPS情報を認識するための設定（例: native）
SERVER_PORT: アプリケーションが待ち受けるサーバのポート番号を指定する設定（例: 5000）
```

```
spring:
  datasource:
    # 本番(Elastic Beanstalk)では必ず環境変数で指定する
    # 例: jdbc:mysql://<RDS-ENDPOINT>:3306/sales202606?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Tokyo
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
#      ddl-auto: update # ローカル用
      ddl-auto: none # AWS 用
    show-sql: false
#    properties: # ローカル用
#      hibernate:
#        dialect: org.hibernate.dialect.MySQL8Dialect

  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB

  security:
    oauth2:
      client:
        registration:
          google:
            # 未設定だとOAuth機能が動かないが、アプリ自体は起動できるようにする（空文字許容）
            client-id: ${SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID:}
            client-secret: ${SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET:}
            scope:
              - openid
              - email
              - profile
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub

app:
  # ローカルではこのリストを使う / EBでは APP_ADMIN_EMAILS で上書き可能
  admin-emails: ${APP_ADMIN_EMAILS:example1@gmail.com,example2@gmail.com}
  image-storage:
    type: ${APP_IMAGE_STORAGE_TYPE:local}
    local-dir: ${APP_IMAGE_LOCAL_DIR:uploads/store-locations}
    public-path: ${APP_IMAGE_PUBLIC_PATH:/uploads/store-locations}
    s3-bucket: ${APP_IMAGE_S3_BUCKET:}
    s3-prefix: ${APP_IMAGE_S3_PREFIX:store-locations}
    s3-region: ${APP_IMAGE_S3_REGION:ap-northeast-1}


server:
  error:
    whitelabel:
      enabled: false

```

2. 以下のコマンドを実行して、データベースとして MySQL コンテナを立てます。AWS上では、Amazon RDSを用いてデータベースを作成します。同じVPCに配置することを忘れないでください。

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