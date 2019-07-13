package com.telco.flows

import net.corda.core.flows.*
import co.paralleluniverse.fibers.Suspendable
import com.telco.contracts.SubscriptionContract
import com.telco.states.SubscriptionState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class ServiceProviderFlow(private val subscriptionId: UUID) : FlowLogic<SignedTransaction>() {

    companion object {
        object GENERATING_TRANSACTION : ProgressTracker.Step("Updating Subscriber Service Status")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    override fun call(): SignedTransaction {

        // Obtain a reference to the notary we want to use.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        val subscriptionStateAndRef = serviceHub.vaultService.queryBy<SubscriptionState>(QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier(id = subscriptionId)))).states.single()
        val subscription = subscriptionStateAndRef.state.data


        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION

        val commandSigners = subscription.participants.map { it.owningKey }

        // Generate an unsigned transaction.
        val txCommand = Command(SubscriptionContract.Commands.Approved(subscription.subscriber, ourIdentity, subscription.accountStatus, subscription.serviceStatus), commandSigners)

        val txBuilder = TransactionBuilder(notary)
                .addInputState(subscriptionStateAndRef)
                .addOutputState(subscription.copy(serviceStatus = "Approved"), SubscriptionContract.ID)
                .addCommand(txCommand)

        // Stage 2.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        // Verify that the transaction is valid.
        txBuilder.verify(serviceHub)

        // Stage 3.
        progressTracker.currentStep = SIGNING_TRANSACTION
        // Sign the transaction.
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)


        // Stage 6.
        progressTracker.currentStep = GATHERING_SIGS
        // Send the state to the counterparty, and receive it back with their signature.
        val sessions = (subscription.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, sessions))


        // Stage 7.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in all parties' vaults.
        return subFlow(FinalityFlow(fullySignedTx, sessions, FINALISING_TRANSACTION.childProgressTracker()))

    }
}
