#!/bin/bash

INSTALL_DIR="$HOME/pocketbase"
PB_VERSION="0.36.6"
PORT="8090"
USER_NAME="root"
PB_USER="boris.i.pronin@gmail.com"
PB_PASS="blue88cofe"

function loadPackage() {
  echo "Installing PocketBase"

  mkdir -p "$INSTALL_DIR"
  cd "$INSTALL_DIR" || return 
  
  echo "Downloading PocketBase v$PB_VERSION..."
  wget -q "https://github.com/pocketbase/pocketbase/releases/download/v$PB_VERSION/pocketbase_${PB_VERSION}_linux_amd64.zip" -O pb.zip
  
  echo "Unpacking..."
  unzip -o pb.zip
  chmod +x pocketbase
  rm pb.zip
}

function startService() {
  echo "Setup service..."
  cd "$INSTALL_DIR" || return

  ./pocketbase superuser create "$PB_USER" "$PB_PASS"
  
  bash -c "cat > /etc/systemd/system/pocketbase.service <<EOF 
[Unit]
Description=PocketBase Service
After=network.target

[Service]
Type=simple
User=$USER_NAME
Group=$USER_NAME
WorkingDirectory=$INSTALL_DIR
ExecStart=$INSTALL_DIR/pocketbase serve --http=\"0.0.0.0:$PORT\"
Restart=always

[Install]
WantedBy=multi-user.target
EOF"                       
                           
  echo "Starting service..."
  systemctl daemon-reload
  systemctl enable pocketbase
  systemctl start pocketbase
  systemctl is-active pocketbase
  
  echo "Admin interface is available at: http://$(hostname -I | awk '{print $1}'):$PORT/_/"
}

function setupNetwork() {
  echo "Setup traffic rules for port $PORT..."
  iptables -A INPUT -p tcp --dport "$PORT" -j ACCEPT 
  #todo: persist iptables   
}

function uninstall() {
  echo "Unstalling PocketBase"
  
  systemctl stop pocketbase 2>/dev/null
  systemctl disable pocketbase 2>/dev/null
  rm -f "/etc/systemd/system/pocketbase.service"
  
  iptables -D INPUT -p tcp --dport "$PORT" -j ACCEPT 2>/dev/null
  
  rm -f -r "$INSTALL_DIR"
}

uninstall
loadPackage
setupNetwork
startService

