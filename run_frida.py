import subprocess
import time
import sys

def main():
    print("[*] Menjalankan adb forward tcp:27042 tcp:27042...")
    subprocess.run(["adb", "forward", "tcp:27042", "tcp:27042"])
    
    print("\n[!] SILAKAN BUKA APLIKASI TURBOVPN DI HP ANDA SEKARANG.")
    print("[*] Menunggu Frida Gadget aktif di HP...", end="")
    sys.stdout.flush()
    
    listening = False
    while not listening:
        try:
            res = subprocess.run(["adb", "shell", "cat", "/proc/net/tcp"], capture_output=True, text=True)
            if "69A2" in res.stdout or "69a2" in res.stdout:
                listening = True
                print("\n[+] Frida Gadget terdeteksi AKTIF!")
                break
        except Exception as e:
            pass
        time.sleep(0.5)
        print(".", end="")
        sys.stdout.flush()
        
    print("\n[*] Menjalankan: frida -R -n Gadget -l ad_block.js\n")
    try:
        # Run frida process interactively
        subprocess.run(["frida", "-R", "-n", "Gadget", "-l", "ad_block.js"])
    except KeyboardInterrupt:
        print("\n[!] Dihentikan oleh user.")

if __name__ == "__main__":
    main()
