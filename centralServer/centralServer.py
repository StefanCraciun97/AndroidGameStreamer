import socket
import sqlite3
import random
import socket
import struct
import hashlib
import threading
import time



def hash(s):
    return hashlib.sha256(s.encode()).hexdigest()


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


# define custom exception
class WindowsMachineDisconnectedError(Exception):
    pass



# parent class, shouldn't create instances of this
class NetworkingThread (threading.Thread):

    def __init__(self, talkingSocketAcceptor, sessionID):
        threading.Thread.__init__(self)
        self.talkingSocketAcceptor = talkingSocketAcceptor
        
    def run(self):
        self.talkingSocket, self.addr = self.talkingSocketAcceptor.accept()

        



# for communication with WindowsClient instances
class MachineNetworkingThread(NetworkingThread):

    def __init__(self, talkingSocketAcceptor, machineID):
        super().__init__(talkingSocketAcceptor, machineID)
        self.machineID = int(machineID)

    def __del__(self):
        # have to delete this thread from list of running threads somehow
        print("machine exit")
        pass


    # def sendCommandToMachine(self, command):
    #     sendMessage(self.talkingSocket, command

    # app will send list of shortcuts in its 'apps' folder
    # which is the folder that contains shortcuts to games to be streamed using this program
    def getMachineAppList(self):

        try:
            sendMessage(self.talkingSocket, b'app_list')
            apps = recvMessage(self.talkingSocket)
            return apps
        except ConnectionResetError:
            raise WindowsMachineDisconnectedError
        
    def startApp(self, app, peerIP):
        try:
            sendMessage(self.talkingSocket, b'start')
            sendMessage(self.talkingSocket, peerIP.encode())
            sendMessage(self.talkingSocket, app)
        except ConnectionResetError:
            raise WindowsMachineDisconnectedError


    def run(self):
        super().run() # accepts the connection
        print("new machineNetThread started")
        #self.sendCommandToMachine(b'salut brb')





# for communication with AndroidClient instances
class AndroidNetworkingThread(NetworkingThread):

    def __init__(self, talkingSocketAcceptor, currentUserID):
        super().__init__(talkingSocketAcceptor, currentUserID)
        self.currentUserID = currentUserID
        self.selectedMachineThread = None

    # main networking function
    def handleCommand(self, command):

        if command == b'salutare':
            sendMessage(self.talkingSocket, b'merge, boss!')

        elif command == b'download_user_table': # actually the table of machines associated with this user
            conn = connect("users.db")
            result = conn.execute("select machine_id, machine_name, current_IP from machines where user_id == %d" %self.currentUserID)

            for i in result:
                print(sqlInsertValuesAsString(i, separator=",", asList = True))
                if self.isMachineOnline(i[0]): # i[0] is machine_id
                    #append 1 to sent string if machine is online, 0 otherwise
                    sendMessage(self.talkingSocket, (sqlInsertValuesAsString(i, separator=",", asList = True) + ",1").encode()) 
                else:
                    sendMessage(self.talkingSocket, (sqlInsertValuesAsString(i, separator=",", asList = True) + ",0").encode()) 

            sendMessage(self.talkingSocket, b'end_table')

        # server expects machine_id for selected machine
        elif command == b'select_machine':

            machine_id = int(recvMessage(self.talkingSocket).decode())
            print(machine_id)
            

            for i in activeMachineConnections:
                if machine_id == i.machineID:
                    self.selectedMachineThread = i
                    
            


        # send list of apps available on selected machine to AndroidClient
        elif command == b'app_list':

            try:
                apps = self.selectedMachineThread.getMachineAppList() # returns byte string
                print(apps)
                sendMessage(self.talkingSocket, apps)
            except WindowsMachineDisconnectedError:
                sendMessage(self.talkingSocket, b'error_machine_went_offline')

        
        elif command == b'start':
            # AndroidClient tells us what app to start on WindowsMachine
            try:
                app = recvMessage(self.talkingSocket)
                # we have to pass Android's IP to windowsClient, so it will know
                # where to send video stream
                self.selectedMachineThread.startApp(app, self.addr[0])
                sendMessage(self.talkingSocket, b'all_ok')
            except WindowsMachineDisconnectedError:
                sendMessage(self.talkingSocket, b'error_machine_went_offline')

        else:
            sendMessage(self.talkingSocket, b'default')




    # returns connection thread associated wtih a machine
    def getMachineConnectionThread(self, machine_id):
        for i in activeMachineConnections:
            if machine_id == i.machineID:
                return i
        return None    


    # checks if there is active connection associated with a machine
    def isMachineOnline(self, machine_id):
        if self.getMachineConnectionThread(machine_id) == None:
            return False
        return True


    def run(self):
        super().run() # accepts the connection

        while True:
            
            try:
                self.command = recvMessage(self.talkingSocket)
            except TypeError:
                print("Connection terminated by client")
                break
            print(self.command)
            try:
                self.handleCommand(self.command)
            except WindowsMachineDisconnectedError: # WindowsClient went offline 
                removeDeadConnection(self.selectedMachineThread)
                self.selectedMachineThread = None

        self.talkingSocket.close()
        print("talking socket closed, thread ending")






