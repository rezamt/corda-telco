# Telecommunication On Corda (Telco-Chains)

![Highlevel Design](docs/sc01.png)

# Real World Model

![Highlevel Design](docs/sc01.png)

# Useless Commands

```bash

flow start CashIssueFlow amount: $1000, issuerBankPartyRef: 1234, notary: "O=Controller, L=London, C=GB"

start CashIssueFlow amount: $1000, issuerBankPartyRef: 1234, notary: "O=Controller, L=London, C=GB" 



```

# Useless Notes
```bash

# For Access Database update node.conf file 

h2Settings {
    address: "localhost:0"
}



# Corda Default Keystore Password
cordacadevpass

# Enable Remote Debugging on Corda Node

java -Dcapsule.jvm.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005" -jar corda.jar


```

# Running flows From Corda Shell - Command-line:
```bash


start SubscriptionFlow customerID: "1234567788", firstName: "Reza", lastName: "MT", email: "reza@gmail.com", serviceType: "ADSL", serviceLevel: "10Mbps", contractID: "34534535345", serviceProvider: TurkCell, startDate:  "Thu Jul 11 2019", endDate:  "Thu Jul 11 2019", billingCycle: Monthly, billDeliveryMethod: Email

start ServiceProvider subscriptionId: a8671cba-bed3-48c2-aa6c-54192fa35ae9
start ServiceDelivery subscriptionId: a8671cba-bed3-48c2-aa6c-54192fa35ae9
start Charging subscriptionId: a8671cba-bed3-48c2-aa6c-54192fa35ae9

```  
