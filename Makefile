# Docker開発環境用Makefile

.PHONY: build up down shell run test clean

# Docker環境構築
build:
	docker compose build

# 開発環境起動
up:
	docker compose up -d

# 開発環境停止
down:
	docker compose down

# コンテナに入る
shell:
	docker compose exec dice-app bash

# アプリ実行
run:
	docker compose exec dice-app ./gradlew run

# テスト実行
test:
	docker compose exec dice-app ./gradlew test

# クリーンアップ
clean:
	docker compose down -v
	docker system prune -f

# X11 forwarding設定（macOS）
setup-x11-mac:
	@echo "macOSでX11 forwardingを有効にするには："
	@echo "1. XQuartzをインストール: brew install --cask xquartz"
	@echo "2. XQuartzを起動して設定 > セキュリティ > ネットワーククライアントからの接続を許可"
	@echo "3. export DISPLAY=host.docker.internal:0"

# X11 forwarding設定（Linux）
setup-x11-linux:
	@echo "LinuxでX11 forwardingを有効にするには："
	@echo "xhost +local:docker"