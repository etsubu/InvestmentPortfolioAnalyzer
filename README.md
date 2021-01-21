# Investment portfolio analyzer

The application aims to help individual investors to track and analyze their portfolios performance if 
the broker does not provide tools for this. Additionally this solutions helps when switching brokers 
or when holding assets in multiple places. The application aims to different brokers and help with 
tax reports.

## How to build

You need to have java jdk 11 or newer installed to compile the application.
Java JRE is enough for running the tool.


### Installing java
#### Windows
You can download and install java jdk for windows from [https://adoptopenjdk.net/](https://adoptopenjdk.net/)

#### Debian
For debian based distros you can install java jdk with
`sudo apt install openjdk-11-jdk-headless -y`

### Building the application

```
https://github.com/etsubu/InvestmentPortfolioAnalyzer.git
cd InvestmentPortfolioAnalyzer
./gradlew clean build
```
The application will be located in build/libs folder

## How to use 

The tool uses transaction files which can be exported from the broker. Currently Degiro is only supported 
but other platforms and their transaction exports are intended to be supported in future.

The tool combines portfolio performance analyzing capabilities and tax report aid for Finland's tax 
authority [vero.fi](https://www.vero.fi). The tax report outputs are most likely valid and useful for other 
tax authorities as well but these are not supported. Author takes no responsibility in mistakes or invalid 
values in the tax report output and the user shall take full responsibility for their tax report.

TODO