package com.telco.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.time.LocalDate
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table


/**
 * The family of schemas for SubscriptionSchema
 */
object SubscriptionSchema


/**
 * A SubscriptionSchema
 */

object SubscriptionSchemaV1 : MappedSchema(
        schemaFamily = SubscriptionSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentSusbcription::class.java)) {


    @Entity
    @Table(name = "telco_subscription")  // namespace: telco _ table: subscription
    class PersistentSusbcription(

            // Customer Registration Portal ID
            @Column(name = "customerID")
            val customerID: String,

            // Contacts
            @Column(name = "firstName")
            val firstName: String,
            @Column(name = "lastName")
            val lastName: String,
            @Column(name = "email")
            val email: String,

            @Column(name = "serviceProvider")
            val serviceProvider: String,

            // Service Types: WIRELESS, ADSL, FIBER_OPTIC, DSL, SATELLITE, etc
            // @todo: may be we better use Enum here
            @Column(name = "serviceType")
            val serviceType: String,
            // Service Level Agreement: 10Mbps, 50Mbps, 100Mbps, 1000Mbps, UNLIMITED_SPEED
            @Column(name = "serviceLevel")
            val serviceLevel: String,

            // Customer Contract ID
            @Column(name = "contractID")
            val contractID: String,
            // Customer Billing Account ID (@todo: customer in the future might have more than one account
            @Column(name = "billingAccountID")
            val billingAccountID: String,

            // Subscription start and end dates
            @Column(name = "subscriptionStartDate")
            val subscriptionStartDate: LocalDate,
            @Column(name = "subscriptionEndDate")
            val subscriptionEndDate: LocalDate,

            @Column(name = "linear_id")
            var linearId: UUID


    ) : PersistentState() {

        // Default constructor required by hibernate.
        constructor() : this("", "", "", "", "", "", "", "", "", LocalDate.MIN, LocalDate.MIN, UUID.randomUUID())
    }


}


