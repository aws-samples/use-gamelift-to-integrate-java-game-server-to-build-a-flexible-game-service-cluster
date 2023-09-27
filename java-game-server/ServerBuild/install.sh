#!/bin/bash
echo "install dependencies here"
curl https://raw.githubusercontent.com/fluent/fluent-bit/master/install.sh | sh
sudo mv ./fluent-bit.conf /etc/fluent-bit/fluent-bit.conf
## we need to create log folder at first, because if not the fluent bit will not be started success.
logs_path="/local/game/logs"
if [ ! -d "$logs_path" ]; then
    echo "log folder not exists..."
    mkdir -p "$logs_path"
    if [ $? -eq 0 ]; then
        echo "log folder create success."
    else
        echo "log folder create failed."
        exit 1
    fi
else
    echo "log folder exists。"
fi
sudo chmod 777 /local/game/logs
sudo echo "Test logs" > /local/game/logs/test.log
sudo systemctl start fluent-bit