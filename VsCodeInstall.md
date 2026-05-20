# VS Code Installation with Oracle Proxy

## Overview

This guide installs Microsoft Visual Studio Code on Oracle Linux or other RHEL-compatible systems using `dnf`, with Oracle proxy settings applied for the current shell.

## 1. Set proxy variables

Run the following in your current shell:

```bash
export http_proxy="http://www-proxy.us.oracle.com:80"
export HTTP_PROXY="http://www-proxy.us.oracle.com:80"
export https_proxy="http://www-proxy.us.oracle.com:80"
export HTTPS_PROXY="http://www-proxy.us.oracle.com:80"
export no_proxy="localhost,127.0.0.1,.us.oracle.com,.oraclecorp.com,.oraclevcn.com"
export NO_PROXY="localhost,127.0.0.1,.us.oracle.com,.oraclecorp.com,.oraclevcn.com"
```

## 2. Create the VS Code repository file

```bash
sudo tee /etc/yum.repos.d/vscode.repo >/dev/null <<'EOF'
[code]
name=Visual Studio Code
baseurl=https://packages.microsoft.com/yumrepos/vscode
enabled=1
autorefresh=1
type=rpm-md
gpgcheck=1
gpgkey=https://packages.microsoft.com/keys/microsoft.asc
EOF
```

## 3. Import Microsoft's signing key

```bash
sudo env \
  http_proxy="$http_proxy" HTTP_PROXY="$HTTP_PROXY" \
  https_proxy="$https_proxy" HTTPS_PROXY="$HTTPS_PROXY" \
  no_proxy="$no_proxy" NO_PROXY="$NO_PROXY" \
  rpm --import https://packages.microsoft.com/keys/microsoft.asc
```

## 4. Install VS Code

```bash
sudo env \
  http_proxy="$http_proxy" HTTP_PROXY="$HTTP_PROXY" \
  https_proxy="$https_proxy" HTTPS_PROXY="$HTTPS_PROXY" \
  no_proxy="$no_proxy" NO_PROXY="$NO_PROXY" \
  dnf install -y code
```

## 5. Verify the installation

```bash
code --version
which code
```

## 6. Launch VS Code

```bash
code
```

## Notes

- These steps are intended for Oracle Linux 8 or similar RHEL-compatible systems using `dnf`.
- The important environment-specific detail is using `http://www-proxy.us.oracle.com:80` for both `http_proxy` and `https_proxy`.
- If a broken `/etc/yum.repos.d/vscode.repo` file already exists, replace it with the repository definition above before installing.
