import socket
import struct
import hashlib
import json
import os
import sys
import subprocess
import time
import getpass
from winreg import *




# c

# networking stuff: separation of messages sent through a TCP stream
def sendMessage(sock, data):
    length = len(data)
    sock.send(struct.pack('!I', length))
    sock.send(data)

def recvMessage(sock):
    lengthbuf = recvall(sock,4)
    #lengthbuf = sock.recv(4)
    #print(lengthbuf)
    length, = struct.unpack('!I', lengthbuf)
    #print(length)
    return recvall(sock, length)


def recvall(sock, count):
    buf = b''
    while count:
        newbuf = sock.recv(count)
        if not newbuf: return None
        buf += newbuf
        count -= len(newbuf)
    return buf

# /c

def hash(s):
    return hashlib.sha256(s.encode()).hexdigest()
    

# at login we send the following messages:
# "login"
# username
# password
# machine_ID stored in JSON file or, if machine is unregistered, -1

# after these, we receive a response: either "login_success" or "login_failed"


def login(sock, username, passwordHash, machine_ID):

    sendMessage(sock, b'login_windows')
    sendMessage(sock, username.encode())
    sendMessage(sock, passwordHash.encode())

    # we send -1 as the machineID. Server will know this machine is not yet registered
    sendMessage(sock, str(machine_ID).encode())

    resp = recvMessage(sock)

    #if resp == b'login_success':
    #    uid = recvMessage(sock)

    return resp



def launchGame(gameShortcut):

    # subprocess.Popen would fail with 64-bit programs
    # I don't know why so I used "runner.bat" as a workaround
    # runner.bat contents:
    # echo off
    # start %1

    try:
        os.chdir("apps")
    except FileNotFoundError: # apps directory non existent
        os.mkdir("apps")
        os.chdir("apps")
    subprocess.Popen("..\\runner.bat " + gameShortcut)
    os.chdir("..")


if __name__ == "__main__":

    if len(sys.argv) < 2:
        print("usage: python windowsClient.py <centralServerIP>")
        sys.exit()
    else:
        serverIP = sys.argv[1]
        
    #serverIP = "192.168.0.164"

    serverLoginAddr = (serverIP, 20000)
    serverCommAddr = (serverIP, 20001)

    clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    ok = False

    # if there is no Internet, probe every 5 seconds for an active connection
    while ok == False:
        try:
            clientSocket.connect(serverLoginAddr)
            ok = True
        except OSError: # most likely not connected to the Internet
            print("No Internet connection..")
            time.sleep(5)


    print("Welcome to AndroidGameStreamer!")
    

    if os.path.exists("data.json"):
        # get user and pass from file and login automatically
        datafile = open("data.json", "r")
        data = json.load(datafile)
        datafile.close()

        print("Logging in automatically as user \"%s\"" % data["username"])
        
        if login(clientSocket, data["username"], data["password"], data["machine_ID"]) == b'login_success':
            print("Login successful")


        else:
            print("Login failed with stored credentials")
            print("Exiting...")
            sys.exit()

    else:
        # ask for username and password from keyboard

        print("It looks like you are new here!")
        print("Enter your login credentials.\n")

        while True:
            

            username = input("Username: ")
            password = getpass.getpass("Password: ")
            
            # we send -1 as the machine_id so that server will know this is an unregistred machine
            if login(clientSocket, username, hash(password), str(-1)) == b'login_success':
                print("Login successful")


                # unregistered machine has to send its preferred name to the server 
                machine_name = input("Enter a name for this machine: ")
                sendMessage(clientSocket, machine_name.encode())


                # after recording this machine in its database, server will send machine_id
                machineID = int(recvMessage(clientSocket).decode())

                # store username and hashed password in json file for future autologin
                # machineID will be used to identify this Windows machine to the server in the future
                data = {"username": username, "password": hash(password), "machine_ID": machineID}               
                datafile = open("data.json", "w")
                json.dump(data, datafile)
                datafile.close()


                if not os.path.exists("apps"):
                    os.mkdir("apps")




                
                # this is code that adds new key to Windows Registry
                # commented out because multiple instances of this program would run
                # which interfered with testing
                
                ####################################################################
                # newKey = OpenKey(HKEY_CURRENT_USER, "Software\Microsoft\Windows\CurrentVersion\Run", 0, KEY_ALL_ACCESS)

                # SetValueEx(newKey, 'AndroidGameStreamer', REG_SZ, '%CD%\windowsClient.exe')
                #####################################################################




                print("\nThis machine is now registered to your account.")
                print("AndroidGameStreamer will now start automatically on Windows startup.")
                print("Remember the following:")
                print("1. Add shortcuts to games you want to play to \"apps\" directory that was just created.")
                print("2. Have fun! Otherwise, what's the point?")

                break

            else:
                print("Wrong username or password. Try again.\n")
                #print("Exiting...")
                #sys.exit()
            
    clientSocket.close()



    clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    clientSocket.connect(serverCommAddr)
    #print(recvMessage(clientSocket))

    while True:

        command = recvMessage(clientSocket)
        pass
        # handle commands from server


        if command == b'start':
            peerIP = recvMessage(clientSocket).decode()
            appToStart = recvMessage(clientSocket).decode()
            print("start " + appToStart)
            launchGame(appToStart)
            #os.system("python capture_winapi.py")
            #subprocess.Popen("python capture_winapi.py")
            subprocess.Popen("python streamingModule.py %s" %peerIP)
            subprocess.Popen("control.exe")


        elif command == b'stop':
            pass

        # send a list of shortcuts in 'apps' directory
        elif command == b'app_list':
            apps = ','.join(os.listdir("apps"))
            sendMessage(clientSocket, apps.encode())


    clientSocket.close()


