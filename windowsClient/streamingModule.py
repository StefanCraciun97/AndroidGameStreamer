import d3dshot
from PIL import Image
from io import BytesIO
import socket
import struct
import sys


datagramID = 0
datagramMaxLength = 50000

def fragmentDatagram(datagram):

    # max datagram size will be 20000 bytes (excluding headers)
    # datagramID + currentNr + totalNr + data
    # short         char        char 

    global datagramID
    datagramFragments = []
    totalNr = int(len(datagram) / datagramMaxLength) + 1

    # if length of data divides by datagramMaxLength,
    # there might be a problem (zero-length fragment)

    for i in range(totalNr):
        dID = struct.pack("<H", datagramID)
        currentNr = struct.pack("<B", i+1)
        tNr = struct.pack("<B", totalNr)
        data = datagram[i*datagramMaxLength:(i+1)*datagramMaxLength]

        fragment = dID + currentNr + tNr + data
        datagramFragments.append(fragment)

    if datagramID > 30000: # so that we do not go higher than short limit (signed in java)
        datagramID = 0
    else:
        datagramID += 1

    return datagramFragments





def msec(t_init, t_fin):
    print((t_fin-t_init)*1000)




if __name__ == "__main__":



    if len(sys.argv) < 2:
        print("usage: python streamingModule.py <IP of Android device>")
        sys.exit()
    else:
        peerIP = sys.argv[1]


    ramfile = BytesIO()
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    dInstance = d3dshot.create(capture_output="pil", frame_buffer_size=1)
    dInstance.capture(target_fps=35) # start screen capture




    while True:
        

        
        #frame = dInstance.screenshot() # 12-20ms
        #image = Image.fromarray(frame) # 35-40ms

        
        # get_last_frame() works way better, it only takes like 6-9ms to return frame and 
        # compress it as jpeg
        frame = dInstance.get_latest_frame()
        
        try:
            frame.save(ramfile, format="jpeg") # 8ms
        except AttributeError: # for a few seconds, get_latest_frame returns None
            # I think it need some time to initialize
            continue
        


        ############
        ramfile.seek(0)
        data = ramfile.read()
        ramfile.seek(0)
        ############ nothing
        
        
        fragments = fragmentDatagram(data) # nothing


        # actually 7-12ms with spikes to 40ms, don't know what happened before
        for i in fragments: # 30-50ms, spikes up to 100ms
            sock.sendto(i, (peerIP, 20010))




    dInstance.stop()