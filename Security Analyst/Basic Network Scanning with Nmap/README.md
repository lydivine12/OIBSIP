# Basic Network Scanning with Nmap

A hands-on lab documenting the use of **Nmap** to identify open ports and running
services on a local machine, with a security analysis of the findings.

Target scanned: `127.0.0.1` (localhost) on an Ubuntu Linux lab machine — scanning
your own machine or a VM you own is the safe, legal way to practice this skill
(see [Ethical Use Guidelines](#ethical-use-guidelines) below before pointing Nmap
at anything else).

---

## What Is Nmap?

**Nmap** ("Network Mapper") is a free, open-source command-line tool used to
discover hosts and services on a computer network. It works by sending
specially crafted packets to target IP addresses and analysing the responses to
determine:

- Which hosts on a network are up/reachable
- Which **ports** are open, closed, or filtered on those hosts
- Which **services and version numbers** are running on open ports (`-sV`)
- What **operating system** the target is likely running (`-O`)
- Whether known **vulnerabilities** may be present (via the Nmap Scripting
  Engine, `-sC` / `--script`)

It's one of the most widely used tools in networking and cybersecurity, relied
on by system administrators for inventory and troubleshooting, and by security
professionals for penetration testing and vulnerability assessment.

## Why Network Scanning Matters

You can't secure what you don't know exists. Network scanning is a foundational
security practice because it answers a simple but critical question: **"What is
actually exposed, and to what?"**

- **Asset visibility** — servers quietly pick up new listening services over
  time (a developer installs a database, a debug web server gets left running).
  Scanning surfaces what's *actually* running, not just what's documented.
- **Attack surface reduction** — every open port is a potential entry point.
  Identifying and closing unnecessary ones shrinks the number of ways an
  attacker could get in.
- **Detecting misconfigurations** — an outdated service version, a database
  exposed to the wrong network, or a debug service left open in production are
  all the kinds of issues a scan reveals before an attacker finds them first.
- **Compliance and auditing** — many security standards (PCI-DSS, ISO 27001,
  SOC 2) require regular vulnerability/port scanning as part of demonstrating
  due diligence.

In short: this is the same reconnaissance step both attackers *and* defenders
perform — the difference is authorization and intent.

## Ethical Use Guidelines

**Only scan systems you own, or have explicit, documented permission to test.**
Unauthorized port scanning of systems you don't control can be illegal under
laws such as the U.S. Computer Fraud and Abuse Act (CFAA), the UK Computer
Misuse Act, and equivalent laws in most other countries — even a "harmless"
scan with no follow-up exploitation can constitute unauthorized access.

Guidelines followed in this lab:

1. **Own systems only.** All scans in this repository were run against
   `127.0.0.1` (localhost) on a machine the author controls — never against
   third-party infrastructure, public IP ranges, or systems without written
   authorization.
2. **Get permission in writing first**, if scanning anything beyond your own
   equipment — even scanning a friend's home network or your employer's
   internal systems without a signed authorization/scope document is not
   acceptable.
3. **Understand scope boundaries.** A scan authorized for one IP range doesn't
   extend to adjacent ranges, cloud provider infrastructure, or third-party
   services (e.g., a shared hosting IP) that happen to be reachable from the
   same network.
4. **Be mindful of impact.** Aggressive scan types (e.g., `-T5` timing,
   intensive script scans) can disrupt fragile devices (some IoT/embedded
   systems crash under port scans) — even on your own network, scan
   responsibly.
5. **Disclose responsibly.** If a scan (authorized or not) surfaces a real
   vulnerability on a system you don't own, use responsible disclosure —
   contact the owner privately, don't exploit or publicize it.

---

## Installation

### Debian / Ubuntu Linux

```bash
sudo apt update
sudo apt install -y nmap
```

### Kali Linux

Nmap comes **preinstalled** on Kali Linux. Verify with:

```bash
nmap --version
```

### macOS (via Homebrew)

```bash
brew install nmap
```

### Windows

Download and run the installer from the official site: https://nmap.org/download.html
(includes the Npcap packet-capture driver, required for full functionality).

### Verify installation (any platform)

```bash
nmap --version
```

Expected output (version used in this lab):

```
Nmap version 7.94SVN ( https://nmap.org )
```

---

## Lab Setup

To make the scan results meaningful, three services were intentionally running
on the target machine before scanning:

| Port | Service | How it was started |
|---|---|---|
| 22 | SSH (OpenSSH) | `openssh-server` installed and `sshd` started |
| 8080 | HTTP (Python's built-in dev server) | `python3 -m http.server 8080` |
| 8000 | Generic TCP listener | `nc -lk 8000` |

This mirrors a realistic small server: a remote-access service, a web service,
and a "someone left a debug listener running" service.

---

## Scans Performed

All three required scans were run against `127.0.0.1`, in order:

### 1. Basic scan

```bash
nmap 127.0.0.1
```

Performs a default SYN scan against the 1,000 most common ports. Fast, and a
good first pass to see what's listening. See `screenshots/01_basic_scan.png`
and the **Basic Scan** section of `nmap_scan_results.txt` for full output.

### 2. Service version scan

```bash
nmap -sV 127.0.0.1
```

Adds **version detection** — Nmap probes each open port with protocol-specific
queries to identify the exact software and version behind it, not just that a
port is open. This is far more useful for security analysis, since
vulnerabilities are usually tied to specific software versions. See
`screenshots/02_service_version_scan.png`.

### 3. OS detection scan

```bash
nmap -O 127.0.0.1
```

Attempts to fingerprint the target's operating system by analysing subtle
differences in how its TCP/IP stack responds to a series of crafted probes
(initial sequence numbers, TCP options, window sizes, etc.). See
`screenshots/03_os_detection_scan.png`.

> **Note on this specific result:** scanning `127.0.0.1` from inside a
> containerised/virtualised Linux environment gave Nmap an ambiguous
> fingerprint ("No exact OS matches for host") rather than a clean
> `Linux 5.X` result. This is a known, common outcome of OS fingerprinting
> over the loopback interface in containers — the raw TCP/IP fingerprint is
> still included in `nmap_scan_results.txt` for reference. On a full VM (e.g.
> VirtualBox with a real network interface) you should get a cleaner match.

All raw output is preserved unedited in `nmap_scan_results.txt` and in the
individual `basic_scan.txt` / `version_scan.txt` / `os_scan.txt` files.

---

## Repository Structure

```
.
├── README.md                  <- this file
├── nmap_scan_results.txt      <- structured findings + full raw scan output
├── basic_scan.txt             <- raw output: nmap 127.0.0.1
├── version_scan.txt           <- raw output: nmap -sV 127.0.0.1
├── os_scan.txt                <- raw output: nmap -O 127.0.0.1
└── screenshots/
    ├── 01_basic_scan.png
    ├── 02_service_version_scan.png
    └── 03_os_detection_scan.png
```

## Summary of Findings

| Port | Service | Version Detected | Risk Level |
|---|---|---|---|
| 22/tcp | SSH | OpenSSH 9.6p1 (Ubuntu) | Low (if hardened) — see full analysis |
| 8000/tcp | Unidentified TCP listener (`http-alt?`) | Not identified | **High** — unauthenticated, unknown service |
| 8080/tcp | HTTP (Python dev server) | SimpleHTTPServer 0.6 / Python 3.12.3 | **High** — not production-safe |

Full explanations, risk reasoning, and remediation recommendations for each
port are in [`nmap_scan_results.txt`](./nmap_scan_results.txt).
