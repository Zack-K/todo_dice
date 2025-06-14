# 最適化されたDice App Dockerfile
# マルチステージビルド + Alpine + 非rootユーザー + セキュリティ強化

# =============================================================================
# Runtime Stage: 軽量実行環境 (JRE + 最小パッケージ)
# =============================================================================
FROM eclipse-temurin:17-jre-alpine@sha256:b10e4fda9d71b3819a91fbb0dbb28512edbb37a45f6af2a301c780223bb42fb8

# メタデータ
LABEL maintainer="dice-app-team"
LABEL description="Lightweight Dice App based on Compose Multiplatform"
LABEL version="1.0"

# 実行に最低限必要なパッケージのみ
RUN apk add --no-cache \
    bash \
    curl \
    && rm -rf /var/cache/apk/* \
    && addgroup -g 1000 diceapp \
    && adduser -u 1000 -G diceapp -s /bin/bash -D diceapp

# 作業ディレクトリを作成し権限設定
WORKDIR /app
RUN chown -R diceapp:diceapp /app

# アプリケーションファイルをコピー
COPY --chown=diceapp:diceapp . .

# 非rootユーザーに切り替え
USER diceapp

# ヘルスチェック
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# ポート公開
EXPOSE 8080

# アプリケーション起動
CMD ["./gradlew", ":composeApp:run"]