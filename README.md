# IoTproject
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


