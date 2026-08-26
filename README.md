# Clustered Smart Home Alarm System

## 🚀 How to run

To build the images and start the entire cluster, open your terminal in the project folder and run:

```bash
docker compose build
docker compose up
```

#

To enter the PIN, you need to attach to the keypad container's shell
1. Open a second terminal window
2. ```bash
   docker compose attach keypad
   ```
3. Type the default PIN (1234) and press Enter to disarm the system
4. When prompted by the control unit logs, type 1, 2, or 3 to choose the active mode (Full, Night, or Day) and arm the system.

#

To verify failure and recovery, restart the container or simulate a crash in a third terminal window.

```bash
# Option A: quick restart
docker compose restart smarthome

# Option B: stop and start manually
docker compose stop smarthome
docker compose start smarthome
```

#

You can follow the logs with:
```bash
docker compose logs -f smarthome
```

#

To stop the cluster (destroy containers and clear the recovery memory)
```bash
docker compose down
```