# def handleCommand(talkingSocket, command):

    

#     if command == b'salutare':
#         sendMessage(talkingSocket, b'merge, boss!')

#     elif command == b'download_user_table':

        
#         conn = connect("users.db")

#         #sendMessage(tcpConn, b'start_table')
#         for i in printTable(conn, "machines"):
#             print(sqlInsertValuesAsString(i, separator=",", asList = True))
#             sendMessage(talkingSocket, sqlInsertValuesAsString(i, separator=",", asList = True).encode())

#         sendMessage(talkingSocket, b'end_table')

#     # elif command == b'login':
        
#     #     login(tcpConn)

#     else:
#         sendMessage(talkingSocket, b'default')



# to login client should send "login", followed by username and password
# it will receive "login_success" or "login_failure"
def loginAndroidUser(loginSocket, talkingSocketAcceptor):

    conn = connect("users.db")
    username = recvMessage(loginSocket).decode()
    password = recvMessage(loginSocket).decode()
    
    result = conn.execute("select password from users where username == \'%s\'" %username)

    try:
        storedPassword = result.fetchone()[0]
    except TypeError: # no such user
        sendMessage(loginSocket, b"login_failure")
        return 0


    print(storedPassword)
    print(password)
    if password == storedPassword:
        sendMessage(loginSocket, b"login_success")

        result = conn.execute("select user_id from users where username == \'%s\'" %username)
        uid = result.fetchone()[0]

        sendMessage(loginSocket, str(uid).encode())
        print("login successful")


        # need to create a new thread here for further communication after login

        networkingThread = AndroidNetworkingThread(talkingSocketAcceptor, uid)
        networkingThread.start()
        return 1

    else:
        sendMessage(loginSocket, b"login_failure")
        print("hecar detected")
        return 0



# at login client on Windows machine sends the following messages:
# "login"
# username
# password
# machine_ID stored in JSON file on machine or, if machine is unregistered, -1


def loginWindowsMachine(loginSocket, talkingSocketAcceptor, current_IP):

    conn = connect("users.db")
    username = recvMessage(loginSocket).decode()
    password = recvMessage(loginSocket).decode()
    machine_ID = recvMessage(loginSocket).decode()
    
    result = conn.execute("select password from users where username == \'%s\'" %username)

    try:
        storedPassword = result.fetchone()[0]
    except TypeError: # no such user
        sendMessage(loginSocket, b"login_failure")
        return None

    print(storedPassword)


    if password == storedPassword:
        sendMessage(loginSocket, b"login_success")


        
        #conn = connect("users.db")

        # machine registered previously
        if int(machine_ID) >= 0:
            conn.execute("update machines set current_IP = \"%s\" where machine_id == %s" %(current_IP, machine_ID))
            conn.commit()
        else:
        # unregistered machine

            # machine will send the name it wants to be identified as
            machine_name = recvMessage(loginSocket).decode()

            result = conn.execute("select user_id from users where username == \"%s\"" %username)
            user_id = result.fetchone()[0]
            insertRow(conn, "machines", "null", str(user_id), machine_name, current_IP)
            #insertRow(conn, "machines", "null", "1", "akon_machine_nvidia", "192.168.0.200")

            # send the machine_ID back to client, so it can identify itself in the future
            result = conn.execute("select last_insert_rowid()")
            machine_ID = result.fetchone()[0]
            print("new machine_ID %d" %machine_ID)
            sendMessage(loginSocket, str(machine_ID).encode())



        # result = conn.execute("select user_id from users where username == \'%s\'" %username)
        # uid = result.fetchone()[0]

        # sendMessage(loginSocket, str(uid).encode())
        print("login successful")
        


        # need to create a new thread here for further communication after login

        networkingThread = MachineNetworkingThread(talkingSocketAcceptor, machine_ID)
        return networkingThread
        

    else:
        sendMessage(loginSocket, b"login_failure")
        print("hecar detected")
        return None

    conn.close()



def register(loginSock):

    conn = connect("users.db")

    username = recvMessage(loginSock).decode()
    passHash = recvMessage(loginSock).decode()

    print("register")
    print(username)
    print(passHash)


    # if username already exists send error code
    result = conn.execute("select * from users where username == \'%s\'" %username)
    databaseUsername = result.fetchall()

    if len(databaseUsername) > 0:
        sendMessage(loginSock, b"register_failed_username_exists")
    else:
        insertRow(conn, "users", "null", username, passHash)
        sendMessage(loginSock, b"register_success")

    conn.close()


# removes thread if TCP connection with machine has been reset
def removeDeadConnection(winConnectionThread):
    activeMachineConnections.remove(winConnectionThread)



# database stuff (vulnerable to SQL injection, need to repair it)

