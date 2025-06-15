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

# Android SDK環境変数
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=${ANDROID_HOME}
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# Android SDK Command Line Toolsをインストール
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d /tmp && \
    mv /tmp/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# 必要なAndroid SDKコンポーネントをインストール
RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses && \
    ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager \
        "platform-tools" \
        "platforms;android-34" \
        "build-tools;34.0.0" \
        --verbose

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