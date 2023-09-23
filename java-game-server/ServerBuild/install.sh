#!/bin/bash
echo "install dependencies here"
curl https://raw.githubusercontent.com/fluent/fluent-bit/master/install.sh | sh
sudo mv ./fluent-bit.conf /etc/fluent-bit/fluent-bit.conf
sudo systemctl start fluent-bit