def connect(databaseFile):
    conn = sqlite3.connect(databaseFile)
    return conn



def createTable(conn, tableName):
    conn.execute('CREATE TABLE IF NOT EXISTS ' + tableName + 
                '(id INTEGER PRIMARY KEY AUTOINCREMENT, ip VARCHAR2(25), path VARCHAR2(300))')
    conn.commit()




# returns a string: "(value_1, value_2, ..., value_n)" from provided list
# to be used in an INSERT SQL statement
# if separator is "|" for example, it returns "value_1|value_2|...|value_n"
# asList should be True if we pass a list or a tuple with values
# and False if we pass the values individually as arguments
def sqlInsertValuesAsString(*values, asList=False, separator=""):

    if asList == False:
        values = [str(i) for i in values]
    else:
        values = [str(i) for i in values[0]]

    values2 = []

    # in sql queries, strings must be surrounded by quotes, but not integers or null values
    for i in values:
        if i == 'null':
            values2.append(i)
        else:
            try:
                j = int(i)
                values2.append(i)
            except ValueError:
                values2.append('\"' + i + '\"')
    
    if separator != "":
        s = separator.join([i for i in values2])
    else:
        s = "(" + ", ".join([i for i in values2]) + ")"
    return s




def insertRow(conn, table, *values):

    conn.execute('INSERT INTO ' + table + ' VALUES ' + sqlInsertValuesAsString(values, asList=True))
    conn.commit()

def dropTable(conn, table):
    conn.execute('DROP TABLE IF EXISTS ' + table)

def printTable(conn, table):
    for row in conn.execute("select * from " + table):
        yield row






def recreateAllTables(conn):

    dropTable(conn, "users")
    dropTable(conn, "machines")
    dropTable(conn, "apps")

    conn.execute('CREATE TABLE IF NOT EXISTS users (user_id INTEGER PRIMARY KEY AUTOINCREMENT, username VARCHAR2(100), password VARCHAR2(100))')
    conn.execute('CREATE TABLE IF NOT EXISTS machines (machine_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, machine_name VARCHAR2(30), current_IP VARCHAR2(25))')
    #conn.execute('CREATE TABLE IF NOT EXISTS apps (app_id INTEGER PRIMARY KEY AUTOINCREMENT, machine_id INTEGER, app_name VARCHAR2(50), app_path VARCHAR2(300))')



def populateTables(conn):

    insertRow(conn, "users", "null", "akon", hash("1234"))
    insertRow(conn, "users", "null", "bkon", hash("4321"))
    insertRow(conn, "machines", "null", "1", "akon_machine", "192.168.0.100")
    insertRow(conn, "machines", "null", "1", "akon_machine_nvidia", "192.168.0.200")
    #insertRow(conn, "machines", "null", "2", "bkon_machine_nvidia", "192.168.0.250")










activeMachineConnections = []

if __name__ == '__main__':


    conn = connect('users.db')

    # insertRow(conn, "machines", "null", "3", "masina_falsa1", "192.168.0.93")
    # insertRow(conn, "machines", "null", "3", "masina_falsa2", "192.168.0.172")
    
    
    #recreateAllTables(conn)
    #populateTables(conn)

    print("users table:")
    for i in printTable(conn, "users"):
        print(i)
    print("machines table:")
    for i in printTable(conn, "machines"):
        print(i)

    conn.close()
    


    loginAcceptorSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    loginAcceptorSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    loginAcceptorSocket.bind((socket.gethostbyname(socket.gethostname()), 20000))
    loginAcceptorSocket.listen(3)

    talkingAcceptorSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    talkingAcceptorSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    talkingAcceptorSocket.bind((socket.gethostbyname(socket.gethostname()), 20001))
    talkingAcceptorSocket.listen(5)



    
    # a list of threads that handle currently active TCP connections with Windows machines
    # access thread's sessionID field for the ID of the machine associated with a thread
    


    # this loop accepts logins
    # separate threads are created for further communication with clients
    while True:

        print("Waiting for incoming connections...")
        loginSocket, addr = loginAcceptorSocket.accept()
        print("Connection established")
        print(addr)

        #command = 1

        while True:

            try:
                command = recvMessage(loginSocket)
            except TypeError:
                print("Connection terminated by client")
                break
            except ConnectionResetError:
                print("Connection terminated by client")
            print(command)
            
            if command == b'login_windows':
                machineThread = loginWindowsMachine(loginSocket, talkingAcceptorSocket, addr[0])
                if machineThread != None:
                    activeMachineConnections.append(machineThread) # we add the ID to this list
                    # to keep track of what Windows machines are connected
                    machineThread.start()
                    break

            elif command == b'login_android':
                # it will create a new thread in login function to handle this connection
                if loginAndroidUser(loginSocket, talkingAcceptorSocket) == 1:
                    break
                
            # if comand is register don't break the loop because user might want to log in
            elif command == b'register_android':
                register(loginSocket)

            else:
                sendMessage(loginSocket, b'access_denied')


        loginSocket.close()
    