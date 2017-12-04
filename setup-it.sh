#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd ${DIR}/docker && docker-compose rm -f && ./build-all.sh && docker-compose up -d  && cd ${DIR}
