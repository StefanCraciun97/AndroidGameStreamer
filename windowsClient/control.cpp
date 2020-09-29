#include <stdio.h>
#include <winsock2.h>
#include <windows.h>


// se compileaza cu comanda: 
// gcc control.cpp -lws2_32 -o control.exe


// device ids
#define MOUSE 0x01
#define KEYBOARD 0x02

// event types
#define TAP 0x01
#define HOLD 0x02
#define RELEASE 0x03


#define MOUSE_TAP 0x11
#define MOUSE_HOLD 0x12
#define MOUSE_RELEASE 0x13
#define MOUSE_MOVE 0x14
#define KEYBOARD_TAP 0x21
#define KEYBOARD_HOLD 0x22
#define KEYBOARD_RELEASE 0x23



// VIRTUAL key codes
// #define KEY_A 0x41
// #define KEY_B 0x42
// #define KEY_C 0x43
// #define KEY_D 0x44
// #define KEY_E 0x45
// #define KEY_F 0x46
// #define KEY_G 0x47
// #define KEY_H 0x48
// #define KEY_I 0x49
// #define KEY_J 0x4A
// #define KEY_K 0x4B
// #define KEY_L 0x4C
// #define KEY_M 0x4D
// #define KEY_N 0x4E
// #define KEY_O 0x4F
// #define KEY_P 0x50
// #define KEY_Q 0x51
// #define KEY_R 0x52
// #define KEY_S 0x53
// #define KEY_T 0x54
// #define KEY_U 0x55
// #define KEY_V 0x56
// #define KEY_W 0x57
// #define KEY_X 0x58
// #define KEY_Y 0x59
// #define KEY_Z 0x5A


// REAL keyboard scan codes

# define KEY_W 0x11
# define KEY_A 0x1E
# define KEY_S 0x1F
# define KEY_D 0x20




#define KEY_LSHIFT VK_LSHIFT
#define KEY_LCTRL VK_LCONTROL
#define KEY_SPACE VK_SPACE
#define KEY_LALT VL_MENU
#define KEY_TAB VK_TAB
#define KEY_CAPS VK_CAPITAL




void holdKey(char key);
void tapKey(char key);
void releaseKey(char key);
void pressMouse();
void moveMouse(short x, short y);




int UDPsockinit(SOCKET* sock, short port){

    WSADATA wsa;
    struct sockaddr_in localAddr;
    char recvBuffer; 



    printf("Initialising Winsock...\n");

    if (WSAStartup(MAKEWORD(2,2), &wsa) != 0)
    {
        printf("Failed. Error Code : %d",WSAGetLastError());
        return 1;
    }




    // find out the local IP of this machine
    // WSAStartup is needed before gethostname()
    char hostbuffer[200]; 
    gethostname(hostbuffer, sizeof(hostbuffer));
    struct hostent *h = gethostbyname(hostbuffer); 
    char *localIP = inet_ntoa(*((struct in_addr*) h->h_addr_list[0])); 


    localAddr.sin_addr.s_addr = inet_addr(localIP);
    localAddr.sin_family = AF_INET;
    localAddr.sin_port = htons(port);



    printf("Initialised.\n");

    //Create a socket
    if((*sock = socket(AF_INET , SOCK_DGRAM , 0 )) == INVALID_SOCKET)
    {
        printf("Could not create socket : %d" , WSAGetLastError());
        closesocket(*sock);
        WSACleanup();
        return 1;
    }

    printf("Socket created.\n");


    if (bind(*sock, (sockaddr*) &localAddr, sizeof(localAddr)) == SOCKET_ERROR) {
        printf("Bind failed with error %d\n", WSAGetLastError());
        closesocket(*sock);
        WSACleanup();
        return 1;
    }

    
    printf("Bind returned success\n");
    

}

char interpretAction(SOCKET sock){

    char recvBuffer[9];

    if(recvfrom(sock, recvBuffer, 8, 0, NULL, NULL) == SOCKET_ERROR){
        printf("recvfrom failed with error %d\n", WSAGetLastError());
        closesocket(sock);
        WSACleanup();
        return -1;
    }

    // Received UDP packets are maximum 8 bytes long. 


    switch(recvBuffer[0]){

        case KEYBOARD_TAP:
            tapKey(recvBuffer[1]);
            break;
        case KEYBOARD_HOLD:
            holdKey(recvBuffer[1]);
            break;
        case KEYBOARD_RELEASE:
            releaseKey(recvBuffer[1]);
            break;
        case MOUSE_TAP:
            pressMouse();
            break;
        case MOUSE_MOVE:
            // x speed is position 1 and 2 in buffer, y speed is position 3 and 4 (2 bytes each)
            // first byte most significant
            //printf("x = %hd", (recvBuffer[1]<<8) | recvBuffer[2]);
            //printf("  y = %hd\n", (recvBuffer[3]<<8) | recvBuffer[4]);
            moveMouse( (recvBuffer[1]<<8) | recvBuffer[2], (recvBuffer[3]<<8) | recvBuffer[4]);
            break;
        case MOUSE_HOLD:
            break;
        case MOUSE_RELEASE:
            break;
        default:
            return -1;

    }

    return 0;
}

