#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu/app"
JAR_FILE="$PROJECT_ROOT/build/libs/capstone-0.0.1-SNAPSHOT.jar"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# 디렉토리 보장
mkdir -p "$PROJECT_ROOT"

# JAR 존재 여부 확인
if [ ! -f "$JAR_FILE" ]; then
  echo "$TIME_NOW > JAR 파일이 존재하지 않습니다: $JAR_FILE" >> $DEPLOY_LOG
  exit 1
fi

# JAR 복사 생략, 원래 파일 그대로 실행
echo "$TIME_NOW > $JAR_FILE 파일 실행" >> $DEPLOY_LOG
nohup env $(cat /home/ubuntu/.env | xargs) java -jar "$JAR_FILE" > "$APP_LOG" 2> "$ERROR_LOG" &

CURRENT_PID=$(pgrep -f "$JAR_FILE")
echo "$TIME_NOW > 실행된 프로세스 아이디 $CURRENT_PID 입니다." >> $DEPLOY_LOG
