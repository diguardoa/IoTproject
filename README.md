# IoTproject

--------------------------------------------------------------
Content:
ActuatorsPrototype - it contains contiki files related to actuators
Configuration - it contains Java Code to configure Motes /id
Executable - it contains executable
oM2M - it contains oM2M MN and IN executable
Proxy_Client - it contains proxy Java Code
rpl-border-router - it contains Border Router contiki code
SensorPrototype - it contains contiki files related to sensors
Simulations - it contains Cooja simulations
WebApp - it contains WebApp Javascript application
WebServer - it contains WebServer Java code

CommandList.txt - it contains debug information of developed message protocol


--------------------------------------------------------------
Start Cooja Simulation
It is necessary dispose of Contiki 3.0 preconfigured virtual machine. One way to open Cooja execute following commands:

cd Simulations
sh openCooja.sh


Then use Cooja GUI to open or Project8.0.csc or Project9.0.csc project files present in Simulations folder.
Finally, it is possible to start the simulation.
----------------------------------------------------------------------------------
Start tunslip6

cd rpl-border-router
sudo make connect-router-cooja
----------------------------------------------------------------------------------
Configure 6LowPAN motes

With this phase id are assign to all motes. Without doing this, all resources would belong to Patient0 or Room0. To configure motes execute:
java -jar Executable/simulationX.jar
with the letter X stays for 8 or 9 (dependent on what Project has been opened in previous step. Configuration goes well if the word 'fine' is printed on the screen.
----------------------------------------------------------------------------------
Start oM2M infrastructure

Open a new terminal and execute:
cd oM2M
sh start.sh
and insert the pwd user in both opened terminals.
----------------------------------------------------------------------------------
Starting Proxy ADN

Open a new terminal and type:
java -jar Executable/proxy.jar
----------------------------------------------------------------------------------
Starting WebServer ADN

Open a new terminal and type
java -jar Executable/WebServer.jar
----------------------------------------------------------------------------------
Open User Interface

Double click on WebApp/SmartHosp.html to access the user interface using a browser.


---------------------------------------------------------------
Di Guardo Antonio - Vincentini Andrea 
IoT DiViProject2017
antonio.diguardo@santannapisa.it
andrea.vincentini@santannapisa.it