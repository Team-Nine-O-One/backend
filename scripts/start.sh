#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu/app"
JAR_FILE="$PROJECT_ROOT/capstone.jar"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

LATEST_JAR=$(ls -t $PROJECT_ROOT/build/libs/capstone-0.0.1-SNAPSHOT.jar 2>/dev/null | head -n 1)

# build 파일 복사
echo "$TIME_NOW > $LATEST_JAR -> $JAR_FILE 복사" >> $DEPLOY_LOG
cp "$LATEST_JAR" "$JAR_FILE"

# jar 파일 실행
echo "$TIME_NOW > $JAR_FILE 파일 실행" >> $DEPLOY_LOG
nohup java -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG &

CURRENT_PID=$(pgrep -f $JAR_FILE)
echo "$TIME_NOW > 실행된 프로세스 아이디 $CURRENT_PID 입니다." >> $DEPLOY_LOG