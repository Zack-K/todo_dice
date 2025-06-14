FROM openjdk:17-slim

# 必要なパッケージをインストール
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    curl \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    fontconfig \
    libgl1-mesa-glx \
    libgl1-mesa-dri \
    libglu1-mesa \
    mesa-utils \
    xvfb \
    && rm -rf /var/lib/apt/lists/*

# Gradleをインストール
ENV GRADLE_VERSION=8.5
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp
RUN unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip
RUN ln -s /opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle

# 環境変数設定
ENV GRADLE_HOME=/opt/gradle/gradle-${GRADLE_VERSION}
ENV PATH=${GRADLE_HOME}/bin:${PATH}
ENV DISPLAY=:0

# 作業ディレクトリ
WORKDIR /app

# Gradleキャッシュ用のボリューム
VOLUME ["/root/.gradle"]

# 開発用ポート（必要に応じて）
EXPOSE 8080

CMD ["bash"]