void tapKey(char key){

    // This structure will be used to create the keyboard
    // input event.
    INPUT ip;
 
    // Set up a generic keyboard event.
    ip.type = INPUT_KEYBOARD;
    ip.ki.wScan = key; // hardware scan code for key
    ip.ki.time = 0;
    ip.ki.dwExtraInfo = 0;
 
    // Press the key
    ip.ki.wVk = 0; // virtual-key code key
    ip.ki.dwFlags = KEYEVENTF_SCANCODE; // 0 for key press
    SendInput(1, &ip, sizeof(INPUT));
 
    // Delay is needed because default (10ms) is too short to be detected by games
    // Maybe there is another method for delay (a non-blocking one)
    // this might cause problems ( if program is unresponsive, it might not pick up simultaneous key presses)
    // need to look into threads for this one
    Sleep(50);

    // Release the key
    ip.ki.dwFlags = KEYEVENTF_SCANCODE | KEYEVENTF_KEYUP; // KEYEVENTF_KEYUP for key release
    SendInput(1, &ip, sizeof(INPUT));
 
}

void holdKey(char key){

    // This structure will be used to create the keyboard
    // input event.
    INPUT ip;
 
    // Set up a generic keyboard event.
    ip.type = INPUT_KEYBOARD;
    ip.ki.wScan = key; // hardware scan code for key
    ip.ki.time = 0;
    ip.ki.dwExtraInfo = 0;
 
    // hold the key down
    ip.ki.wVk = 0; // virtual-key code key
    ip.ki.dwFlags = KEYEVENTF_SCANCODE; // 0 for key press
    SendInput(1, &ip, sizeof(INPUT));
 
}

void releaseKey(char key){

    // This structure will be used to create the keyboard
    // input event.
    INPUT ip;
 
    // Set up a generic keyboard event.
    ip.type = INPUT_KEYBOARD;
    ip.ki.wScan = key; // hardware scan code for key
    ip.ki.time = 0;
    ip.ki.dwExtraInfo = 0;
 
    // Release the key
    ip.ki.wVk = 0; // virtual-key code key
    ip.ki.dwFlags = KEYEVENTF_SCANCODE | KEYEVENTF_KEYUP; // KEYEVENTF_KEYUP for key release
    SendInput(1, &ip, sizeof(INPUT));
 
}


void pressMouse(){

    // This structure will be used to create the keyboard
    // input event.
    INPUT ip;
 
    // Set up a generic mouse event.
    ip.type = INPUT_MOUSE;

    // setting click position 0 (we want to click at current location)
    ip.mi.dx = 0;
    ip.mi.dy = 0;
    // some other useless stuff
    ip.mi.time = 0;
    ip.mi.dwExtraInfo = 0;

    // press left mouse
    ip.mi.dwFlags = (MOUSEEVENTF_LEFTDOWN);
    SendInput(1, &ip, sizeof(INPUT));

    Sleep(50);
 
    // release left mouse
    ip.mi.dwFlags = (MOUSEEVENTF_LEFTUP); 
    SendInput(1, &ip, sizeof(INPUT));
 
    
}



// I disabled "enhance pointer precision" (mouse acceleration) in mouse settings
// It made cursor move way faster than needed when moving it from android joystick input

void moveMouse(short x, short y){

    // This structure will be used to create the keyboard
    // input event.
    INPUT ip;
 
    // Set up a generic mouse event.
    ip.type = INPUT_MOUSE;

    // setting click position 0 (we want to click at current location)
    ip.mi.dx = x;
    ip.mi.dy = y;
    // some other useless stuff
    ip.mi.time = 0;
    ip.mi.dwExtraInfo = 0;

    // press left mouse
    ip.mi.dwFlags = (MOUSEEVENTF_MOVE);
    SendInput(1, &ip, sizeof(INPUT));

 
    
}





int main(){

    
    SOCKET sock;
    UDPsockinit(&sock, 20011);

    while(1){

        interpretAction(sock);

    }
    

    


    printf("Closing socket.\n");
    
    if (closesocket(sock) == SOCKET_ERROR) {
        printf("closesocket failed with error %d\n", WSAGetLastError());
        return 1;
    }


    WSACleanup();
    return 0;
}