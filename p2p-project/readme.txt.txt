###########################  P2P compute system ########################################

Currently supports TSP and Mandelbrot

to start a peer type:
	# ant peer

Once started three parameters have to be specified:
 - Host name or ip address of peer to connect to (default is localhost)
 - Port that the remote peer uses (default is 1099)
 - Local port (if left blank if will chose port 1099 and not connect to another peer. This is used for the firs peer int the circle)


