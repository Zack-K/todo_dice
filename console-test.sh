#!/bin/bash
# コンソールテスト実行スクリプト

echo "🎲 DiceApp 機能テスト開始"

# 簡単なKotlinファイルのコンパイルと実行
cd /app

# gradlewでrunタスクを使用してConsoleTestを実行
echo "📝 Gradleを使用してテストを実行..."
timeout 30s ./gradlew --console=plain -q --no-daemon exec -PmainClass=com.diceapp.test.ConsoleTest

echo "✅ テスト実行完了"