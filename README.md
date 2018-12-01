## Config ##

All information to get your auth key is on https://apps.twitter.com/

Create an application, copy consumer key and consumer secret.

Create access token for your account, copy access token and access token secret.

Copy `src/main/resources/application.conf.template` to `src/main/resources/application.conf` and fill in above keys/tokens/secrets.

Run with `sbt run`

Use `ws://localhost:8080/ws` to gets tweets in WebSocket