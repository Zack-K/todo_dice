# 🎲 DiceApp - TODO・ランダム選択アプリ

[![CI](https://github.com/Zack-K/todo_dice/actions/workflows/ci.yml/badge.svg)](https://github.com/Zack-K/todo_dice/actions/workflows/ci.yml)

水曜どうでしょうの「サイコロの旅」にインスパイアされた、実用的でエンターテイメント性のある3-in-1アプリケーションです。

## ✨ 機能

### 📝 TODO管理
- タスクの作成・編集・削除・完了
- 優先度設定（高・中・低）
- カテゴリ管理
- 期限設定
- ローカルJSONファイルでの永続化

### 🎯 ランダム選択
- TODOからのランダム選択
- カスタム選択肢の作成
- **6秒間のダイスアニメーション** 🎲
- 選択履歴の保存
- 統計情報の表示

### 🎲 ダイス機能（内部実装）
- 標準ダイス（D4, D6, D8, D10, D12, D20, D100）
- ダイス記法パース（例：3d6+2）
- ロール履歴
- 統計分析

## 🏗️ アーキテクチャ

### Clean Architecture
```
UI Layer (Compose)
├── ViewModel (StateFlow)
├── UseCase (ビジネスロジック)
└── Repository (データアクセス)
    └── LocalStorage (JSON)
```

### 技術スタック
- **Kotlin/JVM**: メイン言語
- **Compose Multiplatform**: UI フレームワーク
- **Material Design 3**: デザインシステム
- **Kotlinx Coroutines**: 非同期処理
- **Kotlinx Serialization**: JSON処理
- **Docker**: 開発環境

### モジュール構成
```
src/jvmMain/kotlin/com/diceapp/
├── core/           # 共通機能
│   ├── config/     # アプリ設定
│   ├── error/      # エラーハンドリング
│   └── logging/    # ログ機能
├── todo/           # TODO管理
├── dice/           # ダイス機能
└── randomselector/ # ランダム選択
```

## 🚀 開発環境セットアップ

### 必要な環境
- Docker & Docker Compose
- Git

### 1. リポジトリクローン
```bash
git clone https://github.com/Zack-K/todo_dice.git
cd todo_dice
```

### 2. Docker環境構築
```bash
# イメージビルド
docker compose build

# 開発環境起動
docker compose run --rm dice-app bash
```

### 3. アプリケーション実行

#### GUI版（推奨）
```bash
# ソフトウェアレンダリング（OpenGL問題回避）
docker compose run --rm dice-app env LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe ./gradlew run

# 通常実行
docker compose run --rm dice-app ./gradlew run
```

#### コンソール版（機能確認用）
```bash
docker compose run --rm dice-app ./gradlew runConsole
```

## 🧪 テスト実行

### 全テスト実行
```bash
docker compose run --rm dice-app ./gradlew test
```

### テストカバレッジ
- **53+テストケース**
- Repository、UseCase、ViewModelレイヤーを包括
- TDD（テスト駆動開発）アプローチで実装

## 📦 ビルド

### Desktop版ビルド
```bash
./gradlew build
```

### 配布パッケージ作成
```bash
./gradlew createDistributable
```

## ⚙️ 設定

### アニメーション設定
`src/jvmMain/kotlin/com/diceapp/core/config/AppConfig.kt`
```kotlin
data class AppConfig(
    val diceAnimationDurationMs: Long = 6000L, // 6秒
    val diceAnimationUpdateIntervalMs: Long = 100L,
    val showDiceTab: Boolean = false
)
```

### ログレベル設定
デバッグ時は`Logger.kt`で出力レベルを調整可能

## 🔄 開発フロー（GitHub Flow）

1. **機能ブランチ作成**
   ```bash
   git checkout -b feature/新機能名
   ```

2. **開発・テスト**
   ```bash
   ./gradlew test  # テスト実行
   ./gradlew check # 品質チェック
   ```

3. **プルリクエスト作成**
   - CI/CDが自動実行
   - コードレビュー後マージ

## 📱 ロードマップ

### Phase 2: モバイル対応
- [ ] Compose Multiplatform Mobile移行
- [ ] Android版リリース
- [ ] iOS版リリース
- [ ] モバイル向けUI最適化

### 将来的な機能
- [ ] クラウド同期
- [ ] チーム共有機能
- [ ] より高度なアニメーション
- [ ] 音声効果

## 🤝 コントリビューション

1. フォーク
2. 機能ブランチ作成
3. 変更実装
4. テスト追加
5. プルリクエスト作成

## 📄 ライセンス

MIT License

## 🙏 謝辞

「水曜どうでしょう」の素晴らしい番組からインスピレーションを得ています。