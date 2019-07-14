package com.telco.states

import com.telco.contracts.SubscriptionContract
import com.telco.schema.SubscriptionSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.contracts.LinearState
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

import java.time.LocalDate


@BelongsToContract(SubscriptionContract::class)
data class SubscriptionState(

        val customerID: String,
        val firstName: String,
        val lastName: String,
        val email: String,

        val serviceType: String,
        val serviceLevel: String,

        val contractID: String,
        val billingAccountID: String,           // Will be setup by charging

        val subscriptionStartDate: LocalDate,
        val subscriptionEndDate: LocalDate,


        val serviceStatus: String,
        val accountStatus: String,

        val billingCycle: String,
        val billDeliveryMethod: String,

        val serviceProvider: Party,             // Telstra, Optus, NBN = CName
        val subscriber: Party,                  // Subscriber@ Telstra, Optus, NBN [can be an individual or wholesaler]

        override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState, QueryableState {


    override val participants: List<Party> get() = listOf(serviceProvider, subscriber)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is SubscriptionSchemaV1 -> SubscriptionSchemaV1.PersistentSusbcription(
                    this.customerID,
                    this.firstName,
                    this.lastName,
                    this.email,

                    this.serviceProvider.name.toString(),
                    this.serviceType,
                    this.serviceLevel,

                    this.contractID,
                    this.billingAccountID,

                    this.subscriptionStartDate,
                    this.subscriptionEndDate,

                    this.serviceStatus,
                    this.accountStatus,

                    this.billingCycle,
                    this.billDeliveryMethod,

                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(SubscriptionSchemaV1)

}