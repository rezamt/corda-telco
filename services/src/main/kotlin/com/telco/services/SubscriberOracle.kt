package com.telco.services;

import com.telco.contracts.BillingContract
import liquibase.util.csv.opencsv.CSVReader
import net.corda.core.contracts.Command
import net.corda.core.crypto.TransactionSignature
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.transactions.FilteredTransaction
import java.io.StringReader
import java.util.*

// We sub-class 'SingletonSerializeAsToken' to ensure that instances of this class are never serialised by Kryo.
// When a flow is check-pointed, the annotated @Suspendable methods and any object referenced from within those
// annotated methods are serialised onto the stack. Kryo, the reflection based serialisation framework we use, crawls
// the object graph and serialises anything it encounters, producing a graph of serialised objects.
// This can cause issues. For example, we do not want to serialise large objects on to the stack or objects which may
// reference databases or other external services (which cannot be serialised!). Therefore we mark certain objects with
// tokens. When Kryo encounters one of these tokens, it doesn't serialise the object. Instead, it creates a
// reference to the type of the object. When flows are de-serialised, the token is used to connect up the object
// reference to an instance which should already exist on the stack.
@CordaService
class SubscriberOracle(private val services: AppServiceHub) : SingletonSerializeAsToken() {
    private val myKey = services.myInfo.legalIdentities.first().owningKey

    // A table of (contractor, company) -> salary expectations (per hour)
    private val subscribersTable: HashMap<String, Boolean> = hashMapOf()

    init {
        val reader = CSVReader(StringReader(javaClass.getResource("/customers.csv").readText()))
        val lines = reader.readAll()
        for (line in lines) {
            //@todo: Later on we will add (TelcoID, MSISDN) = Status
            //@todo: Later on we will add (TelcoID, SubscriberID, MSISDN) = Status
            //subscribersTable[Pair(line[0].trim(), line[1].trim())] = line[1].trim().toBoolean()
            subscribersTable[line[0].trim()] = line[1].trim().toBoolean()

        }
    }

    // Returns the salary for the given contractor at that company
    // TODO: Figure out what to do about missing pay rates
    fun query(msisdnOf: MSISDNOf): MSISDN =
        MSISDN(msisdnOf, subscribersTable[msisdnOf.customerID]!!)
    //MSISDN(msisdnOf, subscribersTable[Pair(msisdnOf.serviceProvider.name.organisation, msisdnOf.subscriber.name.organisation, msisdnOf.customerID)]!!)

    // Signs over a transaction if the specified Nth prime for a particular N is correct.
    // This function takes a filtered transaction which is a partial Merkle tree. Any parts of the transaction which
    // the oracle doesn't need to see in order to verify the correctness of the nth prime have been removed. In this
    // case, all but the [PrimeContract.Create] commands have been removed. If the Nth prime is correct then the oracle
    // signs over the Merkle root (the hash) of the transaction.
    fun sign(ftx: FilteredTransaction): TransactionSignature {
        // Check the partial Merkle tree is valid.
        ftx.verify()

        /** Returns true if the component is an command that:
         *  - States the correct rate
         *  - Has the oracle listed as a signer
         */
        fun isCommandWithValidSubscriberAndIAmSigner(elem: Any) = when {
            elem is Command<*> && elem.value is BillingContract.Commands.IssueBill -> {
                val cmdData = elem.value as BillingContract.Commands.IssueBill
                myKey in elem.signers && query(MSISDNOf(cmdData.serviceProvider, cmdData.subscriber, cmdData.customerID)).value == true
            }

            elem is Command<*> && elem.value is BillingContract.Commands.OverDueBill -> {
                val cmdData = elem.value as BillingContract.Commands.OverDueBill
                myKey in elem.signers && query(MSISDNOf(cmdData.serviceProvider, cmdData.subscriber, cmdData.customerID)).value == true
            }

            elem is Command<*> && elem.value is BillingContract.Commands.OverDueBill -> {
                val cmdData = elem.value as BillingContract.Commands.PayBill
                myKey in elem.signers && query(MSISDNOf(cmdData.serviceProvider, cmdData.subscriber, cmdData.customerID)).value == true
            }

            else -> false
        }

        // Is it a Merkle tree we are willing to sign over?
        val isValidMerkleTree = ftx.checkWithFun(::isCommandWithValidSubscriberAndIAmSigner)

        if (isValidMerkleTree) {
            return services.createSignature(ftx, myKey)
        } else {
            throw IllegalArgumentException("Subscriber Oracle signature requested over invalid transaction.")
        }
    }
}
