This provides a cmdline interface to some parts of the vsphere API

# Build the jar file:
git clone git@github.com:jedi4ever/jvspherecontrol.git
cd jvspherecontrol
mvn package
ls target/*.jar 
target/jvspherecontrol-0.0.4-SNAPSHOT-jar-with-dependencies.jar

# Usage:
$ java -jar jvspherecontrol-0.0.4-SNAPSHOT-jar-with-dependencies.jar <options>

Usage:
list     hosts|datacenters|datastores|clusters|networks|users|vms|all|resourcepools
--url <url to connect to (including the sdk part>
--user <username>
--password <password>

createvm     --bootorder <order to boot: allow:cd,hd,net or deny:net,cd>
--cdromdatastore <dvd datastorename>
--cdromisopath <path to dvd isofile>
--cluster <name of the cluster to store new Vm>
--cpus <number of cpu's to allocate>
--datacenter <name of the datacenter to store new Vm>
--datastore <name of the datastore to store new Vm>
--diskdatatastore1..n <name of the datastore to create the disk>
--diskmode1..n <disk mode>
--disksize1..n <size in kb of disk to create>
--memory <memory size to allocate>
--name <name of vm to create>
--nicconnected1..n <connect Nic or not>
--nicname1..n <name of the Nic interface>
--nicnetwork1..n <network of the Nic interface>
--nicpxe1..n <enable Nic pxe boot or not >
--nicstartconnected1..n <connect Nic at start or not >
--nictype1..n <type of the Nic interface>
--omapihost <omapi hostname>
--omapikeyname <omapi key to use>
--omapikeyvalue <omapi value>
--omapioverwrite <overwrite omapi entry>
--omapiport <omapi portname>
--omapiregister <register with omapi server>
--ostype <type of vm to create>
--overwrite <overwrite vm Flag>
--password <password to connect to vSphere>
--pxeinterface <name of the network interface to PXE from>
--url <url to connect to>
--user <username to connect to vSphere>

omapiregister     --hostname <hostname to register>
--macaddress <mac address to register>
--omapihost <omapi hostname>
--omapikeyname <omapi key to use>
--omapikeyvalue <omapi value>
--omapioverwrite <overwrite omapi entry>
--omapiport <omapi portname>
--omapiregister <register with omapi server>

activatevnc     --password <password to connect to vSphere>
--url <url to connect to>
--user <username to connect to vSphere>
--vmname <name of vm to create>
--vncpassword <password to set on the VNC>
--vncport <port to enable VNC on>

deactivatevnc     --password <password to connect to vSphere>
--url <url to connect to>
--user <username to connect to vSphere>
--vmname <vmname to disable vnc>

sendvnctext     --host <host to send it to>
--password <password to use>
--port <port to connect to>
--text <text to send>
--wait <seconds to wait in between sending different texts (default=1s)>

